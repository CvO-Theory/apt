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

package uniol.apt.analysis.separation;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;

import uniol.apt.TestNetsForSeparation;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.exception.UnboundedException;
import static org.testng.Assert.assertTrue;

/**
 * Tests for Separation class -- weakly separable
 *
 * @author Daniel
 */
@Test
public class SeparationWeakTest {

	@BeforeClass
	public void setup() {
	}

	@AfterClass
	public void teardown() {
	}

	private boolean testNetWeakly(PetriNet pn, int k) {
		// up to length 6 because there are test nets with finite fire sequences with length 6
		Separation sep = null;
		try {
			sep = new Separation(pn, false, k, 6, false);
		} catch (UnboundedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (sep == null) {
			return false;
		}

		return sep.isSeparable();
	}

	@Test
	public void getStrongSeparableFromLectureWithoutToken() {
		PetriNet pn = TestNetsForSeparation.getStrongSeparableFromLectureWithoutToken();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getStrongSeparableFromLecture() {
		PetriNet pn = TestNetsForSeparation.getStrongSeparableFromLecture();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparableFromLecture() {
		PetriNet pn = TestNetsForSeparation.getNoSeparableFromLecture();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparableLiveRevFC() {
		PetriNet pn = TestNetsForSeparation.getNoSeparableLiveRevFC();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableCycle() {
		PetriNet pn = TestNetsForSeparation.getSeparableCycle();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k6() {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 6;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k3() {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 3;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k2() {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 2;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36First() {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 6;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36Second() {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 3;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36Third() {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 2;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableLine() {
		PetriNet pn = TestNetsForSeparation.getSeparableLine();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableTrivial() {
		PetriNet pn = TestNetsForSeparation.getSeparableTrivial();
		int kTo = 2;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparable() {
		PetriNet pn = TestNetsForSeparation.getNoSeparable();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getWeakSeparable() {
		PetriNet pn = TestNetsForSeparation.getWeakSeparable();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getWeakSeparableFromLecture() {
		PetriNet pn = TestNetsForSeparation.getWeakSeparableFromLecture();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
