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

package uniol.apt.analysis.reversible;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.SpanningTree;

/**
 * An LTS is reversible if the initial state can be reached from every state that is reachable within the system.
 * @author Vincent Göbel, Uli Schlachter
 */
public class ReversibleTS {
	private final TransitionSystem ts;
	private final Set<State> unreversibleStates;

	public ReversibleTS(TransitionSystem ts) {
		this.ts = ts;
		SpanningTree<TransitionSystem, Arc, State> forwardTree
			= SpanningTree.<TransitionSystem, Arc, State>get(ts, ts.getInitialState());
		SpanningTree<TransitionSystem, Arc, State> backwardTree
			= SpanningTree.<TransitionSystem, Arc, State>getReversed(ts, ts.getInitialState());

		// Calculate reachable states
		unreversibleStates = new HashSet<>(ts.getNodes());
		unreversibleStates.removeAll(forwardTree.getUnreachableNodes());

		// Keep only those which cannot reach the initial state again
		unreversibleStates.retainAll(backwardTree.getUnreachableNodes());
	}

	/**
	 * Checks the system for reversibility. If it is not reversible a counterexample is saved in the variable node_.
	 */
	public final void check() {
	}

	/**
	 * @param id The ID of the node corresponding to the checked state
	 * @return False iff the state is reachable, but not reversible
	 */
	public boolean check(String id) {
		State node = ts.getNode(id);
		return unreversibleStates.contains(node);
	}

	/**
	 * @return A Set of all unreversible states in the LTS
	 */
	public Set<State> getUnreversibleStates() {
		return Collections.unmodifiableSet(unreversibleStates);
	}

	/**
	 * @return true, if the LTS is reversible, or false, if it is not
	 */
	public boolean isReversible() {
		return unreversibleStates.isEmpty();
	}

	/**
	 * @return A non-reversible state, if one exists.
	 */
	public State getNode() {
		if (unreversibleStates.isEmpty())
			return null;
		return unreversibleStates.iterator().next();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
