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

package uniol.apt.generator.philnet;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import java.util.List;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Class containig places and transitions representing one philosopher which has
 * been generated from a PhilNetGenerator.
 *
 * @author vsp
 */
public class Philosopher {
	private final Place fork;
	private final List<Place> places;
	private final List<Transition> transitions;

	/**
	 * Constructor
	 *
	 * @param fork Place representing the fork of this philosopher
	 * @param places Array of places representing the state of this philosopher in logical order
	 * @param transitions Array of transitions of this philosopher in the order in which they can fire
	 */
	Philosopher(Place fork, Place[] places, Transition[] transitions) {
		this.fork        = fork;
		this.places      = unmodifiableList(asList(places));
		this.transitions = unmodifiableList(asList(transitions));
	}

	/**
	 * Get the fork of this philosopher
	 *
	 * @return place representing the fork
	 */
	public Place getFork() {
		return fork;
	}

	/**
	 * Get the places representing the state of this philosopher
	 *
	 * @return Array of places representing the state of this philosopher in logical order
	 */
	public List<Place> getPlaces() {
		return places;
	}

	/**
	 * Get the transitions of this philosopher
	 *
	 * @return Array of transitions of this philosopher in the order in which they can fire
	 */
	public List<Transition> getTransitions() {
		return transitions;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
