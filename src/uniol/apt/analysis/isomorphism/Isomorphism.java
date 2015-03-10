/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2015  Members of the project group APT
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

package uniol.apt.analysis.isomorphism;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import uniol.apt.adt.ts.State;

/**
 * This class represents an isomorphism. It is needed for the module system.
 * @author Uli Schlachter
 */
public class Isomorphism extends DualHashBidiMap<State, State> {
	public static final long serialVersionUID = 0x1l;

	/**
	 * Construct a Isomorphism from the given map.
	 * @param m The map to copy.
	 */
	public Isomorphism(BidiMap<? extends State, ? extends State> m) {
		super(m);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
