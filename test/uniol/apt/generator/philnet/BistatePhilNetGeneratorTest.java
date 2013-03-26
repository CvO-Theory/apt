/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.generator.philnet;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import uniol.apt.adt.pn.PetriNet;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author vsp */
@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
@IntRangeParameter(start = 2, end = 30)
public class BistatePhilNetGeneratorTest {
	BistatePhilNetGenerator generator;

	@BeforeClass
	public void setup() {
		generator = new BistatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		generator = null;
	}

	public void testCounts(int size) {
		PetriNet pn = generator.generateNet(size);

		assertEquals(pn.getPlaces().size(), size * 3, "Generated net has wrong number of places");
		assertEquals(pn.getTransitions().size(), size * 2, "Generated net has wrong number of transitions");
		assertEquals(pn.getEdges().size(), size * 8, "Generated net has wrong number of arcs");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
