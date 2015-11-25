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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.equations.InequalitySystem;

import static uniol.apt.util.DebugUtil.debug;

/**
 * This class finds impure solutions to separation problems without any other properties.
 * @author Uli Schlachter
 */
class BasicImpureSeparation extends BasicPureSeparation implements Separation {
	/**
	 * Construct a new instance for solving separation problems. This constructor does not do any checks for
	 * supported properties. It is the caller's responsibility to check this.
	 * @param utility The region utility to use.
	 * @param locationMap Mapping that describes the location of each event.
	 */
	protected BasicImpureSeparation(RegionUtility utility, String[] locationMap) {
		super(utility, locationMap);
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public BasicImpureSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this(utility, locationMap);
		if (!properties.equals(new PNProperties()))
			throw new UnsupportedPNPropertiesException();
	}

	/**
	 * Get a region solving some event/state separation problem.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		// Calculate a region which assigns to state a marking less than the marking of any state in which event
		// is enabled. This means we want 0 > r_E(Psi_s - Psi_s').
		InequalitySystem system = new InequalitySystem();
		int eventIndex = utility.getEventIndex(event);
		final List<Region> basis = utility.getRegionBasis();
		if (!utility.getSpanningTree().isReachable(state))
			// Unreachable states cannot be separated
			return null;

		stateLoop:
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!SeparationUtility.isEventEnabled(otherState, event))
				continue;

			List<BigInteger> inequality = new ArrayList<>(basis.size());
			for (Region region : basis) {
				// Evaluate [Psi_s - Psi_s'] in this region
				BigInteger stateValue, otherStateValue;
				try {
					stateValue = region.getMarkingForState(state);
				} catch (UnreachableException e) {
					throw new AssertionError("Made sure that the state is reachable, but "
							+ "apparently it isn't?!", e);
				}
				try {
					otherStateValue = region.getMarkingForState(otherState);
				} catch (UnreachableException e) {
					// Just ignore and skip unreachable states
					continue stateLoop;
				}
				inequality.add(stateValue.subtract(otherStateValue));
			}

			system.addInequality(0, ">", inequality, "inequality for state " + otherState);
		}

		// Calculate the resulting linear combination
		debug("Solving an inequality system to separate ", state, " from ", event, ":");
		Region result = findRegionFromSystem(system, basis, event);
		if (result == null)
			return null;

		// If this already is a separating region, return it
		if (SeparationUtility.isSeparatingRegion(result, state, event))
			return result;

		// Calculate m = min { r_S(s') | delta(s', event) defined }:
		// For each state in which 'event' is enabled...
		BigInteger min = null;
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!SeparationUtility.isEventEnabled(otherState, event))
				continue;

			try {
				BigInteger stateMarking = result.getMarkingForState(otherState);
				if (min == null || min.compareTo(stateMarking) > 0)
					min = stateMarking;
			} catch (UnreachableException e) {
				// Silently ignore unreachable states
				continue;
			}
		}

		// If the event is dead, no reachable marking fires it. Handle this by just adding a simple loop
		if (min == null)
			min = BigInteger.ONE;

		// Make the event have backward weight m. By construction, this must solve separation. Since the region
		// could already have a non-zero backward weight, we have to handle that.
		// (This solves ESSP, because the inequality system gave us a region where our state 'state' has a lower
		// marking than all states in which 'event' is enabled. Thus, we can add suitably many loops to solve
		// ESSP.)
		min = min.subtract(result.getBackwardWeight(eventIndex));
		debug("Adding self-loop to event ", event, " with weight ", min);
		assert min.compareTo(BigInteger.ZERO) > 0;
		return new Region.Builder(result).addLoopAround(eventIndex, min)
			.withInitialMarking(result.getInitialMarking());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
