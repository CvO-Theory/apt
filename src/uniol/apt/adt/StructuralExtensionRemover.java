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
 * This class listens to changes to a graph and removes a given extension from the graph when any structural change
 * occurs.
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * @author Uli Schlachter
 */
public class StructuralExtensionRemover<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	implements IGraphListener<G, E, N> {
	private final String key;

	/**
	 * Constructor
	 * @param key The extension key that should be removed on changes.
	 */
	public StructuralExtensionRemover(String key) {
		this.key = key;
	}

	@Override
	public boolean changeOccurred(IGraph<G, E, N> graph) {
		graph.removeExtension(key);
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
