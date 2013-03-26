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
 * Some methods for mathematical calculations like ggt and modulo.
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
	 * Calculates the ggT of a given set of BigIntegers.
	 * <p/>
	 * @param set - the BigIntegers to calculate the ggT from.
	 * <p/>
	 * @return the ggT of the BigIntegers of the given set.
	 */
	public static BigInteger ggTBigInteger(Collection<BigInteger> set) {
		Iterator<BigInteger> iter = set.iterator();
		BigInteger ggt = iter.next();
		while (iter.hasNext()) {
			BigInteger b = iter.next();
			ggt = ggt.compareTo(b) < 0 ? b.gcd(ggt) : ggt.gcd(b);
		}
		return ggt;
	}

	/**
	 * Calculates the ggT of a given set of integers.
	 * <p/>
	 * @param set - the integers to calculate the ggT from.
	 * <p/>
	 * @return the ggT of the integers of the given set.
	 */
	public static int ggT(Collection<Integer> set) {
		Iterator<Integer> iter = set.iterator();
		int ggt = iter.next();
		while (iter.hasNext()) {
			ggt = ggT(ggt, iter.next());
		}
		return ggt;
	}

	/**
	 * Calculates the ggT of two integers.
	 * <p/>
	 * @param a - first integer for calculating the ggT.
	 * @param b - second integer for calculating the ggT.
	 * <p/>
	 * @return the ggT of the two given integers.
	 */
	public static int ggT(int a, int b) {
		if (a < b) {
			return BigInteger.valueOf(b).gcd(BigInteger.valueOf(a)).intValue();
		} else {
			return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
		}
	}

	/**
	 * Calculates the mathematical modulo, so that it's given the positiv value.
	 * <p/>
	 * @param a - divisor.
	 * @param b - divident.
	 * <p/>
	 * @return a modulo b.
	 */
	public static int mod(int a, int b) {
		return BigInteger.valueOf(a).mod(BigInteger.valueOf(b)).intValue();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
