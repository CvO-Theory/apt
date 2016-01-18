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

package uniol.apt.analysis.bcf;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.analysis.exception.UnboundedException;

import static uniol.apt.TestNetCollection.*;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author Uli Schlachter, vsp */
public class BCFTest {
	private BCF bcf;
	private CycleGenerator cycleGenerator;
	private BistatePhilNetGenerator biStatePhilNetGenerator;
	private TristatePhilNetGenerator triStatePhilNetGenerator;
	private QuadstatePhilNetGenerator quadStatePhilNetGenerator;

	@BeforeClass
	public void setup() {
		bcf = new BCF();
		cycleGenerator = new CycleGenerator();
		biStatePhilNetGenerator = new BistatePhilNetGenerator();
		triStatePhilNetGenerator = new TristatePhilNetGenerator();
		quadStatePhilNetGenerator = new QuadstatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		bcf = null;
		cycleGenerator = null;
		biStatePhilNetGenerator = null;
		triStatePhilNetGenerator = null;
		quadStatePhilNetGenerator = null;
	}

	private BCF.Result testNet(PetriNet pn) throws UnboundedException {
		return bcf.check(pn);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 6)
	public void testCycle(int size) throws UnboundedException {
		PetriNet pn = cycleGenerator.generateNet(size);

		assertNull(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 6)
	public void testBistatePhilNet(int size) throws UnboundedException {
		PetriNet pn = biStatePhilNetGenerator.generateNet(size);

		assertNotNull(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 6)
	public void testTristatePhilNet(int size) throws UnboundedException {
		PetriNet pn = triStatePhilNetGenerator.generateNet(size);

		assertNotNull(testNet(pn));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 6)
	public void testQuadstatePhilNet(int size) throws UnboundedException {
		PetriNet pn = quadStatePhilNetGenerator.generateNet(size);

		assertNotNull(testNet(pn));
	}

	@Test
	public void testEmptyNet() throws UnboundedException {
		assertNull(testNet(getEmptyNet()));
	}

	@Test
	public void testNoTransitionOnePlaceNet() throws UnboundedException {
		assertNull(testNet(getNoTransitionOnePlaceNet()));
	}

	@Test
	public void testOneTransitionNoPlaceNet() throws UnboundedException {
		assertNull(testNet(getOneTransitionNoPlaceNet()));
	}

	@Test(expectedExceptions = UnboundedException.class)
	public void testTokenGeneratorNet() throws UnboundedException {
		assertNull(testNet(getTokenGeneratorNet()));
	}

	@Test
	public void testDeadlockNet() throws UnboundedException {
		assertNotNull(testNet(getDeadlockNet()));
	}

	@Test
	public void testNonPersistentNet() throws UnboundedException {
		assertNotNull(testNet(getNonPersistentNet()));
	}

	@Test
	public void checkPersistentBiCFNet() throws UnboundedException {
		assertNotNull(testNet(getPersistentBiCFNet()));
	}

	@Test
	public void testConcurrentDiamondNet() throws UnboundedException {
		assertNull(testNet(getConcurrentDiamondNet()));
	}

	@Test
	public void testConflictingDiamondNet() throws UnboundedException {
		assertNotNull(testNet(getConflictingDiamondNet()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
