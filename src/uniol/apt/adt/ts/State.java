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

package uniol.apt.adt.ts;

import uniol.apt.adt.Node;
import uniol.apt.util.StringComparator;

/**
 * State is the node class of the TransitionSystem.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class State extends Node<TransitionSystem, Arc, State> implements Comparable<State> {

	/**
	 * Constructor.
	 * <p/>
	 * @param ts the Transitionsystem the state belongs to.
	 * @param id the id of the state.
	 */
	State(TransitionSystem ts, String id) {
		super(ts, id);
	}

	/**
	 * Constructor for copying a given state to a given transistionsystem. The constructor also copies the
	 * references of the extensions.
	 * <p/>
	 * @param ts the transitionsystem the state belongs to.
	 * @param s  the state to copy.
	 */
	State(TransitionSystem ts, State s) {
		super(ts, s);
	}

	@Override
	public int compareTo(State that) {		
		return StringComparator.staticCompare(this.getId(), that.getId());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
