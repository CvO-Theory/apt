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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Helper functions for having debug output. This class provides a debug() method which can be called with any number of
 * arguments. These arguments are converted to strings, concatenated and printed.  For example, you can statically
 * import the debug method and do this:
 * <pre>
 * {@code
 * debug("Done with step ", step, " after ", time, " seconds");
 * }
 * </pre>
 * The reason for doing things like this is to avoid the overhead of string concatenation if it is not necessary.
 * @author Uli Schlachter
 */
final public class DebugUtil {
	static final public boolean OUTPUT_ENABLED = false || Boolean.getBoolean("apt.debug");
	static final private ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss.SSS");
		}
	};

	/**
	 * Private constructor, don't create instances of this.
	 */
	private DebugUtil() {
	}

	static private void printDebug(String prefix, String message) {
		prefix = DATE_FORMAT.get().format(new Date()) + " " + prefix + ": ";
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

	/**
	 * Generate a formatted debug message if debugging output is enabled. The arguments are passed to {@link
	 * String#format} and the result is printed with a newline appended.
	 * @param format The format string
	 * @param args Formatting arguments
	 * @see String#format
	 */
	static public void debugFormat(String format, Object... args) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), String.format(format, args));
	}

	/**
	 * Generate a debug message if debugging output is enabled. This will print the message with a newline.
	 * @param message The message
	 */
	static public void debug(String message) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), message);
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug("");
	 * }
	 * </pre>
	 */
	static public void debug() {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), "");
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug(java.util.Objects.toString(obj));
	 * }
	 * </pre>
	 * @param obj The object to print
	 */
	static public void debug(Object obj) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), Objects.toString(obj));
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug(java.util.Objects.toString(obj1) + java.util.Objects.toString(obj2));
	 * }
	 * </pre>
	 * @param obj1 The first object to print
	 * @param obj2 The second object to print
	 */
	static public void debug(Object obj1, Object obj2) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), Objects.toString(obj1) + Objects.toString(obj2));
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug(Objects.toString(obj1) + Objects.toString(obj2) + Objects.toString(obj3));
	 * }
	 * </pre>
	 * @param obj1 The first object to print
	 * @param obj2 The second object to print
	 * @param obj3 The third object to print
	 */
	static public void debug(Object obj1, Object obj2, Object obj3) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), Objects.toString(obj1) + Objects.toString(obj2)
					+ Objects.toString(obj3));
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug(Objects.toString(obj1) + Objects.toString(obj2) + Objects.toString(obj3) + Objects.toString(obj4));
	 * }
	 * </pre>
	 * @param obj1 The first object to print
	 * @param obj2 The second object to print
	 * @param obj3 The third object to print
	 * @param obj4 The fourth object to print
	 */
	static public void debug(Object obj1, Object obj2, Object obj3, Object obj4) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), Objects.toString(obj1) + Objects.toString(obj2) + Objects.toString(obj3)
					+ Objects.toString(obj4));
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * debug(Objects.toString(obj1) + Objects.toString(obj2) + Objects.toString(obj3)
	 *     + Objects.toString(obj4) + Objects.toString(obj5));
	 * }
	 * </pre>
	 * @param obj1 The first object to print
	 * @param obj2 The second object to print
	 * @param obj3 The third object to print
	 * @param obj4 The fourth object to print
	 * @param obj5 The fifth object to print
	 */
	static public void debug(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5) {
		if (OUTPUT_ENABLED)
			printDebug(getCaller(), Objects.toString(obj1) + Objects.toString(obj2) + Objects.toString(obj3)
					+ Objects.toString(obj4) + Objects.toString(obj5));
	}

	/**
	 * Generate a debug message. This is equivalent to:
	 * <pre>
	 * {@code
	 * StringBuilder sb = new StringBuilder();
	 * for (Object o : objs)
	 *         sb.append(java.util.Objects.toString(o));
	 * debug(sb.toString();
	 * }
	 * </pre>
	 * @param objs The objects to print
	 */
	static public void debug(Object... objs) {
		if (OUTPUT_ENABLED) {
			StringBuilder sb = new StringBuilder();
			for (Object o : objs)
				sb.append(Objects.toString(o));
			printDebug(getCaller(), sb.toString());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
