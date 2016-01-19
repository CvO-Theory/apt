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

package uniol.apt.io.parser.impl;

import java.util.HashMap;

/**
 * A mapping from ids of places to token counts with the functionality of adding the token count if a key already
 * exists.
 * @author Manuel Gieseking
 */
public class MarkingHashMap extends HashMap<String, Integer> {

	private static final long serialVersionUID = 1L;

	/**
	 * Associates the given token count as value to the as key given id of the place. If the key already exists it
	 * adds the token count to the previously existing token count.
	 * @param key   - the id of the place.
	 * @param value - the token count.
	 * @return the previously token count if it has been existed.
	 */
	@Override
	public Integer put(String key, Integer value) {
		Integer pre = super.get(key);
		if (pre != null) {
			value += pre;
		}
		return super.put(key, value);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
