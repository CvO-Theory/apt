/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       Uli Schlachter
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

package uniol.apt.io.parser.impl;

import uniol.apt.io.parser.ParseException;

/**
 * A runtime exception used internally in parsers to abort the parse. This is used in places where a
 * {@link ParseException} cannot be used directly, because it is a checked exception. This exception then has to be
 * caught at a suitable place and the result of {@link #getParseException()} should be thrown.
 * @author Uli Schlachter
 */
class ParseRuntimeException extends RuntimeException {
	public static final long serialVersionUID = 0;

	public ParseRuntimeException(ParseException cause) {
		super(cause);
		if (cause == null)
			throw new NullPointerException("cause cannot be zero");
	}

	public ParseRuntimeException(String message, ParseException cause) {
		super(message, cause);
		if (cause == null)
			throw new NullPointerException("cause cannot be zero");
	}

	public ParseRuntimeException(Throwable cause) {
		this(new ParseException(cause));
	}

	public ParseRuntimeException(String message) {
		this(message, new ParseException(message));
	}

	public ParseRuntimeException(String message, Throwable cause) {
		this(message, new ParseException(message, cause));
	}

	/**
	 * Get the {@link ParseException} that is wrapped in this exception. That exception should be thrown instead.
	 * @return The exception
	 */
	public ParseException getParseException() {
		return (ParseException) getCause();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
