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

package uniol.apt.adt;

import uniol.apt.adt.extension.Extensible;

/**
 * Represents an edge of type E in a graph of type G with nodes of type N having a id of the source node and target
 * node.
 * @param <G> the graph the edge belongs to.
 * @param <E> the edge class itself.
 * @param <N> the node class belonging to the graph.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Edge<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends Extensible implements IEdge<G, E, N> {

	protected final G graph;
	protected final N source;
	protected final N target;

	/**
	 * Constructor for creating an arc with the sourceId and targetId in the given graph.
	 * @param graph  The graph this Edge belongs to.
	 * @param source the source node.
	 * @param target the target node.
	 */
	protected Edge(G graph, N source, N target) {
		this.graph = graph;
		this.source = source;
		this.target = target;
	}

	/**
	 * Constructor for copying an arc. The constructor also copies the references of the extensions.
	 * @param graph the graph this edge belongs to
	 * @param e     the edge that should get copied.
	 */
	protected Edge(G graph, Edge<G, E, N> e) {
		this(graph, graph.getNode(e.source.getId()), graph.getNode(e.target.getId()));
		copyExtensions(e);
	}

	@Override
	public G getGraph() {
		return this.graph;
	}

	@Override
	public N getSource() {
		return this.source;
	}

	@Override
	public N getTarget() {
		return this.target;
	}

	@Override
	public String toString() {
		return source.getId() + "->" + target.getId();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
