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

package uniol.apt.analysis.deterministic;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * @author Renke Grunwald
 *
 */
public class DeterministicTest {
	private static void assertDeterministic(Deterministic deterministic) {
		assertTrue(deterministic.isDeterministic());
		assertNull(deterministic.getNode());
		assertNull(deterministic.getLabel());
	}

	@Test
	public void testSingleStateTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();

		assertDeterministic(new Deterministic(ts));
		assertDeterministic(new Deterministic(ts, false));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();

		assertDeterministic(new Deterministic(ts));
		assertDeterministic(new Deterministic(ts, false));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() {
		TransitionSystem ts = TestTSCollection.getTwoStateCycleSameLabelTS();

		assertDeterministic(new Deterministic(ts));
		assertDeterministic(new Deterministic(ts, false));
	}

	@Test
	public void testSingleStateLoopTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateLoop();

		assertDeterministic(new Deterministic(ts));
		assertDeterministic(new Deterministic(ts, false));
	}

	@Test
	public void testNonDeterministicTS() {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();

		Deterministic deterministic = new Deterministic(ts);

		assertFalse(deterministic.isDeterministic());

		assertNotNull(deterministic.getNode());
		assertEquals(deterministic.getNode().getId(), "s0");

		assertNotNull(deterministic.getLabel());
		assertEquals(deterministic.getLabel(), "a");

		assertDeterministic(new Deterministic(ts, false));
	}

	@Test
	public void testNonBackwardsDeterministicTS() {
		TransitionSystem ts = TestTSCollection.getNonBackwardsDeterministicTS();

		assertDeterministic(new Deterministic(ts));

		Deterministic deterministic = new Deterministic(ts, false);

		assertFalse(deterministic.isDeterministic());

		assertNotNull(deterministic.getNode());
		assertEquals(deterministic.getNode().getId(), "s0");

		assertNotNull(deterministic.getLabel());
		assertEquals(deterministic.getLabel(), "a");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
