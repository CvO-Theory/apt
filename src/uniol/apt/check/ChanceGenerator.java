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

import java.util.Collection;
import java.util.HashSet;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Generator chance:
 *   get a solution by chance
 *
 * @author Daniel
 */
public class ChanceGenerator {

	PetriNet pn;
	int modificationCounter;
	int transitionCounter;
	int placeCounter;
	int lastScore;
	int markingValue;

	/**
	 * Class constructor
	 */
	public ChanceGenerator() {
		modificationCounter = 0;
		lastScore = 0;
	}

	/**
	 * Generate net
	 *
	 * @param value score
	 * @param initMarkingValue marking value (k1 * k2 * ... * kn for kx != ky)
	 * @return generated net
	 */
	public PetriNet generateNet(int value, int initMarkingValue) {

		markingValue = Math.abs(initMarkingValue);

		if (value > lastScore) {
			modifyNet();
			modificationCounter = 0;
		} else if (value == lastScore) {
			if (modificationCounter < 5) {
				modifyNet();
			} else {
				renewNet();
			}
		} else {
			renewNet();
		}

		lastScore = value;

		return (pn);
	}

	/**
	 * Renew net
	 */
	public void renewNet() {

		modificationCounter = 0;
		lastScore = 0;

		pn = new PetriNet();

		Place p1 = pn.createPlace("p1");
		p1.setInitialToken(markingValue);

		Place p2 = pn.createPlace("p2");

		Transition t1 = pn.createTransition("t1");

		pn.createFlow(p1, t1);
		pn.createFlow(t1, p2);

		placeCounter = 2;
		transitionCounter = 1;

		long incrementSize = Math.round(3 + ((Math.random() - 0.5) * 6));

		for (int i = 0; i < incrementSize; i++) {

			double magic = Math.random();

			if (magic < 0.5) {
				addPlace();
			} else {
				addTransition();
			}
		}
	}

	/**
	 * Modify net
	 */
	public void modifyNet() {
		double magic = Math.random();

		if (magic < 0.20) {
			addPlace();
		} else if (magic < 0.30) {
			addMarks();
		} else if (magic < 0.35) {
			addWeight();
		} else if (magic < 0.7) {
			addTransition();
		} else {
			removeTransition();
		}

		modificationCounter++;
	}

	/**
	 * Add marks to place
	 * Most times one place, sometimes none or more...
	 */
	private void addMarks() {
		double addChance = 1.0 / pn.getPlaces().size();

		for (Place place : pn.getPlaces()) {
			double addDelete = Math.random();

			if (addDelete < addChance) {
				place.setInitialToken(markingValue);
			}

		}

	}

	/**
	 * Change weight of arc
	 * Most times one arc, sometimes none or more...
	 */
	private void addWeight() {
		double addChance = 1.0 / pn.getEdges().size();

		for (Flow arc : pn.getEdges()) {
			double addWeight = Math.random();
			int weight = (int) Math.round(2 + (Math.random() - 0.5) * 2);

			if (addWeight < addChance) {
				arc.setWeight(weight);
			}

		}

	}

	/**
	 * Add place to net with arcs
	 */
	private void addPlace() {

		placeCounter++;

		String name = "p" + placeCounter;

		Place p = pn.createPlace(name);
		p.setInitialToken(0);

		double arcChance = 1.0 / pn.getTransitions().size();

		for (Transition transition : pn.getTransitions()) {
			double randomValueTo = Math.random();
			double randomValueFrom = Math.random();

			if (randomValueTo < arcChance) {
				pn.createFlow(transition, p);
			}

			if (randomValueFrom < arcChance) {
				pn.createFlow(p, transition);
			}
		}
	}

	/**
	 * Add transition to net with arcs
	 */
	private void addTransition() {

		transitionCounter++;

		String name = "t" + transitionCounter;

		Transition t = pn.createTransition(name);

		double arcChance = 1.0 / pn.getPlaces().size();

		for (Place place : pn.getPlaces()) {
			double randomValueTo = Math.random();
			double randomValueFrom = Math.random();

			if (randomValueTo < arcChance) {
				pn.createFlow(place, t);
			}

			if (randomValueFrom < arcChance) {
				pn.createFlow(t, place);
			}
		}
	}

	/**
	 * Remove transition from net.
	 * Most times one transition, sometimes none or more...
	 *
	 */
	private void removeTransition() {
		double deleteChance = 1.0 / pn.getTransitions().size();

		Collection<Transition> toRemove = new HashSet<>();
		for (Transition transition : pn.getTransitions()) {
			double randomDelete = Math.random();

			if (randomDelete < deleteChance) {
				toRemove.add(transition);
			}

		}

		for (Transition transition : toRemove) {
			pn.removeTransition(transition);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
