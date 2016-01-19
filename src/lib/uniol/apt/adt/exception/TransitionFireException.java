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

package uniol.apt.adt.exception;

/**
 * A TransitionFireException is thrown if a transition is fired though the current marking does not allow it.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class TransitionFireException extends DatastructureException {

	public static final long serialVersionUID = 0xdeadbeef00000002l;

	/**
	 * Constructor creates a new TransitionFireException with given message.
	 * @param message A string containing a describing message.
	 */
	public TransitionFireException(String message) {
		super(message);
	}

	/**
	 * Constructor creates a new TransitionFireException with a given message and cause.
	 * @param message A string containing a describing message.
	 * @param cause   The cause for this exception as Throwable.
	 */
	public TransitionFireException(String message, Throwable cause) {
		super(message, cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
