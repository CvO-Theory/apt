/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.sum;

import uniol.apt.adt.pn.Transition;

/**
 * Exception that is supposed to be thrown when the synchronous sum of two PNs
 * should be created but they contain transitions with matching ids but
 * mismatching labels.
 *
 * @author Jonas Prellberg
 *
 */
public class LabelMismatchException extends Exception {

	private static final long serialVersionUID = 6860647605160593802L;

	private final Transition t1;
	private final Transition t2;

	/**
	 * Creates a new LabelMismatchException given the two causing
	 * transitions.
	 *
	 * @param t1
	 *                problematic transition of first summand
	 * @param t2
	 *                problematic transition of second summand
	 */
	public LabelMismatchException(Transition t1, Transition t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public String getMessage() {
		return String.format(
			"Two transitions with matching ids '%s' and '%s' have mismatching labels '%s' and '%s'.",
			t1.getId(), t2.getId(), t1.getLabel(), t2.getLabel());
	}

}

//vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
