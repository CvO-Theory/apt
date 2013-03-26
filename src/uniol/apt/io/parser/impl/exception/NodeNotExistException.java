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

package uniol.apt.io.parser.impl.exception;

/**
 * An exception thrown if it is tried to use a node which do not belong to the graph.
 * <p/>
 * @author Manuel Gieseking
 */
public class NodeNotExistException extends FormatException {

	private static final long serialVersionUID = 1L;
	private String nodeId;

	/**
	 * Creates a new NodeNotExistsException with a given id of a node which is responsible for this exception.
	 * <p/>
	 * @param nodeId - the id of the node which do not exists.
	 */
	public NodeNotExistException(String nodeId) {
		super("Node " + nodeId + " does not exist.");
		this.nodeId = nodeId;
	}

	/**
	 * Returns the id of the node which do not exists.
	 * <p/>
	 * @return the id of the node which do not exists.
	 */
	public String getNodeId() {
		return nodeId;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
