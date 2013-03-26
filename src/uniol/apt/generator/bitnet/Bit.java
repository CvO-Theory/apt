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

package uniol.apt.generator.bitnet;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import java.util.List;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Class containig places and transitions representing a bit which has
 * been generated from a BitNetGenerator.
 *
 * @author vsp
 */
public class Bit {
	private final List<Place> places;
	private final List<Transition> transitions;

	/**
	 * Constructor
	 *
	 * @param places Array of places representing the states of this bit in ascending order
	 * @param transitions Array of transitions of this bit
	 */
	Bit(Place[] places, Transition[] transitions) {
		this.places      = unmodifiableList(asList(places));
		this.transitions = unmodifiableList(asList(transitions));
	}

	/**
	 * Get the places representing the states of this bits
	 *
	 * @return Array of places representing the states of this bit in ascending order
	 */
	public List<Place> getPlaces() {
		return places;
	}

	/**
	 * Get the transitions of this bit
	 *
	 * @return Array of transitions of this bit
	 */
	public List<Transition> getTransitions() {
		return transitions;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
