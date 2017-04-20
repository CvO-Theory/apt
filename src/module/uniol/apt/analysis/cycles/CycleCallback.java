/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

package uniol.apt.analysis.cycles;

import java.util.List;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;

/**
 * Callback which gets used by the {@link CycleSearch} class to notify the
 * caller about cycles.
 * @param <G> The type of the graph.
 * @param <E> The type of edges of the graph.
 * @param <N> The type of nodes of the graph.
 *
 * @author vsp
 */
public interface CycleCallback<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> {
	/**
	 * This methods gets executed once for each cycle found
	 * @param nodes List of nodes which form the cycle.
	 * @param edges List of edges which form the cycle, the first edge in this list will be between the first and
	 * the second node from the node list.
	 */
	public void cycleFound(List<N> nodes, List<E> edges);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
