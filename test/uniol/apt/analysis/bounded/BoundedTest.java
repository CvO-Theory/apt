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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import uniol.apt.adt.pn.PetriNet;

import static uniol.apt.TestNetCollection.*;

/** @author Uli Schlachter, vsp */
@Test
public class BoundedTest {
	private void testUnbounded(PetriNet pn) {
		BoundedResult result = new Bounded().checkBounded(pn);
		assertEquals(result.k, null);
	}

	private void testBounded(PetriNet pn, int k) {
		BoundedResult result = new Bounded().checkBounded(pn);
		assertNotNull(result.k);
		assertEquals(result.k, Integer.valueOf(k));
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
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
