/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.analysis.mf;

import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleInvoker;

import uniol.apt.CrashCourseNets;
import static uniol.apt.TestNetCollection.*;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/**
 * @author Manuel Gieseking, Uli Schlachter
 */
public class MergeFreeTest {

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

	private boolean testNet(PetriNet pn) throws ModuleException {
		MergeFreeModule mod = new MergeFreeModule();
		ModuleInvoker m = new ModuleInvoker();
		List<Object> objs = m.invoke(mod, pn);
		Boolean ret = (Boolean) objs.get(0);
		return ret;
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) throws ModuleException {
		PetriNet pn = cycleGenerator.generateNet(size);
		assertTrue(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testBistatePhilNet(int size) throws ModuleException {
		PetriNet pn = biStatePhilNetGenerator.generateNet(size);

		assertFalse(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testTristatePhilNet(int size) throws ModuleException {
		PetriNet pn = triStatePhilNetGenerator.generateNet(size);

		assertFalse(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 30)
	public void testQuadstatePhilNet(int size) throws ModuleException {
		PetriNet pn = quadStatePhilNetGenerator.generateNet(size);

		assertFalse(testNet(pn));
	}

	@DataProvider(name = "goodNets")
	private Object[][] createGoodTestNets() {
		return new Object[][]{
				{CrashCourseNets.getCCNet1()},
				{CrashCourseNets.getCCNet7()},
				{CrashCourseNets.getCCNet13()},
				{getEmptyNet()},
				{getNoTransitionOnePlaceNet()},
				{getOneTransitionNoPlaceNet()},
				{getTokenGeneratorNet()},
				{getConcurrentDiamondNet()},
				{getDeadlockNet()},
				{getDeadTransitionNet()}};
	}

	@DataProvider(name = "badNets")
	private Object[][] createBadTestNets() {
		return new Object[][]{
				{CrashCourseNets.getCCNet2()},
				{CrashCourseNets.getCCNet2inf()},
				{CrashCourseNets.getCCNet3()},
				{CrashCourseNets.getCCNet4()},
				{CrashCourseNets.getCCNet5()},
				{CrashCourseNets.getCCNet6()},
				{CrashCourseNets.getCCNet8()},
				{CrashCourseNets.getCCNet9()},
				{CrashCourseNets.getCCNet10()},
				{CrashCourseNets.getCCNet11()},
				{CrashCourseNets.getCCNet12()},
				{CrashCourseNets.getCCNet14()},
				{getNonPersistentNet()},
				{getPersistentBiCFNet()},
				{getConflictingDiamondNet()},
				{getABCLanguageNet()}};
	}

	@Test(dataProvider = "goodNets")
	public void testGoodNet(PetriNet pn) throws ModuleException {
		assertTrue(testNet(pn), "Examining PN " + pn.getName() + " which should be good");
	}

	@Test(dataProvider = "badNets")
	public void testBadNet(PetriNet pn) throws ModuleException {
		assertFalse(testNet(pn), "Examining PN " + pn.getName() + " which should fail");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
