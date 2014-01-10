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

package uniol.apt.analysis.cycles.lts;

import uniol.apt.adt.ts.ParikhVector;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import uniol.apt.TestTSCollection;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class ComputeCyclesTest {

	@Test
	public void testEmptyTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = new TransitionSystem();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

	}

	// TESTTSCOLLECTION
	@Test
	public void testNonDeterministicTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testNonPersistentTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		Set<Pair<List<String>, ParikhVector>> c =
			calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testNotTotallyReachableTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testPersistentTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testReversibleTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2]", "{a=1, b=1, c=1}"));

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertEquals(c.size(), 3);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s0]", "{a=1, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s0, s1]", "{a=1, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s0, s1, s2]", "{a=1, b=1, c=1}"));
	}

	@Test
	public void testSingleStateLoop() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s]", "{a=1}"));

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s, s]", "{a=1}"));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0]", "{NotA=1}"));

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s0]", "{NotA=1}"));
	}

	@Test
	public void testSingleStateTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s1, s1]", "{NotA=1}"));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(c.isEmpty());

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s, t]", "{a=2}"));

		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
		assertEquals(c.size(), 2);
		assertTrue(testCycleAndParikh(c, "[s, t, s]", "{a=2}"));
		assertTrue(testCycleAndParikh(c, "[t, s, t]", "{a=2}"));
	}

	@Test
	public void testSameOrDisjointParikhVectors() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
	}

	@Test
	public void testSameParikhVectors() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();

		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertTrue(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.DFS));
		assertFalse(calc.checkSamePVs(ts, ComputeSmallestCycles.Algorithm.FloydWarshall));
	}

	@Test
	public void testSmallestCycles() {
		ComputeSmallestCycles calc = new ComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts,
			ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s3]", "{a=1, b=1, c=1, d=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s3, s0]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s0, s1]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s0, s1, s2]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s0, s1, s2, s3]", "{a=1, b=1, c=1, d=1}"));

		ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3]", "{a=2, b=0, d=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 3);
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s1]", "{a=2, b=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s1, s2]", "{a=2, b=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s1, s2, s3]", "{a=2, b=0, d=1}"));

		ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 0);
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 0);

		ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 2);
		//bcd
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3]", "{a=0, b=1, c=1, d=1}"));
		//bbb
		assertTrue(testCycleAndParikh(c, "[s1, s4, s5]", "{a=0, b=3, c=0, d=0}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 6);
		//bcd
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s1]", "{a=0, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s1, s2]", "{a=0, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s1, s2, s3]", "{a=0, b=1, c=1, d=1}"));
		//bbb
		assertTrue(testCycleAndParikh(c, "[s1, s4, s5, s1]", "{a=0, b=3, c=0, d=0}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s4]", "{a=0, b=3, c=0, d=0}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s4, s5]", "{a=0, b=3, c=0, d=0}"));

		ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 1);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s3, s4, s5]", "{a=0, b=3, c=0, d=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 4);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s3, s4, s5, s1]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s1, s3]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s3, s4]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s3, s4, s5]", "{a=0, b=3, c=0, d=1}"));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 2);
		//bcdbb
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s4, s5]", "{a=0, b=3, c=1, d=1}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s6, s3, s4, s5]", "{a=0, b=3, c=1, d=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 10);
		//bcdbb
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s4, s5, s1]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s4, s5, s1, s2]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s1, s2, s3]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s2, s3, s4]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s2, s3, s4, s5]", "{a=0, b=3, c=1, d=1}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s6, s3, s4, s5, s1]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s6, s3, s4, s5, s1, s6]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s1, s6, s3]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s6, s3, s4]", "{a=0, b=3, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s6, s3, s4, s5]", "{a=0, b=3, c=1, d=1}"));

		ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 4);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s0]", "{a=1, b=0, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1]", "{a=0, b=1, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s2]", "{a=0, b=0, c=1, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s3]", "{a=0, b=0, c=0, d=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 4);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s0, s0]", "{a=1, b=0, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s1]", "{a=0, b=1, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s2, s2]", "{a=0, b=0, c=1, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s3, s3]", "{a=0, b=0, c=0, d=1}"));

		ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s0, s3]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s6]", "{a=1, b=0, c=3}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 10);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s0]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s0, s3, s0]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s1, s0, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s2, s1, s2]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s3, s0, s3]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s6, s3]", "{a=1, b=0, c=3}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s6, s3, s4]", "{a=1, b=0, c=3}"));
		assertTrue(testCycleAndParikh(c, "[s5, s6, s3, s4, s5]", "{a=1, b=0, c=3}"));
		assertTrue(testCycleAndParikh(c, "[s6, s3, s4, s5, s6]", "{a=1, b=0, c=3}"));

		ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.DFS);
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s0, s2]", "{a=1, b=0, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s1]", "{a=0, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2]", "{a=0, b=1, c=1}"));
		c = calc.computePVsOfSmallestCycles(ts, ComputeSmallestCycles.Algorithm.FloydWarshall);
		assertEquals(c.size(), 6);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s0]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s0, s2, s0]", "{a=1, b=0, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s0, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s2, s0, s2]", "{a=1, b=0, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s1, s2]", "{a=0, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s1]", "{a=0, b=1, c=1}"));
	}

	private boolean testCycleAndParikh(Set<Pair<List<String>, ParikhVector>> c, String cycle, String parikh) {
		for (Pair<List<String>, ParikhVector> pair : c) {
			if (pair.getFirst().toString().equals(cycle)
				&& pair.getSecond().toString().equals(parikh)) {
				return true;
			}
		}
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
