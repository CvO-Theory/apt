/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2015  Members of the project group APT
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

package uniol.apt.analysis.cycles.lts;

import java.util.List;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.matcher.Matchers.*;
import uniol.apt.TestTSCollection;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.PreconditionFailedException;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 * @author Uli Schlachter, (Manuel Gieseking)
 */
@SuppressWarnings("unchecked") // I hate generics
public class AllSmallCyclesHavePVOneTest {
	static void checkCyclesHavePV1(TransitionSystem ts) throws PreconditionFailedException {
		AllSmallCyclesHavePVOne check = new AllSmallCyclesHavePVOne(ts);
		assertThat(check.smallCyclesHavePVOne(), is(true));
		assertThat(check.noPV1CycleFound(), is(false));
		assertThat(check.getCounterExample(), empty());
	}

	static void checkCyclesLargerPV1(TransitionSystem ts) throws PreconditionFailedException {
		AllSmallCyclesHavePVOne check = new AllSmallCyclesHavePVOne(ts);
		assertThat(check.smallCyclesHavePVOne(), is(false));
		assertThat(check.noPV1CycleFound(), is(true));
		assertThat(check.getCounterExample(), empty());
	}

	static List<Arc> checkCyclesSmallerPV1(TransitionSystem ts) throws PreconditionFailedException {
		AllSmallCyclesHavePVOne check = new AllSmallCyclesHavePVOne(ts);
		assertThat(check.smallCyclesHavePVOne(), is(false));
		assertThat(check.noPV1CycleFound(), is(true));
		return check.getCounterExample();
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = "TS  is not deterministic")
	public void testNonDeterministicTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		new AllSmallCyclesHavePVOne(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = "TS  is not totally reachable")
	public void testSingleStateWithUnreachableTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		new AllSmallCyclesHavePVOne(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = "TS  is not reversible")
	public void testThreeStatesTwoEdgesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		new AllSmallCyclesHavePVOne(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = "TS  is not persistent")
	public void testDeterministicReachableReversibleNonPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDeterministicReachableReversibleNonPersistentTS();
		new AllSmallCyclesHavePVOne(ts);
	}

	@Test
	public void testReversibleTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		checkCyclesHavePV1(ts);
	}

	@Test
	public void testSingleStateLoop() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		checkCyclesHavePV1(ts);
	}

	@Test
	public void testSingleStateSingleTransitionTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		checkCyclesHavePV1(ts);
	}

	@Test
	public void testSingleStateTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		checkCyclesHavePV1(ts);
	}

	@Test
	public void testTwoStateCycleSameLabelTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		checkCyclesLargerPV1(ts);
	}

	@Test
	public void testOneCycle() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		checkCyclesHavePV1(ts);
	}

	@Test
	public void testSmallerCycle() throws Exception {
		TransitionSystem ts = TestTSCollection.getDifferentCyclesTS();
		assertThat(checkCyclesSmallerPV1(ts), anyOf(
					contains(arcThatConnects("s11", "s12"), arcThatConnects("s12", "s11")),
					contains(arcThatConnects("s11", "s10"), arcThatConnects("s10", "s11")),
					contains(arcThatConnects("s11", "s21"), arcThatConnects("s21", "s11")),
					contains(arcThatConnects("s11", "s01"), arcThatConnects("s01", "s11"))
					));
	}

	@Test
	public void testWithUncomparableCycle() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1");
		ts.setInitialState("s0");

		ts.createArc("s0", "s1", "a");
		ts.createArc("s0", "s1", "b");
		ts.createArc("s1", "s0", "a");
		ts.createArc("s1", "s0", "b");

		// This lts has three small cycles: a,b; a,a and b,b; obviously the last two don't have a Parikh-vector
		// of all ones. However, the special thing is that they are incomparable to a PV of (1,1).
		AllSmallCyclesHavePVOne check = new AllSmallCyclesHavePVOne(ts);
		assertThat(check.smallCyclesHavePVOne(), is(false));
		assertThat(check.noPV1CycleFound(), is(false));
		assertThat(check.getCounterExample(), anyOf(
					contains(arcThatConnectsVia("s0", "s1", "a"), arcThatConnectsVia("s1", "s0", "a")),
					contains(arcThatConnectsVia("s0", "s1", "b"), arcThatConnectsVia("s1", "s0", "b"))));
	}

	@Test
	public void testWithDoubleCycle() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2");
		ts.setInitialState("s0");

		ts.createArc("s0", "s1", "a");
		ts.createArc("s1", "s2", "a");
		ts.createArc("s2", "s1", "b");
		ts.createArc("s1", "s0", "b");

		checkCyclesHavePV1(ts);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
