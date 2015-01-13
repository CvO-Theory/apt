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

package uniol.apt.analysis.synthesize.separation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** @author Uli Schlachter */
@Test
public class InequalitySystemSeparationTest {
	static public class InequalitySystemSeparationFactory implements SeparationTestHelper.SeparationFactory {
		private final PNProperties properties;

		public InequalitySystemSeparationFactory(PNProperties properties) {
			this.properties = properties;
		}

		@Override
		public Separation createSeparation(RegionUtility utility, List<Region> basis, String[] locationMap) {
			return new InequalitySystemSeparation(utility, basis, properties, locationMap);
		}
	}

	@Factory
	public Object[] factory() {
		List<Object> tests = new ArrayList<>();
		tests.addAll(Arrays.asList(SeparationTestHelper.factory(new InequalitySystemSeparationFactory(
							new PNProperties()))));
		tests.addAll(Arrays.asList(SeparationTestHelper.factory(new InequalitySystemSeparationFactory(
							new PNProperties(PNProperties.PURE)))));
		tests.addAll(Arrays.asList(SeparationTestHelper.factory(new InequalitySystemSeparationFactory(
							new PNProperties(PNProperties.kBounded(19))))));
		return tests.toArray(new Object[tests.size()]);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
