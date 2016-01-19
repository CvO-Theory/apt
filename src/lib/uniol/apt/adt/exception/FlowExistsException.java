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

import uniol.apt.adt.EdgeKey;
import uniol.apt.adt.IGraph;

/**
 * The FlowExistsException is thrown if an flow is added to a petri net where an other flow with the same source, target
 * already exists.
 * @author Manuel Gieseking
 */
public class FlowExistsException extends DatastructureException {

	public static final long serialVersionUID = 0xdeadbeef00000008l;

	/**
	 * Constructor creates a new FlowExistsException for a given Graph and EdgeKey.
	 * @param g   The graph where the flow already exists.
	 * @param key The EdgeKey which already exists.
	 */
	public FlowExistsException(IGraph<?, ?, ?> g, EdgeKey key) {
		super("Arc with sourceId '" + key.getSourceId() + "' and targetId '"
			+ key.getTargetId() + "' already exists in graph '" + g.getName() + "'");
	}

	/**
	 * Constructor creates a new FlowExistsException for a given Graph and EdgeKey.
	 * @param g     The graph where the flow already exists.
	 * @param key   The Edgekey which already exists.
	 * @param cause The cause for this exception as Throwable.
	 */
	public FlowExistsException(IGraph<?, ?, ?> g, EdgeKey key, Throwable cause) {
		super("Arc with sourceId '" + key.getSourceId() + "'and targetId '"
			+ key.getTargetId() + "' already exists in graph '" + g.getName() + "'", cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
