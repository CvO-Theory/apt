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
 * @author vsp
 */
public class ComputeSmallestCyclesDFSTest extends AbstractComputeSmallestCyclesTestBase {
	ComputeSmallestCycles createComputeSmallestCycles() {
		return new ComputeSmallestCyclesDFS();
	}

	@Test
	public void testReversibleTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2]", "a", "b", "c"));
	}

	@Test
	public void testSingleStateLoop() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s]", "a"));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0]", "NotA"));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s, t]", "a", "a"));
	}

	@Test
	public void testOneCycle() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s3]", "a", "b", "c", "d"));
	}

	@Test
	public void testOneCycle1() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3]", "a", "a", "d"));
	}

	@Test
	public void testTwoCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 2);
		//bcd
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3]", "b", "c", "d"));
		//bbb
		assertTrue(testCycleAndParikh(c, "[s1, s4, s5]", "b", "b", "b"));
	}

	@Test
	public void testTwoIntersectingCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 1);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s3, s4, s5]", "b", "b", "b", "d"));
	}

	@Test
	public void testCyclesWithSameParikhVector() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 2);
		//bcdbb
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s4, s5]", "b", "b", "b", "c", "d"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s6, s3, s4, s5]", "b", "b", "b", "c", "d"));
	}

	@Test
	public void testCyclesWithDisjunktParikhVector() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 4);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s0]", "a"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1]", "b"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s2]", "c"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s3]", "d"));
	}

	@Test
	public void testCyclesWithSameParikhVector1() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1]", "a", "b"));
		assertTrue(testCycleAndParikh(c, "[s1, s2]", "a", "b"));
		assertTrue(testCycleAndParikh(c, "[s0, s3]", "a", "b"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s6]", "a", "c", "c", "c"));
	}

	@Test
	public void testFullyConnected() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1]", "a", "b"));
		assertTrue(testCycleAndParikh(c, "[s0, s2]", "a", "c"));
		assertTrue(testCycleAndParikh(c, "[s2, s1]", "b", "c"));
		assertTrue(testCycleAndParikh(c, "[s1, s2]", "b", "c"));
	}

	@Test
	public void testRemovalOfNonSmallCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		Set<Pair<List<String>, ParikhVector>> c = calc
				.computePVsOfSmallestCycles(getRemovalOfNonSmallCyclesTS());
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[7, 8, 9]", "a", "b", "c"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
