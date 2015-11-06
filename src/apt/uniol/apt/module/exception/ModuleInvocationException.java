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
 * Thrown when the invocation of the
 * {@link uniol.apt.module.Module#run(uniol.apt.module.ModuleInput, uniol.apt.module.ModuleOutput)}
 * method fails.
 *
 * @author Renke Grunwald, Uli Schlachter
 */
public class ModuleInvocationException extends ModuleException {
	public static final long serialVersionUID = 0x1l;

	/**
	 * Constructs a new module invocation exception with the given error message.
	 * @param message The user-readable error description.
	 */
	public ModuleInvocationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new module invocation exception with the given error message and cause.
	 * @param message The user-readable error description.
	 * @param cause The cause for this exception. Please note that this is usually ignored.
	 */
	public ModuleInvocationException(String message, Throwable cause) {
		super(message, cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
