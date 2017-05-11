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

package uniol.apt.analysis.cycles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
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
public class CycleSearchTest {
	private List<List<Pair<State, Arc>>> getCycles(TransitionSystem ts) {
		final List<List<Pair<State, Arc>>> cycles = new ArrayList<>();
		new CycleSearch().searchCycles(ts, new CycleCallback<TransitionSystem, Arc, State>() {
			@Override
			public void cycleFound(List<State> nodes, List<Arc> edges) {
				cycles.add(Pair.zip(nodes, edges));
			}
		});
		return cycles;
	}

	private static Matcher<Iterable<? extends Pair<State, Arc>>> cycle(List<String> nodes, List<String> edges) {
		if (nodes.size() != edges.size())
			throw new IllegalArgumentException("Lists need equal sizes");

		List<Matcher<? super Pair<State, Arc>>> ret = new ArrayList<>();
		Iterator<String> it = edges.iterator();
		for (String node : nodes) {
			assert it.hasNext();
			String edge = it.next();

			ret.add(pairWith(nodeWithID(node), arcWithLabel(edge)));
		}
		return containsRotated(ret);
	}

	@Test
	public void testEmptyTS() {
		TransitionSystem ts = new TransitionSystem();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	// TESTTSCOLLECTION
	@Test
	public void testNonDeterministicTS() {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testNonPersistentTS() {
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testDeterministicReachableReversibleNonPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDeterministicReachableReversibleNonPersistentTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s0", "s1"), Arrays.asList("a", "a")),
			cycle(Arrays.asList("s0", "s2"), Arrays.asList("b", "a"))
		));
	}

	@Test
	public void testNotTotallyReachableTS() {
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testPersistentTS() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testSingleStateTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testReversibleTS() {
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s0", "s1", "s2"), Arrays.asList("a", "b", "c"))
		));
	}

	@Test
	public void testSingleStateLoop() {
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s"), Arrays.asList("a"))
		));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s0"), Arrays.asList("NotA"))
		));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s1"), Arrays.asList("NotA"))
		));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s", "t"), Arrays.asList("a", "a"))
		));
	}

	@Test
	public void testDetPersButNotDisjointSmallCyclesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDetPersButNotDisjointSmallCyclesTS();
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s", "t"), Arrays.asList("a", "a")),
			cycle(Arrays.asList("s", "t"), Arrays.asList("a", "b")),
			cycle(Arrays.asList("s", "t"), Arrays.asList("b", "a")),
			cycle(Arrays.asList("s", "t"), Arrays.asList("b", "b"))
		));
	}

	@Test
	public void testNoCycle() {
		TransitionSystem ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testOneCycle() {
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s0", "s1", "s2", "s3"), Arrays.asList("a", "b", "c", "d"))
		));
	}

	@Test
	public void testOneCycle1() {
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, contains(
			cycle(Arrays.asList("s1", "s2", "s3"), Arrays.asList("a", "a", "d"))
		));
	}

	@Test
	public void testTwoCycles() {
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s1", "s2", "s3"), Arrays.asList("b", "c", "d")),
			cycle(Arrays.asList("s1", "s4", "s5"), Arrays.asList("b", "b", "b"))
		));
	}

	@Test
	public void testTwoIntersectingCycles() {
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s1", "s3", "s4", "s5"), Arrays.asList("b", "d", "b", "b")),
			cycle(Arrays.asList("s1", "s2", "s3", "s4", "s5"), Arrays.asList("b", "c", "d", "b", "b"))
		));
	}

	@Test
	public void testCyclesWithSameParikhVector() {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s1", "s2", "s3", "s4", "s5"), Arrays.asList("b", "c", "d", "b", "b")),
			cycle(Arrays.asList("s1", "s6", "s3", "s4", "s5"), Arrays.asList("b", "c", "d", "b", "b"))
		));
	}

	@Test
	public void testCyclesWithDisjunktParikhVector() {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s0"), Arrays.asList("a")),
			cycle(Arrays.asList("s1"), Arrays.asList("b")),
			cycle(Arrays.asList("s2"), Arrays.asList("c")),
			cycle(Arrays.asList("s3"), Arrays.asList("d")),
			cycle(Arrays.asList("s1", "s2", "s3", "s4", "s5"), Arrays.asList("b", "c", "d", "b", "b")),
			cycle(Arrays.asList("s1", "s6", "s3", "s4", "s5"), Arrays.asList("b", "c", "d", "b", "b"))
		));
	}

	@Test
	public void testCyclesWithSameParikhVector1() {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s0", "s1"), Arrays.asList("a", "b")),
			cycle(Arrays.asList("s1", "s2"), Arrays.asList("b", "a")),
			cycle(Arrays.asList("s0", "s3"), Arrays.asList("a", "b")),
			cycle(Arrays.asList("s3", "s4", "s5", "s6"), Arrays.asList("a", "c", "c", "c"))
		));
	}

	@Test
	public void testFullyConnected() {
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s0", "s1"), Arrays.asList("a", "b")),
			cycle(Arrays.asList("s0", "s2"), Arrays.asList("a", "c")),
			cycle(Arrays.asList("s1", "s2"), Arrays.asList("b", "c")),
			cycle(Arrays.asList("s0", "s1", "s2"), Arrays.asList("a", "b", "c")),
			cycle(Arrays.asList("s0", "s2", "s1"), Arrays.asList("a", "c", "b"))
		));
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

		List<List<Pair<State, Arc>>> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			cycle(Arrays.asList("s0", "s1"), Arrays.asList("a", "b")),
			cycle(Arrays.asList("s1", "s2"), Arrays.asList("a", "b"))
		));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
