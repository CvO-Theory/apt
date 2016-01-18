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
public class ComputeSmallestCyclesFloydWarshallTest extends AbstractComputeSmallestCyclesTestBase {
	ComputeSmallestCycles createComputeSmallestCycles() {
		return new ComputeSmallestCyclesFloydWarshall();
	}

	@Test
	public void testReversibleTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 3);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s0]", "{a=1, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s0, s1]", "{a=1, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s0, s1, s2]", "{a=1, b=1, c=1}"));
	}

	@Test
	public void testSingleStateLoop() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s, s]", "{a=1}"));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s0, s0]", "{NotA=1}"));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 1);
		assertTrue(testCycleAndParikh(c, "[s1, s1]", "{NotA=1}"));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertEquals(c.size(), 2);
		assertTrue(testCycleAndParikh(c, "[s, t, s]", "{a=2}"));
		assertTrue(testCycleAndParikh(c, "[t, s, t]", "{a=2}"));
	}

	@Test
	public void testOneCycle() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 4);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s2, s3, s0]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s0, s1]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s0, s1, s2]", "{a=1, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s0, s1, s2, s3]", "{a=1, b=1, c=1, d=1}"));
	}

	@Test
	public void testOneCycle1() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 3);
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s1]", "{a=2, b=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s1, s2]", "{a=2, b=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s1, s2, s3]", "{a=2, b=0, d=1}"));
	}

	@Test
	public void testTwoCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertEquals(c.size(), 6);
		//bcd
		assertTrue(testCycleAndParikh(c, "[s1, s2, s3, s1]", "{a=0, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s3, s1, s2]", "{a=0, b=1, c=1, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s1, s2, s3]", "{a=0, b=1, c=1, d=1}"));
		//bbb
		assertTrue(testCycleAndParikh(c, "[s1, s4, s5, s1]", "{a=0, b=3, c=0, d=0}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s4]", "{a=0, b=3, c=0, d=0}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s4, s5]", "{a=0, b=3, c=0, d=0}"));
	}

	@Test
	public void testTwoIntersectingCycles() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 4);
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s3, s4, s5, s1]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s3, s4, s5, s1, s3]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s4, s5, s1, s3, s4]", "{a=0, b=3, c=0, d=1}"));
		assertTrue(testCycleAndParikh(c, "[s5, s1, s3, s4, s5]", "{a=0, b=3, c=0, d=1}"));
	}

	@Test
	public void testCyclesWithSameParikhVector() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
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
		assertTrue(testCycleAndParikh(c, "[s0, s0]", "{a=1, b=0, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s1, s1]", "{a=0, b=1, c=0, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s2, s2]", "{a=0, b=0, c=1, d=0}"));
		//bdbb
		assertTrue(testCycleAndParikh(c, "[s3, s3]", "{a=0, b=0, c=0, d=1}"));
	}

	@Test
	public void testCyclesWithSameParikhVector1() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts));
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
	}

	@Test
	public void testFullyConnected() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertFalse(calc.checkSamePVs(ts));
		assertFalse(calc.checkSameOrMutallyDisjointPVs(ts));
		assertEquals(c.size(), 6);
		assertTrue(testCycleAndParikh(c, "[s0, s1, s0]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s0, s2, s0]", "{a=1, b=0, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s0, s1]", "{a=1, b=1, c=0}"));
		assertTrue(testCycleAndParikh(c, "[s2, s0, s2]", "{a=1, b=0, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s2, s1, s2]", "{a=0, b=1, c=1}"));
		assertTrue(testCycleAndParikh(c, "[s1, s2, s1]", "{a=0, b=1, c=1}"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
