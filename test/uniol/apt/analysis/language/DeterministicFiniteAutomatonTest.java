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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.TestTSCollection;
import static uniol.apt.adt.matcher.Matchers.*;

import java.util.List;

/** @author Uli Schlachter */
@Test
@SuppressWarnings("unchecked")
public class DeterministicFiniteAutomatonTest {

	private void newEdge(TransitionSystem ts, State source, State target, String label) {
		ts.createArc(source, target, label);
	}

	// Test if the given DFA is the minimal DFA for (abc)^*(ab?)?
	private void testABCNet(DeterministicFiniteAutomaton dfa) {
		assertThat(dfa.getLabels(), containsInAnyOrder("a", "b", "c"));

		DeterministicFiniteAutomatonState stateInit = dfa.getInitialState();
		assertThat(stateInit, is(not(nullValue())));
		assertThat(stateInit.isAccepting(), equalTo(true));

		DeterministicFiniteAutomatonState stateA = stateInit.getState("a");
		assertThat(stateA, is(not(nullValue())));
		assertThat(stateA.isAccepting(), equalTo(true));
		assertThat(stateA, is(not(equalTo(stateInit))));

		DeterministicFiniteAutomatonState stateB = stateA.getState("b");
		assertThat(stateB, is(not(nullValue())));
		assertThat(stateB.isAccepting(), equalTo(true));
		assertThat(stateB, is(not(equalTo(stateInit))));
		assertThat(stateB, is(not(equalTo(stateA))));

		DeterministicFiniteAutomatonState stateErr = stateB.getState("a");
		assertThat(stateErr, is(not(nullValue())));
		assertThat(stateErr.isAccepting(), equalTo(false));
		assertThat(stateErr, is(not(equalTo(stateInit))));
		assertThat(stateErr, is(not(equalTo(stateA))));
		assertThat(stateErr, is(not(equalTo(stateB))));

		assertThat(stateInit.getState("a"), equalTo(stateA));
		assertThat(stateInit.getState("b"), equalTo(stateErr));
		assertThat(stateInit.getState("c"), equalTo(stateErr));
		assertThat(stateA.getState("a"), equalTo(stateErr));
		assertThat(stateA.getState("b"), equalTo(stateB));
		assertThat(stateA.getState("c"), equalTo(stateErr));
		assertThat(stateB.getState("a"), equalTo(stateErr));
		assertThat(stateB.getState("b"), equalTo(stateErr));
		assertThat(stateB.getState("c"), equalTo(stateInit));
		assertThat(stateErr.getState("a"), equalTo(stateErr));
		assertThat(stateErr.getState("b"), equalTo(stateErr));
		assertThat(stateErr.getState("c"), equalTo(stateErr));

		// Kids, the above is why I will not write more tests for the TS -> DFA transformation
	}

	private TransitionSystem getABCSystem() {
		// Construct the DEA that accepts (abc)*(\epsilon|a|ab)
		TransitionSystem ts = new TransitionSystem();
		State init = ts.createState();
		State a = ts.createState();
		State ab = ts.createState();
		ts.setInitialState(init);

		newEdge(ts, init, a, "a");
		newEdge(ts, a, ab, "b");
		newEdge(ts, ab, init, "c");

		return ts;
	}

	@Test
	public void testDEADeterminisation() {
		// This DFA should be identical to the above DFA (plus an error state)
		testABCNet(new DeterministicFiniteAutomaton(getABCSystem()));
	}

	@Test
	public void testDEAMinimsation() {
		// Construct the DEA that accepts (abc)*(\epsilon|a|ab), but construct it twice, so that we get
		// unnecessary states
		TransitionSystem ts = getABCSystem();
		State init = ts.getInitialState();
		State init2 = ts.createState();
		State a = ts.createState();
		State ab = ts.createState();
		newEdge(ts, init, a, "a");
		newEdge(ts, init2, a, "a");
		newEdge(ts, a, ab, "b");
		newEdge(ts, ab, init2, "c");

		testABCNet(new DeterministicFiniteAutomaton(ts).minimize());
	}

	@Test
	public void testWordDifference() {
		TransitionSystem ts = getABCSystem();
		DeterministicFiniteAutomaton dfa1 = new DeterministicFiniteAutomaton(ts);

		// Now modify the transition system a little
		newEdge(ts, ts.getInitialState(), ts.createState(), "b");
		DeterministicFiniteAutomaton dfa2 = new DeterministicFiniteAutomaton(ts);

		// dfa1 accepts the prefix language of (abc)^*. dfa2 accepts the prefix language of (abc)^*b. Due to the
		// way the algorithm is implemented (read: iteration order over the set of labels), we can reach the
		// state that describes the difference either via abcb (if we start in the root with label a) or b (if
		// we start with label b). So either result is fine.

		List<String> list = DeterministicFiniteAutomaton.checkAutomatonEquivalence(dfa1, dfa2);
		assertThat(list, anyOf(contains("b"), contains("a", "b", "c", "b")));
	}

	@Test
	public void testWordDifferenceAll() {
		TransitionSystem ts = getABCSystem();
		DeterministicFiniteAutomaton dfa1 = new DeterministicFiniteAutomaton(ts);

		// Now modify the transition system a little
		newEdge(ts, ts.getInitialState(), ts.createState(), "b");
		DeterministicFiniteAutomaton dfa2 = new DeterministicFiniteAutomaton(ts);

		// dfa1 accepts the prefix language of (abc)^*. dfa2 accepts the prefix language of (abc)^*b. Due to the
		// way the algorithm is implemented (read: iteration order over the set of labels), we can reach the
		// state that describes the difference either via abcb (if we start in the root with label a) or b (if
		// we start with label b). So either result is fine.

		List<Word> list = DeterministicFiniteAutomaton.checkAutomatonEquivalence(dfa1, dfa2, true);
		assertThat(list, contains(anyOf(contains("b"), contains("a", "b", "c", "b"))));
	}

	private void testTS(TransitionSystem ts) {
		DeterministicFiniteAutomaton automaton = new DeterministicFiniteAutomaton(ts);
		automaton = automaton.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton), is(nullValue()));
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton,
					true), is(empty()));

		if (ts.getNodes().size() == 1)
			return;

		// Compare to a single state TS
		DeterministicFiniteAutomaton automaton2 = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateTS());
		automaton2 = automaton2.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton2), is(not(nullValue())));
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton2,
					true), is(not(empty())));
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
		State a = lts.createState();
		State b = lts.createState();
		lts.createArc(a, b, "");
		lts.createArc(b, a, "");
		lts.setInitialState(a);
		testTS(lts);
	}

	@Test
	public void testNonEqualTS() {
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getPersistentTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNotTotallyReachableTS());
		a = a.minimize();
		b = b.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), anyOf(contains("b"), contains("a", "b")));
	}

	@Test
	public void testNonEqualTSAll() {
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getPersistentTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNotTotallyReachableTS());
		a = a.minimize();
		b = b.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true),
				containsInAnyOrder(
					contains("b"),
					contains("a", "b")));
	}

	@Test
	public void testDifferentLabelSets() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateSingleTransitionTS());
		a = a.minimize();
		b = b.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), anyOf(contains("NotA"), contains("a")));
	}

	@Test
	public void testDifferentLabelSetsAll() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateSingleTransitionTS());
		a = a.minimize();
		b = b.minimize();
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true), containsInAnyOrder(
					contains("NotA"),
					contains("a")));
	}

	@Test
	public void testDifferentLabelSetsErrorState() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateWithUnreachableTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), contains("a"));
	}

	@Test
	public void testDifferentLabelSetsErrorStateAll() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateWithUnreachableTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		assertThat(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true),
				contains(contains("a")));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
