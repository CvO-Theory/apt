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
import uniol.apt.adt.ts.ArcKey;

/**
 * The ArcExistsException is thrown if an arc is added to a TS where an other arc with the same source, target and label
 * already exists
 * @author Manuel Gieseking
 */
public class ArcExistsException extends DatastructureException {

	public static final long serialVersionUID = 0xdeadbeef00000008l;

	/**
	 * Constructor creates a new ArcExistsException for a given Graph and a given ArcKey.
	 * @param g   The graph where the arc already exists.
	 * @param key The ArcKey which already exists.
	 */
	public ArcExistsException(IGraph<?, ?, ?> g, ArcKey key) {
		super("Arc with sourceId '" + key.getSourceId() + "', targetId '"
			+ key.getTargetId() + "' and label '" + key.getLabel()
			+ "' already exists in graph '" + g.getName() + "'");
	}

	/**
	 * Constructor creates a new ArcExistsException for a given Graph and the given ArcKey.
	 * @param g     The graph where the arc already exists.
	 * @param key   The ArcKey which already exists.
	 * @param cause The cause for this exception as Throwable.
	 */
	public ArcExistsException(IGraph<?, ?, ?> g, ArcKey key, Throwable cause) {
		super("Arc with sourceId '" + key.getSourceId() + "', targetId '"
			+ key.getTargetId() + "' and label '" + key.getLabel()
			+ "' already exists in graph '" + g.getName() + "'", cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
