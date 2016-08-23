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

package uniol.apt.analysis.homogeneous;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static uniol.apt.BestNetCollection.getNetDistrFig12;
import static uniol.apt.BestNetCollection.getNetSepBasic1;
import static uniol.apt.CrashCourseNets.getCCNet3;
import static uniol.apt.CrashCourseNets.getCCNet4;
import static uniol.apt.TestNetCollection.getABCLanguageNet;
import static uniol.apt.TestNetCollection.getACBCCLoopNet;
import static uniol.apt.TestNetCollection.getConcurrentDiamondNet;
import static uniol.apt.TestNetCollection.getConflictingDiamondNet;
import static uniol.apt.TestNetCollection.getDeadTransitionNet;
import static uniol.apt.TestNetCollection.getDeadlockNet;
import static uniol.apt.TestNetCollection.getEmptyNet;
import static uniol.apt.TestNetCollection.getNoTransitionOnePlaceNet;
import static uniol.apt.TestNetCollection.getNonPersistentNet;
import static uniol.apt.TestNetCollection.getOneTransitionNoPlaceNet;
import static uniol.apt.TestNetCollection.getPersistentBiCFNet;
import static uniol.apt.TestNetCollection.getTokenGeneratorNet;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.module.impl.ModuleInvoker;
import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author vsp */
public class HomogeneousTest {

	private CycleGenerator cycleGenerator;
	private BistatePhilNetGenerator biStatePhilNetGenerator;
	private TristatePhilNetGenerator triStatePhilNetGenerator;
	private QuadstatePhilNetGenerator quadStatePhilNetGenerator;

	@BeforeClass
	public void setup() {
		cycleGenerator = new CycleGenerator();
		biStatePhilNetGenerator = new BistatePhilNetGenerator();
		triStatePhilNetGenerator = new TristatePhilNetGenerator();
		quadStatePhilNetGenerator = new QuadstatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		cycleGenerator = null;
		biStatePhilNetGenerator = null;
		triStatePhilNetGenerator = null;
		quadStatePhilNetGenerator = null;
	}

	private boolean testNet(PetriNet pn) {
		return new Homogeneous().check(pn) == null;
	}

	private boolean testNetModule(PetriNet pn) throws Exception {
		HomogeneousModule module = new HomogeneousModule();
		ModuleInvoker invoker = new ModuleInvoker();
		return (Boolean) (invoker.invoke(module, pn).get(0));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) throws Exception {
		PetriNet pn = cycleGenerator.generateNet(size);

		assertTrue(testNet(pn));
		assertTrue(testNetModule(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testBistatePhilNet(int size) throws Exception {
		PetriNet pn = biStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
		assertTrue(testNetModule(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testTristatePhilNet(int size) throws Exception {
		PetriNet pn = triStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
		assertTrue(testNetModule(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testQuadstatePhilNet(int size) throws Exception {
		PetriNet pn = quadStatePhilNetGenerator.generateNet(size);

		assertTrue(testNet(pn));
		assertTrue(testNetModule(pn));
	}

	@DataProvider(name = "goodNets")
	private Object[][] createGoodTestNets() {
		return new Object[][]{
				{getEmptyNet()},
				{getNoTransitionOnePlaceNet()},
				{getOneTransitionNoPlaceNet()},
				{getTokenGeneratorNet()},
				{getDeadlockNet()},
				{getNonPersistentNet()},
				{getPersistentBiCFNet()},
				{getConflictingDiamondNet()},
				{getConcurrentDiamondNet()},
				{getDeadTransitionNet()},
				{getABCLanguageNet()},
				{getACBCCLoopNet()}};
	}

	@DataProvider(name = "badNets")
	private Object[][] createBadTestNets() {
		return new Object[][]{
			{getCCNet3()},
			{getCCNet4()},
			{getNetDistrFig12()},
			{getNetSepBasic1()}};
	}

	@Test(dataProvider = "goodNets")
	public void testGoodNet(PetriNet pn) throws Exception {
		assertTrue(testNet(pn), "Examining PN " + pn.getName() + " which should be good");
		assertTrue(testNetModule(pn), "Examining PN " + pn.getName() + " which should be good");
	}

	@Test(dataProvider = "badNets")
	public void testBadNet(PetriNet pn) throws Exception {
		assertFalse(testNet(pn), "Examining PN " + pn.getName() + " which should fail");
		assertFalse(testNetModule(pn), "Examining PN " + pn.getName() + " which should fail");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
