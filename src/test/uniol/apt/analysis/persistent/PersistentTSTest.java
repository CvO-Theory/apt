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

package uniol.apt.analysis.persistent;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * @author Vincent Göbel, Uli Schlachter
 */
public class PersistentTSTest {
	@Test
	public void testPersistentTS() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		PersistentTS det = new PersistentTS(ts, false);
		assertTrue(det.isPersistent());
	}

	@Test
	public void testPersistentTSBackwards() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		PersistentTS det = new PersistentTS(ts, true);
		assertTrue(det.isPersistent());
	}

	@Test
	public void testPersistentNonDeterministicTS() {
		TransitionSystem ts = TestTSCollection.getPersistentNonDeterministicTS();
		PersistentTS det = new PersistentTS(ts, false);
		assertTrue(det.isPersistent());
	}

	@Test
	public void testNonPersistentNonDeterministicTS() {
		TransitionSystem ts = TestTSCollection.getNonPersistentNonDeterministicTS();
		PersistentTS det = new PersistentTS(ts, false);
		assertFalse(det.isPersistent());
	}

	@Test
	public void testNonPersistentTS() {
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		PersistentTS det = new PersistentTS(ts, false);
		assertFalse(det.isPersistent());
		assertEquals(det.getNode().getId(), "r");
		assertTrue(det.getLabel1().equals("fail") || det.getLabel2().equals("fail"));
		assertTrue(det.getLabel2().equals("a") || det.getLabel1().equals("a"));
	}

	@Test
	public void testNonPersistentTSBackwards() {
		TransitionSystem ts = TestTSCollection.getNonPersistentTS();
		PersistentTS det = new PersistentTS(ts, true);
		assertTrue(det.isPersistent());
	}

	@Test
	public void testNonPersistentTSReversed() {
		TransitionSystem ts = TestTSCollection.getNonPersistentTSReversed();
		PersistentTS det = new PersistentTS(ts, false);
		assertTrue(det.isPersistent());
	}

	@Test
	public void testNonPersistentTSReversedBackwards() {
		TransitionSystem ts = TestTSCollection.getNonPersistentTSReversed();
		PersistentTS det = new PersistentTS(ts, true);
		assertFalse(det.isPersistent());
		assertEquals(det.getNode().getId(), "r");
		assertTrue(det.getLabel1().equals("fail") || det.getLabel2().equals("fail"));
		assertTrue(det.getLabel2().equals("a") || det.getLabel1().equals("a"));
	}

	@Test
	public void testNonPersistentButActivatedTS() {
		TransitionSystem ts = TestTSCollection.getNonPersistentButActivatedTS();
		PersistentTS det = new PersistentTS(ts, false);
		assertFalse(det.isPersistent());
		assertEquals(det.getNode().getId(), "r");
		assertTrue(det.getLabel1().equals("fail") || det.getLabel2().equals("fail"));
		assertTrue(det.getLabel2().equals("a") || det.getLabel1().equals("a"));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
