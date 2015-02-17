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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.StructuralExtensionRemover;
import uniol.apt.adt.exception.StructureException;

/**
 * Calculate a spanning tree of a graph based on a breadth-first search.
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * @author Uli Schlachter
 */
public class SpanningTree<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> {
	private final Map<N, E> predecessorMap;
	private final Set<N> unreachableNodes;
	private final Set<E> chords;
	private final N startNode;
	private final G graph;

	/**
	 * Construct a spanning tree for the given graph. If a spanning tree was already computed, it is re-used instead
	 * of creating a new one.
	 * @param graph The graph for which a spanning tree should be constructed.
	 * @return A spanning tree.
	 */
	static public <G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
			SpanningTree<G, E, N> get(G graph) {
		return get(graph, graph.getNodes().isEmpty() ? null : graph.getNodes().iterator().next());
	}

	/**
	 * Construct a spanning tree for the given graph. If a spanning tree was already computed, it is re-used instead
	 * of creating a new one.
	 * @param graph The graph for which a spanning tree should be constructed.
	 * @param startNode The start node for the spanning tree.
	 * @return A spanning tree.
	 */
	static public <G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
			SpanningTree<G, E, N> get(G graph, N startNode) {
		String key = SpanningTree.class.getName();

		Object extension = null;
		try {
			extension = graph.getExtension(key);
		}
		catch (StructureException e) {
			// No such extension. Returning "null" would be too easy...
		}

		Map<Object, Object> map = null;
		if (extension != null && extension instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> castedMap = (Map<Object, Object>) extension;
			map = castedMap;
		} else {
			map = new HashMap<>();
			graph.putExtension(key, map);
			// Save this map as an extension, but make sure that it is removed if the structure of
			// the graph is changed in any way.
			graph.addListener(new StructuralExtensionRemover<G, E, N>(key));
		}

		Object tree = map.get(startNode);
		if (tree != null && tree instanceof SpanningTree) {
			@SuppressWarnings("unchecked")
			SpanningTree<G, E, N> result = (SpanningTree<G, E, N>) tree;
			return result;
		}

		SpanningTree<G, E, N> result = new SpanningTree<>(graph, startNode);
		map.put(startNode, result);
		return result;
	}

	/**
	 * Construct a new spanning tree
	 * @param graph The graph for which a spanning tree should be constructed.
	 * @param startNode The start node for the spanning tree.
	 */
	private SpanningTree(G graph, N startNode) {
		// Calculate the spanning tree: For each node we remember its predecessor in the tree and we keep a set
		// of unvisited nodes. We visit unvisited nodes in turn, look at all their children which we haven't
		// visited yet and enlarge the spanning tree by the path from the current node to the children.
		Map<N, E> predecessorMap = new HashMap<>();
		Set<N> unvisitedNodes = new HashSet<>(graph.getNodes());
		Deque<N> stillToVisit = new LinkedList<>();
		Set<E> chords = new HashSet<>();

		// Start by visiting the start node.
		if (startNode != null) {
			unvisitedNodes.remove(startNode);
		}
		N node = startNode;

		while (node != null) {
			// For each child of the current node...
			for (E edge : node.getPostsetEdges()) {
				N child = edge.getTarget();

				// ...if it was not yet reached, mark it as reachable and...
				if (unvisitedNodes.remove(child)) {
					// ...remember the path to the start node.
					predecessorMap.put(child, edge);
					// Also, we have to visit this node later.
					stillToVisit.addLast(child);
				} else {
					// It was already visited, so we found a new chord
					chords.add(edge);
				}
			}

			// Pick a new node to visit for the next iteration
			node = stillToVisit.pollFirst();
		}

		this.predecessorMap = Collections.unmodifiableMap(predecessorMap);
		this.unreachableNodes = Collections.unmodifiableSet(unvisitedNodes);
		this.chords = Collections.unmodifiableSet(chords);
		this.startNode = startNode;
		this.graph = graph;
	}

	/**
	 * Check if the given node is reachable.
	 * @param node The node to check.
	 * @return True if the node is reachable in this spanning tree, false otherwise.
	 */
	public boolean isReachable(N node) {
		return !unreachableNodes.contains(node);
	}

	/**
	 * Check if the underlying graph is totally reachable.
	 * @return True if there exists a path to any node, all starting in the specified initial node.
	 */
	public boolean isTotallyReachable() {
		return unreachableNodes.isEmpty();
	}

	/**
	 * Get all the nodes in the graph which are not reachable from the start node.
	 * @return Set of unreachable nodes.
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
	 * @return The predecessor edge if one exists or null. The start node and unreachable nodes do not have
	 * predecessors.
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

		// Possible optimization: Cache the result
		List<N> result = new ArrayList<>();

		while (node != null) {
			result.add(node);
			node = getPredecessor(node);
		}

		Collections.reverse(result);
		return Collections.unmodifiableList(result);
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
