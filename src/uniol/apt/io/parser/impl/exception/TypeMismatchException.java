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

package uniol.apt.io.parser.impl.exception;

/**
 * An exception thrown if the type of the parsed graph does not match to the type of the graph in which it should be
 * converted.
 * <p/>
 * @author Manuel Gieseking
 */
public class TypeMismatchException extends FormatException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new TypeMismatchException with a given message.
	 * <p/>
	 * @param message - the cause of this exception.
	 */
	public TypeMismatchException(String message) {
		super("Type mismatch." + message);
	}

	/**
	 * Creates a new TypeMismatchException with a type which should be declared and the type which had been
	 * declared.
	 * <p/>
	 * @param needed - the string representation of the type which should be defined.
	 * @param found  - the string representation of the type which has been defined.
	 */
	public TypeMismatchException(String needed, String found) {
		super("Type mismatch. Needed: " + needed + " vs. found: " + found);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
