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
import java.util.Collection;
import java.util.Iterator;

/**
 * Some methods for mathematical calculations like gcd and modulo.
 * <p/>
 * @author Manuel Gieseking
 */
public class MathTools {

	/**
	 * Hidden constructor.
	 */
	private MathTools() {
	}

	/**
	 * Calculates the gcd of a given set of BigIntegers.
	 * <p/>
	 * @param set - the BigIntegers to calculate the gcd from.
	 * <p/>
	 * @return the gcd of the BigIntegers of the given set.
	 */
	public static BigInteger gcdBigInteger(Collection<BigInteger> set) {
		if (set.isEmpty())
			return BigInteger.ZERO;
		Iterator<BigInteger> iter = set.iterator();
		BigInteger gcd = iter.next();
		while (iter.hasNext()) {
			BigInteger b = iter.next();
			gcd = gcd.compareTo(b) < 0 ? b.gcd(gcd) : gcd.gcd(b);
		}
		return gcd;
	}

	/**
	 * Calculates the gcd of a given set of integers.
	 * <p/>
	 * @param set - the integers to calculate the gcd from.
	 * <p/>
	 * @return the gcd of the integers of the given set.
	 */
	public static int gcd(Collection<Integer> set) {
		if (set.isEmpty())
			return 0;
		Iterator<Integer> iter = set.iterator();
		int gcd = iter.next();
		while (iter.hasNext()) {
			gcd = gcd(gcd, iter.next());
			if (gcd == 1) return 1;
		}
		return gcd;
	}

	/**
	 * Calculates the gcd of two integers.
	 * <p/>
	 * @param a - first integer for calculating the gcd.
	 * @param b - second integer for calculating the gcd.
	 * <p/>
	 * @return the gcd of the two given integers.
	 */
	public static int gcd(int a, int b) {
		if (a < b) {
			return BigInteger.valueOf(b).gcd(BigInteger.valueOf(a)).intValue();
		} else {
			return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
		}
	}

	/**
	 * Calculates the mathematical modulo, so that it's given the positive value.
	 * <p/>
	 * @param a - divisor.
	 * @param b - dividend.
	 * <p/>
	 * @return a modulo b.
	 */
	public static int mod(int a, int b) {
		return BigInteger.valueOf(a).mod(BigInteger.valueOf(b)).intValue();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
