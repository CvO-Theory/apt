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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.util.equations.InequalitySystem;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
public class SeparationUtility {
	private static void debug(String message) {
		// System.err.println("SeparationUtility: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	private final RegionUtility utility;
	private final Collection<Region> basis;
	private final PNProperties properties;
	private final String[] locationMap;

	/**
	 * This inequality system describes the regions that we are looking for. The first unknowns describe the weights
	 * for the calculated region. The next unknowns are the coefficients for the entries of the basis that describe
	 * how this region is produced from the basis. Then come separate variables for the forward weights and
	 * afterwards variables for the backward weights. Finally there is a single variable for the initial marking.
	 */
	private final InequalitySystem system;
	private final int systemWeightsStart;
	private final int systemCoefficientsStart;
	private final int systemForwardWeightsStart;
	private final int systemBackwardWeightsStart;
	private final int systemInitialMarking;
	private final int systemNumberOfVariables;

	/**
	 * Construct a new SeparationUtility for the given event/state separation instance.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param properties Properties that the calculated region should satisfy.
	 */
	public SeparationUtility(RegionUtility utility, Collection<Region> basis, PNProperties properties) throws MissingLocationException {
		this.utility = utility;
		this.basis = basis;
		this.properties = new PNProperties(properties);
		this.systemWeightsStart = 0;
		this.systemCoefficientsStart = systemWeightsStart + utility.getNumberOfEvents();
		this.systemForwardWeightsStart = systemCoefficientsStart + basis.size();
		this.systemBackwardWeightsStart = systemForwardWeightsStart + utility.getNumberOfEvents();
		this.systemInitialMarking = systemBackwardWeightsStart + utility.getNumberOfEvents();
		this.systemNumberOfVariables = systemInitialMarking + 1;
		this.locationMap = getLocationMap(utility);

		debug("Variables:");
		debug("Weights start at " + systemWeightsStart);
		debug("Coefficients from basis start at " + systemCoefficientsStart);
		debug("Forward weights start at " + systemForwardWeightsStart);
		debug("Backward weights start at " + systemBackwardWeightsStart);
		debug("Initial marking is variable " + systemInitialMarking);

		this.system = makeInequalitySystem();

		if (properties.isKBounded())
			requireKBoundedness(properties.getKForKBoundedness());
		if (properties.isPlain())
			requirePlainness();
		if (properties.isTNet())
			requireTNet();
		if (properties.isOutputNonbranching())
			requireOutputNonbranchingNet(system);
	}

	/**
	 * Construct a new SeparationUtility for the given event/state separation instance.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 */
	public SeparationUtility(RegionUtility utility, Collection<Region> basis) throws MissingLocationException {
		this(utility, basis, new PNProperties());
	}

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

		for (Region region : regions) {
			// We need r(state) to be smaller than the event's backward weight in some region.
			if (region.getMarkingForState(state) < region.getBackwardWeight(event))
				return region;
		}

		return null;
	}

	/**
	 * Get an array of coefficients that describe the marking of the given state.
	 * @param state The state whose marking should be calculated.
	 * @return An array of coefficients or null if the state is not reachable.
	 */
	private int[] coefficientsForStateMarking(State state) {
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		int[] inequality = new int[systemNumberOfVariables];

		// Evaluate the Parikh vector in the region described by the system, just as
		// Region.evaluateParikhVector() would do.
		for (int event = 0; event < stateParikhVector.size(); event++)
			inequality[systemWeightsStart + event] = stateParikhVector.get(event);

		inequality[systemInitialMarking] = 1;

		return inequality;
	}

	/**
	 * Create an inequality system for calculating a region.
	 * @return An inequality system prepared for calculating separating regions.
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
			inequality[systemWeightsStart + thisEvent] = -1;
			int basisEntry = 0;
			for (Region region : basis)
				inequality[systemCoefficientsStart + basisEntry++] = region.getWeight(thisEvent);

			system.addInequality(0, "=", inequality, "Resulting region is a linear combination of basis for event " + thisEvent);
		}

		// The weight is a combination of the forward and backward weight
		int inequalitySize = systemBackwardWeightsStart + utility.getNumberOfEvents();
		for (int thisEvent = 0; thisEvent < events; thisEvent++) {
			// weight = forwardWeight - backwardWeight (=> 0 = -w + f - b)
			int[] inequality = new int[inequalitySize];
			inequality[systemWeightsStart + thisEvent] = -1;
			inequality[systemForwardWeightsStart + thisEvent] = 1;
			inequality[systemBackwardWeightsStart + thisEvent] = -1;
			system.addInequality(0, "=", inequality, "weight = forward - backward for event " + thisEvent);

			// Forward weight must be non-negative
			inequality = new int[inequalitySize];
			inequality[systemForwardWeightsStart + thisEvent] = 1;
			system.addInequality(0, "<=", inequality, "Forward weight must be positive");

			// Backward weight must be non-negative
			inequality = new int[inequalitySize];
			inequality[systemBackwardWeightsStart + thisEvent] = 1;
			system.addInequality(0, "<=", inequality, "Backward weight must be positive");
		}

		// Any enabled event really must be enabled in the calculated region
		for (Arc arc : utility.getTransitionSystem().getEdges()) {
			State state = arc.getSource();
			if (!utility.getSpanningTree().isReachable(state))
				continue;

			// r_B(event) <= r_S(state) = r_S(s0) + r_E(Psi_state)
			int[] inequality = coefficientsForStateMarking(state);
			inequality[systemBackwardWeightsStart + utility.getEventIndex(arc.getLabel())] += -1;

			system.addInequality(0, "<=", inequality, "Event " + arc.getLabel() + " is enabled in state " + state);
		}

		return system;
	}

	/**
	 * Add the needed inequalities so that the system may only produce k-bounded regions.
	 * @param k The limit for the bound.
	 */
	private void requireKBoundedness(int k) {
		int inequalitySize = systemInitialMarking + 1;

		// Require k >= r_S(s) = r_S(s0) + r_E(Psi_s)
		for (State state : utility.getTransitionSystem().getNodes()) {
			if (!utility.getSpanningTree().isReachable(state))
				continue;

			// k >= r_S(state)
			int[] inequality = coefficientsForStateMarking(state);
			system.addInequality(k, ">=", inequality, "State " + state + " must obey " + k + "-boundedness");
		}
	}

	/**
	 * Add the needed inequalities so that the system may only produce plain regions.
	 */
	private void requirePlainness() {
		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			int[] inequality = new int[systemNumberOfVariables];

			inequality[systemForwardWeightsStart + event] = 1;
			inequality[systemBackwardWeightsStart + event] = 0;
			system.addInequality(1, ">=", inequality, "Plain");

			inequality[systemForwardWeightsStart + event] = 0;
			inequality[systemBackwardWeightsStart + event] = 1;
			system.addInequality(1, ">=", inequality, "Plain");
		}
	}

	/**
	 * Add the needed inequalities so that the system may only produce T-Net regions.
	 * This requires plainness as a pre-condition!
	 */
	private void requireTNet() {
		// The implementation (in PNProperties) makes sure that T-Net also requires output-nonbranching. So we
		// only have to handle the postsets here.
		int[] inequality = new int[systemNumberOfVariables];
		Arrays.fill(inequality, systemForwardWeightsStart, systemForwardWeightsStart + utility.getNumberOfEvents(), 1);
		system.addInequality(1, ">=", inequality, "T-Net");
	}

	/**
	 * Add the needed inequalities so that the system may only produce output-nonbranching regions.
	 * @param system The inequality system to which the inequalities should be added.
	 */
	private void requireOutputNonbranchingNet(InequalitySystem system) {
		// A ON-net has at most one place that removes token from it.
		int[] inequality = new int[systemNumberOfVariables];
		Arrays.fill(inequality, systemBackwardWeightsStart, systemBackwardWeightsStart + utility.getNumberOfEvents(), 1);
		system.addInequality(1, ">=", inequality, "Output-nonbranching");
	}

	/**
	 * Calculate a mapping from events to their location.
	 * @param utility The region utility that describes the events.
	 * @return An array containing the location for each event.
	 */
	static public String[] getLocationMap(RegionUtility utility) throws MissingLocationException {
		// Build a mapping from events to locations. Yaaay. Need to iterate over all arcs...
		String[] locationMap = new String[utility.getNumberOfEvents()];
		boolean hadEventWithLocation = false;

		for (Arc arc : utility.getTransitionSystem().getEdges()) {
			String location;
			try {
				location = arc.getExtension("location").toString();
			} catch (StructureException e) {
				// Because just returning "null" is too easy...
				continue;
			}

			int event = utility.getEventIndex(arc.getLabel());
			String oldLocation = locationMap[event];
			locationMap[event] = location;
			hadEventWithLocation = true;

			// The parser makes sure that this assertion always holds. If something constructs a PN which
			// breaks this assumption, then the bug is in that code.
			assert oldLocation == null || oldLocation.equals(location);
		}

		// Do all events have a location?
		if (hadEventWithLocation && Arrays.asList(locationMap).contains(null))
			throw new MissingLocationException("Trying to synthesize a Petri Net where some events have a "
					+ "location and others do not. Either all or no event must have a location.");

		return locationMap;
	}

	/**
	 * Add the needed inequalities to guarantee that a distributable Petri Net region is calculated.
	 * @param system The inequality system to which the inequalities should be added.
	 * @param locationMap Mapping that describes the location of each event.
	 * @param event Only events with the same location as this event may consume tokens from this region.
	 */
	private void requireDistributableNet(InequalitySystem system, String[] locationMap, String event) {
		int[] inequality = new int[systemNumberOfVariables];
		String location = locationMap[utility.getEventIndex(event)];

		if (location == null)
			return;

		// Only events having the same location as 'event' may consume token from this region.
		for (int eventIndex = 0; eventIndex < utility.getNumberOfEvents(); eventIndex++) {
			if (locationMap[eventIndex] != null && !locationMap[eventIndex].equals(location))
				inequality[systemBackwardWeightsStart + eventIndex] = 1;
		}

		system.addInequality(0, "=", inequality, "Only events with same location as event " + event + " may consume tokens from this region");
	}

	/**
	 * Add the needed inequalities to guarantee that the preset is contained in the postset. This is used for
	 * synthesizing conflict-free nets.
	 * @param system The inequality system to which the inequalities should be added.
	 */
	private void requirePostsetContainsPreset(InequalitySystem system) {
		for (int eventIndex = 0; eventIndex < utility.getNumberOfEvents(); eventIndex++) {
			int[] inequality = new int[systemNumberOfVariables];
			inequality[systemWeightsStart + eventIndex] = 1;
			system.addInequality(0, "<=", inequality, "Preset contains postset for event " + eventIndex + " (No tokens are consumed)");
		}
	}

	/**
	 * Try to calculate a pure region which separates some state and some event. This calculates a linear combination of
	 * the given basis of abstract regions.
	 * @param utility The region utility to use.
	 * @param system An inequality system that is suitably prepared.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @param pure Whether the generated region should describe part of a pure Petri Net and thus must not generate
	 * any side-conditions.
	 * @return A separating region or null.
	 */
	private Region calculateSeparatingRegion(RegionUtility utility, InequalitySystem system,
			Collection<Region> basis, State state, State otherState, boolean pure) {
		final int events = utility.getNumberOfEvents();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state) || !utility.getSpanningTree().isReachable(otherState))
			return null;

		// We want r_S(s) != r_S(s'). Since for each region there exists a complementary region (we are only
		// looking at the bounded case!), we can require r_S(s) < r_S(s')
		int[] inequality = coefficientsForStateMarking(state);
		int[] otherInequality = coefficientsForStateMarking(state);

		for (int i = 0; i < inequality.length; i++)
			inequality[i] -= otherInequality[i];

		system.addInequality(-1, ">=", inequality, "Region should separate state " + state + " from state " + otherState);

		// Calculate the resulting linear combination
		debug("Solving the following system to separate " + state + " from " + otherState + ":");
		debug(system);
		List<Integer> solution = system.findSolution();
		if (solution.isEmpty()) {
			debug("No solution found");
			return null;
		}

		debug("solution: " + solution);

		Region r = new Region(utility,
				solution.subList(systemBackwardWeightsStart, systemBackwardWeightsStart + events),
				solution.subList(systemForwardWeightsStart, systemForwardWeightsStart + events))
			.withInitialMarking(solution.get(systemInitialMarking));
		debug("region: " + r);

		if (pure)
			r = r.makePure();
		assert r.getNormalRegionMarking() <= solution.get(systemInitialMarking) : solution;
		return r;
	}


	/**
	 * Try to calculate a pure region which separates some state and some event. This calculates a linear combination of
	 * the given basis of abstract regions.
	 * @param utility The region utility to use.
	 * @param system An inequality system that is suitably prepared.
	 * @param basis A basis of abstract regions of the underlying transition system. This collection must guarantee
	 * stable iteration order!
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @param pure Whether the generated region should describe part of a pure Petri Net and thus must not generate
	 * any side-conditions.
	 * @return A separating region or null.
	 */
	private Region calculateSeparatingRegion(RegionUtility utility, InequalitySystem system,
			Collection<Region> basis, State state, String event, boolean pure) {
		final int eventIndex = utility.getEventIndex(event);
		final int events = utility.getNumberOfEvents();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);

		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// Each state must be reachable in the resulting region, but event 'event' should be disabled in state.
		// We want -1 >= r_S(s) - r_B(event)
		int[] inequality = coefficientsForStateMarking(state);

		if (pure) {
			// In the pure case, in the above -r_B(event) is replaced with +r_E(event). Since all
			// states must be reachable, this makes sure that r_E(event) really is negative and thus
			// the resulting region solves ESSP.
			inequality[systemWeightsStart + eventIndex] += 1;
		} else {
			inequality[systemBackwardWeightsStart + eventIndex] += -1;
		}

		system.addInequality(-1, ">=", inequality, "Region should separate state " + state + " from event " + event);

		// Calculate the resulting linear combination
		debug("Solving the following system to separate " + state + " from " + event + ":");
		debug(system);
		List<Integer> solution = system.findSolution();
		if (solution.isEmpty()) {
			debug("No solution found");
			return null;
		}

		debug("solution: " + solution);

		Region r = new Region(utility,
				solution.subList(systemBackwardWeightsStart, systemBackwardWeightsStart + events),
				solution.subList(systemForwardWeightsStart, systemForwardWeightsStart + events))
			.withInitialMarking(solution.get(systemInitialMarking));
		debug("region: " + r);

		if (pure)
			r = r.makePure();
		assert r.getNormalRegionMarking() <= solution.get(systemInitialMarking) : solution;
		return r;
	}

	/**
	 * Get a region solving some separation problem.
	 * @param regions The regions to choose from.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	public Region getSeparatingRegion(Collection<Region> regions, State state, State otherState) {
		Region r = findSeparatingRegion(utility, regions, state, otherState);
		if (r == null)
		{
			InequalitySystem systemCopy = null;
			InequalitySystem system = new InequalitySystem(this.system);
			// TODO: How can this be implemented?
			//requireDistributableNet(system, locationMap, event);

			// TODO: Is this needed? Can this be optimized? Think about it
			if (properties.isConflictFree() && !properties.isOutputNonbranching()) {
				systemCopy = new InequalitySystem(system);

				// Conflict free: Either the place is output-nonbranching or the preset is contained in
				// the postset.
				requireOutputNonbranchingNet(systemCopy);
				requirePostsetContainsPreset(system);
			}

			r = calculateSeparatingRegion(utility, system, basis, state, otherState, properties.isPure());

			if (r == null && systemCopy != null) {
				debug("Trying again with output-nonbranching");
				r = calculateSeparatingRegion(utility, systemCopy, basis, state, otherState, properties.isPure());
			}
		}
		return r;
	}

	/**
	 * Get a region solving some separation problem.
	 * @param regions The regions to choose from.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	public Region getSeparatingRegion(Collection<Region> regions, State state, String event) {
		Region r = findSeparatingRegion(utility, regions, state, event);
		if (r == null)
		{
			InequalitySystem systemCopy = null;
			InequalitySystem system = new InequalitySystem(this.system);
			requireDistributableNet(system, locationMap, event);

			if (properties.isConflictFree() && !properties.isOutputNonbranching()) {
				systemCopy = new InequalitySystem(system);

				// Conflict free: Either the place is output-nonbranching or the preset is contained in
				// the postset.
				requireOutputNonbranchingNet(systemCopy);
				requirePostsetContainsPreset(system);
			}

			r = calculateSeparatingRegion(utility, system, basis, state, event, properties.isPure());

			if (r == null && systemCopy != null) {
				debug("Trying again with output-nonbranching");
				r = calculateSeparatingRegion(utility, systemCopy, basis, state, event, properties.isPure());
			}
		}
		return r;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
