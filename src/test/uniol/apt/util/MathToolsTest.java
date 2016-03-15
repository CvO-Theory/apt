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
	private final int bigPrime1 = 496167127;
	private final int bigPrime2 = 284678627;

	@Test
	public void testGGT() {
		assertEquals(MathTools.gcd(17, 5), 1);
		assertEquals(MathTools.gcd(-17, 5), 1);
		assertEquals(MathTools.gcd(-17, -5), 1);
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
		BigInteger a = BigInteger.valueOf(bigPrime1);
		BigInteger b = BigInteger.valueOf(bigPrime2);
		BigInteger product = a.multiply(b);
		assertEquals(MathTools.lcm(a, b), product);
	}

	@Test(expectedExceptions = ArithmeticException.class, expectedExceptionsMessageRegExp = "Cannot represent value as int: 141248176476894629")
	public void testLCMOverflow() {
		MathTools.lcm(bigPrime1, bigPrime2);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
