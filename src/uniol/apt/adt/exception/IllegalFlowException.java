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
 * The IllegalFlowException is thrown if a flow is added between two places or two transitions.
 * @author Uli Schlachter
 */
public class IllegalFlowException extends DatastructureException {
	public static final long serialVersionUID = 1;

	/**
	 * Constructor creates a new IllegalFlowException for a given Graph and EdgeKey.
	 * @param g   The graph where an illegal flow was constructed.
	 * @param key The EdgeKey corresponding to the illegal flow.
	 */
	public IllegalFlowException(IGraph<?, ?, ?> g, EdgeKey key) {
		super("Cannot create flow with sourceId '" + key.getSourceId() + "' and targetId '"
			+ key.getTargetId() + "' in graph '" + g.getName() + "', because "
			+ "a flow can only be created between a place and a transition.");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
