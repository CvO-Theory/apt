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

import java.util.Set;
import org.apache.commons.collections4.IteratorUtils;
import uniol.apt.adt.extension.Extensible;

/**
 * An implementation of the interface INode for just delegating the pre- and postset methodes to the graph of the type
 * G.
 * @param <G> The type of the graph.
 * @param <E> The type of the edge itself.
 * @param <N> The type of the nodes.
 * @see INode
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public abstract class Node<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends Extensible implements INode<G, E, N> {

	protected final G graph;
	protected final String id;

	/**
	 * Constructor for creating a Node
	 * @param graph the graph this node belongs to.
	 * @param id    the id this node should have.
	 */
	protected Node(G graph, String id) {
		this.graph = graph;
		this.id = id;
	}

	/**
	 * Constructor for copying a Node. The constructor also copies the references of the extensions.
	 * @param graph the graph this node belongs to.
	 * @param n     the node that should get copied.
	 */
	protected Node(G graph, Node<G, E, N> n) {
		this(graph, n.getId());
		copyExtensions(n);
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public G getGraph() {
		return this.graph;
	}

	@Override
	public Set<N> getPresetNodes() {
		return this.graph.getPresetNodes(this.id);
	}

	@Override
	public Set<N> getPostsetNodes() {
		return this.graph.getPostsetNodes(this.id);
	}

	@Override
	public Set<E> getPresetEdges() {
		return this.graph.getPresetEdges(this.id);
	}

	@Override
	public Set<E> getPostsetEdges() {
		return this.graph.getPostsetEdges(this.id);
	}

	/**
	 * Retrieves a view of all edges connected to this node.
	 * @return An unmodifable iterable of edges of given type E.
	 */
	public Iterable<E> getNeighboringEdges() {
		return IteratorUtils.asIterable(IteratorUtils.chainedIterator(
					getPresetEdges().iterator(), getPostsetEdges().iterator()));
	}

	@Override
	public String toString() {
		return "Node{" + "id=" + id + '}';
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
