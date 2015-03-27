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

package uniol.apt.analysis.exception;

import uniol.apt.adt.ts.TransitionSystem;

/**
 * A NonDeterministicException is thrown when an analysis needs a deterministic input, but was given a non-deterministic
 * one.
 * @author Uli Schlachter
 */
public class NonDeterministicException extends PreconditionFailedException {
	public static final long serialVersionUID = 1L;

	/**
	 * Constructor creates a new NonDeterministicException saying that the given TransitionSystem is
	 * non-deterministic.
	 * @param lts A non-deterministic transition system.
	 */
	public NonDeterministicException(TransitionSystem ts) {
		super("Transition system " + ts.getName() + " is non-deterministic, only deterministic inputs are"
				+ "(currently) supported.");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
