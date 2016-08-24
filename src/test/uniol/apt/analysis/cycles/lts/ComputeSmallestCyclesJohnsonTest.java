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
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.util.matcher.Matchers.*;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 *
 * @author vsp
 */
@SuppressWarnings("unchecked")
public class ComputeSmallestCyclesJohnsonTest extends AbstractComputeSmallestCyclesTestBase {
	ComputeSmallestCycles createComputeSmallestCycles() {
		return new ComputeSmallestCyclesJohnson();
	}

	private static Matcher<? super Pair<? extends Iterable<String>, ParikhVector>>
			cycle(Collection<String> cycle, String... pv) {
		return pairWith(containsRotated(cycle.toArray(new String[0])), equalTo(new ParikhVector(pv)));
	}

	@Test
	public void testReversibleTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0", "s1", "s2"), "a", "b", "c")));
	}

	@Test
	public void testSingleStateLoop() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s"), "a")));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0"), "NotA")));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1"), "NotA")));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s", "t"), "a", "a")));
	}

	@Test
	public void testOneCycle() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s0", "s1", "s2", "s3"), "a", "b", "c", "d")));
	}

	@Test
	public void testOneCycle1() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1", "s2", "s3"), "a", "a", "d")));
	}

	@Test
	public void testTwoCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(false));
		assertThat(calc.checkSamePVs(ts), equalTo(false));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s1", "s2", "s3"), "b", "c", "d"),
					cycle(Arrays.asList("s1", "s4", "s5"), "b", "b", "b")
					));
	}

	@Test
	public void testTwoIntersectingCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("s1", "s3", "s4", "s5"), "b", "b", "b", "d")));
	}

	@Test
	public void testCyclesWithSameParikhVector() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, containsInAnyOrder(
					cycle(Arrays.asList("s1", "s2", "s3", "s4", "s5"), "b", "b", "b", "c", "d"),
					cycle(Arrays.asList("s1", "s6", "s3", "s4", "s5"), "b", "b", "b", "c", "d")
					));
	}

	@Test
	public void testCyclesWithDisjunktParikhVector() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
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
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
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
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
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
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getRemovalOfNonSmallCyclesTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertThat(calc.checkSameOrMutallyDisjointPVs(ts), equalTo(true));
		assertThat(calc.checkSamePVs(ts), equalTo(true));
		assertThat(c, contains(cycle(Arrays.asList("7", "8", "9"), "a", "b", "c")));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
