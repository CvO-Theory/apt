/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper functions for having debug output.
 * @author Uli Schlachter
 */
public class DebugUtil {
	static final public boolean debugOutputEnabled = false;
	static final private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	static private void debug(String prefix, String message) {
		prefix = dateFormat.format(new Date()) + " " + prefix + ": ";
		for (String line : message.split("\n"))
			System.err.println(prefix + line);
	}

	static private String getCaller() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace.length < 3)
			return "unknown";
		String klass = trace[3].getClassName();
		int index = klass.lastIndexOf(".");
		if (index < 0)
			return klass;
		return klass.substring(index + 1);
	}

	static protected void debug(String message) {
		if (debugOutputEnabled)
			debug(getCaller(), message);
	}

	static protected void debug() {
		if (debugOutputEnabled)
			debug(getCaller(), "");
	}

	static protected void debug(Object obj) {
		if (debugOutputEnabled)
			debug(getCaller(), obj.toString());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
