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

package uniol.apt.io.renderer.impl;

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

/**
 * A stringtemplate STErrorListener which throws an error on every kind of problem. This override ST's default of just
 * silently skipping these kinds of problems.
 * @author Uli Schlachter
 */
public class ThrowingErrorListener implements STErrorListener {
	@Override
	public void compileTimeError(STMessage msg) {
		throwFor(msg);
	}

	@Override
	public void runTimeError(STMessage msg) {
		throwFor(msg);
	}

	@Override
	public void IOError(STMessage msg) {
		throwFor(msg);
	}

	@Override
	public void internalError(STMessage msg) {
		throwFor(msg);
	}

	private void throwFor(STMessage msg) {
		// Best API in the world, have to format things manually, because only toString() does this for us, but
		// toString() will also include the stack trace and the result would be ugly.
		String text = String.format(msg.error.message, msg.arg, msg.arg2, msg.arg3);

		// We throw Errors, because ST will catch Exceptions and call us again
		if (msg.cause != null)
			throw new Error(text, msg.cause);
		else
			throw new Error(text);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
