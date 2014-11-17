/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * Synthesize a Petri Net from a transition system.
 * @author Uli Schlachter
 */
public class SynthesizePN {
	private final TransitionSystem ts;
	private final RegionBasis regionBasis;
	private final RegionUtility utility;
	private final Set<Region> regions = new HashSet<>();
	private final Set<Set<State>> failedStateSeparationProblems = new HashSet<>();
	private final Set<Pair<String, State>> failedEventStateSeparationProblems = new HashSet<>();

	private static void debug(String message) {
		//System.err.println("SynthesizePN: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 */
	public SynthesizePN(RegionUtility utility) {
		this.ts = utility.getTransitionSystem();
		this.utility = utility;
		this.regionBasis = new RegionBasis(utility);

		debug("Region basis: " + regionBasis);

		// TODO: Test preconditions on ts (deterministic, totally reachable, reduced)
		// Deterministic: Non-determinism will make SSP fail
		// Totally Reachable: Uhm. NullPointerExceptions when we try to get a parikh vector? (Or if empty
		// vector: SSP fails)
		// reduced: Otherwise the resulting PN will contain an isolated transitions.

		debug();
		debug("Solving state separation");
		solveStateSeparation();

		debug();
		debug("Solving event-state separation");
		solveEventStateSeparation();

		debug();
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param ts The transition system to synthesize.
	 */
	public SynthesizePN(TransitionSystem ts) {
		this(new RegionUtility(ts));
	}

	/**
	 * Solve all instances of the state separation problem (SSP).
	 */
	private void solveStateSeparation() {
		Set<State> alreadyHandled = new HashSet<>();
		for (State state : ts.getNodes()) {
			alreadyHandled.add(state);
			for (State otherState : ts.getNodes()) {
				if (alreadyHandled.contains(otherState))
					continue;

				debug("Trying to separate " + state + " from " + otherState);
				Region r = SeparationUtility.findSeparatingRegion(utility, regions, state, otherState);
				if (r == null) {
					r = SeparationUtility.findSeparatingRegion(utility, regionBasis,
							state, otherState);
					if (r == null) {
						Set<State> problem = new HashSet<>();
						problem.add(state);
						problem.add(otherState);
						failedStateSeparationProblems.add(problem);

						debug("Failure!");
					} else {
						debug("Found region " + r);
						regions.add(r);
					}
				} else {
					debug("Found already-existing separating region " + r);
				}
			}
		}
	}

	/**
	 * Solve all instances of the event/state separation problem (ESSP).
	 */
	private void solveEventStateSeparation() {
		for (State state : ts.getNodes())
			for (String event : ts.getAlphabet()) {
				if (SeparationUtility.getFollowingState(state, event) == null) {
					debug("Trying to separate " + state + " from event '" + event + "'");
					Region r = SeparationUtility.findOrCalculateSeparatingRegion(utility,
							regions, regionBasis, state, event);
					if (r == null) {
						failedEventStateSeparationProblems.add(
								new Pair<>(event, state));
						debug("Failure!");
					} else if (regions.add(r))
						debug("Found region " + r);
					else
						debug("Found already-existing separating region " + r);
				}
			}
	}

	/**
	 * Get all separating regions which were calculated
	 * @return All separating regions found.
	 */
	public Set<Region> getSeparatingRegions() {
		return Collections.unmodifiableSet(regions);
	}

	/**
	 * Check if the transition system was successfully separated.
	 * @return True if the transition was successfully separated.
	 */
	public boolean wasSuccessfullySeparated() {
		return failedStateSeparationProblems.isEmpty() && failedEventStateSeparationProblems.isEmpty();
	}

	/**
	 * Get all the state separation problems which could not be solved.
	 * @return A set containing sets of two states which cannot be differentiated by any region.
	 */
	public Set<Set<State>> getFailedStateSeparationProblems() {
		return Collections.unmodifiableSet(failedStateSeparationProblems);
	}

	/**
	 * Get all the event/state separation problems which could not be solved.
	 * @return A set containing instances of the event/state separation problem.
	 */
	public Set<Pair<String, State>> getFailedEventStateSeparationProblems() {
		return Collections.unmodifiableSet(failedEventStateSeparationProblems);
	}

	/**
	 * Synthesize a Petri Net from the separating regions that were calculated.
	 * @return The synthesized PetriNet
	 */
	public PetriNet synthesizePetriNet() {
		if (!wasSuccessfullySeparated())
			return null;
		return synthesizePetriNet(regions);
	}

	/**
	 * Synthesize a Petri Net from the given regions.
	 * @param regions The regions that should be used for synthesis.
	 * @return The synthesized PetriNet
	 */
	public static PetriNet synthesizePetriNet(Set<Region> regions) {
		PetriNet pn = new PetriNet();

		debug("Synthesizing PetriNet from these regions:");
		debug(regions);

		for (Region region : regions) {
			Place place = pn.createPlace();
			place.setInitialToken(region.getNormalRegionMarking());
			place.putExtension(Region.class.getName(), region);

			for (String event : region.getRegionUtility().getEventList()) {
				Transition transition = getOrCreateTransition(pn, event);
				int backward = region.getBackwardWeight(event);
				assert backward >= 0;
				if (backward > 0)
					pn.createFlow(place, transition, backward);

				int forward = region.getForwardWeight(event);
				assert forward >= 0;
				if (forward > 0)
					pn.createFlow(transition, place, forward);
			}
		}

		return pn;
	}

	/**
	 * Get or create the given transition in the Petri Net.
	 */
	private static Transition getOrCreateTransition(PetriNet pn, String id) {
		try {
			return pn.getTransition(id);
		} catch (NoSuchNodeException ex) {
			try {
				return pn.createTransition(id);
			} catch (NodeExistsException ex2) {
				throw new AssertionError("Tried to get or create transition with id '" + id + "'. " +
						"Getting failed, claiming that the transition doesn't exist. " +
						"Creating failed, claiming that the transition already exists.", ex2);
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
