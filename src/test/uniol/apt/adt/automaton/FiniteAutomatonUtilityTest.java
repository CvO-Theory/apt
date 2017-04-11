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
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;

/**
 * @author Uli Schlachter
 */
public class FiniteAutomatonUtilityTest {

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
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testUnion2() {
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));

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
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, true, "a", "b");
		wordInLanguage(automaton, false, "a", "b", "c");

		assertThat(automaton.getInitialState(), equalTo(automaton.getInitialState()));
	}

	@Test
	public void testConcatenate2() {
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(Symbol.EPSILON));

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

	@Test
	public void testOptionalOptimises() {
		FiniteAutomaton automaton = union(getAtomicLanguage(Symbol.EPSILON),
					getAtomicLanguage(new Symbol("a")));
		// We want to test the non-DFA version of the function
		assert !(automaton instanceof DeterministicFiniteAutomaton);
		automaton = optional(automaton);
		assertThat(optional(automaton), sameInstance(automaton));
	}

	@Test
	public void testOptionalDFA() {
		Symbol a = new Symbol("a");
		DeterministicFiniteAutomaton orig = constructDFA(getAtomicLanguage(a));
		DeterministicFiniteAutomaton automaton = optional(orig);
		assertThat(optional((FiniteAutomaton) orig), instanceOf(DeterministicFiniteAutomaton.class));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");

		DFAState initial = automaton.getInitialState();
		assertThat(initial, equalTo(initial));
		assertThat(initial, not(equalTo(orig.getInitialState())));
		assertThat(orig.getInitialState().isFinalState(), is(false));
		assertThat(initial.isFinalState(), is(true));
		assertThat(initial.getFollowingState(a), equalTo(orig.getInitialState().getFollowingState(a)));
	}

	@Test
	public void testOptionalDFAOptimises() {
		DeterministicFiniteAutomaton automaton = optional(constructDFA(getAtomicLanguage(Symbol.EPSILON)));
		assertThat(optional(automaton), sameInstance(automaton));
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
		wordInLanguage(dfa, false, "b");
		wordInLanguage(dfa, true, "a", "b", "a");
		wordInLanguage(dfa, false, "a", "a", "a");
		wordInLanguage(dfa, true, "a", "b", "a", "a", "b", "a");
	}

	@Test
	public void testConstructUnion() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		DeterministicFiniteAutomaton dfa1 = constructDFA(kleeneStar(concatenate(a, concatenate(b, a))));
		DeterministicFiniteAutomaton dfa2 = constructDFA(kleeneStar(concatenate(a, optional(union(a, b)))));
		DeterministicFiniteAutomaton dfa = union(dfa1, dfa2);

		assertThat(dfa.getAlphabet(), containsInAnyOrder(new Symbol("a"), new Symbol("b")));
		assertThat(dfa.getInitialState(), equalTo(dfa.getInitialState()));
		wordInLanguage(dfa, true);
		wordInLanguage(dfa, true, "a");
		wordInLanguage(dfa, false, "b");
		wordInLanguage(dfa, true, "a", "b", "a");
		wordInLanguage(dfa, true, "a", "a", "a");
		wordInLanguage(dfa, true, "a", "b", "a", "a", "b", "a");
	}

	@Test
	public void testFindWordDifference() {
		Symbol a = new Symbol("a");
		Symbol b = new Symbol("b");
		FiniteAutomaton autA = getAtomicLanguage(a);
		FiniteAutomaton autB = getAtomicLanguage(b);
		FiniteAutomaton automaton1 = kleeneStar(concatenate(autA, concatenate(autB, autA)));
		FiniteAutomaton automaton2 = kleeneStar(concatenate(autA, optional(union(autA, autB))));

		assertThat(findWordDifference(automaton1, automaton2), contains("a"));
	}

	@Test
	public void testFindWordDifferenceSame() {
		Symbol a = new Symbol("a");
		Symbol b = new Symbol("b");
		FiniteAutomaton autA = getAtomicLanguage(a);
		FiniteAutomaton autB = getAtomicLanguage(b);
		FiniteAutomaton automaton = kleeneStar(concatenate(autA, optional(union(autA, autB))));

		assertThat(findWordDifference(automaton, automaton), nullValue());
	}

	private void newEdge(TransitionSystem ts, String source, String target, String label) {
		ts.createArc(source, target, label);
	}

	// Test if the given DFA is the minimal DFA for (abc)^*(ab?)?
	private void testABCNet(FiniteAutomaton fa) {
		DeterministicFiniteAutomaton dfa = minimize(fa);

		DFAState stateInit = dfa.getInitialState();
		assertThat(stateInit, is(not(nullValue())));
		assertThat(stateInit.isFinalState(), equalTo(true));

		DFAState stateA = stateInit.getFollowingState(new Symbol("a"));
		assertThat(stateA, is(not(nullValue())));
		assertThat(stateA.isFinalState(), equalTo(true));
		assertThat(stateA, is(not(equalTo(stateInit))));

		DFAState stateB = stateA.getFollowingState(new Symbol("b"));
		assertThat(stateB, is(not(nullValue())));
		assertThat(stateB.isFinalState(), equalTo(true));
		assertThat(stateB, is(not(equalTo(stateInit))));
		assertThat(stateB, is(not(equalTo(stateA))));

		DFAState stateErr = stateB.getFollowingState(new Symbol("a"));
		assertThat(stateErr, is(not(nullValue())));
		assertThat(stateErr.isFinalState(), equalTo(false));
		assertThat(stateErr, is(not(equalTo(stateInit))));
		assertThat(stateErr, is(not(equalTo(stateA))));
		assertThat(stateErr, is(not(equalTo(stateB))));

		assertThat(stateInit.getFollowingState(new Symbol("a")), equalTo(stateA));
		assertThat(stateInit.getFollowingState(new Symbol("b")), equalTo(stateErr));
		assertThat(stateInit.getFollowingState(new Symbol("c")), equalTo(stateErr));
		assertThat(stateA.getFollowingState(new Symbol("a")), equalTo(stateErr));
		assertThat(stateA.getFollowingState(new Symbol("b")), equalTo(stateB));
		assertThat(stateA.getFollowingState(new Symbol("c")), equalTo(stateErr));
		assertThat(stateB.getFollowingState(new Symbol("a")), equalTo(stateErr));
		assertThat(stateB.getFollowingState(new Symbol("b")), equalTo(stateErr));
		assertThat(stateB.getFollowingState(new Symbol("c")), equalTo(stateInit));
		assertThat(stateErr.getFollowingState(new Symbol("a")), equalTo(stateErr));
		assertThat(stateErr.getFollowingState(new Symbol("b")), equalTo(stateErr));
		assertThat(stateErr.getFollowingState(new Symbol("c")), equalTo(stateErr));

		// Kids, the above is why I will not write more tests for the TS -> DFA transformation
	}

	private TransitionSystem getABCSystem() {
		// Construct the DEA that accepts (abc)*(\epsilon|a|ab)
		TransitionSystem ts = new TransitionSystem();
		ts.createState("init");
		ts.createState("a");
		ts.createState("ab");
		ts.setInitialState("init");

		newEdge(ts, "init", "a", "a");
		newEdge(ts, "a", "ab", "b");
		newEdge(ts, "ab", "init", "c");

		return ts;
	}

	@Test
	public void testDEADeterminisation() {
		// This DFA should be identical to the above DFA (plus an error state)
		testABCNet(fromPrefixLanguageLTS(getABCSystem()));
	}

	@Test
	public void testDEAMinimsation() {
		// Construct the DEA that accepts (abc)*(\epsilon|a|ab), but construct it twice, so that we get
		// unnecessary states
		TransitionSystem ts = getABCSystem();
		ts.createState("init2");
		ts.createState("a2");
		ts.createState("ab2");
		newEdge(ts, "init", "a2", "a");
		newEdge(ts, "init2", "a2", "a");
		newEdge(ts, "a2", "ab2", "b");
		newEdge(ts, "ab2", "init2", "c");

		testABCNet(fromPrefixLanguageLTS(ts));
	}

	@Test
	public void testWordDifference() {
		TransitionSystem ts = getABCSystem();
		DeterministicFiniteAutomaton dfa1 = minimize(fromPrefixLanguageLTS(ts));

		// Now modify the transition system a little
		ts.createState("foo");
		newEdge(ts, "init", "foo", "b");
		DeterministicFiniteAutomaton dfa2 = minimize(fromPrefixLanguageLTS(ts));

		// dfa1 accepts the prefix language of (abc)^*. dfa2 accepts the prefix language of (abc)^*b. Due to the
		// way the algorithm is implemented (read: iteration order over the set of labels), we can reach the
		// state that describes the difference either via abcb (if we start in the root with label a) or b (if
		// we start with label b). So either result is fine.

		List<String> list = findWordDifference(dfa1, dfa2);
		assertThat(list, anyOf(contains("b"), contains("a", "b", "c", "b")));
	}

	@Test
	public void testWordDifference2() {
		TransitionSystem ts = getABCSystem();
		FiniteAutomaton dfa1 = fromLTS(ts, Arrays.asList(ts.getNode("init")));
		FiniteAutomaton dfa2 = fromLTS(ts, Arrays.asList(ts.getNode("a")));

		// dfa1 accepts (abc)^*, dfa2 accepts (abc)^*a

		assertThat(dfa1.getInitialState(), not(equalTo(dfa2.getInitialState())));

		List<String> list = findWordDifference(dfa1, dfa2);
		assertThat(list, anyOf(contains("a"), contains("a", "b", "c", "a")));
	}

	private void testTS(TransitionSystem ts) {
		DeterministicFiniteAutomaton automaton = minimize(fromPrefixLanguageLTS(ts));
		assertThat(findWordDifference(automaton, automaton), is(nullValue()));
		assertThat(languageEquivalent(automaton, automaton), is(true));

		assertThat(languageEquivalent(automaton, fromPrefixLanguageLTS(prefixLanguageLTS(automaton))),
				is(true));

		if (ts.getNodes().size() == 1)
			return;

		// Compare to a single state TS
		DeterministicFiniteAutomaton automaton2 = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getSingleStateTS()));
		assertThat(findWordDifference(automaton, automaton2), is(not(nullValue())));
	}

	@Test
	public void testEmptyLanguageTS() {
		testTS(prefixLanguageLTS(getEmptyLanguage()));
	}

	@Test
	public void testSingleStateTS() {
		testTS(TestTSCollection.getSingleStateTS());
	}

	@Test
	public void testNonDeterministicTS() {
		testTS(TestTSCollection.getNonDeterministicTS());
	}

	@Test
	public void testPersistentTS() {
		testTS(TestTSCollection.getPersistentTS());
	}

	@Test
	public void testNonPersistentTS() {
		testTS(TestTSCollection.getNonPersistentTS());
	}

	@Test
	public void testNonTotallyReachableTS() {
		testTS(TestTSCollection.getNotTotallyReachableTS());
	}

	@Test
	public void testReversibleTS() {
		testTS(TestTSCollection.getReversibleTS());
	}

	@Test
	public void testCircleTS() {
		TransitionSystem lts = new TransitionSystem();
		lts.createState("a");
		lts.createState("b");
		lts.createArc("a", "b", "t");
		lts.createArc("b", "a", "t");
		lts.setInitialState("a");
		testTS(lts);
	}

	@Test
	public void testNonEqualTS() {
		DeterministicFiniteAutomaton a = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getPersistentTS()));
		DeterministicFiniteAutomaton b = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getNotTotallyReachableTS()));
		assertThat(findWordDifference(a, b), anyOf(contains("b"), contains("a", "b")));
	}

	@Test
	public void testDifferentLabelSets() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getNonDeterministicTS()));
		DeterministicFiniteAutomaton b = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getSingleStateSingleTransitionTS()));
		assertThat(findWordDifference(a, b), anyOf(contains("NotA"), contains("a")));
	}

	@Test
	public void testDifferentLabelSetsErrorState() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getSingleStateWithUnreachableTS()));
		DeterministicFiniteAutomaton b = minimize(fromPrefixLanguageLTS(
				TestTSCollection.getNonDeterministicTS()));
		assertThat(findWordDifference(a, b), contains("a"));
	}

	@Test
	public void testPrefixClosureFullLanguage() {
		FiniteAutomaton automaton = kleeneStar(union(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b"))));
		FiniteAutomaton prefixClosure = prefixClosure(automaton);

		assertThat(languageEquivalent(prefixClosure, automaton), is(true));
	}

	@Test
	public void testPrefixEmptyFullLanguage() {
		FiniteAutomaton automaton = getEmptyLanguage();
		FiniteAutomaton prefixClosure = prefixClosure(automaton);
		assertThat(languageEquivalent(prefixClosure, automaton), is(true));
	}

	void testContainsABBAAAndPrefixes(FiniteAutomaton aut) {
		wordInLanguage(aut, true);
		wordInLanguage(aut, true, "a");
		wordInLanguage(aut, true, "a", "b");
		wordInLanguage(aut, true, "a", "b", "b");
		wordInLanguage(aut, true, "a", "b", "b", "a");
		wordInLanguage(aut, true, "a", "b", "b", "a", "a");
	}

	@Test
	public void testPrefixClosureABBAA() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		FiniteAutomaton abbaa = concatenate(a, concatenate(b, concatenate(b, concatenate(a, a))));
		FiniteAutomaton prefixClosure = prefixClosure(abbaa);

		testContainsABBAAAndPrefixes(prefixClosure);

		// Now test some equivalence by constructing the prefix closure by hand
		FiniteAutomaton secondPrefixClosure = optional(concatenate(a, optional(concatenate(b,
						optional(concatenate(b, optional(concatenate(a, optional(a)))))))));
		assertThat(languageEquivalent(prefixClosure, secondPrefixClosure), is(true));
	}

	@Test
	public void testPrefixClosureABSigmaStar() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		FiniteAutomaton abSigmaStar = concatenate(a, concatenate(b, kleeneStar(union(a, b))));
		FiniteAutomaton prefixClosure = prefixClosure(abSigmaStar);

		testContainsABBAAAndPrefixes(prefixClosure);

		// Now test some equivalence by constructing the prefix closure by hand
		FiniteAutomaton secondPrefixClosure = abSigmaStar;
		secondPrefixClosure = union(secondPrefixClosure, getEmptyLanguage());
		secondPrefixClosure = union(secondPrefixClosure, a);
		assertThat(languageEquivalent(prefixClosure, secondPrefixClosure), is(true));
	}

	@Test
	public void testPrefixClosureABStar() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		FiniteAutomaton abStar = kleeneStar(concatenate(a, b));
		FiniteAutomaton prefixClosure = prefixClosure(abStar);

		// Now test some equivalence by constructing the prefix closure by hand
		FiniteAutomaton secondPrefixClosure = union(abStar, concatenate(abStar, a));
		assertThat(languageEquivalent(prefixClosure, secondPrefixClosure), is(true));
	}

	@Test
	public void testLanguageEquivalentStates() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("end");
		ts.createState("init");
		ts.createState("a");
		ts.setInitialState("init");

		ts.createArc("init", "a", "a");
		ts.createArc("end", "a", "a");
		ts.createArc("a", "end", "b");

		FiniteAutomaton aut = fromLTS(ts, Arrays.asList(ts.getNode("end")));
		wordInLanguage(aut, true, "a", "b");
		aut = minimize(aut);
		wordInLanguage(aut, true, "a", "b");
	}

	@Test
	public void testRenderToGraphviz() {
		assertThat(renderToGraphviz(getAtomicLanguage(new Symbol("a"))),
				is("digraph G {\n  s1 [peripheries=2];\n" +
					"  start [shape=point, color=white, fontcolor=white];\n  start -> s0;\n" +
					"  s0 -> s1 [label=\"[a]\"];\n}\n"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
