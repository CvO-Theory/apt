/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

import uniol.apt.adt.ts.ParikhVector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.util.matcher.Matchers.*;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 *
 * @author vsp
 */
@SuppressWarnings("unchecked")
public class ComputeSmallestCyclesTest {
	private static Matcher<? super Cycle> cycleWithNodeIDs(Matcher<Iterable<? extends String>> nodes) {
		return new FeatureMatcher<Cycle, List<String>>(nodes, "node IDs", "node IDs") {
			@Override
			protected List<String> featureValueOf(Cycle cycle) {
				return cycle.getNodeIDs();
			}
		};
	}

	private static Matcher<? super Cycle> cycleWithPV(Matcher<ParikhVector> pv) {
		return new FeatureMatcher<Cycle, ParikhVector>(pv, "Parikh vector", "Parikh vector") {
			@Override
			protected ParikhVector featureValueOf(Cycle cycle) {
				return cycle.getParikhVector();
			}
		};
	}

	private static Matcher<? super Cycle> cycle(Collection<String> cycle, String... pv) {
		return allOf(cycleWithNodeIDs(containsRotated(cycle.toArray(new String[0]))),
				cycleWithPV(equalTo(new ParikhVector(pv))));
	}

	// TESTTSCOLLECTION
	@Test
	public void testNonDeterministicTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testNonPersistentTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testNotTotallyReachableTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testPersistentTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testSingleStateTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testNoCycle() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, empty());
	}

	@Test
	public void testReversibleTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0", "s1", "s2"), "a", "b", "c")));
	}

	@Test
	public void testSingleStateLoop() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s"), "a")));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0"), "NotA")));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1"), "NotA")));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s", "t"), "a", "a")));
	}

	@Test
	public void testOneCycle() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0", "s1", "s2", "s3"), "a", "b", "c", "d")));
	}

	@Test
	public void testOneCycle1() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1", "s2", "s3"), "a", "a", "d")));
	}

	@Test
	public void testTwoCycles() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(false));
		assertThat(calc.checkSamePVs(ts), equalTo(false));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s1", "s2", "s3"), "b", "c", "d"),
					cycle(Arrays.asList("s1", "s4", "s5"), "b", "b", "b")
					));
	}

	@Test
	public void testTwoIntersectingCycles() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1", "s3", "s4", "s5"), "b", "b", "b", "d")));
	}

	@Test
	public void testCyclesWithSameParikhVector() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s1", "s2", "s3", "s4", "s5"), "b", "b", "b", "c", "d"),
					cycle(Arrays.asList("s1", "s6", "s3", "s4", "s5"), "b", "b", "b", "c", "d")
					));
	}

	@Test
	public void testCyclesWithDisjunktParikhVector() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(false));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s0"), "a"),
					cycle(Arrays.asList("s1"), "b"),
					cycle(Arrays.asList("s2"), "c"),
					cycle(Arrays.asList("s3"), "d")
					));
	}

	@Test
	public void testCyclesWithSameParikhVector1() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(false));
		assertThat(calc.checkSamePVs(ts), equalTo(false));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s0", "s1"), "a", "b"),
					cycle(Arrays.asList("s1", "s2"), "a", "b"),
					cycle(Arrays.asList("s0", "s3"), "a", "b"),
					cycle(Arrays.asList("s3", "s4", "s5", "s6"), "a", "c", "c", "c")
					));
	}

	@Test
	public void testFullyConnected() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(false));
		assertThat(calc.checkSamePVs(ts), equalTo(false));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s0", "s1"), "a", "b"),
					cycle(Arrays.asList("s0", "s2"), "a", "c"),
					cycle(Arrays.asList("s1", "s2"), "b", "c")
					));
	}

	@Test
	public void testRemovalOfNonSmallCycles() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getRemovalOfNonSmallCyclesTS();
		Set<Cycle> c = calc.computePVsOfSmallestCyclesViaCycleSearch(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("7", "8", "9"), "a", "b", "c")));
	}

	protected TransitionSystem getRemovalOfNonSmallCyclesTS() {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
		ts.setInitialState("0");
		ts.createArc("0", "1", "a");
		ts.createArc("1", "2", "b");
		ts.createArc("2", "3", "c");
		ts.createArc("3", "4", "a");
		ts.createArc("4", "5", "b");
		ts.createArc("4", "6", "c");
		ts.createArc("5", "0", "c");
		ts.createArc("6", "0", "b");

		// Just for reachability
		ts.createArc("4", "7", "whatever");

		// The datastructures make sure that state "0" stays the first state in getNodes() and thus the
		// implementation in ComputeSmallestCycles will find cycles going through that state first. Thus,
		// when this state is reached, two cycles with Parikh vector 2*[a,b,c] are currently known. The bug that
		// we are testing is that only one of these will be removed when the following cycle is found.

		ts.createArc("7", "8", "a");
		ts.createArc("8", "9", "b");
		ts.createArc("9", "7", "c");

		return ts;
	}

	protected boolean testCycleAndParikh(Set<Cycle> c, String cycle, String... parikh) {
		for (Cycle cyc : c) {
			if (cyc.getNodeIDs().toString().equals(cycle)
				&& cyc.getParikhVector().equals(new ParikhVector(parikh))) {
				return true;
			}
		}
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
