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
 * IGraph<G,E,N> is an abstract interface to a common graph. It is possible to retrieve the current nodes and edges of
 * this graph or a single node by its id. Furthermore it provides the possibility the retrieve the preset and postset
 * nodes or edges of a node.
 * <p/>
 * The generic parameter define what types are used for the edges and nodes as well as the graph itself. This allows to
 * extend any graph in a typesafe manner at compiletime. Additionally one may add any data to the graph or one of its
 * components by using the IExtensible interface methods at runtime.
 * <p/>
 * For examples have a look at the pn.PetriNet or ts.TransitionSystem.
 * <p/>
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public interface IGraph<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends IExtensible {

	/**
	 * Retrieves the name of this graph or "" if not set.
	 * <p/>
	 * @return a String containing the name.
	 */
	public String getName();

	/**
	 * Retrieves the node with the given id.
	 * <p/>
	 * @param id The id of the node as a String.
	 * <p/>
	 * @return A reference object of type N to the node.
	 */
	public N getNode(String id);

	/**
	 * Retrieves a view of the current edge set of the graph.
	 * <p/>
	 * @return A unmodifiable set containing edges of given type E.
	 */
	public Set<E> getEdges();

	/**
	 * Retrieves a view of the current node set of the graph.
	 * <p/>
	 * @return A unmodifiable set containing nodes of given type N.
	 */
	public Set<N> getNodes();

	/**
	 * Retrieves the 'first' node with the given @a extension value.
	 * @param key The key of the extended attribute.
	 * @param value The value of the extended attribute.
	 * @return The node of given type N, or null when not found.
	 */
	public N getNodeByExtension(String key, Object value);
	
	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the node with the given id.
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPresetNodes(String id);

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the given node.
	 * <p/>
	 * @param node The node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPresetNodes(N node);

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the node with the given id.
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPostsetNodes(String id);

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the given node.
	 * <p/>
	 * @param node The node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPostsetNodes(N node);

	/**
	 * Retrieves a view of all edges targeting the node with the given id.
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPresetEdges(String id);

	/**
	 * Retrieves a view of all edges targeting the given node.
	 * <p/>
	 * @param n The node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPresetEdges(N n);

	/**
	 * Retrieves a view of all edges beginning in the node with the given id.
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPostsetEdges(String id);

	/**
	 * Retrieves a view of all edges beginning in the given node.
	 * <p/>
	 * @param n The node.
	 * <p/>
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPostsetEdges(N n);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
