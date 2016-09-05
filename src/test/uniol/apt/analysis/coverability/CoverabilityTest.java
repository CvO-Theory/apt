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

import org.hamcrest.Matcher;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;

import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.adt.pn.Token;
import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author Uli Schlachter, vsp */
@SuppressWarnings("unchecked") // I hate generics
public class CoverabilityTest {

	private CycleGenerator cycleGenerator;
	private BistatePhilNetGenerator biStatePhilNetGenerator;
	private TristatePhilNetGenerator triStatePhilNetGenerator;
	private QuadstatePhilNetGenerator quadStatePhilNetGenerator;

	@BeforeClass
	public void setup() {
		cycleGenerator = new CycleGenerator();
		biStatePhilNetGenerator = new BistatePhilNetGenerator();
		triStatePhilNetGenerator = new TristatePhilNetGenerator();
		quadStatePhilNetGenerator = new QuadstatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		cycleGenerator = null;
		biStatePhilNetGenerator = null;
		triStatePhilNetGenerator = null;
		quadStatePhilNetGenerator = null;
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) {
		PetriNet pn = cycleGenerator.generateNet(size);

		List<Matcher<? super State>> nodeMatchers = new ArrayList<>();
		List<Matcher<? super Arc>> edgeMatchers = new ArrayList<>();

		Marking initialMark = pn.getInitialMarking();
		Marking curMark = pn.getInitialMarking();
		Marking prevMark;

		for (int i = 0; i < size; i++) {
			prevMark = curMark;
			Transition t = pn.getTransition("t" + Integer.toString(i));
			curMark = t.fire(curMark);
			nodeMatchers.add(nodeMarkingEq(curMark));
			edgeMatchers.add(edgeNodesMarkingEq(prevMark, curMark));
		}

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// i=1: init <-    (self loop)
		// i>1: cycle of size i
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(nodeMatchers));
		assertThat(lts.getEdges(), containsInAnyOrder(edgeMatchers));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testBistatePhilNet(int size) {
		PetriNet pn = biStatePhilNetGenerator.generateNet(size);
		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		/* These LTS are growing quite fast. I don't think we can sanely calculate the number of edges
		 * and nodes. We shouldn't even think about trying to match the exact nodes and edges.
		 */
		assertEquals(lts.getNodes().size(), size + 1);
		assertEquals(lts.getEdges().size(), size * 2);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testTristatePhilNet(int size) {
		PetriNet pn = triStatePhilNetGenerator.generateNet(size);
		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		/* These LTS are growing quite fast. I don't think we can sanely calculate the number of edges
		 * and nodes. We shouldn't even think about trying to match the exact nodes and edges.
		 */
		assertEquals(lts.getNodes().size(), size * 8 - 10);
		assertEquals(lts.getEdges().size(), size * 19 - 30);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testQuadstatePhilNet(int size) {
		PetriNet pn = quadStatePhilNetGenerator.generateNet(size);
		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		/* These LTS are growing quite fast. I don't think we can sanely calculate the number of edges
		 * and nodes. We shouldn't even think about trying to match the exact nodes and edges.
		 */
		assertEquals(lts.getNodes().size(), size * 25 - 40);
		assertEquals(lts.getEdges().size(), size * 61 - 108);
	}

	@Test
	public void testEmptyNet() {
		PetriNet pn = getEmptyNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init       (no edge)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), contains(
					nodeMarkingEq(initialMark)));
		assertEquals(lts.getEdges().size(), 0);
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		PetriNet pn = getNoTransitionOnePlaceNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init       (no edge)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), contains(
					nodeMarkingEq(initialMark)));
		assertEquals(lts.getEdges().size(), 0);
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		PetriNet pn = getOneTransitionNoPlaceNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init <-    (self loop)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), contains(
					nodeMarkingEq(initialMark)));
		assertThat(lts.getEdges(), contains(
					edgeNodesMarkingEq(initialMark, initialMark)));
	}

	@Test
	public void testTokenGeneratorNet() {
		PetriNet pn = getTokenGeneratorNet();
		Marking initialMark = pn.getInitialMarking();
		Marking aMark = initialMark.setTokenCount(pn.getPlaces().iterator().next(), Token.OMEGA);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init --> a <-    (self loop)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(aMark, aMark)));
	}

	@Test
	public void testDeadlockNet() {
		PetriNet pn = getDeadlockNet();
		Transition transitions[] = pn.getTransitions().toArray(new Transition[0]);
		Marking initialMark = pn.getInitialMarking();
		Marking aMark = transitions[0].fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init ==> a (2 edges to a)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(initialMark, aMark)));
	}

	@Test
	public void testNonPersistentNet() {
		PetriNet pn = getNonPersistentNet();
		Marking initialMark = pn.getInitialMarking();
		Transition ta = pn.getTransition("a");
		Marking aMark = ta.fire(initialMark);

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// init ==> a (2 edges to a)
		//      <--   (1 edge back to init)
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(aMark, initialMark)));
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
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		//     -> init <-   (every edge is bidirectional)
		//    /          \
		// a <            > b
		//    \          /
		//     ->   c  <-
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark),
					nodeMarkingEq(abMark),
					nodeMarkingEq(bMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(initialMark, bMark),
					edgeNodesMarkingEq(aMark, initialMark),
					edgeNodesMarkingEq(aMark, abMark),
					edgeNodesMarkingEq(bMark, initialMark),
					edgeNodesMarkingEq(bMark, abMark),
					edgeNodesMarkingEq(abMark, aMark),
					edgeNodesMarkingEq(abMark, bMark)));
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
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// a <-- init --> b
		//    \        /
		//     ->  c <-
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark),
					nodeMarkingEq(abMark),
					nodeMarkingEq(bMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(initialMark, bMark),
					edgeNodesMarkingEq(aMark, abMark),
					edgeNodesMarkingEq(bMark, abMark)));
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
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS looks like:
		// a <-- init --> b
		//    \        /
		//     ->  c <-
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), containsInAnyOrder(
					nodeMarkingEq(initialMark),
					nodeMarkingEq(aMark),
					nodeMarkingEq(abMark),
					nodeMarkingEq(bMark)));
		assertThat(lts.getEdges(), containsInAnyOrder(
					edgeNodesMarkingEq(initialMark, aMark),
					edgeNodesMarkingEq(initialMark, bMark),
					edgeNodesMarkingEq(aMark, abMark),
					edgeNodesMarkingEq(bMark, abMark)));
	}

	@Test
	public void testMultiArcNet() {
		PetriNet pn = getMultiArcNet();
		Marking initialMark = pn.getInitialMarking();

		CoverabilityGraph cov = CoverabilityGraph.get(pn);
		TransitionSystem lts = cov.toCoverabilityLTS();

		// LTS has a self-loop in the initial state with weight 2
		assertThat(lts.getInitialState(), nodeMarkingEq(initialMark));
		assertThat(lts.getNodes(), contains(nodeMarkingEq(initialMark)));
		assertThat(lts.getEdges(), contains(edgeNodesMarkingEq(initialMark, initialMark)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
