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
public class SeparationWeakTest {

	@BeforeClass
	public void setup() {
	}

	@AfterClass
	public void teardown() {
	}

	private boolean testNetWeakly(PetriNet pn, int k) throws UnboundedException {
		// up to length 6 because there are test nets with finite fire sequences with length 6
		return new Separation(pn, false, k, 6, false).isSeparable();
	}

	@Test
	public void getStrongSeparableFromLectureWithoutToken() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getStrongSeparableFromLectureWithoutToken();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getStrongSeparableFromLecture() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getStrongSeparableFromLecture();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparableFromLecture() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getNoSeparableFromLecture();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparableLiveRevFC() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getNoSeparableLiveRevFC();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableCycle() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableCycle();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k6() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 6;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k3() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 3;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK3Not26k2() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK3Not26();
		int kTo = 2;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36First() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 6;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36Second() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 3;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableK2Not36Third() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableK2Not36();
		int kTo = 2;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableLine() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableLine();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getSeparableTrivial() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getSeparableTrivial();
		int kTo = 2;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getNoSeparable() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getNoSeparable();
		int kTo = 0;

		assertFalse(testNetWeakly(pn, kTo));
	}

	@Test
	public void getWeakSeparable() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getWeakSeparable();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

	@Test
	public void getWeakSeparableFromLecture() throws UnboundedException {
		PetriNet pn = TestNetsForSeparation.getWeakSeparableFromLecture();
		int kTo = 0;

		assertTrue(testNetWeakly(pn, kTo));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
