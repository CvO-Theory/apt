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

package uniol.apt.analysis.bounded;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.TestNetCollection.*;

/** @author Uli Schlachter, vsp */
public class BoundedTest {
	private void testUnbounded(PetriNet pn) {
		BoundedResult result = Bounded.checkBounded(pn);
		assertThat(result.k, is(nullValue()));
		assertThat(result.isKBounded(42), is(false));
		assertThat(pn.getInitialMarking().fireTransitions(
					result.getSequenceExceeding(42).toArray(new Transition[0]))
				.getToken(result.unboundedPlace).getValue(),
				greaterThan(42l));
	}

	private void testBounded(PetriNet pn, int k) {
		BoundedResult result = Bounded.checkBounded(pn);
		assertThat(result.k, equalTo(Long.valueOf(k)));
		assertThat(result.isSafe(), equalTo(k <= 1));
		assertThat(result.isKBounded(k), is(true));
		assertThat(result.getSequenceExceeding(k), is(nullValue()));
		assertThat(result.getSequenceExceeding(-1), is(nullValue()));

		if (k > 0) {
			assertThat(result.isKBounded(k - 1), is(false));
			assertThat(pn.getInitialMarking().fireTransitions(
						result.getSequenceExceeding(k - 1).toArray(new Transition[0]))
					.getToken(result.unboundedPlace).getValue(),
					greaterThan(k - 1l));
		}
	}

	@Test
	public void testEmptyNet() {
		testBounded(getEmptyNet(), 0);
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		testBounded(getNoTransitionOnePlaceNet(), 0);
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		testBounded(getOneTransitionNoPlaceNet(), 0);
	}

	@Test
	public void testTokenGeneratorNet() {
		testUnbounded(getTokenGeneratorNet());
	}

	@Test
	public void testDeadlockNet() {
		testBounded(getDeadlockNet(), 1);
	}

	@Test
	public void testNonPersistentNet() {
		testBounded(getNonPersistentNet(), 1);
	}

	@Test
	public void testPersistentBiCFNet() {
		testBounded(getPersistentBiCFNet(), 2);
	}

	@Test
	public void testConcurrentDiamondNet() {
		testBounded(getConcurrentDiamondNet(), 1);
	}

	@Test
	public void testConflictingDiamondNet() {
		testBounded(getConflictingDiamondNet(), 1);
	}

	@Test
	public void testABCLanguageNet() {
		testUnbounded(getABCLanguageNet());
	}

	@Test
	public void testABCLanguageNetFiringSequences() {
		PetriNet pn = getABCLanguageNet();
		Node ta1 = pn.getNode("ta1");
		BoundedResult result = Bounded.checkBounded(pn);

		assertThat(result.pn, is(pn));
		assertThat(result.unboundedPlace, is(pn.getNode("p2")));
		assertThat(result.k, nullValue());
		assertThat(result.sequence, is(empty()));
		assertThat(result.cycle, contains(ta1));

		assertThat(result.getSequenceExceeding(0), contains(ta1));
		assertThat(result.getSequenceExceeding(1), contains(ta1, ta1));
	}

	@Test
	public void testUnsafeNetFiringSequences() {
		PetriNet pn = new PetriNet();
		Node s = pn.createPlace("s");
		Node t = pn.createTransition("t");
		pn.createFlow("t", "s");

		BoundedResult result = Bounded.checkBounded(pn);

		assertThat(result.pn, is(pn));
		assertThat(result.unboundedPlace, is(s));
		assertThat(result.k, nullValue());
		assertThat(result.sequence, is(empty()));
		assertThat(result.cycle, contains(t));

		assertThat(result.getSequenceExceeding(0), contains(t));
		assertThat(result.getSequenceExceeding(1), contains(t, t));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
