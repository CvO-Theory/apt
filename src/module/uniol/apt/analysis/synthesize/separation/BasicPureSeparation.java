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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.equations.InequalitySystem;
import uniol.apt.util.equations.InequalitySystemSolver;

import static uniol.apt.util.DebugUtil.debug;

/**
 * This class finds pure solutions to separation problems without any other properties.
 * @author Uli Schlachter
 */
class BasicPureSeparation implements Separation {
	protected final RegionUtility utility;
	protected final String[] locationMap;

	/**
	 * Construct a new instance for solving separation problems. This constructor does not do any checks for
	 * supported properties. It is the caller's responsibility to check this.
	 * @param utility The region utility to use.
	 * @param locationMap Mapping that describes the location of each event.
	 */
	protected BasicPureSeparation(RegionUtility utility, String[] locationMap) {
		this.utility = utility;
		this.locationMap = locationMap;
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public BasicPureSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this(utility, locationMap);

		PNProperties required = new PNProperties().setPure(true);
		if (!properties.equals(required))
			throw new UnsupportedPNPropertiesException();
	}

	/**
	 * Prepare an inequality system for use by this class.
	 * @return A new inequality system.
	 */
	protected InequalitySystem prepareInequalitySystem() {
		return new InequalitySystem();
	}

	/**
	 * Calculate a region solving some state separation problem.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		/*
		 * More clever approach if no locationMap is necessary and no further properties are needed (eg plain):
		for (Region region : utility.getRegionBasis())
			if (SeparationUtility.isSeparatingRegion(utility, region, state, otherState))
				return region;
		*/

		if (!utility.getSpanningTree().isReachable(state) || !utility.getSpanningTree().isReachable(otherState))
			// Unreachable states cannot be separated
			return null;

		Region result = calculateSeparatingRegionInternal(state, otherState);
		if (result == null && locationMap != null)
			// With locations, it is not always true that we can strengthen "!=" to ">", so try both.
			result = calculateSeparatingRegionInternal(otherState, state);
		return result;
	}

	private Region calculateSeparatingRegionInternal(State state, State otherState) {
		final List<Region> basis = utility.getRegionBasis();
		InequalitySystem system = prepareInequalitySystem();

		List<BigInteger> inequality = new ArrayList<>(basis.size());
		for (Region region : basis) {
			BigInteger stateValue, otherStateValue;
			try {
				stateValue = region.getMarkingForState(state);
				otherStateValue = region.getMarkingForState(otherState);
			} catch (UnreachableException e) {
				throw new AssertionError("Made sure that the state is reachable, but "
						+ "apparently it isn't?!", e);
			}
			inequality.add(stateValue.subtract(otherStateValue));
		}
		// We want r_E(Psi_s - Psi_{s'}) != 0. Since we are in a finite LTS, we can just use ">".
		system.addInequality(0, ">", inequality, "Region should separate state " + state
				+ " from state " + otherState);

		// Calculate the resulting linear combination
		debug("Solving an inequality system to separate ", state, " from ", otherState, ":");
		return findRegionFromSystem(system, basis, null);
	}

	/**
	 * Get a region solving some event/state separation problem.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		// The initial marking of a normal region is max { r_E(-Psi_s') | s' in states }. The marking in state s
		// for a normal region is max { r_E(Psi_s - Psi_s') | s' in states }. We want 'event' to be disabled, so
		// this means: 0 > max { r_E(Psi_s - Psi_s' + 1*event) | s' in states }.
		// Since we are limiting the maximum from above, we can just require this for all states.
		InequalitySystem system = prepareInequalitySystem();
		int eventIndex = utility.getEventIndex(event);
		final List<Region> basis = utility.getRegionBasis();
		if (!utility.getSpanningTree().isReachable(state))
			// Unreachable states cannot be separated
			return null;

		stateLoop:
		for (State otherState : utility.getTransitionSystem().getNodes()) {
			List<BigInteger> inequality = new ArrayList<>(basis.size());
			for (Region region : basis) {
				// Evaluate [Psi_s - Psi_s' + 1*event] in this region
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
				inequality.add(stateValue.subtract(otherStateValue).add(region.getWeight(eventIndex)));
			}

			system.addInequality(0, ">", inequality, "inequality for state " + otherState);
		}

		// Calculate the resulting linear combination
		debug("Solving an inequality system to separate ", state, " from ", event, ":");
		return findRegionFromSystem(system, basis, event);
	}

	/**
	 * Generate the needed inequalities to guarantee that some distribution requirement is obeyed.
	 * @param utility Utility instance for which a separation problem is being solved.
	 * @param locationMap Mapping describing how events should be distributed to locations.
	 * @param event Optional event that surely consumes tokens from this region, or null.
	 * @return The needed inequalities
	 */
	static private InequalitySystem[] requireDistributableNet(RegionUtility utility, String[] locationMap,
			String event) {
		Set<String> locations;
		if (event == null) {
			locations = new HashSet<>(Arrays.asList(locationMap));
			locations.remove(null);
		} else {
			String location = locationMap[utility.getEventIndex(event)];
			if (location == null)
				return new InequalitySystem[0];

			locations = Collections.singleton(location);
		}

		InequalitySystem[] result = new InequalitySystem[locations.size()];
		int index = 0;
		for (String location : locations) {
			result[index] = new InequalitySystem();

			// Only events having location "location" may consume token.
			for (int eventIndex = 0; eventIndex < utility.getNumberOfEvents(); eventIndex++) {
				if (locationMap[eventIndex] != null && !locationMap[eventIndex].equals(location)) {
					// Require that 0 <= r_E(event)
					List<BigInteger> inequality = new ArrayList<>();
					for (Region region : utility.getRegionBasis())
						inequality.add(region.getWeight(eventIndex));

					result[index].addInequality(0, "<=", inequality, "Only events with location "
							+ location + " may consume tokens from this region");
				}
			}
			index++;
		}

		return result;
	}

	/**
	 * Get a region by solving an inequality system.
	 * @param system The inequality system to solve. Variables must be weights of the entries of the basis.
	 * @param basis The region basis in which the solution of the system should be interpreted.
	 * @param event Optional event that surely consumes tokens from this region, or null.
	 * @return A pure region from a solution of the system or null if the system was unsolvable.
	 */
	protected Region findRegionFromSystem(InequalitySystem system, List<Region> basis, String event) {
		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(system)
			.assertDisjunction(requireDistributableNet(utility, locationMap, event))
			.findSolution();
		if (solution.isEmpty())
			return null;

		assert solution.size() == basis.size();
		Region.Builder builder = new Region.Builder(utility);
		int i = 0;
		for (Region region : basis)
			builder.addRegionWithFactor(region, solution.get(i++));

		Region result = builder.makePure().withNormalRegionInitialMarking();
		debug("region: ", result);
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
