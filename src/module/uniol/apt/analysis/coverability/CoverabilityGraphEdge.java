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

package uniol.apt.analysis.coverability;

import uniol.apt.adt.pn.Transition;

/**
 * This class represents an edge in a coverability graph. An edge has a source and a target node and is labeled with a
 * transition.
 * @author Uli Schlachter
 */
public class CoverabilityGraphEdge {
	private final Transition transition;
	private final CoverabilityGraphNode source;
	private final CoverabilityGraphNode target;

	/**
	 * Construct a new coverability graph edge for the given arguments.
	 * @param transition The transition that the edge is labeled with.
	 * @param source The node that the edge begins in.
	 * @param target The node that the edge goes to.
	 */
	CoverabilityGraphEdge(Transition transition, CoverabilityGraphNode source, CoverabilityGraphNode target) {
		this.transition = transition;
		this.source = source;
		this.target = target;
	}

	/**
	 * Get the transition that this edge was fired for.
	 * @return the transition.
	 */
	public Transition getTransition() {
		return this.transition;
	}

	/** Get the node that this edge leads to.
	 * @return the target.
	 */
	public CoverabilityGraphNode getTarget() {
		return this.target;
	}

	/**
	 * Get the node that this edge originates from.
	 * @return the source.
	 */
	public CoverabilityGraphNode getSource() {
		return this.source;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
