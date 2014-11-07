/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.util;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculate a spanning tree of a graph
 * @author Uli Schlachter
 */
public class SpanningTree<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> {
	private final Map<N, E> predecessorMap;
	private final Set<N> unreachableNodes;
	private final Set<E> chords;
	private final N startNode;
	private final G graph;
	private final Map<N, List<N>> pathMap = new HashMap<>();

	// TODO: Possible extensions: Spanning forest?

	/**
	 * Construct a new spanning tree
	 * @param graph The graph for which a spanning tree should be constructed.
	 * @param startNode The start node for the spanning tree.
	 */
	public SpanningTree(G graph, N startNode) {
		// Calculate the spanning tree: For each node we remember its predecessor in the tree and we keep a set
		// of unvisited nodes. We visit unvisited nodes in turn, look at all their children which we haven't
		// visited yet and enlarge the spanning tree by the path from the current node to the children.
		Map<N, E> predecessorMap = new HashMap<>();
		Set<N> unvisitedNodes = new HashSet<>(graph.getNodes());
		Set<N> stillToVisit = new HashSet<>();
		Set<E> chords = new HashSet<>();

		// Start by visiting the start node.
		if (startNode != null) {
			stillToVisit.add(startNode);
			unvisitedNodes.remove(startNode);
		}

		while (!stillToVisit.isEmpty()) {
			N node = stillToVisit.iterator().next();
			stillToVisit.remove(node);

			// For each child of the current node...
			for (E edge : node.getPostsetEdges()) {
				N child = edge.getTarget();

				// ...if it was not yet reached, mark it as reachable and...
				if (unvisitedNodes.remove(child)) {
					// ...remember the path to the start node.
					predecessorMap.put(child, edge);
					// Also, we have to visit this node later.
					stillToVisit.add(child);
				} else {
					// It was already visited, so we found a new chord
					chords.add(edge);
				}
			}
		}

		this.predecessorMap = Collections.unmodifiableMap(predecessorMap);
		this.unreachableNodes = Collections.unmodifiableSet(unvisitedNodes);
		this.chords = Collections.unmodifiableSet(chords);
		this.startNode = startNode;
		this.graph = graph;
	}

	/**
	 * Construct a new spanning tree
	 * @param graph The graph for which a spanning tree should be constructed.
	 */
	public SpanningTree(G graph) {
		this(graph, graph.getNodes().isEmpty() ? null : graph.getNodes().iterator().next());
	}

	/**
	 * Get all the nodes in the graph which are not reachable from the start node.
	 */
	public Set<N> getUnreachableNodes() {
		return unreachableNodes;
	}

	/**
	 * Get the predecessor in the tree.
	 * @param node The node whose predecessor should be returned.
	 * @return The predecessor if one exists or null. The start node and unreachable nodes do not have predecessors.
	 */
	public N getPredecessor(N node) {
		E e = getPredecessorEdge(node);
		if (e != null)
			return e.getSource();
		return null;
	}

	/**
	 * Get the edge via which the given node is reached in the tree.
	 * @param node The node whose predecessor edge should be returned.
	 * @return The predecessor edge if one exists or null. The start node and unreachable nodes do not have predecessors.
	 */
	public E getPredecessorEdge(N node) {
		return predecessorMap.get(node);
	}

	/**
	 * Get the path from the start node.
	 * @param node The node whose path to return.
	 * @return The path from the start node to this node. The first entry will be the start node, the last one will
	 * be the given node.
	 */
	public List<N> getPathFromStart(N node) {
		if (!predecessorMap.containsKey(node) && !node.equals(startNode)) {
			return Collections.emptyList();
		}

		// TODO: Optimization: This (indirectly) also calculates the paths for the previous nodes, it would be
		// nice to save them
		List<N> result = pathMap.get(node);

		if (result == null) {
			result = new ArrayList<>();

			while (node != null) {
				result.add(node);
				node = getPredecessor(node);
			}

			Collections.reverse(result);
			result = Collections.unmodifiableList(result);
			pathMap.put(node, result);
		}

		return result;
	}

	/**
	 * Get the chords of the spanning tree. A chord is an edge which forms a cycle with respect to the tree. In
	 * other words, a chord is an edge which is not part of the tree.
	 * @return A set of chords for the spanning tree.
	 */
	public Set<E> getChords() {
		return chords;
	}

	/**
	 * @return The start node from which the tree was constructed.
	 */
	public N getStartNode() {
		return startNode;
	}

	/**
	 * @return The graph from which the tree was constructed.
	 */
	public G getGraph() {
		return graph;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
