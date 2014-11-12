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

package uniol.apt.analysis.totallyreachable;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.SpanningTree;

/**
 * Check if all nodes are reachable from the initial node.
 *
 * @author Uli Schlachter
 */
public class TotallyReachable {
	private final State unreachableState;

	/**
	 * Gets a transition system and checks if it is totally reachable
	 * @param ts Transition system
	 */
	public TotallyReachable(TransitionSystem ts) {
		SpanningTree<TransitionSystem, Arc, State> tree = new SpanningTree<>(ts, ts.getInitialState());
		if (tree.isTotallyReachable())
			unreachableState = null;
		else
			unreachableState = tree.getUnreachableNodes().iterator().next();
	}

	public boolean isTotallyReachable() {
		return unreachableState == null;
	}

	/**
	 * Add the postset of each node to the current postset and search for
	 * current node.
	 */
	public void check() {
	}

	/**
	 * Returns the label which makes the lts not totally reachable.
	 * @return - Id of the node
	 */
	public String getLabel() {
		return unreachableState.getId();
	}

	/**
	 * Returns the node which makes the lts not totally reachable.
	 * @return - State
	 */
	public State getNode() {
		return unreachableState;
	}

	/**
	 * Returns true if the lts is reachable.
	 * @return - Boolean
	 */
	public boolean isReachable() {
		// TODO: Duplicate API?!
		return isTotallyReachable();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
