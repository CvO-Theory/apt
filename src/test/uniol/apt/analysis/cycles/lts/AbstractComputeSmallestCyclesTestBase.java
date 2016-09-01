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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 *
 * @author Manuel Gieseking
 */
public abstract class AbstractComputeSmallestCyclesTestBase {
	abstract ComputeSmallestCycles createComputeSmallestCycles();

	@Test
	public void testEmptyTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = new TransitionSystem();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());

	}

	// TESTTSCOLLECTION
	@Test
	public void testNonDeterministicTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testNonPersistentTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testNotTotallyReachableTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testPersistentTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testSingleStateTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(c.isEmpty());
	}

	@Test
	public void testNoCycle() {
		ComputeSmallestCycles calc = createComputeSmallestCycles();
		TransitionSystem ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		Set<Pair<List<String>, ParikhVector>> c = calc.computePVsOfSmallestCycles(ts);
		assertEquals(c.size(), 0);
		assertTrue(calc.checkSamePVs(ts));
		assertTrue(calc.checkSameOrMutallyDisjointPVs(ts));
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
		// implementation in ComputeSmallestCyclesJohnson will find cycles going through that state first. Thus,
		// when this state is reached, two cycles with Parikh vector 2*[a,b,c] are currently known. The bug that
		// we are testing is that only one of these will be removed when the following cycle is found.

		ts.createArc("7", "8", "a");
		ts.createArc("8", "9", "b");
		ts.createArc("9", "7", "c");

		return ts;
	}

	protected boolean testCycleAndParikh(Set<Pair<List<String>, ParikhVector>> c, String cycle, String... parikh) {
		for (Pair<List<String>, ParikhVector> pair : c) {
			if (pair.getFirst().toString().equals(cycle)
				&& pair.getSecond().equals(new ParikhVector(parikh))) {
				return true;
			}
		}
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
