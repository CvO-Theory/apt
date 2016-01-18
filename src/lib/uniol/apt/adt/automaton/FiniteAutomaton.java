/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Schlachter
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

package uniol.apt.adt.automaton;

/**
 * A finite automaton represents a regular language. It consists of states and transitions between states which are
 * labeled with symbols. Some states are final state. Some input is accepted by the automaton if and only if there is a
 * final state which can be reached from the initial state by following suitably labeled transitions.
 * @author Uli Schlachter
 */
public interface FiniteAutomaton {
	/**
	 * Get the initial state of the automaton. This is the state in which the automaton begins reading the input.
	 * @return the initial state.
	 */
	State getInitialState();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
