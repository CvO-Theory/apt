/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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

package uniol.apt.adt.subgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.IGraphListener;
import uniol.apt.adt.INode;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.extension.Extensible;

/**
 * A subgraph represents a part of another graph and is defined by a set of nodes of that graph. The subgraph contains
 * exactly that set of nodes and all edges between this nodes.
 *
 * Note that this graph has to create wrappers around the nodes and edges of the original graph. Use {@link
 * SubEdge#getOriginalEdge} and {@link SubNode#getOriginalNode} to get the edge respectively node of the original graph
 * that is represented by such a wrapper object.
 *
 * @param <G> The type of the graph of which we are a subgraph.
 * @param <E> The type of edges of the graph of which we are a subgraph.
 * @param <N> The type of nodes of the graph of which we are a subgraph.
 * @author Uli Schlachter
 */
public class SubGraph<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
		extends Extensible
		implements IGraph<SubGraph<G, E, N>, SubEdge<G, E, N>, SubNode<G, E, N>> {

	private final G originalGraph;
	private final Set<String> nodeIDs;

	/**
	 * Create a new sub graph for the given set of nodes.
	 * @param graph The graph from which to create a subgraph.
	 * @param nodes The set of nodes that should be used.
	 * @return The subgraph.
	 * @param <G> The type of the graph of which we are a subgraph.
	 * @param <E> The type of edges of the graph of which we are a subgraph.
	 * @param <N> The type of nodes of the graph of which we are a subgraph.
	 */
	public static <G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
			SubGraph<G, E, N> getSubGraphByNodes(G graph, Collection<N> nodes) {
		Set<String> ids = new HashSet<>();
		for (N node : nodes) {
			if (!node.getGraph().equals(graph))
				throw new IllegalArgumentException(
						"Node " + node + " does not belong to the graph " + graph);
			ids.add(node.getId());
		}
		return new SubGraph<G, E, N>(graph, ids);
	}

	/**
	 * Create a new sub graph for the given set of nodes.
	 * @param graph The graph from which to create a subgraph.
	 * @param ids The set of node IDs that should be used.
	 * @return The subgraph.
	 * @param <G> The type of the graph of which we are a subgraph.
	 * @param <E> The type of edges of the graph of which we are a subgraph.
	 * @param <N> The type of nodes of the graph of which we are a subgraph.
	 */
	public static <G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
			SubGraph<G, E, N> getSubGraphByNodeIDs(G graph, Collection<String> ids) {
		for (String id : ids)
			if (graph.getNode(id) == null)
				throw new IllegalArgumentException("No node with id " + id + " in graph " + graph);
		return new SubGraph<G, E, N>(graph, new HashSet<>(ids));
	}

	private SubGraph(G originalGraph, Set<String> nodeIDs) {
		this.originalGraph = originalGraph;
		this.nodeIDs = nodeIDs;
	}

	/**
	 * Create a new sub graph for the given set of nodes.
	 * Instead of creating a subgraph of this subgraph instance, this method will create a subgraph of the original
	 * graph. So, this is equivalent to <code>getSubGraphByNodes(getOriginalGraph(), nodes)</code>. However, the
	 * nodes must belong to this subgraph and not to the original graph.
	 * @param nodes The set of nodes that should be used.
	 * @return The subgraph.
	 */
	public SubGraph<G, E, N> getFlatSubGraphByNodes(Collection<SubNode<G, E, N>> nodes) {
		Set<String> ids = new HashSet<>();
		for (SubNode<G, E, N> node : nodes) {
			if (!node.getGraph().equals(this))
				throw new IllegalArgumentException(
						"Node " + node + " does not belong to the graph " + this);
			ids.add(node.getId());
		}
		return new SubGraph<G, E, N>(originalGraph, ids);
	}

	/**
	 * Create a new sub graph for the given set of nodes.
	 * Instead of creating a subgraph of this subgraph instance, this method will create a subgraph of the original
	 * graph. So, this is equivalent to <code>getSubGraphByNodeIDs(getOriginalGraph(), ids)</code>.
	 * @param ids The set of node IDs that should be used.
	 * @return The subgraph.
	 */
	public SubGraph<G, E, N> getFlatSubGraphByNodeIDs(Collection<String> ids) {
		Set<String> notExistentIDs = new HashSet<>(ids);
		notExistentIDs.removeAll(this.nodeIDs);
		for (String id : notExistentIDs)
			throw new IllegalArgumentException("No node with id " + id + " in graph " + this);
		return new SubGraph<G, E, N>(originalGraph, new HashSet<>(ids));
	}

	/**
	 * Get the original graph of which we are a subgraph.
	 * @return The original graph.
	 */
	public G getOriginalGraph() {
		return originalGraph;
	}

	/**
	 * Get the identifiers of the nodes that are contained in this subgraph.
	 * @return The node identifiers.
	 */
	public Set<String> getNodeIDs() {
		return Collections.unmodifiableSet(nodeIDs);
	}

	@Override
	public boolean addListener(IGraphListener<SubGraph<G, E, N>, SubEdge<G, E, N>, SubNode<G, E, N>> listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeListener(IGraphListener<SubGraph<G, E, N>, SubEdge<G, E, N>, SubNode<G, E, N>> listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return originalGraph.getName();
	}

	/**
	 * Get the representation of some node in this SubGraph.
	 * @param node The node whose representation should be returned.
	 * @return The node's representation
	 */
	public SubNode<G, E, N> getNode(N node) {
		checkNode(node);
		return new SubNode<G, E, N>(this, node);
	}

	@Override
	public SubNode<G, E, N> getNode(String id) {
		checkNode(id);
		N node = originalGraph.getNode(id);
		if (node == null)
			return null;
		return new SubNode<G, E, N>(this, node);
	}

	@Override
	public Set<SubEdge<G, E, N>> getEdges() {
		return filterEdges(originalGraph.getEdges());
	}

	@Override
	public Set<SubNode<G, E, N>> getNodes() {
		return filterNodes(originalGraph.getNodes());
	}

	@Override
	public Set<SubNode<G, E, N>> getPresetNodes(String id) {
		checkNode(id);
		return filterNodes(originalGraph.getPresetNodes(id));
	}

	@Override
	public Set<SubNode<G, E, N>> getPresetNodes(SubNode<G, E, N> node) {
		checkNode(node);
		return filterNodes(originalGraph.getPresetNodes(node.getOriginalNode()));
	}

	@Override
	public Set<SubNode<G, E, N>> getPostsetNodes(String id) {
		checkNode(id);
		return filterNodes(originalGraph.getPostsetNodes(id));
	}

	@Override
	public Set<SubNode<G, E, N>> getPostsetNodes(SubNode<G, E, N> node) {
		checkNode(node);
		return filterNodes(originalGraph.getPostsetNodes(node.getOriginalNode()));
	}

	@Override
	public Set<SubEdge<G, E, N>> getPresetEdges(String id) {
		checkNode(id);
		return filterEdges(originalGraph.getPresetEdges(id));
	}

	@Override
	public Set<SubEdge<G, E, N>> getPresetEdges(SubNode<G, E, N> node) {
		checkNode(node);
		return filterEdges(originalGraph.getPresetEdges(node.getOriginalNode()));
	}

	@Override
	public Set<SubEdge<G, E, N>> getPostsetEdges(String id) {
		checkNode(id);
		return filterEdges(originalGraph.getPostsetEdges(id));
	}

	@Override
	public Set<SubEdge<G, E, N>> getPostsetEdges(SubNode<G, E, N> node) {
		checkNode(node);
		return filterEdges(originalGraph.getPostsetEdges(node.getOriginalNode()));
	}

	@Override
	public int hashCode() {
		return originalGraph.hashCode() + nodeIDs.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof SubGraph) {
			SubGraph<?, ?, ?> other = (SubGraph<?, ?, ?>) o;
			return Objects.equals(originalGraph, other.originalGraph)
				&& Objects.equals(nodeIDs, other.nodeIDs);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("SubGraph of '%s' with nodes %s", originalGraph, nodeIDs);
	}

	private Set<SubNode<G, E, N>> filterNodes(Set<N> nodes) {
		Set<SubNode<G, E, N>> result = new HashSet<>();
		for (N node : nodes)
			if (nodeIDs.contains(node.getId()))
				result.add(new SubNode<G, E, N>(this, node));
		return result;
	}

	private Set<SubEdge<G, E, N>> filterEdges(Set<E> edges) {
		Set<SubEdge<G, E, N>> result = new HashSet<>();
		for (E edge : edges)
			if (nodeIDs.contains(edge.getSource().getId()) &&
					nodeIDs.contains(edge.getTarget().getId()))
				result.add(new SubEdge<G, E, N>(this, edge));
		return result;
	}

	private void checkNode(String id) {
		if (!nodeIDs.contains(id))
			throw new NoSuchNodeException(this, id);
	}

	private void checkNode(N node) {
		if (!node.getGraph().equals(originalGraph))
			throw new NoSuchNodeException(this, node.getId());
		checkNode(node.getId());
	}

	private void checkNode(SubNode<G, E, N> node) {
		if (!node.getGraph().equals(this))
			throw new NoSuchNodeException(this, node.getId());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
