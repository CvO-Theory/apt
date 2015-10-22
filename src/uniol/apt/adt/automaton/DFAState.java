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

import java.util.Collections;
import java.util.Set;

/**
 * A state of a DeterministicFiniteAutomaton.
 * @author Uli Schlachter
 */
public abstract class DFAState implements State {

	/**
	 * Get the state that is reached by some symbol. This function can also be called for symbols which are not
	 * returned by {@link getDefinedSymbols}. However, no such symbols which may have transitions.
	 * @param atom The symbol which should be followed.
	 * @return the state that is reached or null if the atom is epsilon or not in the alphabet.
	 */
	abstract public DFAState getFollowingState(Symbol atom);

	@Override
	final public Set<State> getFollowingStates(Symbol atom) {
		DFAState state = getFollowingState(atom);
		if (atom.isEpsilon() || state == null)
			return Collections.emptySet();
		return Collections.<State>singleton(state);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
