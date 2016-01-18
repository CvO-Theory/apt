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

package uniol.apt.analysis.separation;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import uniol.apt.adt.pn.PetriNet;
import static uniol.apt.TestNetCollection.*;

/**
 * Tests for LargestKTest class
 *
 * @author Daniel
 */
public class LargestKTest {

	@BeforeClass
	public void setup() {
	}

	@AfterClass
	public void teardown() {
	}

	private long testNet(PetriNet pn) {
		LargestK largestK = new LargestK(pn);

		return largestK.computeLargestK();
	}

	@Test
	public void testValues1() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(1, 2, 3, 4, 5)), 1);
	}

	@Test
	public void testValues2() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(2, 4, 6, 8, 10)), 2);
	}

	@Test
	public void testValues3() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(1, 2, 3, 0, 0)), 1);
	}

	@Test
	public void testValues4() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(3, 0, 6, 9, 0)), 3);
	}

	@Test
	public void testValues5() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(12, 0, 0, 8, 4)), 4);
	}

	@Test
	public void testValues6() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(0, 0, 0, 8, 12)), 4);
	}

	@Test
	public void testValues7() {
		assertEquals(testNet(getPersistentBiCFNetWithMarks(0, 1, 0, 8, 12)), 1);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
