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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.util.Pair;

import static uniol.apt.adt.automaton.FiniteAutomatonUtility.minimize;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.statesIterable;

/**
 * Convert a finite automaton to a language-equivalent regular expression.
 *
 * Define L^k_{i,j} to be all words that go from state i to state j and
 * in-between only visit states whose number is at most k. Then the language of
 * a finite automaton is the union of all L^n_{1,j} where n is the number of
 * states and j is some final state. All of these languages are regular. This
 * code computes this construction.
 * @author Uli Schlachter
 */
public class AutomatonToRegularExpression {
	private AutomatonToRegularExpression() { /* hide */ }

	static private final String EPSILON_REGEX = "$";
	static private final String EPSILON_REGEX_OR = "$|";

	static public String automatonToRegularExpression(FiniteAutomaton automaton) {
		DeterministicFiniteAutomaton dfa = minimize(automaton);

		List<DFAState> states = new ArrayList<DFAState>();
		for (DFAState state : statesIterable(dfa))
			states.add(state);

		// Try with two different orders
		String regex1 = automatonToRegularExpression(dfa, states);
		Collections.reverse(states);
		String regex2 = automatonToRegularExpression(dfa, states);

		// And pick the shorter of the two results
		if (regex1.length() > regex2.length())
			return regex2;
		return regex1;
	}

	static private String automatonToRegularExpression(DeterministicFiniteAutomaton dfa,
			Collection<DFAState> states) {
		Map<Pair<DFAState, DFAState>, Set<String>> mapping = getInitialMapping(dfa);
		for (DFAState state : states)
			mapping = handleNextState(dfa, mapping, state);

		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (DFAState state : states) {
			if (!state.isFinalState())
				continue;
			String regex = getRegex(mapping, dfa.getInitialState(), state);
			if (regex == null)
				continue;
			if (!first)
				result.append("|");
			result.append(regex);
			first = false;
		}
		if (first)
			result.append("~");

		return result.toString();
	}

	static private Map<Pair<DFAState, DFAState>, Set<String>> getInitialMapping(DeterministicFiniteAutomaton dfa) {
		Map<Pair<DFAState, DFAState>, Set<String>> result = new HashMap<>();
		for (DFAState state : statesIterable(dfa)) {
			add(result, state, state, Symbol.EPSILON);
			for (Symbol symbol : dfa.getAlphabet()) {
				add(result, state, state.getFollowingState(symbol), symbol);
			}
		}
		return result;
	}

	static private Map<Pair<DFAState, DFAState>, Set<String>> handleNextState(DeterministicFiniteAutomaton dfa,
			Map<Pair<DFAState, DFAState>, Set<String>> mapping, DFAState newState) {
		Map<Pair<DFAState, DFAState>, Set<String>> result = new HashMap<>(mapping);
		for (DFAState state1 : statesIterable(dfa)) {
			for (DFAState state2 : statesIterable(dfa)) {
				String state1ToNew = getRegex(mapping, state1, newState);
				String newToNew = getRegex(mapping, newState, newState);
				String newToState2 = getRegex(mapping, newState, state2);
				if (state1ToNew == null || newToState2 == null)
					continue;

				// Epsilon "disappears" in a concatenation
				if (state1ToNew.equals(EPSILON_REGEX))
					state1ToNew = "";
				if (newToState2.equals(EPSILON_REGEX))
					newToState2 = "";

				// We want to iterate newToNew, but a*a is the same as a*. So skip newToState1 in this
				// case if it would cause such a duplicate.
				if (newToNew.equals(state1ToNew))
					state1ToNew = "";
				if (newToNew.equals(newToState2))
					newToState2 = "";

				// ($|r)* is the same as r*. So if newToNew begins with "$|", skip that part.
				if (newToNew.startsWith(EPSILON_REGEX_OR))
					newToNew = newToNew.substring(2);

				// If newState can reach itself again, we can go through that loop any number of times.
				// However, iterating epsilon doesn't make any sense
				assert newToNew != null : "Each state must reach itself at least via epsilon";
				if (newToNew.equals(EPSILON_REGEX))
					newToNew = "";
				else if (!needsParanthesesInStar(newToNew))
					newToNew = newToNew + "*";
				else
					newToNew = "(" + newToNew + ")*";

				if (newToNew.isEmpty() && state1ToNew.isEmpty()) {
					assert state1.equals(newState);
					continue;
				}
				if (newToNew.isEmpty() && newToState2.isEmpty()) {
					assert state2.equals(newState);
					continue;
				}

				// Concatenation binds stronger than alternative, so we might have to add parentheses
				if (state1ToNew.contains("|"))
					state1ToNew = "(" + state1ToNew + ")";
				if (newToState2.contains("|"))
					newToState2 = "(" + newToState2 + ")";

				add(result, state1, state2, state1ToNew + newToNew + newToState2);
			}
		}
		return result;
	}

	// Check if the given regular expression needs parentheses around it if we want to apply a kleene star to it
	static private boolean needsParanthesesInStar(String regex) {
		// All operators bind either as strong as the star (+ and ?) or bind weaker (| and concatenation).
		// Checking this properly is hard, so we just check if this is just a single atom.
		if (regex.length() == 1)
			return false;
		// An atom can have the form <foo> if "foo" is a single event. Check if the string begins and ends
		// correctly and doesn't contain any other atoms (so to reject <foo><bar>)
		if (regex.startsWith("<") && regex.endsWith(">") && regex.indexOf("<", 1) == -1)
			return false;
		return true;
	}

	static private void add(Map<Pair<DFAState, DFAState>, Set<String>> mapping, DFAState state1, DFAState state2,
			Symbol symbol) {
		if (symbol.isEpsilon()) {
			add(mapping, state1, state2, EPSILON_REGEX);
			return;
		}

		String event = symbol.getEvent();
		assert !event.isEmpty();
		if (event.length() == 1)
			add(mapping, state1, state2, event);
		else
			add(mapping, state1, state2, "<" + event + ">");
	}

	static private void add(Map<Pair<DFAState, DFAState>, Set<String>> mapping, DFAState state1, DFAState state2,
			String newRegex) {
		if (newRegex.isEmpty())
			return;

		Pair<DFAState, DFAState> pair = new Pair<>(state1, state2);
		Set<String> regex = mapping.get(pair);
		if (regex == null) {
			regex = new LinkedHashSet<>();
			mapping.put(pair, regex);
		}

		regex.add(newRegex);
	}

	static private String getRegex(Map<Pair<DFAState, DFAState>, Set<String>> mapping, DFAState state1,
			DFAState state2) {
		Set<String> regex = mapping.get(new Pair<>(state1, state2));
		if (regex == null)
			return null;

		assert !regex.isEmpty();
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (String str : regex) {
			if (!first)
				result.append("|");
			result.append(str);
			first = false;
		}
		return result.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
