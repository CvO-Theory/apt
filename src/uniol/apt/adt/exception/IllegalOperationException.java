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
 * The IllegalOperationException is thrown if an operation is not valid.
 * <p/>
 * @author Manuel Gieseking
 */
public class IllegalOperationException extends RuntimeException {

	public static final long serialVersionUID = 0xdeadbeef00000008l;

	/**
	 * Constructor creates a new IllegalOperationException.
	 * <p/>
	 * @param msg The description why the operation is illegal
	 */
	public IllegalOperationException(String msg) {
		super("Operation not defined: " + msg);
	}

	/**
	 * Constructor creates a new IllegalOperationException with a given cause.
	 * <p/>
	 * @param msg   The description why the operation is illegal.
	 * @param cause The cause for this exception as Throwable.
	 */
	public IllegalOperationException(String msg, Throwable cause) {
		super("Operation not defined: " + msg, cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
