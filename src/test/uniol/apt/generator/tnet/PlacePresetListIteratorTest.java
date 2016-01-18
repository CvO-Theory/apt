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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;

/** @author Uli Schlachter */
public class PlacePresetListIteratorTest {
	private void test(int[][] expected, final List<Integer> transitionPresetSizeList, final int maxTransitions) {
		List<Matcher<? super Iterable<Integer>>> matchers = new LinkedList<>();
		for (int[] elem : expected) {
			List<Matcher<? super Integer>> elemMatchers = new LinkedList<>();
			for (int i : elem) {
				elemMatchers.add(is(equalTo(i)));
			}
			matchers.add(containsInAnyOrder(elemMatchers));
		}

		assertThat(new Iterable<Deque<Integer>>() {
			@Override
			public Iterator<Deque<Integer>> iterator() {
				return new PlacePresetListIterator(transitionPresetSizeList, maxTransitions);
			}
		}, containsInAnyOrder(matchers));
	}

	@Test
	public void testSingleTransition() {
		// If there is just a single transition, it must be in the preset of all places
		int[][] expected = { { 0, 0, 0, 0, 0, 0 } };
		test(expected, asList(4, 2), 1);
	}

	@Test
	public void testCircle() {
		// Let's count to 3^2 in base 3
		int[][] expected = {
			{ 0, 0 },
			{ 0, 1 },
			{ 0, 2 },
			{ 1, 0 },
			{ 1, 1 },
			{ 1, 2 },
			{ 2, 0 },
			{ 2, 1 },
			{ 2, 2 },
		};
		test(expected, asList(1, 1), 3);
	}

	@Test
	public void testTwoTransitionsInPairs() {
		// There are four places and two transitions. However, each of the transitions as two places in its
		// preset and thus we would get isomorphic nets in many cases
		int[][] expected = {
			{ 0, 0, 0, 0 },
			{ 0, 0, 0, 1 },
			{ 0, 0, 1, 1 },
			{ 0, 1, 0, 0 },
			{ 0, 1, 0, 1 },
			{ 0, 1, 1, 1 },
			{ 1, 1, 0, 0 },
			{ 1, 1, 0, 1 },
			{ 1, 1, 1, 1 },
		};
		test(expected, asList(2, 2), 2);
	}

	@Test
	public void testTooManyMaxTransitions() {
		// A single transition can just cause a single extra place to be created
		int[][] expected = {
			{ 0 },
			{ 1 },
		};
		test(expected, asList(1), 5);
	}

	@Test
	public void testTooManyMaxTransitionsAgain() {
		// Two transitions can at most create two extra places
		int[][] expected = {
			{ 0, 0 },
			{ 0, 1 },
			{ 1, 1 },
			{ 1, 2 },
		};
		test(expected, asList(2), 5);
	}

	@Test
	public void testTwoOneMaxTwo() {
		int[][] expected = {
			{ 0, 0, 0 },
			{ 0, 0, 1 },
			{ 0, 1, 0 },
			{ 0, 1, 1 },
			{ 1, 1, 0 },
			{ 1, 1, 1 },
		};
		test(expected, asList(2, 1), 2);
	}

	@Test
	public void testTwoOneMaxThree() {
		int[][] expected = {
			{ 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 2 },
			{ 0, 1, 0 }, { 0, 1, 1 }, { 0, 1, 2 },
			{ 0, 2, 0 }, { 0, 2, 1 }, { 0, 2, 2 },
			{ 1, 1, 0 }, { 1, 1, 1 }, { 1, 1, 2 },
			{ 1, 2, 0 }, { 1, 2, 1 }, { 1, 2, 2 },
			{ 2, 2, 0 }, { 2, 2, 1 }, { 2, 2, 2 },
		};
		test(expected, asList(2, 1), 3);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
