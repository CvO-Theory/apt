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
import uniol.apt.util.DebugUtil;
import uniol.apt.util.equations.InequalitySystem;

/**
 * This class finds pure solutions to separation problems without any other properties.
 * @author Uli Schlachter
 */
class BasicPureSeparation extends DebugUtil implements Separation {
	protected final RegionUtility utility;
	protected final List<Region> basis;

	/**
	 * Construct a new instance for solving separation problems. This constructor does not do any checks for
	 * supported properties. It is the caller's responsibility to check this.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system.
	 */
	protected BasicPureSeparation(RegionUtility utility, List<Region> basis) {
		this.utility = utility;
		this.basis = basis;
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param basis A basis of abstract regions of the underlying transition system.
	 * @param properties Properties that the calculated region should satisfy.
	 */
	public BasicPureSeparation(RegionUtility utility, List<Region> basis, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this(utility, basis);
		// We do not support locations, so no locations may be specified
		if (Collections.frequency(Arrays.asList(locationMap), null) != locationMap.length)
			throw new UnsupportedPNPropertiesException();
		if (!properties.equals(new PNProperties(PNProperties.PURE)))
			throw new UnsupportedPNPropertiesException();
	}

	/**
	 * Calculate a region solving some state separation problem.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		for (Region region : basis)
			if (SeparationUtility.isSeparatingRegion(utility, region, state, otherState))
				return region;

		return null;
	}

	/**
	 * Get a region solving some event/state separation problem.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		// The initial marking of a normal region is max { r_E(-Psi_s') | s' in states }. The marking in state s
		// for a normal region is max { r_E(Psi_s - Psi_s') | s' in states }. We want 'event' to be disabled, so
		// it would need to lead to a negative marking: 0 > max { r_E(Psi_s - Psi_s' + 1*event) | s' in states }.
		// Since we are limiting the maximum from above, we can just require this for all states.
		InequalitySystem system = new InequalitySystem();
		List<Integer> stateParikhVector = utility.getReachingParikhVector(state);
		int eventIndex = utility.getEventIndex(event);
		assert stateParikhVector != null;
		assert utility.getSpanningTree().isReachable(state);

		for (State otherState : utility.getTransitionSystem().getNodes()) {
			if (!utility.getSpanningTree().isReachable(otherState))
				continue;

			List<Integer> otherStateParikhVector = utility.getReachingParikhVector(otherState);
			List<Integer> inequality = new ArrayList<>(basis.size());
			for (Region region : basis) {
				// Evaluate [Psi_s - Psi_s' + 1*event] in this region
				int stateValue = region.evaluateParikhVector(stateParikhVector);
				int otherStateValue = region.evaluateParikhVector(otherStateParikhVector);
				inequality.add(stateValue - otherStateValue + region.getWeight(eventIndex));
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
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
