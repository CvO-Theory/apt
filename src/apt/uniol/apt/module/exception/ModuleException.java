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

package uniol.apt.module.exception;


/**
 * A generic exception that occurs with modules. This exception is used for
 * transporting a user-readable error message.
 * @author Uli Schlachter
 */
public class ModuleException extends Exception {
	public static final long serialVersionUID = 0x1l;

	/**
	 * Constructs a new module exception with the given error message.
	 * @param message The user-readable error description.
	 */
	public ModuleException(String message) {
		super(message);
	}

	/**
	 * Constructs a new module exception with the given error message and cause.
	 * @param message The user-readable error description.
	 * @param cause The cause for this exception. Please note that this is usually ignored.
	 */
	public ModuleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new module exception with the given cause.
	 * @param cause The cause for this exception. Please note that this is usually ignored.
	 */
	public ModuleException(Throwable cause) {
		super(cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
