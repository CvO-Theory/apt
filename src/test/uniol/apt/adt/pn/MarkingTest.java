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

package uniol.apt.adt.pn;

import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.*;

import uniol.apt.adt.exception.NoSuchNodeException;

/**
 * @author Dennis-Michael Borde, Uli Schlachter, vsp
 */
@SuppressWarnings("deprecation")
public class MarkingTest {

	static private final Integer OMEGA = null;
	private PetriNet pn;

	@BeforeMethod
	public void setupPN() {
		pn = new PetriNet("foo");
	}

	@AfterMethod
	public void teardownPN() {
		pn = null;
	}

	private Place[] createPlaces(int size) {
		Place[] places = new Place[size];
		for (int i = 0; i < size; i++) {
			String id = Integer.toString(i);
			places[i] = pn.containsPlace(id) ? pn.getPlace(id) : pn.createPlace(id);
		}
		return places;
	}

	private Marking createMarking(Integer... token) {
		Marking mark = new Marking(pn);
		Place[] places = createPlaces(token.length);
		assert places.length == token.length;
		for (int i = 0; i < token.length; i++) {
			if (token[i] == null) {
				mark = mark.setTokenCount(places[i], Token.OMEGA);
			} else {
				mark = mark.setTokenCount(places[i], token[i]);
			}
		}
		return mark;
	}

	private void markingEqual(Integer... token) {
		Marking a = createMarking(token);
		Marking b = createMarking(token);
		assertEquals(a, a);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testMarkingEqual1() {
		markingEqual();
	}

	@Test
	public void testMarkingEqual2() {
		markingEqual(1, 2, 3);
	}

	@Test
	public void testNotEqual1() {
		Marking a = createMarking(1, 2, 3);
		assertFalse(a == null);
	}

	@Test
	public void testNotEqual2() {
		Marking a = createMarking(1, 2, 3);
		assertFalse(a.equals("foo"));
	}

	@Test
	public void testNotEqual3() {
		Marking a = new PetriNet().getInitialMarkingCopy();
		Marking b = new PetriNet().getInitialMarkingCopy();
		assertFalse(a.equals(b));
	}

	@Test
	public void testNotEqual4() {
		Marking a = createMarking(1, 2, 3);
		assertFalse(a.equals(null));
	}

	@Test
	public void testMarkingNotEqual() {
		Marking a = createMarking(1, 2, 3);
		Marking b = createMarking(1, 5, 3);
		assertFalse(a.equals(b));
	}

	@Test
	public void testOmega() {
		Marking a = createMarking(1, 2, OMEGA, 4, 5);
		assertTrue(a.hasOmega());
	}

	@Test
	public void testNoOmega() {
		Marking a = createMarking(1, 2, 3, 4, 5);
		assertFalse(a.hasOmega());
	}

	private void coverSucceed(Integer[] cover, Integer[] covered, Integer[] expected) {
		assert cover.length == covered.length;
		assert cover.length == expected.length;
		Marking mcover = createMarking(cover);
		Marking mcovered = createMarking(covered);
		Marking mexpected = createMarking(expected);

		Marking mresult = mcover.cover(mcovered);
		assertEquals(mcover, createMarking(cover));
		assertEquals(mresult, mexpected);

		assertTrue(mcover.covers(mcovered));
		assertEquals(mcover, mexpected);
	}

	private void coverFail(Integer[] cover, Integer[] covered) {
		assert cover.length == covered.length;
		Marking mcover = createMarking(cover);
		Marking mcovered = createMarking(covered);
		Marking mexpected = createMarking(cover);

		assertThat(mcover.cover(mcovered), nullValue());
		assertEquals(mcover, mexpected);

		assertFalse(mcover.covers(mcovered));
		assertEquals(mcover, mexpected);
	}

	@Test
	public void testCover1() {
		Integer covered[] = {1, 2, 2};
		Integer cover[] = {1, 2, 3};
		Integer expected[] = {1, 2, OMEGA};
		coverSucceed(cover, covered, expected);
	}

	@Test
	public void testCover2() {
		Integer covered[] = {1, 1, 1};
		Integer cover[] = {2, 2, 2};
		Integer expected[] = {OMEGA, OMEGA, OMEGA};
		coverSucceed(cover, covered, expected);
	}

	@Test
	public void testCover4() {
		Integer covered[] = {21, OMEGA};
		Integer cover[] = {42, OMEGA};
		Integer expected[] = {OMEGA, OMEGA};
		coverSucceed(cover, covered, expected);
	}

	@Test
	public void testNoCover1() {
		Integer covered[] = {1, 3, 2};
		Integer cover[] = {1, 2, 3};
		coverFail(cover, covered);
	}

	@Test
	public void testNoCover2() {
		Integer cover[] = {};
		coverFail(cover, cover);
	}

	@Test
	public void testNoCover3() {
		Integer covered[] = {42};
		Integer cover[] = {42};
		coverFail(cover, covered);
	}

	@Test
	public void testNoCover4() {
		Integer covered[] = {21, OMEGA};
		Integer cover[] = {42, 0};
		coverFail(cover, covered);
	}

	@Test
	public void testNoCover5() {
		Integer covered[] = {21};
		Integer cover[] = {OMEGA};
		coverFail(cover, covered);
	}

	@Test
	public void testValueHashCode1() {
		Token v1 = new Token(3);
		Token v2 = new Token(3);
		assertEquals(v1, v2);
		assertEquals(v1.hashCode(), v2.hashCode());
	}

	@Test
	public void testValueHashCode2() {
		Token v1 = new Token(0);
		Token v2 = new Token(0);
		assertEquals(v1, v2);
		assertEquals(v1.hashCode(), v2.hashCode());
	}

	@Test
	public void testCopyConstructor() {
		Marking mark = createMarking(1, 2, 3);
		Marking other = new Marking(mark);
		// Change the number of tokens on a random place
		mark.addToken(pn.getPlaces().iterator().next(), 42);

		// Verify that the two Markings aren't equal (=the copy constructor works correctly)
		assertThat(mark, not(equalTo(other)));
	}

	@Test
	public void testAddTokenCopies() {
		Marking mark = createMarking(1, 2, 3);
		Marking other = new Marking(mark);
		// Change the number of tokens on a random place
		Marking changed = mark.addTokenCount(pn.getPlaces().iterator().next(), 42);

		// Mark was not changed
		assertThat(mark, equalTo(createMarking(1, 2, 3)));

		// Verify that the two Markings aren't equal (=something was changed)
		assertThat(changed, not(equalTo(other)));
	}

	@Test
	public void testRemoveAddPlace() {
		Place p1 = pn.createPlace("RemoveAddPlace");
		p1.setInitialToken(1);
		pn.removePlace(p1);

		Place p2 = pn.createPlace("RemoveAddPlace");
		assertThat(p2.getInitialToken(), equalTo(new Token(0)));
	}

	@Test
	public void testNetCopy() {
		Place p = pn.createPlace();
		p.setInitialToken(1);

		PetriNet pn2 = new PetriNet(pn);
		Place p2 = pn2.getPlace(p.getId());

		// The bug that we are testing for: initial is a marking on pn2, but contains Token for places from net
		// pn. Thus, we must explicitly call getToken(Place) to test for this bug.
		Marking initial = pn2.getInitialMarkingCopy();
		assertThat(initial.getToken(p2).getValue(), equalTo(1l));
	}

	@Test(expectedExceptions = NoSuchNodeException.class)
	public void testGetTokenAfterRemove() {
		Place p = pn.createPlace();
		p.setInitialToken(1);
		Marking mark = pn.getInitialMarking();
		pn.removePlace(p);

		mark.getToken(p);
	}

	@Test
	public void testToString() {
		// Many places to increase changes of reordering
		Place places[] = pn.createPlaces("p42", "p1", "a", "z", "b", "y", "j", "k");
		places[0].setInitialToken(42);
		places[1].setInitialToken(Token.OMEGA);
		places[2].setInitialToken(0);
		places[3].setInitialToken(5);
		places[4].setInitialToken(1);
		places[5].setInitialToken(4);
		places[6].setInitialToken(2);
		places[7].setInitialToken(3);
		assertThat(pn.getInitialMarkingCopy(),
				hasToString("[ [a:0] [b:1] [j:2] [k:3] [p1:OMEGA] [p42:42] [y:4] [z:5] ]"));
	}

	@Test
	public void testValues() {
		Marking m = createMarking(1, 43, OMEGA, 2);
		assertThat(m.values(), containsInAnyOrder(
					Token.valueOf(1), Token.valueOf(43), Token.OMEGA, Token.valueOf(2)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
