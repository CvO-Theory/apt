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

package uniol.apt.util;

import java.math.BigInteger;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 *
 * @author Manuel Gieseking
 */
public class MathToolsTest {
	private static final int BIG_PRIME1 = 496167127;
	private static final int BIG_PRIME2 = 284678627;

	@Test
	public void testGCD() {
		assertEquals(MathTools.gcd(0, 5), 5);
		assertEquals(MathTools.gcd(5, 0), 5);
		assertEquals(MathTools.gcd(15, 5), 5);
		assertEquals(MathTools.gcd(17, 5), 1);
		assertEquals(MathTools.gcd(-17, 5), 1);
		assertEquals(MathTools.gcd(-17, -5), 1);
	}

	@Test
	public void testGCDArray() {
		assertEquals(MathTools.gcd(), 0);
		assertEquals(MathTools.gcd(0), 0);
		assertEquals(MathTools.gcd(-42), 42);
		assertEquals(MathTools.gcd(2*3*5*7*11, 2, 3, 5, 7, 11), 1);
		assertEquals(MathTools.gcd(10, 20, 25), 5);

		// Make sure we really call the array version
		int[] array = new int[2];
		array[0] = array[1] = -42;
		assertEquals(MathTools.gcd(array), 42);

		array[0] = 0;
		array[1] = -42;
		assertEquals(MathTools.gcd(array), 42);

		array[0] = -42;
		array[1] = 0;
		assertEquals(MathTools.gcd(array), 42);

		array[0] = 15;
		array[1] = 5;
		assertEquals(MathTools.gcd(array), 5);

		array[0] = 17;
		array[1] = 5;
		assertEquals(MathTools.gcd(array), 1);

		array[0] = -17;
		array[1] = 5;
		assertEquals(MathTools.gcd(array), 1);

		array[0] = 17;
		array[1] = -5;
		assertEquals(MathTools.gcd(array), 1);

		array[0] = -17;
		array[1] = -5;
		assertEquals(MathTools.gcd(array), 1);
	}

	@Test
	public void testMod() {
		assertEquals(-11 % 5, -1);
		assertEquals(MathTools.mod(-11, 5), 4);
	}

	@Test
	public void testLCM() {
		assertEquals(MathTools.lcm(2, 3), 6);
		assertEquals(MathTools.lcm(2, -4), 4);
		assertEquals(MathTools.lcm(-7, 7), 7);
		assertEquals(MathTools.lcm(-22, -33), 66);
	}

	@Test
	public void testLCMBig() {
		BigInteger a = BigInteger.valueOf(BIG_PRIME1);
		BigInteger b = BigInteger.valueOf(BIG_PRIME2);
		BigInteger product = a.multiply(b);
		assertEquals(MathTools.lcm(a, b), product);
	}

	@Test(expectedExceptions = ArithmeticException.class, expectedExceptionsMessageRegExp =
			"Cannot represent value as int: 141248176476894629")
	public void testLCMOverflow() {
		MathTools.lcm(BIG_PRIME1, BIG_PRIME2);
	}

	static private void checkMean(int a, int b, int expected) {
		assertEquals(MathTools.meanTowardsMinusInfinity(a, b), expected);
		assertEquals(MathTools.meanTowardsMinusInfinity(b, a), expected);
	}

	@Test
	public void testMeanTowardsMinusInfinity() {
		// Some tests where both arguments are the same
		checkMean(0, 0, 0);
		checkMean(1, 1, 1);
		checkMean(-1, -1, -1);
		checkMean(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		checkMean(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

		// Some tests close to integer limits
		checkMean(Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
		checkMean(Integer.MAX_VALUE - 1, Integer.MIN_VALUE, -1);

		// Even/even
		checkMean(4, 42, 23); // first argument chosen by fair dice roll
		checkMean(-256, 42, -107);
		checkMean(-27970, -30340, -29155);
		checkMean(-42, 42, 0);
		checkMean(-42, 0, -21);
		checkMean(0, 42, 21);

		// Even/odd
		checkMean(0, 1, 0);
		checkMean(0, -1, -1);
		checkMean(6045, 19786, 12915);
		checkMean(572, -22357, -10893);
		checkMean(-8759, -13658, -11209);

		// Odd/odd
		checkMean(1, 3, 2);
		checkMean(-9, 3, -3);
		checkMean(-9, -3, -6);
		checkMean(-343, -26009, -13176);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
