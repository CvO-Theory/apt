/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import uniol.apt.module.exception.ModuleException;

/**
 * A MissingLocationException is thrown when some events in a LTS have a location and others do not.
 * @author Uli Schlachter
 */
public class MissingLocationException extends ModuleException {
	public static final long serialVersionUID = 0L;

	/**
	 * Constructor creates a new MissingLocationException with message @message.
	 * @param message A string containing a describing message.
	 */
	public MissingLocationException(String message) {
		super(message);
	}

	/**
	 * Constructor creates a new MissingLocationException with a message @message and a cause @cause.
	 * @param message A string containing a describing message.
	 * @param cause The cause for this exception as Throwable.
	 */
	public MissingLocationException(String message, Throwable cause) {
		super(message, cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
