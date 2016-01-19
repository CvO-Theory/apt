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

package uniol.apt.adt.exception;

import uniol.apt.adt.IGraph;

/**
 * The NoSuchEdgeException is thrown if the edge, which is tried to achieve, does not exists.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class NoSuchEdgeException extends DatastructureException {

	public static final long serialVersionUID = 0xdeadbeef00000003l;

	/**
	 * Constructor creates a new NoSuchEdgeException.
	 * @param g        The graph where the node does not exist.
	 * @param sourceId Name of the source node of the non existing edge.
	 * @param targetId Name of the target node of the non existing edge.
	 */
	public NoSuchEdgeException(IGraph<?, ?, ?> g, String sourceId, String targetId) {
		super("Edge '" + sourceId + " --> " + targetId + "' does not exist in graph '" + g.getName() + "'");
	}

	/**
	 * Constructor creates a new NoSuchEdgeException.
	 * @param g        The graph where the node does not exist.
	 * @param sourceId Name of the source node of the non existing edge.
	 * @param targetId Name of the target node of the non existing edge.
	 * @param label    Label of the non existing edge.
	 */
	public NoSuchEdgeException(IGraph<?, ?, ?> g, String sourceId, String targetId, String label) {
		super("Edge '" + sourceId + " -" + label + "-> " + targetId
			+ "' does not exist in graph '" + g.getName() + "'");
	}

	/**
	 * Constructor creates a new NoSuchEdgeException with a given cause.
	 * @param g        The graph where the node does not exist.
	 * @param sourceId Name of the source node of the non existing edge.
	 * @param targetId Name of the target node of the non existing edge.
	 * @param cause    The cause for this exception as Throwable.
	 */
	public NoSuchEdgeException(IGraph<?, ?, ?> g, String sourceId, String targetId, Throwable cause) {
		super("Edge '" + sourceId + " --> " + targetId
			+ "' does not exist in graph '" + g.getName() + "'", cause);
	}

	/**
	 * Constructor creates a new NoSuchEdgeException.
	 * @param g        The graph where the node does not exist.
	 * @param sourceId Name of the source node of the non existing edge.
	 * @param targetId Name of the target node of the non existing edge.
	 * @param label    Label of the non existing edge.
	 * @param cause    The cause for this exception as Throwable.
	 */
	public NoSuchEdgeException(IGraph<?, ?, ?> g, String sourceId, String targetId, String label, Throwable cause) {
		super(
			"Edge '" + sourceId + " -" + label + "-> " + targetId
			+ "' does not exist in graph '" + g.getName() + "'", cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
