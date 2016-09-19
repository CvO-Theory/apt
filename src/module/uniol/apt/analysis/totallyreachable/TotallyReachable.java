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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.SpanningTree;

/**
 * Check if all nodes are reachable from the initial node.
 *
 * @author Uli Schlachter
 */
public class TotallyReachable {
	private final State unreachableState;

	/**
	 * Enumeration to select the reachability test implementation.
	 */
	public static enum Algorithm {
		SEARCH,
		SPANNING_TREE
	}

	/**
	 * Gets a transition system and checks if it is totally reachable
	 * @param ts Transition system
	 * @param algo The search algorithm to use
	 */
	public TotallyReachable(TransitionSystem ts, Algorithm algo) {
		switch (algo) {
			case SEARCH:
				Set<State> stillToVisit = new HashSet<>();
				Set<State> unreached = new HashSet<>(ts.getNodes());
				stillToVisit.add(ts.getInitialState());
				unreached.remove(ts.getInitialState());

				while (!stillToVisit.isEmpty()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

					Iterator<State> iter = stillToVisit.iterator();
					State state = iter.next();
					iter.remove();

					for (State follower : state.getPostsetNodes())
						if (unreached.remove(follower))
							stillToVisit.add(follower);
				}

				if (unreached.isEmpty())
					unreachableState = null;
				else
					unreachableState = unreached.iterator().next();
				break;
			case SPANNING_TREE:
				SpanningTree<TransitionSystem, Arc, State> tree
					= SpanningTree.<TransitionSystem, Arc, State>get(ts, ts.getInitialState());
				if (tree.isTotallyReachable())
					unreachableState = null;
				else
					unreachableState = tree.getUnreachableNodes().iterator().next();
				break;
			default:
				throw new AssertionError();
		}
	}

	/**
	 * Gets a transition system and checks if it is totally reachable
	 * @param ts Transition system
	 */
	public TotallyReachable(TransitionSystem ts) {
		this(ts, Algorithm.SPANNING_TREE);
	}

	/**
	 * Returns true if the lts is totally reachable.
	 * @return - Boolean
	 */
	public boolean isTotallyReachable() {
		return unreachableState == null;
	}

	/**
	 * Returns the label which makes the lts not totally reachable.
	 * @return - Id of the node
	 */
	public String getLabel() {
		return unreachableState != null ? unreachableState.getId() : null;
	}

	/**
	 * Returns the node which makes the lts not totally reachable.
	 * @return - State
	 */
	public State getNode() {
		return unreachableState;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
