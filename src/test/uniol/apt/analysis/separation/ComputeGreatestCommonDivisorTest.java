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

/**
 * Tests for computeGreatestCommonDivisor method of LargestK class
 *
 * @author Daniel
 * @version 1.0
 */
public class ComputeGreatestCommonDivisorTest {

	@BeforeClass
	public void setup() {
	}

	@AfterClass
	public void teardown() {
	}

	private long testComputeGreatestCommonDivisor(int value1, int value2) {
		LargestK largestK = new LargestK(null);

		return largestK.computeGreatestCommonDivisor(value1, value2);
	}

	@Test
	public void testValues1() {
		assertEquals(testComputeGreatestCommonDivisor(1, 2), 1);
	}

	@Test
	public void testValues2() {
		assertEquals(testComputeGreatestCommonDivisor(1, 3), 1);
	}

	@Test
	public void testValues21() {
		assertEquals(testComputeGreatestCommonDivisor(3, 1), 1);
	}

	@Test
	public void testValues3() {
		assertEquals(testComputeGreatestCommonDivisor(2, 2), 2);
	}

	@Test
	public void testValues4() {
		assertEquals(testComputeGreatestCommonDivisor(6, 12), 6);
	}

	@Test
	public void testValues42() {
		assertEquals(testComputeGreatestCommonDivisor(12, 6), 6);
	}

	@Test
	public void testValues5() {
		assertEquals(testComputeGreatestCommonDivisor(6, 9), 3);
	}

	@Test
	public void testValues52() {
		assertEquals(testComputeGreatestCommonDivisor(9, 6), 3);
	}

	@Test
	public void testValues6() {
		assertEquals(testComputeGreatestCommonDivisor(4, 8), 4);
	}

	@Test
	public void testValues7() {
		assertEquals(testComputeGreatestCommonDivisor(1071, 1029), 21);
	}

	@Test
	public void testValues72() {
		assertEquals(testComputeGreatestCommonDivisor(1029, 1071), 21);
	}

	@Test
	public void testValues8() {
		assertEquals(testComputeGreatestCommonDivisor(4, 0), 0);
	}

	@Test
	public void testValues9() {
		assertEquals(testComputeGreatestCommonDivisor(0, 5), 0);
	}

	@Test
	public void testValues10() {
		assertEquals(testComputeGreatestCommonDivisor(0, 0), 0);
	}


}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
