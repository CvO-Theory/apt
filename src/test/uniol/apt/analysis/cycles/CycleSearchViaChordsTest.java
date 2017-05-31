/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
 * Copyright (C) 2017 Uli Schlachter
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.exception.NonDisjointCyclesException;
import uniol.apt.analysis.exception.PreconditionFailedException;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 *
 * @author vsp
 */
@SuppressWarnings("unchecked")
public class CycleSearchViaChordsTest {
	private Set<ParikhVector> getCycles(TransitionSystem ts) throws Exception {
		return new CycleSearchViaChords().searchCycles(ts);
	}

	@Test
	public void testEmptyTS() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.setInitialState(ts.createState());
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, empty());
	}

	// TESTTSCOLLECTION
	@Test(expectedExceptions = NonDeterministicException.class)
	public void testNonDeterministicTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		getCycles(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = ".*is not persistent.*")
	public void testNonPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		getCycles(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = ".*is not persistent.*")
	public void testDeterministicReachableReversibleNonPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDeterministicReachableReversibleNonPersistentTS();
		getCycles(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = ".*is not totally reachable.*")
	public void testNotTotallyReachableTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		getCycles(ts);
	}

	@Test
	public void testPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testSingleStateTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = ".* is not persistent.*")
	public void testThreeStatesTwoEdgesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		getCycles(ts);
	}

	@Test
	public void testReversibleTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getReversibleTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "b", "c"))
		));
	}

	@Test
	public void testSingleStateLoop() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a"))
		));
	}

	@Test
	public void testSingleStateSingleTransitionTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateSingleTransitionTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("NotA"))
		));
	}

	@Test(expectedExceptions = PreconditionFailedException.class, expectedExceptionsMessageRegExp = ".*is not totally reachable.*")
	public void testSingleStateWithUnreachableTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateWithUnreachableTS();
		getCycles(ts);
	}

	@Test
	public void testTwoStateCycleSameLabelTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "a"))
		));
	}

	@Test
	public void testDifferentCyclesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDifferentCyclesTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, containsInAnyOrder(
			new ParikhVector(Arrays.asList("a", "b")),
			new ParikhVector(Arrays.asList("c", "d"))
		));
	}

	@Test
	public void testDetPersButNotDisjointSmallCyclesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getDetPersButNotDisjointSmallCyclesTS();
		try {
			getCycles(ts);
			throw new Exception("An exception should have been thrown");
		} catch (NonDisjointCyclesException e) {
			ParikhVector pv1 = new ParikhVector("a", "b");
			ParikhVector pv2 = new ParikhVector("a", "a");
			ParikhVector pv3 = new ParikhVector("b", "b");
			assertThat(Arrays.asList(e.getPV1(), e.getPV2()), containsInAnyOrder(is(pv1),
						either(is(pv2)).or(is(pv3))));
		}
	}

	@Test
	public void testNonDisjointCyclesTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonDisjointCyclesTS();
		try {
			getCycles(ts);
			throw new Exception("An exception should have been thrown");
		} catch (NonDisjointCyclesException e) {
			ParikhVector pv3a = new ParikhVector("a", "a", "a");
			ParikhVector pv2a = new ParikhVector("a", "a", "b", "c");
			ParikhVector pv1a = new ParikhVector("a", "b", "c", "b", "c");
			ParikhVector pv0a = new ParikhVector("b", "c", "b", "c", "b", "c");
			assertThat(Arrays.asList(e.getPV1(), e.getPV2()), anyOf(
						// These two are disjoint!
						//containsInAnyOrder(pv3a, pv0a),
						containsInAnyOrder(pv3a, pv1a),
						containsInAnyOrder(pv3a, pv2a),
						containsInAnyOrder(pv2a, pv0a),
						containsInAnyOrder(pv2a, pv1a),
						containsInAnyOrder(pv1a, pv0a)));
		}
	}

	@Test
	public void testNoCycle() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/NoCycle-aut.apt");
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, empty());
	}

	@Test
	public void testOneCycle() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle-aut.apt");
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "b", "c", "d"))
		));
	}

	@Test
	public void testOneCycle1() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/OneCycle1-aut.apt");
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "a", "d"))
		));
	}


	@Test(expectedExceptions = NonDeterministicException.class)
	public void testTwoCycles() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoCycles-aut.apt");
		getCycles(ts);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testTwoIntersectingCycles() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/TwoIntersectingCycles-aut.apt");
		getCycles(ts);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testCyclesWithSameParikhVector() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector-aut.apt");
		getCycles(ts);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testCyclesWithDisjunktParikhVector() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithDisjunktParikhVector-aut.apt");
		getCycles(ts);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testCyclesWithSameParikhVector1() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/CyclesWithSameParikhVector1-aut.apt");
		getCycles(ts);
	}

	@Test(expectedExceptions = NonDeterministicException.class)
	public void testFullyConnected() throws Exception {
		TransitionSystem ts = getAptLTS("./nets/cycles/FullyConnected-aut.apt");
		getCycles(ts);
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

		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "b"))
		));
	}

	@Test
	public void testPlainTNetReachabilityTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<ParikhVector> c = getCycles(ts);
		assertThat(c, contains(
			new ParikhVector(Arrays.asList("a", "b", "c"))
		));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
