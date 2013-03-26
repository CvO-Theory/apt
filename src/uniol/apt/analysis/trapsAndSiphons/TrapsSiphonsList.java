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

package uniol.apt.analysis.trapsAndSiphons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import uniol.apt.adt.pn.Place;

/**
 * This class represents a list of siphons or traps of a Petri net. It is needed for the module system.
 * @author Uli Schlachter
 */
public class TrapsSiphonsList extends ArrayList<Set<Place>> {
	public static final long serialVersionUID = 0x1l;

	/**
	 * This constructor creates an empty trap/siphon list.
	 */
	public TrapsSiphonsList() {
	}

	/**
	 * This constructor creates a new trap/siphon list and fills it with the
	 * elements of the given set.
	 * @param c The trap/siphon list that should be copied.
	 */
	public TrapsSiphonsList(Collection<? extends Set<Place>> c) {
		super(c);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
