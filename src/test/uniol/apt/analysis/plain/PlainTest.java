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

package uniol.apt.analysis.plain;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;

import static uniol.apt.TestNetCollection.*;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/**
 *  @author Bjoern von der Linde
 */
public class PlainTest {

	private Plain plain;
	private CycleGenerator cycleGenerator;
	private BistatePhilNetGenerator biStatePhilNetGenerator;
	private TristatePhilNetGenerator triStatePhilNetGenerator;
	private QuadstatePhilNetGenerator quadStatePhilNetGenerator;

	@BeforeClass
	public void setup() {
		plain = new Plain();
		cycleGenerator = new CycleGenerator();
		biStatePhilNetGenerator = new BistatePhilNetGenerator();
		triStatePhilNetGenerator = new TristatePhilNetGenerator();
		quadStatePhilNetGenerator = new QuadstatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		plain = null;
		cycleGenerator = null;
		biStatePhilNetGenerator = null;
		triStatePhilNetGenerator = null;
		quadStatePhilNetGenerator = null;
	}

	private boolean testNet(PetriNet pn) {
		return plain.checkPlain(pn);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) {
		PetriNet pn = cycleGenerator.generateNet(size);

		assertTrue(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testBistatePhilNet(int size) {
		PetriNet pn = biStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testTristatePhilNet(int size) {
		PetriNet pn = triStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testQuadstatePhilNet(int size) {
		PetriNet pn = quadStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
	}

	@DataProvider(name = "goodNets")
	private Object[][] createGoodTestNets() {
		return new Object[][]{
				{getEmptyNet()},
				{getNoTransitionOnePlaceNet()},
				{getOneTransitionNoPlaceNet()},
				{getTokenGeneratorNet()},
				{getPersistentBiCFNet()},
				{getDeadlockNet()},
				{getNonPersistentNet()},
				{getConcurrentDiamondNet()},
				{getConflictingDiamondNet()},
				{getDeadTransitionNet()},
				{getABCLanguageNet()}};
	}

	@DataProvider(name = "badNets")
	private Object[][] createBadTestNets() {
		return new Object[][]{
			{getACBCCLoopNet()}};
	}

	@Test(dataProvider = "goodNets")
	public void testGoodNet(PetriNet pn) {
		assertTrue(testNet(pn), "Examining PN " + pn.getName() + " which should be good");
	}

	@Test(dataProvider = "badNets")
	public void testBadNet(PetriNet pn) {
		assertFalse(testNet(pn), "Examining PN " + pn.getName() + " which should fail");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
