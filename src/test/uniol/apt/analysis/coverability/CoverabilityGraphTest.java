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

package uniol.apt.analysis.coverability;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.hamcrest.Matcher;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Transition;
import uniol.apt.generator.cycle.CycleGenerator;

import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.adt.pn.Token;
import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author Uli Schlachter, vsp */
@SuppressWarnings("unchecked") // I hate generics
public class CoverabilityGraphTest {

	private CycleGenerator cycleGenerator;

	@BeforeClass
	public void setup() {
		cycleGenerator = new CycleGenerator();
	}

	@AfterClass
	public void teardown() {
		cycleGenerator = null;
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) {
		PetriNet pn = cycleGenerator.generateNet(size);

		List<Matcher<? super CoverabilityGraphNode>> nodeMatchers = new ArrayList<>();
		List<Matcher<? super CoverabilityGraphEdge>> edgeMatchers = new ArrayList<>();

		Marking initialMark = pn.getInitialMarking();
		Marking curMark = pn.getInitialMarking();
		Marking prevMark;

		for (int i = 0; i < size; i++) {
			prevMark = curMark;
			Transition t = pn.getTransition("t" + Integer.toString(i));
			curMark = t.fire(curMark);
			nodeMatchers.add(coverNodeMarkingEq(curMark));
			edgeMatchers.add(edgeCoverNodesMarkingEq(prevMark, curMark));
		}

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// i=1: init <-    (self loop)
		// i>1: cycle of size i
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(nodeMatchers));
		assertThat(cov.getEdges(), containsInAnyOrder(edgeMatchers));
	}

	@Test
	public void testCache() {
		PetriNet pn = getEmptyNet();

		CoverabilityGraph cov1 = CoverabilityGraph.get(pn);
		CoverabilityGraph cov2 = CoverabilityGraph.get(pn);
		assertThat(cov2, sameInstance(cov1));
	}

	@Test
	public void testCacheReachability() {
		PetriNet pn = getEmptyNet();

		CoverabilityGraph cov1 = CoverabilityGraph.getReachabilityGraph(pn);
		CoverabilityGraph cov2 = CoverabilityGraph.getReachabilityGraph(pn);
		assertThat(cov2, sameInstance(cov1));
	}

	@Test
	public void testCacheMixup() {
		PetriNet pn = getEmptyNet();

		CoverabilityGraph cov1 = CoverabilityGraph.get(pn);
		CoverabilityGraph cov2 = CoverabilityGraph.getReachabilityGraph(pn);
		assertThat(cov2, not(sameInstance(cov1)));
	}

	@Test
	public void testCacheClear() {
		PetriNet pn = getEmptyNet();

		CoverabilityGraph cov1 = CoverabilityGraph.get(pn);
		CoverabilityGraph re1 = CoverabilityGraph.getReachabilityGraph(pn);

		pn.createPlace();

		CoverabilityGraph cov2 = CoverabilityGraph.get(pn);
		CoverabilityGraph re2 = CoverabilityGraph.getReachabilityGraph(pn);

		assertThat(cov2, not(sameInstance(cov1)));
		assertThat(re2, not(sameInstance(re1)));
	}

	@Test
	public void testCacheAfterCopy() {
		PetriNet pn = getEmptyNet();

		CoverabilityGraph cov1 = CoverabilityGraph.get(pn);
		CoverabilityGraph re1 = CoverabilityGraph.getReachabilityGraph(pn);

		PetriNet pn2 = new PetriNet(pn);

		CoverabilityGraph cov2 = CoverabilityGraph.get(pn2);
		CoverabilityGraph re2 = CoverabilityGraph.getReachabilityGraph(pn2);

		pn2.createPlace();
		CoverabilityGraph cov3 = CoverabilityGraph.get(pn2);
		CoverabilityGraph re3 = CoverabilityGraph.getReachabilityGraph(pn2);

		assertThat(cov2, not(sameInstance(cov1)));
		assertThat(re2, not(sameInstance(re1)));
		assertThat(cov3, not(sameInstance(cov1)));
		assertThat(re3, not(sameInstance(re1)));
		assertThat(cov3, not(sameInstance(cov2)));
		assertThat(re3, not(sameInstance(re2)));
	}

	@Test
	public void testEmptyNet() {
		PetriNet pn = getEmptyNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init       (no edge)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), contains(
			coverNodeMarkingEq(initialMark)));
		assertThat(cov.getEdges(), emptyIterable());
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		PetriNet pn = getNoTransitionOnePlaceNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init       (no edge)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), contains(
			coverNodeMarkingEq(initialMark)));
		assertThat(cov.getEdges(), emptyIterable());
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		PetriNet pn = getOneTransitionNoPlaceNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init <-    (self loop)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), contains(
			coverNodeMarkingEq(initialMark)));
		assertThat(cov.getEdges(), contains(
			edgeCoverNodesMarkingEq(initialMark, initialMark)));
	}

	@Test
	public void testTokenGeneratorNet() {
		PetriNet pn = getTokenGeneratorNet();
		Marking initialMark = pn.getInitialMarking();
		Marking aMark = initialMark.setTokenCount(pn.getPlaces().iterator().next(), Token.OMEGA);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init --> a <-    (self loop)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(aMark, aMark)));
	}

	@Test
	public void testDeadlockNet() {
		PetriNet pn = getDeadlockNet();
		Transition transitions[] = pn.getTransitions().toArray(new Transition[0]);
		Marking initialMark = pn.getInitialMarking();
		Marking aMark = transitions[0].fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init ==> a (2 edges to a)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(initialMark, aMark)));
	}

	@Test
	public void testNonPersistentNet() {
		PetriNet pn = getNonPersistentNet();
		Marking initialMark = pn.getInitialMarking();
		Transition ta = pn.getTransition("a");
		Marking aMark = ta.fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// init ==> a (2 edges to a)
		//      <--   (1 edge back to init)
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(aMark, initialMark)));
	}

	@Test
	public void checkPersistentBiCFNet() {
		PetriNet pn = getPersistentBiCFNet();
		Transition ta = pn.getTransition("a");
		Transition tb = pn.getTransition("b");

		Marking initialMark = pn.getInitialMarking();
		Marking aMark = ta.fire(initialMark);
		Marking abMark = tb.fire(aMark);
		Marking bMark = tb.fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		//     -> init <-   (every edge is bidirectional)
		//    /          \
		// a <            > b
		//    \          /
		//     ->   c  <-
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark),
			coverNodeMarkingEq(abMark),
			coverNodeMarkingEq(bMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(initialMark, bMark),
			edgeCoverNodesMarkingEq(aMark, initialMark),
			edgeCoverNodesMarkingEq(aMark, abMark),
			edgeCoverNodesMarkingEq(bMark, initialMark),
			edgeCoverNodesMarkingEq(bMark, abMark),
			edgeCoverNodesMarkingEq(abMark, aMark),
			edgeCoverNodesMarkingEq(abMark, bMark)));
	}

	@Test
	public void testConcurrentDiamondNet() {
		PetriNet pn = getConcurrentDiamondNet();
		Transition transitions[] = pn.getTransitions().toArray(new Transition[0]);

		Marking initialMark = pn.getInitialMarking();
		Marking aMark = transitions[0].fire(initialMark);
		Marking abMark = transitions[1].fire(aMark);
		Marking bMark = transitions[1].fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// a <-- init --> b
		//    \        /
		//     ->  c <-
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark),
			coverNodeMarkingEq(abMark),
			coverNodeMarkingEq(bMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(initialMark, bMark),
			edgeCoverNodesMarkingEq(aMark, abMark),
			edgeCoverNodesMarkingEq(bMark, abMark)));
	}

	@Test
	public void testConflictingDiamondNet() {
		PetriNet pn = getConflictingDiamondNet();
		Transition transitions[] = pn.getTransitions().toArray(new Transition[0]);

		Marking initialMark = pn.getInitialMarking();
		Marking aMark = transitions[0].fire(initialMark);
		Marking abMark = transitions[1].fire(aMark);
		Marking bMark = transitions[1].fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS looks like:
		// a <-- init --> b
		//    \        /
		//     ->  c <-
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), containsInAnyOrder(
			coverNodeMarkingEq(initialMark),
			coverNodeMarkingEq(aMark),
			coverNodeMarkingEq(abMark),
			coverNodeMarkingEq(bMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, aMark),
			edgeCoverNodesMarkingEq(initialMark, bMark),
			edgeCoverNodesMarkingEq(aMark, abMark),
			edgeCoverNodesMarkingEq(bMark, abMark)));
	}

	@Test
	public void testMultiArcNet() {
		PetriNet pn = getMultiArcNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);

		// LTS has a self-loop in the initial state with weight 2
		assertThat(cov.getInitialNode(), coverNodeMarkingEq(initialMark));
		assertThat(cov.getNodes(), contains(coverNodeMarkingEq(initialMark)));
		assertThat(cov.getEdges(), containsInAnyOrder(
			edgeCoverNodesMarkingEq(initialMark, initialMark),
			edgeCoverNodesMarkingEq(initialMark, initialMark)));
	}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void testIteratorHasNext() {
		PetriNet pn = getTokenGeneratorNet();
		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		Iterator<CoverabilityGraphEdge> it = cov.getEdges().iterator();
		int edges = 0;

		// LTS looks like:
		// init --> a <-    (self loop)
		while (it.hasNext()) {
			edges++;
			it.next();
		}
		assertEquals(edges, 2);

		// Make sure that the iterator doesn't break after being used
		assertFalse(it.hasNext());

		// And finally throw some exception
		it.next();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
