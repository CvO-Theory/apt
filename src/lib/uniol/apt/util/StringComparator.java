/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comparator on strings that is useful for sorting numbered names.
 *
 * If the strings to be compared are not equal
 * and only differ in a numeric suffix, then
 * return the result of comparing the suffixes
 * using Integer.compare. Otherwise, return the
 * result of String.compareTo.
 *
 * @author Thomas Strathmann
 */
public class StringComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 0x1l;

	private static final Pattern PATTERN = Pattern.compile("(.*?)([0-9]+)");

	@Override
	public int compare(String s1, String s2) {
		if (s1.equals(s2))
			return 0;

		Matcher m1 = PATTERN.matcher(s1);
		Matcher m2 = PATTERN.matcher(s2);
		if (m1.matches() && m2.matches()) {
			String prefix1 = m1.group(1);
			String prefix2 = m2.group(1);
			int n1 = Integer.parseInt(m1.group(2));
			int n2 = Integer.parseInt(m2.group(2));

			if (!prefix1.equals(prefix2))
				return s1.compareTo(s2);
			else
				return Integer.compare(n1, n2);
		}

		return s1.compareTo(s2);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
