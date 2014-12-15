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
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.util.equations.InequalitySystem;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
public class SeparationUtility {
	/**
	 * Test if there exists an outgoing arc labelled with the given event.
	 * Get the state which is reached by firing the given event in the given state.
	 * @param state The state to examine.
	 * @param event The event that should fire.
	 * @return True if a suitable arc exists, else false.
	 * @return The following state or zero.
	 */
	static public boolean isEventEnabled(State state, String event) {
		for (Arc arc : state.getPostsetEdges())
			if (arc.getLabel().equals(event))
				return true;
		return false;
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
	 * @return A separating region or null.
	 */
	private Region findSeparatingRegion(Collection<Region> regions) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		for (Region region : regions) {
			// We need r(state) to be smaller than the event's backward weight in some region.
			if (region.getNormalRegionMarkingForState(state) < region.getBackwardWeight(event))
				return region;
		}

		return null;
	}

	/**
	 * Create an inequality system for calculating a region.
	 * @return An inequality system with numEvents + basisSize unknowns. The first unknowns describe the weights of
	 * the calculated regions, the last ones are weights for linear combinations of the basis.
	 */
	private InequalitySystem makeInequalitySystem() {
		// Generate an inequality system. The first eventList.size() variables are the weight of the calculated
		// region. The next basis.size() variables represent how this region is a linear combination of the
		// basis.
		final int events = utility.getNumberOfEvents();
		final int basisSize = basis.size();
		InequalitySystem system = new InequalitySystem();

		// The resulting region is a linear combination of the basis:
		//   region = sum lambda_i * r^i
		//        0 = sum lambda_i * r^i - region
		for (int thisEvent = 0; thisEvent < events; thisEvent++) {
			int[] inequality = new int[events + basisSize];
			inequality[thisEvent] = -1;
			int basisEntry = 0;
			for (Region region : basis)
				inequality[events + basisEntry++] = region.getWeight(thisEvent);

			system.addInequality(0, "=", inequality);
		}

		return system;
	}

	/**
	 * Add the needed inequalities so that the system may only produce k-bounded regions.
	 * @param k The limit for the bound.
	 */
	private void requireKBoundedness(int k) {
		int initialMarking = utility.getNumberOfEvents() + basis.size();
		int numVariables = initialMarking + 1;

		// Any initial marking r_S(s0) is possible, as long as it satisfies for each reachable state s:
		//    0 <= r_S(s) = r_S(s0) + r_E(Psi_s)
		// We also want to require k >= r_S(s)
		// (For the synthesized Petri Net we will use the minimal initial marking possible. However, we cannot
		// express this as a linear inequality and thus the solver may as well calculate something higher.)
		for (State state : utility.getTransitionSystem().getNodes()) {
			if (!utility.getSpanningTree().isReachable(state))
				continue;

			int[] inequality = new int[numVariables];
			List<Integer> stateParikhVector = utility.getReachingParikhVector(state);

			inequality[initialMarking] = 1;

			// Evaluate the Parikh vector in the region described by the system, just as
			// Region.evaluateParikhVector() would do.
			for (int event = 0; event < stateParikhVector.size(); event++)
				inequality[event] = stateParikhVector.get(event);

			system.addInequality(0, "<=", inequality);
			system.addInequality(k, ">=", inequality);
		}
	}

	/**
	 * Add the needed inequalities so that the system may only produce plain regions.
	 */
	private void requirePlainness() {
		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			int[] inequality = new int[utility.getNumberOfEvents()];

			inequality[event] = 1;

			system.addInequality(1, ">=", inequality);
			system.addInequality(-1, "<=", inequality);
		}
	}

	/**
	 * Add the needed inequalities so that the system may only produce T-Net regions.
	 * This requires plainness as a pre-condition!
	 */
	private void requireTNet() {
		// Let's assume both event a and event b are in the preset of the place corresponding to the region.
		// This means both events have a non-zero backward weight. Forbid this via -1 <= r(a)+r(b)
		// Analogously 1 >= r(a)+r(b) forbids both places to be in the postset.
		for (int a = 0; a < utility.getNumberOfEvents(); a++)
			for (int b = 0; b < utility.getNumberOfEvents(); b++) {
				if (a == b)
					continue;

				int[] inequality = new int[utility.getNumberOfEvents()];

				inequality[a] = 1;
				inequality[b] = 1;

				system.addInequality(1, ">=", inequality);
				system.addInequality(-1, "<=", inequality);
			}
	}

	/**
	 * Try to calculate a pure region which separates some state and some event. This calculates a linear combination of
	 * the given basis of abstract regions.
	 * @return A separating region or null.
	 */
	private Region calculateSeparatingPureRegion() {
		final int events = utility.getNumberOfEvents();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		assert stateParikhVector != null;

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// Each state must be reachable in the resulting region, but event 'event' should be disabled in state.
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			List<Integer> inequality = new ArrayList<>(events + basis.size());
			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);

			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			// For the resulting region variables, we have value 0
			inequality.addAll(Collections.nCopies(events, 0));

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

		return Region.createPureRegionFromVector(utility, solution.subList(0, events));
	}

	/**
	 * Try to calculate an impure region which separates some state and some event.
	 * @param plainNet Whether the generated region should correspond to a plain Petri Net and thus any
	 * side-condition must be plain, too.
	 * @return A separating region or null.
	 */
	private Region calculateSeparatingImpureRegion(boolean plainNet) {
		final int events = utility.getNumberOfEvents();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		assert stateParikhVector != null;

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// For each state in which 'event' is enabled...
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!isEventEnabled(otherState, event))
				continue;

			// Silently ignore unreachable states
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			List<Integer> inequality = new ArrayList<>(events + basis.size());
			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);

			// For the resulting region variables, we have value 0
			inequality.addAll(Collections.nCopies(events, 0));

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

		Region result = Region.createPureRegionFromVector(utility, solution.subList(0, events));

		// If this already solves ESSP, return it
		if (result.getNormalRegionMarkingForState(state) < result.getBackwardWeight(eventIndex))
			return result;

		// Calculate m = min { r_S(s') | delta(s', event) defined }
		// For each state in which 'event' is enabled...
		Integer min = null;
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!isEventEnabled(otherState, event))
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

		// Does adding the side-condition violate the required plainness?
		if (plainNet && (min > 1 || result.getWeight(eventIndex) != 0))
			// XXX: I'm not totally sure that no separating region exists in this case. Some other region
			// could satisfy the inequality system *and* result in a plain place.
			return null;

		return result.addRegionWithFactor(Region.createUnitRegion(utility, eventIndex), min);
	}

	private final RegionUtility utility;
	private final Collection<Region> basis;
	private final State state;
	private final String event;
	private final int eventIndex;

	/**
	 * This inequality system describes the regions that we are looking for. The first unknowns describe the weights
	 * for the calculated region. The next unknowns are the coefficients for the entries of the basis that describe
	 * how this region is produced from the basis.
	 *
	 * In summary:
	 * [0, numEvents): Weights for the calculated region
	 * [numEvents, numEvents+basisSize): Coefficients for the entries of the basis
	 * [numEvents+basisSize): Initial marking (used by requireKBoundedness)
	 */
	private final InequalitySystem system;
	private final Region resultingRegion;

	/**
	 * Construct a new SeparationUtility for the given event/state separation instance.
	 * @param utility The region utility to use.
	 * @param regions The regions to choose from.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @param properties Properties that the calculated region should satisfy.
	 */
	public SeparationUtility(RegionUtility utility, Collection<Region> regions, Collection<Region> basis,
			State state, String event, PNProperties properties) {
		this.utility = utility;
		this.basis = basis;
		this.state = state;
		this.event = event;
		this.eventIndex = utility.getEventIndex(event);
		this.system = makeInequalitySystem();

		assert this.eventIndex >= 0;

		// TODO: Nothing guarantees that all regions in the basis satisfy <properties> (same for the direct call
		// to findSeparatingRegion() in SynthesizePN)
		Region r = findSeparatingRegion(regions);
		if (r == null) {
			if (properties.isKBounded())
				requireKBoundedness(properties.getKForKBoundedness());
			if (properties.isPlain())
				requirePlainness();
			if (properties.isTNet())
				requireTNet();

			if (properties.isPure())
				r = calculateSeparatingPureRegion();
			else
				r = calculateSeparatingImpureRegion(properties.isPlain());
		}
		this.resultingRegion = r;
	}

	/**
	 * Construct a new SeparationUtility for the given event/state separation instance.
	 * @param utility The region utility to use.
	 * @param regions The regions to choose from.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 */
	public SeparationUtility(RegionUtility utility, Collection<Region> regions, Collection<Region> basis,
			State state, String event) {
		this(utility, regions, basis, state, event, new PNProperties());
	}

	/**
	 * Get the resulting region that solves this separation problem.
	 * @return Either one of the pre-existing regions, a new one that solves the specified problem or null.
	 */
	public Region getRegion() {
		return resultingRegion;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
