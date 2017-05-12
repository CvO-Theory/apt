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

package uniol.apt.analysis.exception;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.ts.ParikhVector;

/**
 * A NonDisjointCyclesException is thrown when an analysis needs its input to have disjoint small cycles, but the given
 * input does not satisfy this requirement.
 * @author Uli Schlachter
 */
public class NonDisjointCyclesException extends PreconditionFailedException {
	public static final long serialVersionUID = 1L;

	private final ParikhVector pv1;
	private final ParikhVector pv2;

	/**
	 * Constructor creates a new NonDisjointCyclesException for the given TransitionSystem.
	 * @param ts A transition system with non-disjoint small cycles.
	 * @param pv1 The Parikh vector of a cycle with non-disjoint support to the second cycle.
	 * @param pv2 The Parikh vector of a cycle with non-disjoint support to the first cycle.
	 */
	public NonDisjointCyclesException(TransitionSystem ts, ParikhVector pv1, ParikhVector pv2) {
		super("Transition system " + ts.getName()
				+ " does not have disjoint small cycles, but this property is required. For example, "
				+ "there is a cycle with Parikh vector " + pv1
				+ " and another cycle with Parikh vector " + pv2 + ".");
		assert !pv1.sameOrMutuallyDisjoint(pv2);
		this.pv1 = pv1;
		this.pv2 = pv2;
	}

	public ParikhVector getPV1() {
		return pv1;
	}

	public ParikhVector getPV2() {
		return pv2;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
