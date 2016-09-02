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
import uniol.apt.adt.extension.IExtensible;

/**
 * {@link INode} is an abstract interface to a common node. It is possible to
 * retrieve a view of the pre- and postsets as edge sets as well as node sets.
 *
 * <p>The generic parameter define what types are used for the graph and edges as
 * well as the node itself. This allows to extend any node in a typesafe manner
 * at compiletime. Additionally one may add any data to the node using the
 * IExtensible interface methods at runtime.
 *
 * <p>For examples have a look at the pn.Place, pn.Transition, ts.Transition or
 * Node classes.
 *
 * @param <G>
 *                The type of the graph.
 * @param <E>
 *                The type of the edge itself.
 * @param <N>
 *                The type of the nodes.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public interface INode<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends IExtensible {

	/**
	 * Returns the id of the node.
	 * @return A string containing the id.
	 */
	public String getId();

	/**
	 * Retrieves the graph this node is part of.
	 * @return A graph of given type G.
	 */
	public G getGraph();

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to this node.
	 * @return A unmodifiable set of nodes of given type N.
	 */
	public Set<N> getPresetNodes();

	/**
	 * Retrieves a view of all nodes which have an incoming edge from this node.
	 * @return A unmodifiable set of nodes of given type N.
	 */
	public Set<N> getPostsetNodes();

	/**
	 * Retrieves a view of all edges targeting this node.
	 * @return A unmodifiable set of edges of given type E.
	 */
	public Set<E> getPresetEdges();

	/**
	 * Retrieves a view of all edges beginning in this node.
	 * @return A unmodifiable set of edges of given type E.
	 */
	public Set<E> getPostsetEdges();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
