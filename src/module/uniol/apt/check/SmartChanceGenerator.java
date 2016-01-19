/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package uniol.apt.check;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * This class implements an (experimental) petri net generator, inspired by the
 * ChanceGenerator. The generator can add random modifications to the net and
 * save them into a history. <br>
 * <br>
 * Based on the score value (which is passed by Check), it modifies the chances
 * for calling each type of modification accordingly. In case a best score is
 * found but further modifications do not lead to a better result, it will undo
 * the modifications and get back to the point with the highest score.<br>
 * <br>
 * Incase of too many undos or a good net is not found early on, the generator
 * will start over with a new net.
 *
 * @author Chris
 */
public class SmartChanceGenerator {

	/**
	 * Enumeration for possible Modifications.
	 *
	 * @author Chris
	 */
	public enum ModificationType {

		Place, Transition, Mark, Arc, Weight;
	}

	private static final int NUMBER_BASE_MODIFICATIONS = 5;
	private static final int MAX_UNDOS_ALLOWED = 16;

	// Generator attributes
	private PetriNet net;
	private Random rnd;
	private int lastScore;
	private int bestScore;
	private int bestScoreHistoryIndex;
	private int numUndosDone;
	private int maxDistanceToBestScore;

	// History
	private Stack<ModificationStep> history;

	// Chances for modifications
	private HashMap<ModificationType, Double> chances;
	private HashMap<ModificationType, Double> penalties;
	private HashMap<ModificationType, Double> uses;

	/**
	 * Constructor.
	 *
	 * @param seed
	 *            the random seed. Pass 0 for no preset seed.
	 */
	public SmartChanceGenerator(final long seed) {
		if (seed != 0) {
			this.rnd = new Random(seed);
		} else {
			this.rnd = new Random();
		}
	}

	/**
	 * Generates a new net and clears the modification histroy. <br>
	 * <br>
	 * The basic net will consist of one place with int initMarkingValue tokens,
	 * one transition and one arc. Base modifications like places, transitions
	 * or arcs can be added by setting the int NUMBER_BASE_MODIFICATIONS.
	 *
	 * @param initMarkingValue
	 *            the initial marking of the net.
	 */
	public void renewNet(final int initMarkingValue) {

		reset(); // reset generator and get new net.

		Place p = net.createPlace("p0");
		Transition t = net.createTransition("t0");

		if (initMarkingValue > 0) {
			p.setInitialToken(initMarkingValue);
		}

		if (rnd.nextDouble() >= 0.5) {
			net.createFlow(p, t);
		} else {
			net.createFlow(t, p);
		}
		for (int i = 0; i < Math.abs(NUMBER_BASE_MODIFICATIONS); i++) {
			addBaseModification();
		}
	}

	/**
	 * Adds a basic modification (Place,Transition,Arc) to the net that does not
	 * get saved in history.
	 */
	private void addBaseModification() {
		double r = rnd.nextDouble();

		if (net.getTransitions().size() + net.getPlaces().size() < 6) {
			// do not add additional arcs to a
			// net that is too small.

			r *= 0.8;
		}
		if (r < 0.4) {
			this.addPlace();
		} else if (r < 0.8) {
			this.addTransition();
		} else {
			this.addArc();
		}
	}

	/**
	 * Resets the history. This function gets called upon creating a new net.
	 */
	protected void reset() {
		this.net = new PetriNet();
		this.history = new Stack<>();

		this.lastScore = 0;
		this.bestScore = 0;
		this.bestScoreHistoryIndex = -1;
		this.numUndosDone = 0;
		this.maxDistanceToBestScore = 8;

		resetChances();
	}

	/**
	 * Resets the chances hashmap and gets all chances back to 1.0.
	 */
	protected void resetChances() {
		if (chances == null) {
			this.chances = new HashMap<>();
			this.penalties = new HashMap<>();
			this.uses = new HashMap<>();
		} else {
			this.chances.clear();
		}
		this.chances.put(ModificationType.Place, 1.0);
		this.chances.put(ModificationType.Transition, 1.0);
		this.chances.put(ModificationType.Mark, 1.0);
		this.chances.put(ModificationType.Arc, 1.0);
		this.chances.put(ModificationType.Weight, 1.0);

		this.penalties.put(ModificationType.Place, 0.0);
		this.penalties.put(ModificationType.Transition, 0.0);
		this.penalties.put(ModificationType.Mark, 0.0);
		this.penalties.put(ModificationType.Arc, 0.0);
		this.penalties.put(ModificationType.Weight, 0.0);

		this.uses.put(ModificationType.Place, 0.0);
		this.uses.put(ModificationType.Transition, 0.0);
		this.uses.put(ModificationType.Mark, 0.0);
		this.uses.put(ModificationType.Arc, 0.0);
		this.uses.put(ModificationType.Weight, 0.0);
	}

	/**
	 * Method that gets called by Check.
	 *
	 * @param value
	 *            the score
	 * @param initMarkingValue
	 *            the initial marking value of the net
	 * @return the new or modified petrinet
	 */
	public PetriNet generateNet(final int value, final int initMarkingValue) {
		boolean success = false;
		boolean validation = false;

		// Check if the net is present and if not, we generate one.
		if (net == null) {
			renewNet(initMarkingValue);
			return net;
		}

		// Process our score information.
		validation = validateScore(value);

		if (validation) {
			for (int i = 0; i < 8 && !success; i++) {
				success = getRandomModification();
			}
		}

		if (!success || !validation) {
			renewNet(initMarkingValue); // renew the net
		}

		return net;
	}

	/**
	 * Called to react on score information.
	 *
	 * @param value value
	 * @return boolean
	 */
	protected boolean validateScore(final int value) {
		// return false if we have made 16 modifications
		// and we still did not find a net with atleast
		// one fulfilled property.
		if (value <= 0) {
			return this.history.size() < 16;
		}

		if (history.size() == 0) {
			return true;
		}

		ModificationType mod = this.history.peek().getType();

		if (value > lastScore) { // Score increased (improvement)
			validateHigherScore(mod, value);
		} else if (value <= lastScore) {
			// Score stayed the same or got less.

			// Check for Undo
			if (this.history.size() - this.bestScoreHistoryIndex > this.maxDistanceToBestScore
					&& this.history.size() > 8) {
				if (numUndosDone >= Math.abs(MAX_UNDOS_ALLOWED)) {
					return false;
				}

				// return to the last point with best score
				while (this.history.size() > this.bestScoreHistoryIndex) {
					if (!undoModification()) {
						return false; // undo was not successful.
					}
				}
				numUndosDone++;
			}

			// the last modification did not change the net.
			if (value == lastScore) {
				validateNoScoreChange(mod);
			}

			if (value < lastScore) {
				validateLesserScore(mod);
			}
		}

		updateMaxDistanceToBestScore();

		// Set the lastScore to the new score.
		this.lastScore = value;

		return true;
	}

	/**
	 * Called upon improving the score, compared to the last score.
	 *
	 * @param mod
	 *            the last added modification
	 * @param value
	 *            the value of the new score
	 */
	private void validateHigherScore(final ModificationType mod, final int value) {
		if (value > bestScore) {
			// reached a new highest score
			this.bestScore = value;
			this.bestScoreHistoryIndex = this.history.size() - 1;
			this.chances.put(mod, this.chances.get(mod) + (bestScore - lastScore));
		}
	}

	/**
	 * Called on when the score did not get improved.
	 *
	 * @param mod
	 *            the last added modification
	 */
	private void validateNoScoreChange(ModificationType mod) {
		for (ModificationType type : this.chances.keySet()) {
			if (type == mod) {
				this.chances.put(type, this.chances.get(mod) - 0.1);
			} else {
				this.chances.put(type, this.chances.get(mod) + 0.05);
			}
		}
	}

	/**
	 * Called on when the score got lesser than the last score.
	 *
	 * @param mod
	 *            the last added modification
	 */
	private void validateLesserScore(final ModificationType mod) {
		// update penalty
		this.penalties.put(mod, this.penalties.get(mod) + 1.0);
		// check how often this modification got used first.
		if (this.uses.get(mod) > 4) {
			if (this.penalties.get(mod) / this.uses.get(mod) > 0.75) {
				// temporarily disable this modificatoin
				this.chances.put(mod, -0.5);
			} else {
				// rank down chances for this modificaton.
				this.chances.put(mod, this.chances.get(mod) - 0.2);
			}
		}
	}

	/**
	 * called to update the maximum allowed distance to the best score.
	 */
	private void updateMaxDistanceToBestScore() {
		if (this.history.size() < 32) {
			this.maxDistanceToBestScore = this.history.size() / 2;
		} else if (this.history.size() <= 64) {
			this.maxDistanceToBestScore = this.history.size();
		} else if (this.history.size() <= 128) {
			this.maxDistanceToBestScore = 16 + ((128 - this.history.size()) / 8);
		} else {
			this.maxDistanceToBestScore = 8;
		}
	}

	/**
	 * Randomly picks a modification based on chances and then adds it to the
	 * net.
	 *
	 * @return true on success
	 */
	private boolean getRandomModification() {
		double d = 0, max = 0, p = 0, sum = 0;
		int i = 0;

		// randomly pick a modification from our pool
		for (Double dBoxed : this.chances.values()) {
			d = dBoxed;
			if (d > 0) {
				i++;
				max += d;
			}
		}

		// No weighted chances, low sum on chances, or only one modification.
		if (d <= 0.2 || i < 2) {
			resetChances();
		}

		// randomly pick a modification based on weights.
		p = rnd.nextDouble() * max;

		for (Map.Entry<ModificationType, Double> entry : this.chances.entrySet()) {
			sum += entry.getValue();
			if (p <= sum) {
				return doModification(entry.getKey());
			}
		}
		return false;
	}

	/**
	 * Delegates the modification type to the right function and logs the
	 * modification in the history.
	 *
	 * @param mod
	 *            the modification.
	 * @return true on success.
	 */
	protected boolean doModification(final ModificationType mod) {
		Object obj = null;

		// Check the modification type and call the function.
		switch (mod) {
		case Place:
			obj = addPlace();
			break;
		case Transition:
			obj = addTransition();
			break;
		case Mark:
			obj = addMark();
			break;
		case Arc:
			obj = addArc();
			break;
		case Weight:
			obj = addWeight();
			break;
		default:
		}

		// Case: no success.
		if (obj == null) {
			return false;
		}

		// Otherwise: log to history.
		history.push(new ModificationStep(obj, mod));
		uses.put(mod, uses.get(mod) + 1);

		return true;
	}

	/**
	 * Undoes a modification.
	 *
	 * @return true on success.
	 */
	protected boolean undoModification() {
		// Check if net is present at all.
		if (net == null) {
			return false;
		}

		// Check if history exists.
		if (history.size() <= 0) {
			return false;
		}

		// Look up our last modification to the net.
		ModificationType mod = history.peek().getType();
		Object obj = history.peek().getObject();

		// try to undo the modification (step back)
		try {
			switch (mod) {
			case Place:
				net.removePlace((Place) obj);
				break;
			case Transition:
				net.removeTransition((Transition) obj);
				break;
			case Mark:
				((Place) obj).setInitialToken(((Place) obj).getInitialToken().getValue() - 1);
				break;
			case Arc:
				net.removeFlow(((Flow) obj).getSource(), ((Flow) obj).getTarget());
				break;
			case Weight:
				((Flow) obj).setWeight(((Flow) obj).getWeight() - 1);
				break;
			default:
				return false;
			}
			// return false, when we catch an exception.
			// Do not remove the modification from history.
		} catch (Exception e) {
			return false;
		}

		// Continue by removing the step from our modification history.
		history.pop();

		return true;
	}

	// Adds a place to the net.
	private Object addPlace() {
		int sizeT = net.getTransitions().size();
		int sizeP = net.getPlaces().size();

		Place p = null;
		Transition t = null;
		try {
			p = net.createPlace("p" + sizeP);
			t = net.getTransition("t" + rnd.nextInt(sizeT));

			if (rnd.nextDouble() >= 0.5) {
				net.createFlow(t, p);
			} else {
				net.createFlow(p, t);
			}
		} catch (Exception e) {
			if (p != null) {
				net.removePlace(p);
			}
			return null;
		}
		return p;
	}

	// Adds a transition to the net
	private Object addTransition() {
		int sizeT = net.getTransitions().size();
		int sizeP = net.getPlaces().size();

		Place p = null;
		Transition t = null;
		try {
			t = net.createTransition("t" + sizeT);
			p = net.getPlace("p" + rnd.nextInt(sizeP));

			if (rnd.nextDouble() >= 0.5) {
				net.createFlow(t, p);
			} else {
				net.createFlow(p, t);
			}
		} catch (Exception e) {
			if (t != null) {
				net.removeTransition(t);
			}
			return null;
		}
		return t;
	}

	// Randomly increments one mark in the net
	private Object addMark() {
		try {
			Place p = net.getPlace("p" + rnd.nextInt(net.getPlaces().size()));
			p.setInitialToken(p.getInitialToken().getValue() + 1);
			return p;
		} catch (Exception e) {
			return null;
		}
	}

	// Randomly increases the weight of an arc in the net.
	private Object addWeight() {
		try {
			Set<Flow> edges = net.getEdges();
			int i = rnd.nextInt(edges.size());
			for (Flow f : edges) {
				if (i == 0) {
					f.setWeight(f.getWeight() + 1);
					return f;
				}
				i--;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	// Randomly adds an arc to the net.
	private Object addArc() {
		Transition t = net.getTransition("t" + rnd.nextInt(net.getTransitions().size()));
		Place p = net.getPlace("p" + rnd.nextInt(net.getPlaces().size()));
		Flow f = null;
		try {
			if (rnd.nextDouble() >= 0.5) {
				f = net.createFlow(p, t);
			} else {
				f = net.createFlow(t, p);
			}
			return f;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper class for storing history information.
	 *
	 * @author Chris
	 */
	static private class ModificationStep {

		private Object obj;
		private ModificationType modification;

		/**
		 * Constructor.
		 *
		 * @param obj the modified object.
		 * @param mod the modification type.
		 */
		private ModificationStep(Object obj, ModificationType mod) {
			this.obj = obj;
			this.modification = mod;
		}

		/**
		 * @return the id of the element that got added/modified.
		 */
		public Object getObject() {
			return this.obj;
		}

		/**
		 * @return the type of the modification.
		 */
		public ModificationType getType() {
			return this.modification;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
