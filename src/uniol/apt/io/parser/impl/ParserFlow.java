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

import java.util.Objects;
import uniol.apt.io.parser.IParserFlow;

/**
 * Represents of flow of a petri net in the internal datastructure of the parser.
 * <p/>
 * @author Manuel Gieseking
 */
public class ParserFlow implements IParserFlow {

	private String fromId;
	private String toId;
	private int weight;

	/**
	 * Creates a flow from the node with id fromid to a node with the id toId and the given weight.
	 * <p/>
	 * @param fromId - the id of the node the flow starts from.
	 * @param toId   - the id of the node the flow ends.
	 * @param weight - the weight of the flow.
	 */
	public ParserFlow(String fromId, String toId, int weight) {
		this.fromId = fromId;
		this.toId = toId;
		this.weight = weight;
	}

	/**
	 * Returns the id of the node this flow starts from.
	 * <p/>
	 * @return the id of the node this flow starts from.
	 */
	public String getFromId() {
		return fromId;
	}

	/**
	 * Sets the id of the node this flow starts from.
	 * <p/>
	 * @param fromId - the id of the node this flow starts from.
	 */
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	/**
	 * Returns the id of the node this flow ends.
	 * <p/>
	 * @return the id of the node this flow ends.
	 */
	public String getToId() {
		return toId;
	}

	/**
	 * Sets the id of the node this flow ends.
	 * <p/>
	 * @param toId - the id of the node this flow ends.
	 */
	public void setToId(String toId) {
		this.toId = toId;
	}

	/**
	 * Returns the weight of this flow.
	 * <p/>
	 * @return the weight of this flow.
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Sets the weight of this flow.
	 * <p/>
	 * @param weight - the weight of this flow.
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.fromId);
		hash = 29 * hash + Objects.hashCode(this.toId);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ParserFlow other = (ParserFlow) obj;
		if (!Objects.equals(this.fromId, other.fromId)) {
			return false;
		}
		if (!Objects.equals(this.toId, other.toId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ParserFlow{" + "fromId=" + fromId + ", toId=" + toId + ", weight=" + weight + '}';
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
