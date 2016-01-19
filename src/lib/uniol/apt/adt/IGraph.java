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
 * {@link IGraph} is an abstract interface to a common graph. It is possible to retrieve the current nodes and edges of
 * this graph or a single node by its id. Furthermore it provides the possibility the retrieve the preset and postset
 * nodes or edges of a node.
 * The generic parameter define what types are used for the edges and nodes as well as the graph itself. This allows to
 * extend any graph in a typesafe manner at compiletime. Additionally one may add any data to the graph or one of its
 * components by using the IExtensible interface methods at runtime.
 * For examples have a look at the pn.PetriNet or ts.TransitionSystem.
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public interface IGraph<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends IExtensible {

	/**
	 * Add a listener to this graph. The listener will be invoked on changes to the graph.
	 * @param listener The listener to add.
	 * @return true if this listener was added, false if it was already added before.
	 */
	public boolean addListener(IGraphListener<G, E, N> listener);

	/**
	 * Remove a listener from this graph.
	 * @param listener The listener to remove.
	 * @return true if this listener was removed, false if it was not added before.
	 */
	public boolean removeListener(IGraphListener<G, E, N> listener);

	/**
	 * Retrieves the name of this graph or "" if not set.
	 * @return a String containing the name.
	 */
	public String getName();

	/**
	 * Retrieves the node with the given id.
	 * @param id The id of the node as a String.
	 * @return A reference object of type N to the node.
	 */
	public N getNode(String id);

	/**
	 * Retrieves a view of the current edge set of the graph.
	 * @return A unmodifiable set containing edges of given type E.
	 */
	public Set<E> getEdges();

	/**
	 * Retrieves a view of the current node set of the graph.
	 * @return A unmodifiable set containing nodes of given type N.
	 */
	public Set<N> getNodes();

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPresetNodes(String id);

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the given node.
	 * @param node The node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPresetNodes(N node);

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPostsetNodes(String id);

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the given node.
	 * @param node The node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<N> getPostsetNodes(N node);

	/**
	 * Retrieves a view of all edges targeting the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPresetEdges(String id);

	/**
	 * Retrieves a view of all edges targeting the given node.
	 * @param n The node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPresetEdges(N n);

	/**
	 * Retrieves a view of all edges beginning in the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPostsetEdges(String id);

	/**
	 * Retrieves a view of all edges beginning in the given node.
	 * @param n The node.
	 * @return A unmodifiable set of nodes of type N.
	 */
	public Set<E> getPostsetEdges(N n);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
