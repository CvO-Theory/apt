/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Uli Schlachter
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

package uniol.apt.ui.impl.parameter;

import java.util.Map;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * A parameter transformation that has a given number of options based on a Map.
 * @param <T> The type of entries
 * @author Uli Schlachter
 */
public abstract class AbstractMapParameterTransformation<T> implements ParameterTransformation<T> {
	private final Trie<String, T> map;

	/**
	 * Constructor.
	 * @param values A map containing the values understood by this transformation.
	 */
	public AbstractMapParameterTransformation(Map<String, T> values) {
		this.map = new PatriciaTrie<>(values);
	}

	/**
	 * Constructor which uses the toString() method of some values to get a string representation.
	 * @param values A collection containing all possible values.
	 */
	@SafeVarargs
	public AbstractMapParameterTransformation(T... values) {
		this.map = new PatriciaTrie<>();
		for (T value : values) {
			T old = map.put(value.toString().toLowerCase(), value);
			if (old != null)
				throw new IllegalArgumentException("Duplicate values for "
						+ value.toString() + " and " + old.toString());
		}
	}

	@Override
	public T transform(String arg) throws ModuleException {
		String lowerCaseArg = arg.toLowerCase();
		T result = map.get(lowerCaseArg);
		if (result != null)
			return result;

		// If this is a unique prefix of an option, return that option
		Map<String, T> prefix = map.prefixMap(lowerCaseArg);
		if (prefix.size() == 1)
			return prefix.entrySet().iterator().next().getValue();

		throw new ModuleException(String.format("Unsupported argument '%s'. Valid options are %s.",
					arg, map.keySet()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
