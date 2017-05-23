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

package uniol.apt.adt.ts;

import uniol.apt.adt.Edge;

/**
 * Arcs serve as edges in a TransitionSystem. They have a source node's id, a target node's id and a label.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Arc extends Edge<TransitionSystem, Arc, State> {

	Event label;

	/**
	 * Constructor.
	 * @param ts     the TransitionSystem this arc belongs to.
	 * @param source the source node.
	 * @param target the target node.
	 * @param label  the label this arc has.
	 */
	Arc(TransitionSystem ts, State source, State target, Event label) {
		super(ts, source, target);
		this.label = label;
	}

	/**
	 * Constructor for copying a Arc to another transitionsystem. The constructor also copies the references of the
	 * extensions.
	 * @param ts the TransitionSystem.
	 * @param a  the arc.
	 */
	Arc(TransitionSystem ts, Arc a) {
		super(ts, a);
		this.label = a.label;
	}

	/**
	 * Gets the label of the arc.
	 * @return the label event.
	 */
	public Event getEvent() {
		return this.label;
	}

	/**
	 * Gets the label of the arc.
	 * @return the label.
	 */
	public String getLabel() {
		return this.label.getLabel();
	}

	/**
	 * Gets the label of the arc. To maintain consistency it's just a delegate to the transitionsystem.
	 * @param label the label to set.
	 */
	public void setLabel(String label) {
		graph.setArcLabel(source.getId(), target.getId(), this.label, label);
	}

	/**
	 * Gets the source node's id.
	 * @return the source node's id
	 */
	public String getSourceId() {
		return source.getId();
	}

	/**
	 * Gets the target node's id.
	 * @return the target node's id
	 */
	public String getTargetId() {
		return target.getId();
	}

	@Override
	public String toString() {
		return getSourceId() + "--" + label + "->" + getTargetId();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
