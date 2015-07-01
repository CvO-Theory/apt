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

package uniol.apt.check;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.snet.SNet;
import uniol.apt.analysis.tnet.TNet;
import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;
import static org.testng.Assert.assertTrue;

/**
 * Tests for check module
 *
 * Some tests for check module to ensure that the module is basically working.
 * There is also a shell test script which runs longer test runs and save intermediate data.
 *
 * @author Daniel
 */
public class CheckTest {

	@BeforeClass
	public void setup() {
	}

	@AfterClass
	public void teardown() {
	}

	// random generator, so we test a lot of times avoiding that we get a
	// good working test just by "luck"
	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 50)
	private void testCheckSnet(int runNumber) throws
		UnsupportedAttributeException, AttributeFormatException, UnsupportedGeneratorException {
		Check c = new Check();

		// using chance generator for tests
		c.setGenerator("chance");

		// net should be an snet
		c.addAttribute("snet");

		// search for max. 5 seconds
		PetriNet pn = c.search(5);

		if (pn == null) {
			// no net was found -- "true" because "no error" was found
			assertTrue(true);
		} else {
			// check if net is an snet
			SNet sNet = new SNet(pn);
			try {
				assertTrue(sNet.testPlainSNet());
			} catch (PreconditionFailedException e) {
				assertTrue(false);
			}
		}
	}

	// random generator, so we test a lot of times avoiding that we get a
	// good working test just by "luck"
	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	private void testCheckSnetSmartchance(int runNumber) throws
		UnsupportedAttributeException, AttributeFormatException, UnsupportedGeneratorException {
		Check c = new Check();

		// using smartchance generator for test
		c.setGenerator("smartchance");

		// net should be an snet
		c.addAttribute("snet");

		// search for max. 5 seconds
		PetriNet pn = c.search(5);

		if (pn == null) {
			// no net was found -- "true" because "no error" was found
			assertTrue(true);
		} else {
			// check if net is an snet
			SNet sNet = new SNet(pn);
			try {
				assertTrue(sNet.testPlainSNet());
			} catch (PreconditionFailedException e) {
				assertTrue(false);
			}
		}
	}

	@Test
	public void testCheckSnetTnetgen() throws
		UnsupportedAttributeException, AttributeFormatException, UnsupportedGeneratorException {
		Check c = new Check();

		// using tnetgen2 generator for test
		c.setGenerator("tnetgen2");

		// net should be an snet
		c.addAttribute("snet");

		// search for max. 5 seconds
		PetriNet pn = c.search(5);

		if (pn == null) {
			// no net was found -- "true" because "no error" was found
			assertTrue(true);
		} else {
			// check if net is an snet
			SNet sNet = new SNet(pn);
			try {
				assertTrue(sNet.testPlainSNet());
			} catch (PreconditionFailedException e) {
				assertTrue(false);
			}
		}
	}

	// random generator, so we test a lot of times avoiding that we get a
	// good working test just by "luck"
	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 50)
	private void testCheckNoTnet(int runNumber) throws
		UnsupportedAttributeException, AttributeFormatException, UnsupportedGeneratorException {
		Check c = new Check();

		// using chance generator for tests
		c.setGenerator("chance");

		// net should be an no tnet
		c.addAttribute("!tnet");

		// search for max. 5 seconds
		PetriNet pn = c.search(5);

		if (pn == null) {
			// no net was found -- "true" because "no error" was found
			assertFalse(false);
		} else {
			// check if net is not a tnet
			TNet tNet = new TNet(pn);
			try {
				assertFalse(tNet.testPlainTNet());
			} catch (PreconditionFailedException e) {
				assertFalse(false);
			}
		}
	}

	// random generator, so we test a lot of times avoiding that we get a
	// good working test just by "luck"
	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	private void testCheckBounded(int runNumber) throws
		UnsupportedAttributeException, AttributeFormatException, UnsupportedGeneratorException {
		Check c = new Check();

		// using chance generator for tests
		c.setGenerator("chance");

		// net should be an snet
		c.addAttribute("bounded");

		// search for max. 5 seconds
		PetriNet pn = c.search(5);

		if (pn == null) {
			// no net was found -- "true" because "no error" was found
			assertTrue(true);
		} else {
			// check if net is bounded
			assertTrue(Bounded.isBounded(pn));
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
