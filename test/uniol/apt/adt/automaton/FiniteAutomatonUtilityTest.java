/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import java.util.Arrays;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
@Test
public class FiniteAutomatonUtilityTest {
	private void noWordInLanguage(FiniteAutomaton aut) {
		assertThat(isWordInLanguage(aut, Arrays.asList("c")), is(false));
		assertThat(isWordInLanguage(aut, Arrays.asList("a", "b", "c")), is(false));
	}

	private void wordInLanguage(FiniteAutomaton aut, boolean result, String... word) {
		assertThat(isWordInLanguage(aut, Arrays.asList(word)), is(result));
	}

	@Test
	public void testGetEmptyLanguage() {
		FiniteAutomaton automaton = getEmptyLanguage();

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(false));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, false);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testGetAtomicLanguageEpsilon() {
		FiniteAutomaton automaton = getAtomicLanguage(Symbol.EPSILON);

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(true));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testGetAtomicLanguageA() {
		FiniteAutomaton automaton = getAtomicLanguage(new Symbol("a"));

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(false));
		assertThat(state.getDefinedSymbols(), contains(new Symbol("a")));
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), hasSize(1));

		state = state.getFollowingStates(new Symbol("a")).iterator().next();
		assertThat(state.isFinalState(), is(true));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testUnion1() {
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testUnion2() {
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(new Symbol("b")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "b");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
		wordInLanguage(automaton, false, "b", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testConcatenate1() {
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(new Symbol("b")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, true, "a", "b");
		wordInLanguage(automaton, false, "a", "b", "c");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testConcatenate2() {
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testKleeneStar1() {
		FiniteAutomaton automaton = kleeneStar(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "a", "a");
		wordInLanguage(automaton, true, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testKleeneStar2() {
		FiniteAutomaton automaton = kleeneStar(getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testKleenePlus1() {
		FiniteAutomaton automaton = kleenePlus(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "a", "a");
		wordInLanguage(automaton, true, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testKleenePlus2() {
		FiniteAutomaton automaton = kleenePlus(getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testOptional() {
		FiniteAutomaton automaton = optional(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	private DeterministicFiniteAutomaton getTestDFA() {
		// Construct the minimal dfa for ((ab)^* | (ba)^* | (ab)^+)
		FiniteAutomaton ab = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));
		FiniteAutomaton ab2 = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));
		FiniteAutomaton ba = concatenate(getAtomicLanguage(new Symbol("b")),
				getAtomicLanguage(new Symbol("a")));
		FiniteAutomaton automaton = union(union(kleeneStar(ab), kleeneStar(ba)), kleenePlus(ab2));
		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, true, "a", "b");
		wordInLanguage(automaton, false, "a", "b", "a");
		wordInLanguage(automaton, true, "a", "b", "a", "b");
		wordInLanguage(automaton, true, "b", "a", "b", "a", "b", "a");
		return constructDFA(automaton);
	}

	@Test
	public void testConstructDFA() {
		DeterministicFiniteAutomaton dfa = getTestDFA();

		assertThat(dfa.getAlphabet(), containsInAnyOrder(new Symbol("a"), new Symbol("b")));
		assertThat(dfa.getInitialState(), equalTo(dfa.getInitialState()));
		wordInLanguage(dfa, true);
		wordInLanguage(dfa, false, "a");
		wordInLanguage(dfa, true, "a", "b");
		wordInLanguage(dfa, false, "a", "b", "a");
		wordInLanguage(dfa, true, "a", "b", "a", "b");
		wordInLanguage(dfa, true, "b", "a", "b", "a", "b", "a");

		assertThat(constructDFA(dfa), sameInstance(dfa));
	}

	@Test
	public void testConstructMinimalDFA() {
		DeterministicFiniteAutomaton dfa = minimize(getTestDFA());

		assertThat(dfa.getAlphabet(), containsInAnyOrder(new Symbol("a"), new Symbol("b")));
		assertThat(dfa.getInitialState(), equalTo(dfa.getInitialState()));
		wordInLanguage(dfa, true);
		wordInLanguage(dfa, false, "a");
		wordInLanguage(dfa, true, "a", "b");
		wordInLanguage(dfa, false, "a", "b", "a");
		wordInLanguage(dfa, true, "a", "b", "a", "b");
		wordInLanguage(dfa, true, "b", "a", "b", "a", "b", "a");

		// The minimal dfa has six states represented by the words epsilon, a, ab, b, ba and bb
		Symbol symA = new Symbol("a");
		Symbol symB = new Symbol("b");
		DFAState stateEps = dfa.getInitialState();
		DFAState stateA = stateEps.getFollowingState(symA);
		DFAState stateAB = stateA.getFollowingState(symB);
		DFAState stateB = stateEps.getFollowingState(symB);
		DFAState stateBA = stateB.getFollowingState(symA);
		DFAState stateBB = stateB.getFollowingState(symB);

		assertThat(stateEps.getFollowingState(symA), equalTo(stateA));
		assertThat(stateEps.getFollowingState(symB), equalTo(stateB));
		assertThat(stateA.getFollowingState(symA), equalTo(stateBB));
		assertThat(stateA.getFollowingState(symB), equalTo(stateAB));
		assertThat(stateAB.getFollowingState(symA), equalTo(stateA));
		assertThat(stateAB.getFollowingState(symB), equalTo(stateBB));
		assertThat(stateB.getFollowingState(symA), equalTo(stateBA));
		assertThat(stateB.getFollowingState(symB), equalTo(stateBB));
		assertThat(stateBA.getFollowingState(symA), equalTo(stateBB));
		assertThat(stateBA.getFollowingState(symB), equalTo(stateB));
		assertThat(stateBB.getFollowingState(symA), equalTo(stateBB));
		assertThat(stateBB.getFollowingState(symB), equalTo(stateBB));

		assertThat(constructDFA(dfa), sameInstance(dfa));
		assertThat(minimize(dfa), sameInstance(dfa));
	}

	@Test
	public void testConstructNegation() {
		DeterministicFiniteAutomaton dfa = negate(getTestDFA());

		assertThat(dfa.getAlphabet(), containsInAnyOrder(new Symbol("a"), new Symbol("b")));
		assertThat(dfa.getInitialState(), equalTo(dfa.getInitialState()));
		wordInLanguage(dfa, false);
		wordInLanguage(dfa, true, "a");
		wordInLanguage(dfa, false, "a", "b");
		wordInLanguage(dfa, true, "a", "b", "a");
		wordInLanguage(dfa, false, "a", "b", "a", "b");
		wordInLanguage(dfa, false, "b", "a", "b", "a", "b", "a");
	}

	@Test
	public void testConstructIntersection() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		DeterministicFiniteAutomaton dfa1 = constructDFA(kleeneStar(concatenate(a, concatenate(b, a))));
		DeterministicFiniteAutomaton dfa2 = constructDFA(kleeneStar(concatenate(a, optional(union(a, b)))));
		DeterministicFiniteAutomaton dfa = intersection(dfa1, dfa2);

		assertThat(dfa.getAlphabet(), containsInAnyOrder(new Symbol("a"), new Symbol("b")));
		assertThat(dfa.getInitialState(), equalTo(dfa.getInitialState()));
		wordInLanguage(dfa, true);
		wordInLanguage(dfa, false, "a");
		wordInLanguage(dfa, true, "a", "b", "a");
		wordInLanguage(dfa, false, "a", "a", "a");
		wordInLanguage(dfa, true, "a", "b", "a", "a", "b", "a");
	}

	// fromPrefixLanguageLTS() is tested through analysis.language
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
