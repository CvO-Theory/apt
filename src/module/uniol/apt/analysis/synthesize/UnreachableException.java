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

package uniol.apt.analysis.synthesize;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;

/**
 * An UnreachableException is thrown when some calculations are done on a node which require this node to be reachable
 * from some other (initial) state.
 * @author Uli Schlachter
 */
public class UnreachableException extends Exception {
	public static final long serialVersionUID = 0L;

	/**
	 * Constructor that creates a UnreachableException for the given details.
	 * @param graph The graph in which a node was unreachable.
	 * @param node The unreachable node.
	 * @param <G> The type of the graph in which some node was unreachable.
	 * @param <N> The type of nodes in the graph.
	 */
	public <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>> UnreachableException(G graph, N node) {
		super("Node " + node.toString() + " is unreachable in " + graph.toString());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
