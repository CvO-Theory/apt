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

import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.util.equations.InequalitySystem;

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
		PNProperties supported = new PNProperties().setPure(true).setPlain(true);
		if (!properties.equals(supported))
			throw new UnsupportedPNPropertiesException();
	}

	/**
	 * Prepare an inequality system for use by this class. The resulting system will only calculate plain regions.
	 * @return A new inequality system.
	 */
	@Override
	protected InequalitySystem prepareInequalitySystem() {
		InequalitySystem system = new InequalitySystem();

		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			List<BigInteger> inequality = new ArrayList<>();
			for (Region region : utility.getRegionBasis())
				inequality.add(region.getWeight(event));

			system.addInequality(1,  ">=", inequality, "Plain for event " + event);
			system.addInequality(-1, "<=", inequality, "Plain for event " + event);
		}

		return system;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
