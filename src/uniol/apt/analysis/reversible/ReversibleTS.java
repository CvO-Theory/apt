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

import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * An LTS is reversible if the initial state can be reached from every state that is reachable within the system.
 *
 *
 * @author Vincent Göbel
 *
 */
public class ReversibleTS {

	private final TransitionSystem ts;
	private boolean reversible_ = false;
	private State node_ = null;

	public ReversibleTS(TransitionSystem ts) {
		this.ts = ts;
		check();
	}

	/**
	 * Checks the system for reversibility. If it is not reversible a counterexample is saved in the variable node_.
	 */
	public final void check() {
		for (State node : ts.getNodes()) {
			if (!reaches(ts.getInitialState(), node)) {
				continue;
			}
			boolean rec = reaches(node, ts.getInitialState());
			if (!rec) {
				reversible_ = false;
				node_ = node;
				return;
			}
		}
		reversible_ = true;
		node_ = null;
	}

	/**
	 * Checks whether a path from one node to another exists in the LTS
	 *
	 * @param start The starting node
	 * @param goal The target node
	 * @return true iff a path exists
	 */
	private boolean reaches(State start, State goal) {
		Set<State> all = new HashSet<>();
		Set<State> now = new HashSet<>();
		Set<State> next = new HashSet<>();

		if (start.equals(goal)) {
			return true;
		}
		now.add(start);
		next.addAll(start.getPostsetNodes());

		while (next.size() > 0) {
			all.addAll(now);
			now = next;
			next = new HashSet<>();
			if (now.contains(goal)) {
				return true;
			}

			for (State node : now) {
				Set<State> candidates = node.getPostsetNodes();
				for (State cand : candidates) {
					if (!all.contains(cand) && !now.contains(cand)) {
						next.add(cand);
					}
				}
			}
		}

		return false;
	}

	/**
	 * @param id The ID of the node corresponding to the checked state
	 * @return False iff the state is reachable, but not reversible
	 */
	public boolean check(String id) {
		State node = ts.getNode(id);
		return !reaches(ts.getInitialState(), node) || reaches(node, ts.getInitialState());
	}

	/**
	 * @return A Set of all reversible states in the LTS
	 */
	public Set<String> getAllReversibleStates() {
		Set<String> revStates = new HashSet<>();
		for (State n : ts.getNodes()) {
			if (check(n.getId())) {
				revStates.add(n.getId());
			}
		}
		return revStates;
	}

	/**
	 * @return true, if the LTS is reversible, or false, if it is not
	 */
	public boolean isReversible() {
		return reversible_;
	}

	/**
	 * @return A non-reversible state, if one exists.
	 */
	public State getNode() {
		return node_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
