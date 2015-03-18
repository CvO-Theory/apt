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

package uniol.apt.analysis.persistent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.LinkedList;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Checks whether an LTS for persistance. The system is persistent, if it satisfies the "small diamond property".
 *
 * TODO: explain SDP
 *           a
 * (s0) ------------> (s1)
 *   |                  |
 *   |                  |
 *   | b                | b
 *   |                  |
 *   V       a          V
 * (s2) ------------> (s4)
 *
 *Above: Mini-example of an persistent LTS
 *
 * @author Vincent Göbel, Uli Schlachter
 */
public class PersistentTS {
	private final TransitionSystem ts;
	private final boolean backwards;
	private final Map<State, Map<String, Set<State>>> statePostsetsCache = new HashMap<>();

	private boolean persistent = true;
	private State node_ = null;
	private String label1_ = null;
	private String label2_ = null;

	public PersistentTS(TransitionSystem ts, boolean backwards) {
		this.backwards = backwards;
		this.ts = ts;
		check();
	}

	public PersistentTS(TransitionSystem ts) {
		this(ts, false);
	}

	/**
	 * Checks whether or not the LTS is persistent. If it is not, a counterexample is saved in the variables
	 * node, label1 and label2.
	 */
	private void check() {
		// Go through all states
		for (State node : ts.getNodes()) {
			Map<String, Set<State>> postset = getStatePostset(node);
			Deque<String> unhandledLabels = new LinkedList<>(postset.keySet());
			// Go through all pairs of (enabled) labels
			while (!unhandledLabels.isEmpty()) {
				String label1 = unhandledLabels.removeFirst();
				Set<State> statesAfterLabel1 = postset.get(label1);
				for (String label2 : unhandledLabels) {
					// Calculate states reached by first following label1 then label2
					Set<State> statesAfter12 = new HashSet<>();
					for (State node1 : statesAfterLabel1) {
						statesAfter12.addAll(getStatePostsetViaLabel(node1, label2));
					}

					// Check if any of these is also reached by label2, then label1
					boolean foundSharedState = false;
					for (State node2 : postset.get(label2)) {
						if (!Collections.disjoint(statesAfter12, getStatePostsetViaLabel(node2, label1))) {
							foundSharedState = true;
							break;
						}
					}

					if (!foundSharedState) {
						this.persistent = false;
						node_ = node;
						label1_ = label1;
						label2_ = label2;
						return;
					}
				}
			}
		}
	}

	// Get the postset of a state as a Map which maps a label to a set of states
	private Map<String, Set<State>> getStatePostset(State node) {
		Map<String, Set<State>> result = statePostsetsCache.get(node);
		if (result != null)
			return result;

		result = new HashMap<>();
		for (Arc arc : getPostsetEdges(node)) {
			Set<State> set = result.get(arc.getLabel());
			if (set == null) {
				set = new HashSet<>();
				result.put(arc.getLabel(), set);
			}
			set.add(getTarget(arc));
		}
		result = Collections.unmodifiableMap(result);
		statePostsetsCache.put(node, result);
		return result;
	}

	// Get the set of states that is reached via "label" from "state"
	private Set<State> getStatePostsetViaLabel(State node, String label) {
		Set<State> result = getStatePostset(node).get(label);
		if (result == null)
			return Collections.emptySet();
		return result;
	}

	// Get the postset or the preset of an arc, depending on the "backwards" variable
	private Set<Arc> getPostsetEdges(State n) {
		if (!backwards)
			return n.getPostsetEdges();
		else
			return n.getPresetEdges();
	}

	// Get the target or the source of an arc, depending on the "backwards" variable
	private State getTarget(Arc arc) {
		if (!backwards)
			return arc.getTarget();
		else
			return arc.getSource();
	}

	public boolean isPersistent() {
		return this.persistent;
	}

	/**
	 *
	 * @return Node which destroys the sdp.
	 */
	public State getNode() {
		return node_;
	}

	/**
	 *
	 * @return Label which destroys the sdp.
	 */
	public String getLabel1() {
		return label1_;
	}

	public String getLabel2() {
		return label2_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
