/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

/**
 * A class can implement this interface to be informed about changes to a graph.
 * @see IGraph#addListener
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * @author Uli Schlachter
 */
public interface IGraphListener<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> {
	/**
	 * This method is called on any structural changes to a graph. The exact definition of what constitutes a
	 * structural change depends on the concrete implementation of the graph.
	 * @param graph The graph that was changed. This listener was previously added to this graph.
	 * @return True if this listener should stay active, false if it should be removed.
	 */
	boolean changeOccurred(IGraph<G, E, N> graph);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
