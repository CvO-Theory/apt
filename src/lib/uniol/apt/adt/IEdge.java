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

import uniol.apt.adt.extension.IExtensible;

/**
 * {@link IEdge} is an abstract interface to a common directed edge. It is possible to set as well as retrieve
 * the source and target nodes as well as to retrieve the parenting graph.
 * The generic parameter define what types are used for the graph and nodes as well as the edge itself. This allows to
 * extend any edge in a typesafe manner at compiletime. Additionally one may add any data to the edge using the
 * IExtensible interface methods at runtime.
 * For examples have a look at the Edge, pn.Flow or ts.Arc classes.
 * @param <G> The type of the graph.
 * @param <E> The type of the edge itself.
 * @param <N> The type of the nodes.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public interface IEdge<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends IExtensible {

	/**
	 * Retrieves the graph this edge is part of.
	 * @return A graph of given type G.
	 */
	public G getGraph();

	/**
	 * Retrieves the source node of this directed edge.
	 * @return A node of given type N.
	 */
	public N getSource();

	/**
	 * Retrieves the target node of this directed edge.
	 * @return A node of given type N.
	 */
	public N getTarget();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
