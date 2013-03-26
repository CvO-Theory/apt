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

package uniol.apt.analysis.language;

import java.util.Map;
import java.util.HashMap;
import static java.util.Collections.unmodifiableMap;

/**
 * This class represents a state in a DEA.
 * @see uniol.apt.analysis.language.DeterministicFiniteAutomaton
 * @author Uli Schlachter
 */
class DeterministicFiniteAutomatonState {
	private Map<String, DeterministicFiniteAutomatonState> transitions = new HashMap<>();
	private final boolean isAccepting;

	/**
	 * Construct a new DEA state.
	 * @param isAccepting If true, this state is an accepting state.
	 */
	DeterministicFiniteAutomatonState(boolean isAccepting) {
		this.isAccepting = isAccepting;
	}

	/**
	 * Call this when no more modifications to this state are allowed.
	 */
	void setupDone() {
		transitions = unmodifiableMap(transitions);
	}

	/**
	 * Add a transition to the state. This only works if #setupDone() was not yet called.
	 * @param input input to the state
	 * @param state following state for that input
	 */
	void addTransition(String input, DeterministicFiniteAutomatonState state) {
		if (transitions.put(input, state) != null)
			// There was already another transition for that state
			throw new UnsupportedOperationException();
	}

	/**
	 * Get the following state for the given input.
	 * @param input The input to the state.
	 * @return The following state or null.
	 */
	public DeterministicFiniteAutomatonState getState(String input) {
		return transitions.get(input);
	}

	/**
	 * @return true if this is an accepting state.
	 */
	public boolean isAccepting() {
		return this.isAccepting;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
