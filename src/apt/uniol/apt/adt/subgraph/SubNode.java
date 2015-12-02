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

import java.util.Objects;
import java.util.Set;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.extension.Extensible;

/**
 * A SubNode is a node of a SubGraph. See {@link SubGraph} for more details.
 * @param <G> The type of the graph of which we are a subgraph.
 * @param <E> The type of edges of the graph of which we are a subgraph.
 * @param <N> The type of nodes of the graph of which we are a subgraph.
 * @author Uli Schlachter
 */
public class SubNode<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
		extends Extensible
		implements INode<SubGraph<G, E, N>, SubEdge<G, E, N>, SubNode<G, E, N>> {

	private final SubGraph<G, E, N> graph;
	private final N originalNode;

	/**
	 * Constructor
	 * @param graph The subgraph of which we are part.
	 * @param originalNode The node that is wrapped.
	 */
	SubNode(SubGraph<G, E, N> graph, N originalNode) {
		this.graph = graph;
		this.originalNode = originalNode;
	}

	/**
	 * Get the node of the original graph that this node represents.
	 * @return The original node
	 */
	public N getOriginalNode() {
		return originalNode;
	}

	@Override
	public String getId() {
		return originalNode.getId();
	}

	@Override
	public SubGraph<G, E, N> getGraph() {
		return graph;
	}

	@Override
	public Set<SubNode<G, E, N>> getPresetNodes() {
		return graph.getPresetNodes(this);
	}

	@Override
	public Set<SubNode<G, E, N>> getPostsetNodes() {
		return graph.getPostsetNodes(this);
	}

	@Override
	public Set<SubEdge<G, E, N>> getPresetEdges() {
		return graph.getPresetEdges(this);
	}

	@Override
	public Set<SubEdge<G, E, N>> getPostsetEdges() {
		return graph.getPostsetEdges(this);
	}

	@Override
	public int hashCode() {
		return originalNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof SubNode) {
			SubNode<?, ?, ?> other = (SubNode<?, ?, ?>) o;
			return Objects.equals(graph, other.graph)
				&& Objects.equals(originalNode, other.originalNode);
		}
		return false;
	}

	@Override
	public String toString() {
		return originalNode.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
