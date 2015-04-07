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
class PlainPureSeparation extends BasicPureSeparation implements Separation {
	/**
	 * Construct a new instance for solving separation problems. This constructor does not do any checks for
	 * supported properties. It is the caller's responsibility to check this.
	 * @param utility The region utility to use.
	 * @param locationMap Mapping that describes the location of each event.
	 */
	protected PlainPureSeparation(RegionUtility utility, String[] locationMap) {
		super(utility, locationMap);
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public PlainPureSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this(utility, locationMap);
		PNProperties supported = new PNProperties();
		supported.setPure(true);
		supported.setPlain(true);
		if (!properties.equals(supported))
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
		final List<Region> basis = utility.getRegionBasis();
		InequalitySystem system = prepareInequalitySystem();

		List<Integer> inequality = new ArrayList<>(basis.size());
		for (Region region : basis) {
			int stateValue, otherStateValue;
			try {
				stateValue = region.getMarkingForState(state);
				otherStateValue = region.getMarkingForState(otherState);
			} catch (UnreachableException e) {
				// Unreachable states cannot be separated
				return null;
			}
			inequality.add(stateValue - otherStateValue);
		}
		// We want r_E(Psi_s - Psi_{s'}) != 0. Since we are in a finite LTS, we can just use ">".
		system.addInequality(0, ">", inequality, "Region should separate state " + state
				+ " from state " + otherState);

		// Calculate the resulting linear combination
		debug("Solving an inequality system to separate ", state, " from ", otherState, ":");
		return findRegionFromSystem(system, basis);
	}

	/**
	 * Prepare an inequality system for use by this class. The resulting system will only calculate plain regions.
	 * @return A new inequality system.
	 */
	@Override
	protected InequalitySystem prepareInequalitySystem() {
		InequalitySystem system = new InequalitySystem();

		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			List<Integer> inequality = new ArrayList<>();
			for (Region region : utility.getRegionBasis())
				inequality.add(region.getWeight(event));

			system.addInequality( 1, ">=", inequality, "Plain for event " + event);
			system.addInequality(-1, "<=", inequality, "Plain for event " + event);
		}

		return system;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
