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

package uniol.apt.analysis.connectivity;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Examine a graph's connectivity
 * @author Uli Schlachter, vsp
 */
public class Connectivity {
	/** No, you should not create instances of this class. */
	private Connectivity() {
	}

	/**
	 * Compute all isolated elements of a graph. A node is isolated if it has no edges.
	 * @param graph The graph whose weak connectivity should be checked.
	 * @param <G> The type of the graph that is examine.
	 * @param <N> The type of the graph's nodes.
	 * @return All isolated elements of the graph.
	 */
	public static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>> Set<N> findIsolatedElements(G graph) {
		Set<N> result = new HashSet<>();
		for (N node : graph.getNodes()) {
			if (node.getPresetNodes().isEmpty() && node.getPostsetNodes().isEmpty()) {
				result.add(node);
			}
		}
		return result;
	}

	/**
	 * Check if a graph is weakly connected. A graph is weakly connected if it has just a single weakly connected
	 * component.
	 * @param graph The graph whose weak connectivity should be checked.
	 * @param <G> The type of the graph that is examine.
	 * @param <N> The type of the graph's nodes.
	 * @return true if the graph is weakly connected.
	 */
	public static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>> boolean isWeaklyConnected(G graph) {
		return getWeaklyConnectedComponents(graph).size() <= 1;
	}

	/**
	 * Compute all of a graph's weakly connected components.
	 * The weakly connected component of a node is the set of nodes that are reachable from it while ignoring the
	 * direction of edges. All weakly connected components form a partition of the graph's nodes.
	 * @param graph The graph whose strong connectivity should be checked.
	 * @param <G> The type of the graph that is examine.
	 * @param <N> The type of the graph's nodes.
	 * @return A partition of the graph's nodes into components.
	 */
	public static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>>
			Set<? extends Set<N>> getWeaklyConnectedComponents(G graph) {
		Collection<N> unhandled = new HashSet<>();
		Set<Set<N>> result = new HashSet<>();

		// As long as not all of the graph's nodes were visited...
		unhandled.addAll(graph.getNodes());
		while (!unhandled.isEmpty()) {
			// ...get a random node and handle its component
			Iterator<N> it = unhandled.iterator();
			N node = it.next();
			it.remove();

			Set<N> component = getWeaklyConnectedComponent(node);
			unhandled.removeAll(component);
			result.add(component);
		}
		return result;
	}

	/**
	 * Get the weakly connected component of a given node.
	 * @param node The node whose component should be calculated.
	 * @param <N> The type of the graph's nodes.
	 * @return The node's component.
	 */
	public static <N extends INode<?, ?, N>> Set<N> getWeaklyConnectedComponent(N node) {
		Set<N> result = new HashSet<>();
		Deque<N> unvisited = new ArrayDeque<>();
		unvisited.add(node);
		result.add(node);

		while (!unvisited.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			node = unvisited.removeLast();

			// Handle the node's preset. All of those nodes belong to this node's component.
			for (N curNode : node.getPresetNodes()) {
				// If this was not yet already handled, add to unvisited
				if (result.add(curNode))
					unvisited.add(curNode);
			}

			// Handle the node's postset in the same way.
			for (N curNode : node.getPostsetNodes()) {
				// If this was not yet already handled, add to unvisited
				if (result.add(curNode))
					unvisited.add(curNode);
			}
		}

		return result;
	}

	/**
	 * Check if a graph is strongly connected. A graph is strongly connected if it just has a single strongly
	 * connected component.
	 * @param graph The graph whose strong connectivity should be checked.
	 * @param <G> The type of the graph that is examine.
	 * @param <N> The type of the graph's nodes.
	 * @return true if the graph is strongly connected.
	 */
	public static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>> boolean isStronglyConnected(G graph) {
		return getStronglyConnectedComponents(graph).size() <= 1;
	}

	/**
	 * Compute all of a graph's strongly connected components.
	 * The strongly connected component of a node is the set of nodes that are reachable from it while just
	 * following edges forwards ("in the direction of the arrow"). All strongly connected components form a
	 * partition of the graph's nodes.
	 * @param graph The graph that should be examined.
	 * @param <G> The type of the graph that is examine.
	 * @param <N> The type of the graph's nodes.
	 * @return A partition of the graph's nodes into components.
	 */
	public static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>>
			Set<? extends Set<N>> getStronglyConnectedComponents(G graph) {
		Set<Set<N>> result = new HashSet<>();
		Map<N, Integer> dfsNumbers = new HashMap<>();
		Map<N, Integer> minNumbers = new HashMap<>();
		int counter = 0;

		// Implemented per 'Algorithmische Datenstrukturen' Sommersemester 2012, algorithm 13.
		// This algorithm does a depth-first-search (DFS) through the graph. Each visited node gets assigned a
		// dfsNumber. In other words, nodes are numbered by the order in which they are visited.
		//
		// Additionally, a minNr is maintained for each node which is initialized to the node's dfsNumber. This
		// number will be the smallest dfsNumber that is reachable from this node via the DFS. When a node's
		// dfsNumber is equal to its minNumber, it is the root of a strongly connected component.
		// While doing this search, we manage a list of visited nodes. When we find a root of a component, all
		// of the nodes on this list up to the root form a strongly connected component.

		// Handle all of the graph's nodes.
		for (N node : graph.getNodes()) {
			if (!dfsNumbers.containsKey(node)) {
				counter = handleStronglyConnectedComponents(node, result, dfsNumbers, minNumbers,
						counter);
			}
		}

		return result;
	}

	/*
	 * Compute the strongly connected components reachable from node.
	 * No, I will not explain the parameters.
	 */
	private static <N extends INode<?, ?, N>> int handleStronglyConnectedComponents(N node, Set<Set<N>> result,
			Map<N, Integer> dfsNumbers, Map<N, Integer> minNumbers, int counter) {
		Deque<N> callers = new ArrayDeque<>();
		Deque<N> stack = new ArrayDeque<>();
		Set<N> stackAsSet = new HashSet<>();

		counter = visitNode(node, dfsNumbers, minNumbers, counter, stack, stackAsSet);
		do {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			boolean done = true;
			for (N current : node.getPostsetNodes()) {
				if (!dfsNumbers.containsKey(current)) {
					// 'current' was not visited yet
					callers.addLast(node);
					node = current;

					counter = visitNode(node, dfsNumbers, minNumbers, counter, stack, stackAsSet);
					done = false;
					break;
				} else if (stackAsSet.contains(current)
						&& minNumbers.get(node) > minNumbers.get(current)) {
					// Set our own minNumbers to current's depth search number if it is smaller
					minNumbers.put(node, Math.min(minNumbers.get(node), dfsNumbers.get(current)));
				}
			}

			if (done) {
				if (dfsNumbers.get(node).equals(minNumbers.get(node))) {
					// We are the root of the current component, let's get it from the stack. All of
					// the nodes on the stack up to the current node form a strongly connected
					// component.
					Set<N> component = new HashSet<>();
					N cur = null;

					while (cur != node) {
						cur = stack.removeLast();
						boolean removed = stackAsSet.remove(cur);
						assert removed;
						component.add(cur);
					}
					result.add(component);
				}

				N next = callers.pollLast();
				if (next != null)
					// Set our own minNumber to current's number if that one is smaller
					minNumbers.put(next, Math.min(minNumbers.get(next), minNumbers.get(node)));
				node = next;
			}
		} while (node != null);

		assert callers.isEmpty();
		assert stack.isEmpty();
		assert stackAsSet.isEmpty();
		return counter;
	}

	/*
	 * Update the data structures for the visit of a new node.
	 * Nope, the parameters aren't explained here either.
	 */
	private static <N extends INode<?, ?, ?>> int visitNode(N node, Map<N, Integer> dfsNumbers,
			Map<N, Integer> minNumbers, int counter, Deque<N> stack, Set<N> stackAsSet) {
		// Node should not have been visited before.
		assert !dfsNumbers.containsKey(node);
		assert !minNumbers.containsKey(node);

		counter++;
		dfsNumbers.put(node, counter);
		minNumbers.put(node, counter);
		stack.add(node);
		boolean added = stackAsSet.add(node);
		assert added;

		return counter;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
