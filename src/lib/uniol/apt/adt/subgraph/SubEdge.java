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

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.extension.Extensible;

/**
 * A SubEdge is an edge of a SubGraph. See {@link SubGraph} for more details.
 * @param <G> The type of the graph of which we are a subgraph.
 * @param <E> The type of edges of the graph of which we are a subgraph.
 * @param <N> The type of nodes of the graph of which we are a subgraph.
 * @author Uli Schlachter
 */
public class SubEdge<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
		extends Extensible
		implements IEdge<SubGraph<G, E, N>, SubEdge<G, E, N>, SubNode<G, E, N>> {

	private final SubGraph<G, E, N> graph;
	private final E originalEdge;

	/**
	 * Constructor
	 * @param graph The subgraph of which we are part.
	 * @param originalEdge The edge that is wrapped.
	 */
	SubEdge(SubGraph<G, E, N> graph, E originalEdge) {
		this.graph = graph;
		this.originalEdge = originalEdge;
	}

	/**
	 * Get the edge of the original graph that this edge represents.
	 * @return The original edge
	 */
	public E getOriginalEdge() {
		return originalEdge;
	}

	@Override
	public SubGraph<G, E, N> getGraph() {
		return graph;
	}

	@Override
	public SubNode<G, E, N> getSource() {
		return graph.getNode(originalEdge.getSource().getId());
	}

	@Override
	public SubNode<G, E, N> getTarget() {
		return graph.getNode(originalEdge.getTarget().getId());
	}

	@Override
	public int hashCode() {
		return originalEdge.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof SubEdge) {
			SubEdge<?, ?, ?> other = (SubEdge<?, ?, ?>) o;
			return Objects.equals(graph, other.graph)
				&& Objects.equals(originalEdge, other.originalEdge);
		}
		return false;
	}

	@Override
	public String toString() {
		return originalEdge.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
