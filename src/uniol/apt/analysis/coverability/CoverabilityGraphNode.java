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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Transition;

/**
 * This class represents a node in a coverability graph. A node is labeled with a marking which identifies it uniquele
 * and has a firing sequence which it can be reached with. Additionally, the postset of the node is available.
 * @author Uli Schlachter
 */
public class CoverabilityGraphNode {
	private final CoverabilityGraph graph;
	private final Marking marking;
	private final List<Transition> firingSequence;
	private final CoverabilityGraphNode parent;
	private final CoverabilityGraphNode covered;
	private Set<CoverabilityGraphEdge> postsetEdges;

	/**
	 * Construct a new coverability graph node.
	 * @param graph The graph that this node belongs to.
	 * @param transition The transition that is fired from this node's parent to reach this new node.
	 * @param marking The marking that identifies this node.
	 * @param parent The parent node of this node.
	 * @param covered The node which is covered by this node.
	 */
	CoverabilityGraphNode(CoverabilityGraph graph, Transition transition, Marking marking,
			CoverabilityGraphNode parent, CoverabilityGraphNode covered) {
		this.graph = graph;
		this.marking = marking;
		this.parent = parent;
		this.covered = covered;
		List<Transition> sequence = new LinkedList<>();
		if (parent != null) {
			sequence.addAll(parent.firingSequence);
			sequence.add(transition);
		} else {
			assert transition == null;
		}
		this.firingSequence = unmodifiableList(sequence);
	}

	/**
	 * Get the parent of this node on the path back to the root of the depth first search tree.
	 * @return the parent or null
	 */
	CoverabilityGraphNode getParent() {
		return this.parent;
	}

	/**
	 * Get the node in the coverability graph that is covered by this node, if such a node exists.
	 * @return the covered node or null
	 */
	public CoverabilityGraphNode getCoveredNode() {
		return this.covered;
	}

	/**
	 * Get the marking that this node represents.
	 * @return The marking.
	 */
	public Marking getMarking() {
		return new Marking(this.marking);
	}

	/**
	 * Get the firing sequence which reaches the marking represented by this instance from the initial marking of
	 * the Petri net.
	 * @return The firing sequence.
	 */
	public List<Transition> getFiringSequence() {
		return this.firingSequence;
	}

	/**
	 * Get the firing sequence which reaches this node from some ancestor. The given ancestor must be a parent,
	 * grandparent or "older" of us. The implementation does not check this requirement and might return bogus
	 * results or thrown an exception if this is violated.
	 * @return The firing sequence.
	 */
	public List<Transition> getFiringSequenceFrom(CoverabilityGraphNode ancestor) {
		int ancestorSequenceLength = ancestor.getFiringSequence().size();
		return this.firingSequence.subList(ancestorSequenceLength, this.firingSequence.size());
	}

	/**
	 * Get all nodes that are in this node's postset.
	 * @return the postset.
	 */
	public Set<CoverabilityGraphNode> getPostset() {
		Set<CoverabilityGraphNode> postset = new HashSet<>();
		for (CoverabilityGraphEdge edge : getPostsetEdges()) {
			postset.add(edge.getTarget());
		}
		return unmodifiableSet(postset);
	}

	/**
	 * Get all edges that begin in this node.
	 * @return all edges.
	 */
	public Set<CoverabilityGraphEdge> getPostsetEdges() {
		if (postsetEdges == null)
			postsetEdges = unmodifiableSet(graph.getPostsetEdges(this));
		return postsetEdges;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
