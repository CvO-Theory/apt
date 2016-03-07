/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.sum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static uniol.apt.adt.matcher.PlaceWithInitialTokenMatcher.placeWithInitialToken;

import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

public class SumTest {

	/**
	 * Test petri net:
	 *
	 * <pre>
	 * [t1]--3-->(p1)-->[t2:b]-->(p2)--\
	 *                   |           [t3:a]
	 *                   \----->(p3)<--/
	 *                           |
	 *                           v
	 *                        [t4:a]
	 * </pre>
	 *
	 * Syntax:
	 *
	 * <pre>
	 * [transition:label], (place), --weight-->
	 * </pre>
	 *
	 * @return the test petri net
	 */
	private PetriNet getTestNet1() {
		PetriNet pn1 = new PetriNet();

		pn1.createPlace("p1");
		pn1.createPlace("p2");
		pn1.createPlace("p3");

		pn1.createTransition("t1");
		pn1.createTransition("t2", "b");
		pn1.createTransition("t3", "a");
		pn1.createTransition("t4", "a");

		pn1.createFlow("t1", "p1", 3);
		pn1.createFlow("p1", "t2");
		pn1.createFlow("t2", "p2");
		pn1.createFlow("t2", "p3");
		pn1.createFlow("p2", "t3");
		pn1.createFlow("t3", "p3");
		pn1.createFlow("p3", "t4");

		return pn1;
	}

	/**
	 * Test petri net:
	 *
	 * <pre>
	 * (p1)-->[t2:b]-->(p2)
	 * </pre>
	 *
	 * Syntax:
	 *
	 * <pre>
	 * [transition:label], (place)
	 * </pre>
	 *
	 * @return the test petri net
	 */
	private PetriNet getTestNet2() {
		PetriNet pn2 = new PetriNet();

		pn2.createPlace("p1");
		pn2.createPlace("p2");

		pn2.createTransition("t2", "b");

		pn2.createFlow("p1", "t2");
		pn2.createFlow("t2", "p2");

		return pn2;
	}

	@Test
	public void testSyncSum() throws LabelMismatchException {
		PetriNet pn1 = getTestNet1();
		PetriNet pn2 = getTestNet2();

		Sum sum = new Sum(pn1, pn2);
		PetriNet result = sum.getSyncSum();

		assertThat(result.getPlaces(), hasSize(5));
		assertThat(result.getTransitions(), hasSize(4));
		assertThat(result.getEdges(), hasSize(9));

		Transition t2 = result.getTransition("t2");
		assertThat(t2.getPreset(), hasSize(2));
		assertThat(t2.getPostset(), hasSize(3));
		assertThat(t2.getLabel(), equalTo("b"));

		Transition t3 = result.getTransition("t3");
		assertThat(t3.getLabel(), equalTo("a"));

		Set<Flow> t1Flows = result.getPostsetEdges("t1");
		assertThat(t1Flows, hasSize(1));
		assertThat(t1Flows.iterator().next().getWeight(), equalTo(3));
	}

	@Test(expectedExceptions = LabelMismatchException.class)
	public void testSyncSumLabelMismatch() throws LabelMismatchException {
		PetriNet pn1 = getTestNet1();

		PetriNet pn2 = new PetriNet();
		pn2.createPlace("p1");
		pn2.createTransition("t2", "foobar");
		pn2.createFlow("p1", "t2");

		Sum sum = new Sum(pn1, pn2);
		sum.getSyncSum();
	}

	@Test
	public void testSyncSumDisjointTransitions() throws LabelMismatchException {
		PetriNet pn1 = new PetriNet();
		Place pn1P1 = pn1.createPlace();
		Transition pn1T1 = pn1.createTransition("t1");
		pn1.createFlow(pn1T1, pn1P1);

		PetriNet pn2 = new PetriNet();
		Place pn2P1 = pn2.createPlace();
		Transition pn2T1 = pn2.createTransition("u1");
		pn2.createFlow(pn2T1, pn2P1);

		Sum sum = new Sum(pn1, pn2);
		PetriNet result = sum.getSyncSum();

		assertThat(result.getPlaces(), hasSize(2));
		assertThat(result.getTransitions(), hasSize(2));
		assertThat(result.getEdges(), hasSize(2));
	}

	@Test
	public void testAsyncSum() {
		PetriNet pn1 = getTestNet1();
		PetriNet pn2 = getTestNet2();

		Sum sum = new Sum(pn1, pn2);
		PetriNet result = sum.getAsyncSum();

		assertThat(result.getPlaces(), hasSize(5));
		assertThat(result.getTransitions(), hasSize(5));
		assertThat(result.getEdges(), hasSize(9));
	}

	@Test
	public void testAsyncSumInitialTokens() {
		PetriNet pn1 = getTestNet1();
		pn1.getPlace("p1").setInitialToken(1);

		PetriNet pn2 = getTestNet2();
		pn2.getPlace("p1").setInitialToken(2);

		Sum sum = new Sum(pn1, pn2);
		PetriNet result = sum.getAsyncSum();

		assertThat(result.getPlaces(), hasItem(placeWithInitialToken(1)));
		assertThat(result.getPlaces(), hasItem(placeWithInitialToken(2)));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
