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

package uniol.tests;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions needed by more than one test.
 * @author Uli Schlachter
 */
public class TestUtils {
	/* Don't create instances of this class */
	private TestUtils() {
	}

	/* Helper function for causing OutOfMemoryErrors to be thrown by the runtime. */
	static public void causeOutOfMemory() throws OutOfMemoryError {
		List<Object> list = new ArrayList<>();
		long maxSize = Runtime.getRuntime().freeMemory();
		int size = maxSize <= Integer.MAX_VALUE ? (int) maxSize : Integer.MAX_VALUE;

		// Too large allocations apparently cause a throw without clearing SoftReferences. This contradicts the
		// docs, but ok, let's work around it.
		size /= 2;

		while (true) {
			list.add(new byte[size]);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
