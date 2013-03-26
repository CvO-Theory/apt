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
import java.util.Map;
import uniol.apt.io.parser.IParserNode;

/**
 * Represents a node in the internal datastructure of the parser.
 * <p/>
 * @author Manuel Gieseking
 */
public class ParserNode implements IParserNode {

	private String id;
	private Map<String, String> options = new HashMap<>();

	/**
	 * Creates a node with the given id.
	 * <p/>
	 * @param id - the id of the node.
	 */
	public ParserNode(String id) {
		this.id = id;
	}

	/**
	 * Adds a list of options to this node.
	 * <p/>
	 * @param m - the options of this node.
	 */
	public void putAllOptions(Map<? extends String, ? extends String> m) {
		options.putAll(m);
	}

	/**
	 * Adds a option to this node.
	 * <p/>
	 * @param key   - the key of the option.
	 * @param value - the option.
	 * <p/>
	 * @return the previous option with the same key in this map if exists.
	 */
	public String putOption(String key, String value) {
		return options.put(key, value);
	}

	/**
	 * Returns the id of this node.
	 * <p/>
	 * @return the id of this node.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the options of this node.
	 * <p/>
	 * @return the options of this node.
	 */
	public Map<String, String> getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return "ParserNode{" + "id=" + id + ", options=" + options + '}';
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
