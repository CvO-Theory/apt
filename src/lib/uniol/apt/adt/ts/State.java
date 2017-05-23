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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.CollectionToUnmodifiableSetAdapter;
import uniol.apt.adt.Node;

/**
 * State is the node class of the TransitionSystem.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class State extends Node<TransitionSystem, Arc, State> {
	final Map<ArcKey, Arc> presetEdges = new HashMap<>();
	final Map<ArcKey, Arc> postsetEdges = new HashMap<>();

	final Map<String, Set<Arc>> postsetEdgesByLabel = new HashMap<>();
	final Map<String, Set<Arc>> presetEdgesByLabel = new HashMap<>();

	/**
	 * Constructor.
	 * @param ts the Transitionsystem the state belongs to.
	 * @param id the id of the state.
	 */
	State(TransitionSystem ts, String id) {
		super(ts, id);
	}

	/**
	 * Constructor for copying a given state to a given transistionsystem. The constructor also copies the
	 * references of the extensions.
	 * @param ts the transitionsystem the state belongs to.
	 * @param s  the state to copy.
	 */
	State(TransitionSystem ts, State s) {
		super(ts, s);
	}

	/**
	 * Returns the set of states from which this node can be reached
	 * with the given label.
	 *
	 * @param label
	 *                the label to look for
	 * @return a set of nodes that allow to reach this node by the
	 *         given label
	 */
	public Set<State> getPresetNodesByLabel(String label) {
		return graph.getPresetNodesByLabel(this, label);
	}

	/**
	 * Returns the set of arcs that end in this node and have the
	 * given label.
	 *
	 * @param label
	 *                the label of all arcs in the result
	 * @return an unmodifiable set of arcs that end at this node and
	 *         have the given label
	 */
	public Set<Arc> getPresetEdgesByLabel(String label) {
		Set<Arc> result = presetEdgesByLabel.get(label);
		if (result == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Returns the set of states that is reached by arcs from this node with
	 * the given label.
	 *
	 * @param label
	 *                the label to look for
	 * @return a set of nodes that can be reached by arcs with the given
	 *         label
	 */
	public Set<State> getPostsetNodesByLabel(String label) {
		return graph.getPostsetNodesByLabel(this, label);
	}

	/**
	 * Returns the set of arcs that start in the given node and have the
	 * given label.
	 *
	 * @param label
	 *                the label of all arcs in the result
	 * @return a set of arcs that begin at this node and have the given
	 *         label
	 */
	public Set<Arc> getPostsetEdgesByLabel(String label) {
		Set<Arc> result = postsetEdgesByLabel.get(label);
		if (result == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(result);
	}

	@Override
	public Set<State> getPresetNodes() {
		return this.graph.getPresetNodes(this);
	}

	@Override
	public Set<State> getPostsetNodes() {
		return this.graph.getPostsetNodes(this);
	}

	@Override
	public Set<Arc> getPresetEdges() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique
		return new CollectionToUnmodifiableSetAdapter<>(presetEdges.values());
	}

	@Override
	public Set<Arc> getPostsetEdges() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique
		return new CollectionToUnmodifiableSetAdapter<>(postsetEdges.values());
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
