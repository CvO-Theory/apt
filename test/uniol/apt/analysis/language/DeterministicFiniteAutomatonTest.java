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
import static org.testng.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.TestTSCollection;
import static uniol.apt.adt.matcher.Matchers.*;

import java.util.Arrays;
import java.util.List;

/** @author Uli Schlachter */
@Test
public class DeterministicFiniteAutomatonTest {

	private void newEdge(TransitionSystem ts, State source, State target, String label) {
		ts.createArc(source, target, label);
	}

	// Test if the given DFA is the minimal DFA for (abc)^*(ab?)?
	private void testABCNet(DeterministicFiniteAutomaton dfa) {
		assertThat(dfa.getLabels(), containsInAnyOrder("a", "b", "c"));

		DeterministicFiniteAutomatonState stateInit = dfa.getInitialState();
		assertNotNull(stateInit);
		assertTrue(stateInit.isAccepting());

		DeterministicFiniteAutomatonState stateA = stateInit.getState("a");
		assertNotNull(stateA);
		assertTrue(stateA.isAccepting());
		assertNotEquals(stateInit, stateA);

		DeterministicFiniteAutomatonState stateB = stateA.getState("b");
		assertNotNull(stateB);
		assertTrue(stateB.isAccepting());
		assertNotEquals(stateInit, stateB);
		assertNotEquals(stateA, stateB);

		DeterministicFiniteAutomatonState stateErr = stateB.getState("a");
		assertNotNull(stateErr);
		assertFalse(stateErr.isAccepting());
		assertNotEquals(stateInit, stateErr);
		assertNotEquals(stateA, stateErr);
		assertNotEquals(stateB, stateErr);

		assertEquals(stateInit.getState("a"), stateA);
		assertEquals(stateInit.getState("b"), stateErr);
		assertEquals(stateInit.getState("c"), stateErr);
		assertEquals(stateA.getState("a"), stateErr);
		assertEquals(stateA.getState("b"), stateB);
		assertEquals(stateA.getState("c"), stateErr);
		assertEquals(stateB.getState("a"), stateErr);
		assertEquals(stateB.getState("b"), stateErr);
		assertEquals(stateB.getState("c"), stateInit);
		assertEquals(stateErr.getState("a"), stateErr);
		assertEquals(stateErr.getState("b"), stateErr);
		assertEquals(stateErr.getState("c"), stateErr);

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

		List<String> list = DeterministicFiniteAutomaton.checkAutomatonEquivalence(dfa1, dfa2);
		assertEquals(list, Arrays.asList("b"));
	}

	@Test
	public void testWordDifferenceAll() {
		TransitionSystem ts = getABCSystem();
		DeterministicFiniteAutomaton dfa1 = new DeterministicFiniteAutomaton(ts);

		// Now modify the transition system a little
		newEdge(ts, ts.getInitialState(), ts.createState(), "b");
		DeterministicFiniteAutomaton dfa2 = new DeterministicFiniteAutomaton(ts);

		List<Word> list = DeterministicFiniteAutomaton.checkAutomatonEquivalence(dfa1, dfa2, true);
		assertEquals(list, Arrays.asList(Arrays.asList("b")));
	}

	private void testTS(TransitionSystem ts) {
		DeterministicFiniteAutomaton automaton = new DeterministicFiniteAutomaton(ts);
		automaton = automaton.minimize();
		assertNull(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton));
		assertTrue(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton,
					true).isEmpty());

		if (ts.getNodes().size() == 1)
			return;

		// Compare to a single state TS
		DeterministicFiniteAutomaton automaton2 = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateTS());
		automaton2 = automaton2.minimize();
		assertNotNull(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton2));
		assertFalse(DeterministicFiniteAutomaton.checkAutomatonEquivalence(automaton, automaton2,
					true).isEmpty());
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
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), Arrays.asList("b"));
	}

	@Test
	public void testNonEqualTSAll() {
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getPersistentTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNotTotallyReachableTS());
		a = a.minimize();
		b = b.minimize();
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true),
				Arrays.asList(
					Arrays.asList("b"),
					Arrays.asList("a", "b")));
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
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), Arrays.asList("NotA"));
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
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true), Arrays.asList(
					Arrays.asList("NotA"),
					Arrays.asList("a")));
	}

	@Test
	public void testDifferentLabelSetsErrorState() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateWithUnreachableTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b), Arrays.asList("a"));
	}

	@Test
	public void testDifferentLabelSetsErrorStateAll() {
		// The second DFA has a label "c" while the first one doesn't. Thus, they obviously can't be equivalent.
		DeterministicFiniteAutomaton a = new DeterministicFiniteAutomaton(
				TestTSCollection.getSingleStateWithUnreachableTS());
		DeterministicFiniteAutomaton b = new DeterministicFiniteAutomaton(
				TestTSCollection.getNonDeterministicTS());
		assertEquals(DeterministicFiniteAutomaton.checkAutomatonEquivalence(a, b, true),
				Arrays.asList(Arrays.asList("a")));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
