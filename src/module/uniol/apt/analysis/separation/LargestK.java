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

import java.util.HashSet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class provides functionality to compute the largest k for which M0 is a k-marking
 *
 * Note: Do not use for nets with omega marks.
 *
 * @author Daniel
 * @version 1.0
 */
public class LargestK {

	private final PetriNet petriNet;

	/**
	 * Class constructor.
	 *
	 * @param petriNet Net which will be used
	 */
	public LargestK(PetriNet petriNet) {
		this.petriNet = petriNet;
	}

	/**
	 * Compute the greatest common divisor of two numbers
	 * Using euclidean algorithm
	 *
	 * Note: Order does not matter. If divisor greater than dividend: first step will swap them
	 *
	 * @param dividend value1
	 * @param divisor value2
	 *
	 * @return greatest common divisor
	 *         ( 0 if dividend or divisor are 0 )
	 */
	public long computeGreatestCommonDivisor(long dividend , long divisor) {
		long lastDivisor = divisor;

		if ((divisor == 0) || (dividend == 0))
			return 0;

		// euclidean algorithm
		while (divisor != 0) {
			lastDivisor   = divisor;
			divisor  = dividend % divisor;
			dividend = lastDivisor;
		}

		return lastDivisor;
	}


	/**
	 * Compute the largest k for which M0 is a k-marking
	 *
	 * Note: Using current marking
	 *
	 * @return Value of k
	 */
	public long computeLargestK() {
		//Marking marking = this.petriNet.getMarking();

		// get values of all marks
		HashSet<Long> markValues = new HashSet<>();
		// over all places
		for (Place place : petriNet.getPlaces()) {
			// get value and save it (except value is zero)
			long value = place.getInitialToken().getValue();
			//int value = marking.getToken(place.getId()).getValue();

			if (value != 0) {
				markValues.add(value);
			}
		}

		// greatest k is greatest common divisor of all different mark values
		long k = 0;
		for (long markValue : markValues) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (k == 0) {
				k = markValue;
			} else {
				k = computeGreatestCommonDivisor(k, markValue);
			}
		}

		return k;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
