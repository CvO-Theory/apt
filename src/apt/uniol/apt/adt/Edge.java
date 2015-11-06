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
 * <p/>
 * @param <G> the graph the edge belongs to.
 * @param <E> the edge class itself.
 * @param <N> the node class belonging to the graph.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Edge<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends Extensible implements IEdge<G, E, N> {

	protected final G graph;
	protected final String sourceId, targetId;

	/**
	 * Constructor for creating an arc with the sourceId and targetId in the given graph.
	 * <p/>
	 * @param graph    The graph this Edge belongs to.
	 * @param sourceId the source node's id.
	 * @param targetId the target node's id.
	 */
	protected Edge(G graph, String sourceId, String targetId) {
		this.graph = graph;
		this.sourceId = sourceId;
		this.targetId = targetId;
	}

	/**
	 * Constructor for copying an arc. The constructor also copies the references of the extensions.
	 * <p/>
	 * @param graph the graph this edge belongs to
	 * @param e     the edge that should get copied.
	 */
	protected Edge(G graph, Edge<G, E, N> e) {
		this(graph, e.sourceId, e.targetId);
		copyExtensions(e);
	}

	@Override
	public G getGraph() {
		return this.graph;
	}

	@Override
	public N getSource() {
		return this.graph.getNode(this.sourceId);
	}

	@Override
	public N getTarget() {
		return this.graph.getNode(this.targetId);
	}

	@Override
	public String toString() {
		return sourceId + "->" + targetId;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
