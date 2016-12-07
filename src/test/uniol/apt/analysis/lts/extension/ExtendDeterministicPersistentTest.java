/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Uli Schlachter
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

package uniol.apt.analysis.lts.extension;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.fail;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.INode;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.MustLeadToSameStateException;
import uniol.apt.analysis.exception.NoFiniteExtensionPossibleException;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.util.Pair;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.util.matcher.Matchers.pairWith;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked")
public class ExtendDeterministicPersistentTest {
	// As a side effect, this modifies ts
	static private void extendSuccessfully(TransitionSystem ts, int maxPhase2Rounds) throws Exception {
		assertThat(new ExtendDeterministicPersistent().extendTs(ts, maxPhase2Rounds), empty());
	}

	static private void noChanges(TransitionSystem ts) throws Exception {
		Set<Matcher<? super INode<?, ?, ?>>> nodes = new HashSet<>();
		Set<Matcher<? super Arc>> edges = new HashSet<>();
		Matcher<INode<?, ?, ?>> initialState = nodeWithID(ts.getInitialState().getId());

		for (State state : ts.getNodes())
			nodes.add(nodeWithID(state.getId()));
		for (Arc arc : ts.getEdges())
			edges.add(arcThatConnectsVia(arc.getSource().getId(), arc.getTarget().getId(), arc.getLabel()));

		extendSuccessfully(ts, 0);

		assertThat(ts.getInitialState(), initialState);
		assertThat(ts.getNodes(), containsInAnyOrder(nodes));
		assertThat(ts.getEdges(), containsInAnyOrder(edges));
	}

	// Test that after completion the ts is a single persistent diamond
	static private void checkPersistentDiamond(TransitionSystem ts, int maxPhase2Rounds) throws Exception {
		String base = ts.getInitialState().getId();

		extendSuccessfully(ts, maxPhase2Rounds);

		assertThat(ts.getInitialState(), nodeWithID(base));
		assertThat(ts.getNodes(), hasSize(4));
		assertThat(ts.getEdges(), hasSize(4));
		assertThat(ts.getInitialState().getPostsetEdges(), hasSize(2));

		State state1 = null, state2 = null;
		String label1 = null, label2 = null;
		for (Arc arc : ts.getInitialState().getPostsetEdges()) {
			if (state1 == null) {
				state1 = arc.getTarget();
				label1 = arc.getLabel();
			} else if (state2 == null) {
				state2 = arc.getTarget();
				label2 = arc.getLabel();
				assertThat(label1, not(equalTo(label2)));
			} else {
				throw new RuntimeException(
						"I'm looking at the third entry of a collection with two entries");
			}
		}
		assertThat(state1, not(nullValue()));
		assertThat(state2, not(nullValue()));
		assertThat(state2, not(is(state1)));

		assertThat(state1.getPostsetEdges(), contains(arcThatConnectsVia(is(state1), anything(), is(label2))));
		assertThat(state2.getPostsetEdges(), contains(arcThatConnectsVia(is(state2), anything(), is(label1))));
		assertThat(state1.getPostsetNodes(), equalTo(state2.getPostsetNodes()));
	}

	@Test
	public void testSingleStateTS() throws Exception {
		noChanges(TestTSCollection.getSingleStateTS());
	}

	@Test
	public void testSingleStateTSWithLoop() throws Exception {
		noChanges(TestTSCollection.getSingleStateTSWithLoop());
	}

	@Test
	public void testTwoStateCycleSameLabelTS() throws Exception {
		noChanges(TestTSCollection.getTwoStateCycleSameLabelTS());
	}

	@Test
	public void testSingleStateWithUnreachableTS() throws Exception {
		noChanges(TestTSCollection.getSingleStateWithUnreachableTS());
	}

	@Test
	public void testPersistentTS() throws Exception {
		noChanges(TestTSCollection.getPersistentTS());
	}

	@Test
	public void testNotTotallyReachableTS() throws Exception {
		noChanges(TestTSCollection.getNotTotallyReachableTS());
	}

	@Test
	public void testReversibleTS() throws Exception {
		noChanges(TestTSCollection.getReversibleTS());
	}

	@Test
	public void testDifferentCyclesTS() throws Exception {
		noChanges(TestTSCollection.getDifferentCyclesTS());
	}

	@Test
	public void testcc1LTS() throws Exception {
		noChanges(TestTSCollection.getcc1LTS());
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testNonDeterministicTS() throws Exception {
		new ExtendDeterministicPersistent().extendTs(TestTSCollection.getNonDeterministicTS(),
				Integer.MAX_VALUE);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testPersistentNonDeterministicTS() throws Exception {
		new ExtendDeterministicPersistent().extendTs(TestTSCollection.getPersistentNonDeterministicTS(),
				Integer.MAX_VALUE);
	}

	@Test
	public void testThreeStatesTwoEdgesTS() throws Exception {
		checkPersistentDiamond(TestTSCollection.getThreeStatesTwoEdgesTS(), 1);
	}

	@Test
	public void testThreeStatesTwoEdgesTS2() throws Exception {
		// This extension will fail, because we limit phase 2 to zero rounds
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		Set<Pair<State, String>> expectedFailure = new HashSet<>();
		expectedFailure.add(new Pair<>(ts.getNode("t"), "b"));
		expectedFailure.add(new Pair<>(ts.getNode("v"), "a"));
		assertThat(new ExtendDeterministicPersistent().extendTs(ts, 0), contains(expectedFailure));
	}

	@Test
	public void testABandBUnfolded() throws Exception {
		checkPersistentDiamond(TestTSCollection.getABandBUnfolded(), 0);
	}

	@Test
	public void testABandB() throws Exception {
		TransitionSystem ts = TestTSCollection.getABandB();
		extendSuccessfully(ts, 0);

		assertThat(ts.getInitialState(), nodeWithID("s"));
		assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s"), nodeWithID("t"), nodeWithID("u")));
		assertThat(ts.getEdges(), containsInAnyOrder(arcThatConnectsVia("s", "t", "a"),
				arcThatConnectsVia("t", "u", "b"), arcThatConnectsVia("s", "u", "b"),
				arcThatConnectsVia("u", "u", "a")));
	}

	@Test(expectedExceptions = MustLeadToSameStateException.class, expectedExceptionsMessageRegExp =
			"^Arcs "
			+ "(r2--a->r and s1--fail->l|s1--fail->l and r2--a->r) must lead to the same state\\.$")
	public void testNonPersistentButActivatedTS() throws Exception {
		new ExtendDeterministicPersistent().extendTs(TestTSCollection.getNonPersistentButActivatedTS(),
				Integer.MAX_VALUE);
	}

	@Test(expectedExceptions = MustLeadToSameStateException.class, expectedExceptionsMessageRegExp =
			"^Arcs "
			+ "(r2--a->r and s1--fail->l|s1--fail->l and r2--a->r) must lead to the same state\\.$")
	public void testNonPersistentButActivatedTS2() throws Exception {
		new ExtendDeterministicPersistent().extendTs(TestTSCollection.getNonPersistentButActivatedTS(), 0);
	}

	private void doDeterministicReachableReversibleNonPersistentTS(int  maxPhase2Rounds) throws Exception {
		TransitionSystem ts = TestTSCollection.getDeterministicReachableReversibleNonPersistentTS();
		try {
			new ExtendDeterministicPersistent().extendTs(ts, maxPhase2Rounds);
		} catch (MustLeadToSameStateException ex) {
			// Examine the state of the LTS when an exception is thrown
			assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"),
						nodeWithID("s2")));
			assertThat(ts.getEdges(), containsInAnyOrder(
						arcThatConnectsVia("s0", "s1", "a"),
						arcThatConnectsVia("s1", "s0", "a"),
						arcThatConnectsVia("s1", "s0", "b"),
						arcThatConnectsVia("s0", "s2", "b"),
						arcThatConnectsVia("s2", "s0", "a")));

			// Re-throw the original exception
			throw ex;
		}
		fail("This line should be unreachable");
	}

	@Test(expectedExceptions = MustLeadToSameStateException.class, expectedExceptionsMessageRegExp =
			"^Arcs "
			+ "(s0--a->s1 and s0--b->s2|s0--b->s2 and s0--a->s1) must lead to the same state\\.$")
	public void testDeterministicReachableReversibleNonPersistentTS() throws Exception {
		doDeterministicReachableReversibleNonPersistentTS(Integer.MAX_VALUE);
	}

	@Test(expectedExceptions = MustLeadToSameStateException.class, expectedExceptionsMessageRegExp =
			"^Arcs "
			+ "(s0--a->s1 and s0--b->s2|s0--b->s2 and s0--a->s1) must lead to the same state\\.$")
	public void testDeterministicReachableReversibleNonPersistentTS2() throws Exception {
		doDeterministicReachableReversibleNonPersistentTS(0);
	}

	@Test(expectedExceptions = MustLeadToSameStateException.class, expectedExceptionsMessageRegExp =
			"^Arcs "
			+ "(r1--a->r2 and l1--b->l2|l1--b->l2 and r1--a->r2) must lead to the same state\\.$")
	public void testABandBAnonPersistentTS() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("0", "l1", "l2", "r1", "r2");
		ts.setInitialState("0");
		ts.createArc("0", "l1", "a");
		ts.createArc("l1", "l2", "b");
		ts.createArc("0", "r1", "b");
		ts.createArc("r1", "r2", "a");
		new ExtendDeterministicPersistent().extendTs(ts, 0);
	}

	private TransitionSystem getAAorBATransitionSystem() {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("0", "a", "b", "f");
		ts.setInitialState("0");
		ts.createArc("0", "a", "a");
		ts.createArc("a", "f", "a");
		ts.createArc("0", "b", "b");
		ts.createArc("b", "f", "a");
		return ts;
	}

	@Test
	public void testAAorBATS0() throws Exception {
		TransitionSystem ts = getAAorBATransitionSystem();
		Set<Pair<State, String>> expectedFailure = new HashSet<>();
		expectedFailure.add(new Pair<>(ts.getNode("f"), "b"));
		expectedFailure.add(new Pair<>(ts.getNode("f"), "a"));
		assertThat(new ExtendDeterministicPersistent().extendTs(ts, 0), contains(expectedFailure));
	}

	@Test(expectedExceptions = NoFiniteExtensionPossibleException.class, expectedExceptionsMessageRegExp =
			"^State f is reachable"
			+ " via different, non-Parikh-equivalent firing sequences and needs completion$")
	public void testAAorBATS1() throws Exception {
		new ExtendDeterministicPersistent().extendTs(getAAorBATransitionSystem(), 1);
	}

	@Test(expectedExceptions = NoFiniteExtensionPossibleException.class, expectedExceptionsMessageRegExp =
			"^State s2 is reachable"
			+ " via different, non-Parikh-equivalent firing sequences and needs completion$")
	public void testNotExtendable1() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s2", "b");
		ts.createArc("s1", "s2", "c");
		new ExtendDeterministicPersistent().extendTs(ts, 1);
	}

	@Test(expectedExceptions = NoFiniteExtensionPossibleException.class, expectedExceptionsMessageRegExp =
			"^State s1 is reachable"
			+ " via different, non-Parikh-equivalent firing sequences and needs completion$")
	public void testNotExtendable2() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s1", "b");
		new ExtendDeterministicPersistent().extendTs(ts, 1);
	}

	@Test
	public void testProblematicPersistentTS() throws Exception {
		// This LTS has uncomparable Parikh vectors reaching the same state, but still is persistent
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s1", "b");
		ts.createArc("s1", "s1", "a");
		ts.createArc("s1", "s1", "b");
		noChanges(ts);
	}

	@Test
	public void testProblematicPersistentTS2() throws Exception {
		// This LTS has uncomparable Parikh vectors reaching the same state, but can still be persistently
		// completed
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s1", "b");
		ts.createArc("s1", "s1", "a");
		ts.createArc("s1", "s2", "c");

		extendSuccessfully(ts, 0);

		assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"), nodeWithID("s2")));
		assertThat(ts.getEdges(), containsInAnyOrder(
					// Pre-existing arcs
					arcThatConnectsVia("s0", "s1", "a"), arcThatConnectsVia("s0", "s1", "b"),
					arcThatConnectsVia("s1", "s1", "a"), arcThatConnectsVia("s1", "s2", "c"),
					// new arcs
					arcThatConnectsVia("s1", "s1", "b"), arcThatConnectsVia("s2", "s2", "a"),
					arcThatConnectsVia("s2", "s2", "b")));
	}

	@Test
	public void testProblematicPersistentTS3() throws Exception {
		// This LTS has uncomparable Parikh vectors reaching the same state, but can still be persistently
		// completed; the difference to the previous test is that the arc --c->s2 originates from s0 instead of
		// s1.
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s1", "b");
		ts.createArc("s1", "s1", "a");
		ts.createArc("s0", "s2", "c");

		extendSuccessfully(ts, Integer.MAX_VALUE);

		// One node was added, find it
		assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"),
					nodeWithID("s2"), anything()));
		Set<State> nodes = new HashSet<>(ts.getNodes());
		nodes.remove(ts.getNode("s0"));
		nodes.remove(ts.getNode("s1"));
		nodes.remove(ts.getNode("s2"));
		assertThat(nodes, hasSize(1));
		String sNew = nodes.iterator().next().getId();

		assertThat(ts.getEdges(), containsInAnyOrder(
					// Pre-existing arcs
					arcThatConnectsVia("s0", "s1", "a"), arcThatConnectsVia("s0", "s1", "b"),
					arcThatConnectsVia("s1", "s1", "a"), arcThatConnectsVia("s0", "s2", "c"),
					// new arcs
					arcThatConnectsVia("s1", sNew, "c"), arcThatConnectsVia("s2", sNew, "a"),
					arcThatConnectsVia("s2", sNew, "b"), arcThatConnectsVia("s1", "s1", "b"),
					arcThatConnectsVia(sNew, sNew, "a"), arcThatConnectsVia(sNew, sNew, "b")));
	}

	@Test
	public void testParallelDiamond() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2", "s3", "s4", "s5");
		ts.setInitialState("s0");
		ts.createArc("s0", "s2", "a");
		ts.createArc("s0", "s3", "b");
		ts.createArc("s1", "s3", "b");
		ts.createArc("s1", "s4", "a");
		ts.createArc("s4", "s5", "b");

		extendSuccessfully(ts, 0);

		assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"), nodeWithID("s2"),
					nodeWithID("s3"), nodeWithID("s4"), nodeWithID("s5")));
		assertThat(ts.getEdges(), containsInAnyOrder(
					// Pre-existing arcs
					arcThatConnectsVia("s0", "s2", "a"), arcThatConnectsVia("s0", "s3", "b"),
					arcThatConnectsVia("s1", "s3", "b"), arcThatConnectsVia("s1", "s4", "a"),
					arcThatConnectsVia("s4", "s5", "b"),
					// new arcs
					arcThatConnectsVia("s2", "s5", "b"), arcThatConnectsVia("s3", "s5", "a")));
	}

	private TransitionSystem getABCTS() {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "sa", "sb", "sc");
		ts.setInitialState("s0");
		ts.createArc("s0", "sa", "a");
		ts.createArc("s0", "sb", "b");
		ts.createArc("s0", "sc", "c");
		return ts;
	}

	@Test
	public void testPersistentABCTSSuccess() throws Exception {
		TransitionSystem ts = getABCTS();

		extendSuccessfully(ts, 2);

		// We have to figure out the names of the created nodes first
		assertThat(ts.getNodes(), hasSize(8));
		String sab = null, sac = null, sbc = null, sabc = null;
		for (Arc arc : ts.getPostsetEdges("sa")) {
			if (arc.getLabel().equals("b")) {
				assertThat(sab, nullValue());
				sab = arc.getTargetId();
			} else {
				assertThat(arc.getLabel(), equalTo("c"));
				assertThat(sac, nullValue());
				sac = arc.getTargetId();
			}
		}
		assertThat(sab, not(nullValue()));
		assertThat(sac, not(nullValue()));
		for (Arc arc : ts.getPostsetEdges("sb")) {
			if (arc.getLabel().equals("c")) {
				assertThat(sbc, nullValue());
				sbc = arc.getTargetId();
				sabc = arc.getTarget().getPostsetNodes().iterator().next().getId();
			} else {
				assertThat(arc.getLabel(), equalTo("a"));
				assertThat(arc.getTarget(), nodeWithID(sab));
			}
		}
		assertThat(sbc, not(nullValue()));
		assertThat(sabc, not(nullValue()));

		// Now check the structure of the ts
		assertThat(ts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("sa"), nodeWithID("sb"),
					nodeWithID("sc"), nodeWithID(sab), nodeWithID(sac), nodeWithID(sbc),
					nodeWithID(sabc)));
		assertThat(ts.getEdges(), containsInAnyOrder(
					arcThatConnectsVia("s0", "sa", "a"), arcThatConnectsVia("s0", "sb", "b"),
					arcThatConnectsVia("s0", "sc", "c"), arcThatConnectsVia("sa", sab, "b"),
					arcThatConnectsVia("sa", sac, "c"), arcThatConnectsVia("sb", sab, "a"),
					arcThatConnectsVia("sb", sbc, "c"), arcThatConnectsVia("sc", sac, "a"),
					arcThatConnectsVia("sc", sbc, "b"), arcThatConnectsVia(sab, sabc, "c"),
					arcThatConnectsVia(sac, sabc, "b"), arcThatConnectsVia(sbc, sabc, "a")));
	}

	@Test
	public void testPersistentABCTSFail1() throws Exception {
		TransitionSystem ts = getABCTS();

		Set<Pair<State, String>> expectedFailure1 = new HashSet<>();
		expectedFailure1.add(new Pair<>(ts.getNode("sa"), "b"));
		expectedFailure1.add(new Pair<>(ts.getNode("sb"), "a"));

		Set<Pair<State, String>> expectedFailure2 = new HashSet<>();
		expectedFailure2.add(new Pair<>(ts.getNode("sa"), "c"));
		expectedFailure2.add(new Pair<>(ts.getNode("sc"), "a"));

		Set<Pair<State, String>> expectedFailure3 = new HashSet<>();
		expectedFailure3.add(new Pair<>(ts.getNode("sb"), "c"));
		expectedFailure3.add(new Pair<>(ts.getNode("sc"), "b"));

		assertThat(new ExtendDeterministicPersistent().extendTs(ts, 0),
				containsInAnyOrder(expectedFailure1, expectedFailure2, expectedFailure3));
	}

	@Test
	public void testPersistentABCTSFail2() throws Exception {
		TransitionSystem ts = getABCTS();
		// This complains about states which were added in the first round. We could make the requirement that
		// all of these states are different and new, but let's be lazy and only test the minimum here.
		assertThat(new ExtendDeterministicPersistent().extendTs(ts, 1), contains(containsInAnyOrder(
						pairWith(anything(), is("a")),
						pairWith(anything(), is("b")),
						pairWith(anything(), is("c")))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
