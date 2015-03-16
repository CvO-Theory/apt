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
 * @author Vincent Göbel
 */
public class PersistentTS {
	private final TransitionSystem ts;
	private final boolean backwards;

	private boolean persistent = false;
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
	public void check() {
		for (State node : ts.getNodes()) {
			Set<String> labels = getPostLabels(node);
			for (String label1 : labels) {
				for (String label2 : labels) {
					if (!checkPersistent(node, label1, label2)) {
						this.persistent = false;
						node_ = node;
						label1_ = label1;
						label2_ = label2;
						return;
					}
				}
			}

		}
		this.persistent = true;
		node_ = null;
		label1_ = null;
		label2_ = null;
	}

	private boolean checkPersistent(State node, String label1, String label2) {
		if (label1.equals(label2))
			return true;

		Set<State> post1 = getDirectlyReachableNodes(node, label1);
		Set<State> post2 = getDirectlyReachableNodes(node, label2);
		Set<State> r1 = new HashSet<State>();
		Set<State> r2 = new HashSet<State>();
		for (State n : post1) {
			r1.addAll(getDirectlyReachableNodes(n, label2));
		}
		for (State n : post2) {
			r2.addAll(getDirectlyReachableNodes(n, label1));
		}
		return !Collections.disjoint(r1, r2);
	}

	private Set<Arc> getPostsetEdges(State n) {
		if (!backwards)
			return n.getPostsetEdges();
		else
			return n.getPresetEdges();
	}

	private Set<String> getPostLabels(State n) {
		Set<String> labels = new HashSet<String>();
		for (Arc e : getPostsetEdges(n)) {
			labels.add(e.getLabel());
		}
		return labels;
	}

	/**
	 * Calculates the set of all nodes n' with  a transition (n,l,n')
	 *
	 * @param n The starting node
	 * @param l A label
	 * @return A set containing the nodes that can are targets of the postset edges of n
	 */
	private Set<State> getDirectlyReachableNodes(State n, String l) {
		Set<State> nodes = new HashSet<State>();
		for (Arc e : getPostsetEdges(n)) {
			if (e.getLabel().equals(l)) {
				if (!backwards)
					nodes.add(e.getTarget());
				else
					nodes.add(e.getSource());
			}
		}
		return nodes;
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
