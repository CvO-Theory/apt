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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Check if all nodes are reachable from the initial node.
 *
 * @author Sören
 *
 */
public class TotallyReachable {

	private final TransitionSystem ts_;
	private State node_;
	private boolean totallyReachable_ = false;
	private final Set<State> allNodes_;
	private final Set<State> reachedNodesSet_ = new HashSet<>();

	/**
	 * Gets a transition system and saves all nodes
	 * @param ts Transition system
	 */
	public TotallyReachable(TransitionSystem ts) {
		ts_ = ts;
		allNodes_ = new HashSet<>(ts.getNodes());
	}

	public boolean isTotallyReachable() {
		return this.totallyReachable_;
	}

	/**
	 * Add the postset of each node to the current postset and search for
	 * current node.
	 */
	public void check() {
		int reachedNodes = 0;

		final Set<State> postSetCopy = new HashSet<>();
		postSetCopy.add(ts_.getInitialState());
		Iterator<State> itr = postSetCopy.iterator();

		while (itr.hasNext()) {
			State postNode = itr.next();
			reachedNodesSet_.add(postNode);
			postSetCopy.addAll(checkPostSet(postNode.getPostsetNodes()));
			postSetCopy.remove(postNode);

			reachedNodes++;
			allNodes_.remove(postNode);

			itr = postSetCopy.iterator();
		}

		if (reachedNodes == ts_.getNodes().size()) {
			totallyReachable_ = true;
		} else {
			Iterator<State> it = allNodes_.iterator();
			node_ = it.next();
		}
	}

	/**
	 * Returns postset nodes which we never reached.
	 *
	 * @param set
	 * @return Postset with nodes we have never reached.
	 */
	private Collection<? extends State> checkPostSet(final Set<State> set) {
		Set<State> nodesNotVisited = new HashSet<>();
		for (State node : set) {
			if (!reachedNodesSet_.contains(node)) {
				nodesNotVisited.add(node);
			}
		}
		return nodesNotVisited;
	}

	/**
	 * Returns the label which makes the lts not totally reachable.
	 * @return - Id of the node
	 */
	public String getLabel() {
		return node_.getId();
	}

	/**
	 * Returns the node which makes the lts not totally reachable.
	 * @return - State
	 */
	public State getNode() {
		return node_;
	}

	/**
	 * Returns true if the lts is reachable.
	 * @return - Boolean
	 */
	public boolean isReachable() {
		return totallyReachable_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
