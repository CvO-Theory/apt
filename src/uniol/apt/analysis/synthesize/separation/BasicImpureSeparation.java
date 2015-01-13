/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.util.equations.InequalitySystem;

/**
 * This class finds impure solutions to separation problems without any other properties.
 * @author Uli Schlachter
 */
class BasicImpureSeparation extends BasicPureSeparation implements Separation {
	private static void debug(String message) {
		// System.err.println("BasicImpureSeparation: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	/**
	 * Construct a new instance for solving separation problems. This constructor does not do any checks for
	 * supported properties. It is the caller's responsibility to check this.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system.
	 */
	protected BasicImpureSeparation(RegionUtility utility, List<Region> basis) {
		super(utility, basis);
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system.
	 * @param properties Properties that the calculated region should satisfy.
	 */
	public BasicImpureSeparation(RegionUtility utility, List<Region> basis, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this(utility, basis);
		// We do not support locations, so no locations may be specified
		if (Collections.frequency(Arrays.asList(locationMap), null) != locationMap.length)
			throw new UnsupportedPNPropertiesException();
		if (!properties.equals(new PNProperties()))
			throw new UnsupportedPNPropertiesException();
	}

	/**
	 * Get a region solving some event/state separation problem.
	 * @param regions Already existing regions to chose from.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(Collection<Region> regions, State state, String event) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		for (Region region : regions)
			if (SeparationUtility.isSeparatingRegion(utility, region, state, event))
				return region;

		// Calculate a region which assigns to state a marking less than the marking of any state in which event
		// is enabled. This means we want 0 > r_E(Psi_s - Psi_s').
		InequalitySystem system = new InequalitySystem();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		int eventIndex = utility.getEventIndex(event);
		assert stateParikhVector != null;
		assert utility.getSpanningTree().isReachable(state);

		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;
			if (SeparationUtility.getFollowingState(otherState, event) == null)
				continue;

			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);
			List<Integer> inequality = new ArrayList<>(basis.size());
			for (Region region : basis) {
				// Evaluate [Psi_s - Psi_s'] in this region
				int stateValue = region.evaluateParikhVector(stateParikhVector);
				int otherStateValue = region.evaluateParikhVector(otherStateParikhVector);
				inequality.add(stateValue - otherStateValue);
			}

			system.addInequality(0, ">", inequality, "inequality for state " + otherState);
		}

		// Calculate the resulting linear combination
		debug("Solving the following system to separate " + state + " from " + event + ":");
		debug(system);
		List<Integer> solution = system.findSolution();
		if (solution.isEmpty()) {
			debug("No solution found");
			return null;
		}
		debug("solution: " + solution);

		assert solution.size() == basis.size();
		Region result = Region.createTrivialRegion(utility);
		int i = 0;
		for (Region region : basis) {
			result = result.addRegionWithFactor(region, solution.get(i++));
		}
		result = result.makePure();
		result = result.withInitialMarking(result.getNormalRegionMarking());
		debug("region: " + result);

		// If this already is a separating region, return it
		if (SeparationUtility.isSeparatingRegion(utility, result, state, event))
			return result;

		// Calculate m = min { r_S(s') | delta(s', event) defined }:
		// For each state in which 'event' is enabled...
		Integer min = null;
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;
			if (SeparationUtility.getFollowingState(otherState, event) == null)
				continue;

			int stateMarking = result.getMarkingForState(otherState);
			if (min == null || min > stateMarking)
				min = stateMarking;
		}

		// If the event is dead, no reachable marking fires it. Handle this by just adding a simple loop
		if (min == null)
			min = 1;

		// Make the event have backward weight m. By construction, this must solve separation. Since the region
		// could already have a non-zero backward weight, we have to handle that.
		// (This solves ESSP, because the inequality system gave us a region where our state 'state' has a lower
		// marking than all states in which 'event' is enabled. Thus, we can add suitably many loops to solve
		// ESSP.)
		min -= result.getBackwardWeight(eventIndex);
		debug("Adding self-loop to event " + event + " with weight " + min);
		assert min > 0;
		return result.addRegionWithFactor(Region.createUnitRegion(utility, eventIndex), min);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
