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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.reverse;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Transition;

/**
 * This class represents a node in a coverability graph. A node is labeled with a marking which identifies it uniquely
 * and has a firing sequence with which it can be reached from the initial marking of the underlying Petri net.
 * Additionally, the postset of the node is available.
 * @author Uli Schlachter
 */
public class CoverabilityGraphNode {
	private final CoverabilityGraph graph;
	private final Marking marking;
	private final Transition reachingTransition;
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
		this.reachingTransition = transition;
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
	 * @see getFiringSequenceFromCoveredNode
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
	 * @see getFiringSequenceFromCoveredNode
	 */
	public List<Transition> getFiringSequence() {
		List<Transition> result = new ArrayList<>();
		CoverabilityGraphNode node = this;
		while (node.reachingTransition != null) {
			result.add(node.reachingTransition);
			node = node.parent;
		}
		reverse(result);
		return unmodifiableList(result);
	}

	/**
	 * Get the firing sequence which reaches this node from the covered node, or null. This function can be used
	 * together with getFiringSequence to describe a way to generate tokens in the Petri net. If the return value is
	 * not null, the sequence returned from this function can be fired in an infinite loop in the marking described
	 * by <code>getCoveredNode.getMarking()</code>, and will increase the number of token in the Petri net.
	 * @return The firing sequence.
	 * @see getCoveredNode
	 * @see getFiringSequence
	 */
	public List<Transition> getFiringSequenceFromCoveredNode() {
		if (this.covered == null)
			return null;
		int coveredSequenceLength = this.covered.getFiringSequence().size();
		List<Transition> firingSequence = getFiringSequence();
		return firingSequence.subList(coveredSequenceLength, firingSequence.size());
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
