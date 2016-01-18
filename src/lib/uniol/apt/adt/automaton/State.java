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

import java.util.Set;

/**
 * A state of a {@link FiniteAutomaton}. A state can be a final or a non-final state and has outgoing transitions. Each
 * transition is labeled with some symbol.
 * @author Uli Schlachter
 */
interface State {
	/**
	 * Is this state a final state?
	 * @return true iff this state is a final state.
	 */
	boolean isFinalState();

	/**
	 * Get the symbols for which transitions are defined. This function will never return the epsilon symbol.
	 * Instead, this symbol should automatically be assumed.
	 * @return Symbols for which there are transitions.
	 */
	Set<Symbol> getDefinedSymbols();

	/**
	 * Get the states that are reached by some symbol. This function can also be called for symbols which are not
	 * returned by {@link getDefinedSymbols}. However, epsilon is the only symbol which may have transitions. All
	 * other symbols must reach the empty set.
	 * @param symbol The symbol which should be followed.
	 * @return States that are reached by the given symbol which may be the empty set.
	 */
	Set<State> getFollowingStates(Symbol symbol);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
