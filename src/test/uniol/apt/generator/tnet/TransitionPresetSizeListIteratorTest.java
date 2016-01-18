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

package uniol.apt.generator.tnet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;

/** @author Uli Schlachter */
public class TransitionPresetSizeListIteratorTest {
	private void test(int[][] expected, final int maxPlaces, final int maxTransitions) {
		List<Matcher<? super Iterable<Integer>>> matchers = new LinkedList<>();
		for (int[] elem : expected) {
			List<Matcher<? super Integer>> elemMatchers = new LinkedList<>();
			for (int i : elem) {
				elemMatchers.add(is(equalTo(i)));
			}
			matchers.add(containsInAnyOrder(elemMatchers));
		}

		assertThat(new Iterable<List<Integer>>() {
			@Override
			public Iterator<List<Integer>> iterator() {
				return new TransitionPresetSizeListIterator(maxPlaces, maxTransitions);
			}
		}, containsInAnyOrder(matchers));
	}

	@Test
	public void testOnePlaceOneTransition() {
		int[][] expected = { { 1 } };
		test(expected, 1, 1);
	}

	@Test
	public void testOnePlaceManyTransitions() {
		int[][] expected = { { 1 } };
		test(expected, 1, 42);
	}

	@Test
	public void testTwoPlacesOneTransition() {
		int[][] expected = { { 1 }, { 2 } };
		test(expected, 2, 1);
	}

	@Test
	public void testTwoPlacesTwoTransitions() {
		int[][] expected = { { 1 }, { 2 }, { 1, 1 } };
		test(expected, 2, 2);
	}

	@Test
	public void testTwoPlacesManyTransitions() {
		int[][] expected = { { 1 }, { 2 }, { 1, 1 } };
		test(expected, 2, 42);
	}

	@Test
	public void testThreePlacesOneTransition() {
		int[][] expected = {
			{ 1 }, { 2 }, { 3 },
		};
		test(expected, 3, 1);
	}

	@Test
	public void testThreePlacesTwoTransitions() {
		int[][] expected = {
			{ 1 }, { 2 }, { 3 },
			{ 1, 1 }, { 2, 1 },
		};
		test(expected, 3, 2);
	}

	@Test
	public void testThreePlacesThreeTransitions() {
		int[][] expected = {
			{ 1 }, { 2 }, { 3 },
			{ 1, 1 }, { 2, 1 },
			{ 1, 1, 1 },
		};
		test(expected, 3, 3);
	}

	@Test
	public void testThreePlacesManyTransitions() {
		int[][] expected = {
			{ 1 }, { 2 }, { 3 },
			{ 1, 1 }, { 2, 1 },
			{ 1, 1, 1 },
		};
		test(expected, 3, 42);
	}

	@Test
	public void testManyPlacesTwoTransitions() {
		int[][] expected = {
			{ 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 },
			{ 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 },
			{ 2, 2 }, { 3, 2 }, { 4, 2 },
			{ 3, 3 },
		};
		test(expected, 6, 2);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
