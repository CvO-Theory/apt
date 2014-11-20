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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.util.equations.InequalitySystem;

/**
 * Helper functions for solving separation problems.
 * @author Uli Schlachter
 */
public class SeparationUtility {
	private SeparationUtility() {
	}

	/**
	 * Try to find an existing region which separates the two given states.
	 * @param utility The region utility to use.
	 * @param regions The regions to choose from.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A separating region or null.
	 */
	static public Region findSeparatingRegion(RegionUtility utility, Collection<Region> regions,
			State state, State otherState) {
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state) || !utility.getSpanningTree().isReachable(otherState))
			return null;

		for (Region region : regions) {
			// We need a region which assigns different values to these two states.
			int stateValue = region.evaluateParikhVector(stateParikhVector);
			int otherStateValue = region.evaluateParikhVector(otherStateParikhVector);
			if (stateValue != otherStateValue)
				return region;
		}

		return null;
	}

	/**
	 * Try to find an existing region which separates some state and some event.
	 * @param utility The region utility to use.
	 * @param regions The regions to choose from.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A separating region or null.
	 */
	static public Region findSeparatingRegion(RegionUtility utility, Collection<Region> regions,
			State state, String event) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		int eventIndex = utility.getEventIndex(event);
		for (Region region : regions) {
			// We need r(state) to be smaller than the event's backward weight in some region.
			if (region.getNormalRegionMarkingForState(state) < region.getBackwardWeight(eventIndex))
				return region;
		}

		return null;
	}

	/**
	 * Try to calculate a region which separates some state and some event. This calculates a linear combination of
	 * the given basis of abstract regions.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A separating region or null.
	 */
	static public Region calculateSeparatingRegion(RegionUtility utility, Collection<Region> basis,
			State state, String event) {
		InequalitySystem system = new InequalitySystem(basis.size());
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		int eventIndex = utility.getEventIndex(event);
		assert stateParikhVector != null;

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// Each state must be reachable in the resulting region, but event 'event' should be disabled in state.
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			List<Integer> inequality = new ArrayList<>(basis.size());
			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);

			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			for (Region region : basis) {
				// We want to evaluate [Psi_s - Psi_{s'} + 1_j] * region where 1_j is the Parikh vector
				// of a single event of type e_j == event.
				int stateValue = region.evaluateParikhVector(stateParikhVector);
				int otherStateValue = region.evaluateParikhVector(otherStateParikhVector);
				int disabledEventValue = region.getWeight(eventIndex);
				inequality.add(stateValue - otherStateValue + disabledEventValue);
			}

			system.addInequality(-1, ">=", inequality);
		}

		// Calculate the resulting linear combination
		List<Integer> solution = system.findSolution();
		if (solution.isEmpty())
			return null;

		assert solution.size() == basis.size();
		Region result = Region.createTrivialRegion(utility);
		int i = 0;
		for (Region region : basis) {
			result = result.addRegionWithFactor(region, solution.get(i++));
		}

		// Because addRegionWithFactor() doesn't work the way that this function would need
		return result.makePure();
	}

	/**
	 * Try to calculate an impure region which separates some state and some event.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A separating region or null.
	 */
	static public Region calculateSeparatingImpureRegion(RegionUtility utility, Collection<Region> basis,
			State state, String event) {
		InequalitySystem system = new InequalitySystem(basis.size());
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		int eventIndex = utility.getEventIndex(event);
		assert stateParikhVector != null;

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// For each state in which 'event' is enabled...
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (getFollowingState(otherState, event) == null)
				continue;

			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			List<Integer> inequality = new ArrayList<>(basis.size());
			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);

			for (Region region : basis) {
				// We want to evaluate [Psi_s - Psi_{s'}] * region
				int stateValue = region.evaluateParikhVector(stateParikhVector);
				int otherStateValue = region.evaluateParikhVector(otherStateParikhVector);
				inequality.add(stateValue - otherStateValue);
			}

			system.addInequality(-1, ">=", inequality);
		}

		// Calculate the resulting linear combination
		List<Integer> solution = system.findSolution();
		if (solution.isEmpty())
			return null;

		assert solution.size() == basis.size();
		Region result = Region.createTrivialRegion(utility);
		int i = 0;
		for (Region region : basis) {
			result = result.addRegionWithFactor(region, solution.get(i++));
		}
		// Because addRegionWithFactor() doesn't work the way that this function would need
		result = result.makePure();

		// If this already solves ESSP, return it
		if (result.getNormalRegionMarkingForState(state) < result.getBackwardWeight(eventIndex))
			return result;

		// Calculate m = min { r_S(s') | delta(s', event) defined }
		// For each state in which 'event' is enabled...
		Integer min = null;
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (getFollowingState(otherState, event) == null)
				continue;

			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			int stateMarking = result.getNormalRegionMarkingForState(otherState);
			if (min == null || min > stateMarking)
				min = stateMarking;
		}

		// If the event is dead, no reachable marking fires it. Handle this by just adding a simple loop
		if (min == null)
			min = 1;

		// Make the event have backward weight m. By construction this must solve separation. Since the region
		// could already have a non-zero backward weight, we have to handle that.
		min -= result.getBackwardWeight(eventIndex);
		assert min > 0;
		return result.addRegionWithFactor(Region.createUnitRegion(utility, eventIndex), min);
	}

	/**
	 * Try to find a region which separates some state and some event. First the existing regions in the collection
	 * are checked. If no suitable region is found, an attempt is made to calculate a new one via the given basis.
	 * @param utility The region utility to use.
	 * @param regions The regions to choose from.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @param impure If impure regions may be calculated
	 * @return A separating region or null.
	 */
	static public Region findOrCalculateSeparatingRegion(RegionUtility utility, Collection<Region> regions,
			Collection<Region> basis, State state, String event, boolean impure) {
		Region r = findSeparatingRegion(utility, regions, state, event);
		if (r == null)
			if (!impure)
				r = calculateSeparatingRegion(utility, basis, state, event);
			else
				r = calculateSeparatingImpureRegion(utility, basis, state, event);
		return r;
	}

	/**
	 * Get the state which is reached by firing the given event in the given state.
	 * @param state The state to examine.
	 * @param event The event that should fire.
	 * @return The following state or zero.
	 */
	static public State getFollowingState(State state, String event) {
		for (Arc arc : state.getPostsetEdges())
			if (arc.getLabel().equals(event))
				return arc.getTarget();
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
