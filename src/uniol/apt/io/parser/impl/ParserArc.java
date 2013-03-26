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

import uniol.apt.io.parser.IParserArc;

/**
 * Represents of arc of a transition system in the internal datastructure of the parser.
 * <p/>
 * @author Manuel Gieseking
 */
public class ParserArc implements IParserArc {

	private String fromId;
	private String toId;
	private String label;

	/**
	 * Creates a arc of the state with the id fromId to a state with the id toId and a given label.
	 * <p/>
	 * @param fromId - the id of the state the arc starts from.
	 * @param label  - the label of the arc.
	 * @param toId   - the id of the state the arc ends.
	 */
	public ParserArc(String fromId, String label, String toId) {
		this.fromId = fromId;
		this.toId = toId;
		this.label = label;
	}

	/**
	 * Returns the id of the state this arc starts from.
	 * <p/>
	 * @return the id of the state this arc starts from.
	 */
	public String getFromId() {
		return fromId;
	}

	/**
	 * Sets the id of the state this arc starts from.
	 * <p/>
	 * @param fromId - the id this arc starts from.
	 */
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	/**
	 * Returns the id of the state this arc ends.
	 * <p/>
	 * @return the id of the state this arc ends.
	 */
	public String getToId() {
		return toId;
	}

	/**
	 * Sets the id of the state this arc ends.
	 * <p/>
	 * @param toId - the id this arc ends.
	 */
	public void setToId(String toId) {
		this.toId = toId;
	}

	/**
	 * Returns the label of this arc.
	 * <p/>
	 * @return the label of this arc.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of this arc.
	 * <p/>
	 * @param label - the label of this arc.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "ParserArc{" + "fromId=" + fromId + ", toId=" + toId + ", label=" + label + '}';
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
