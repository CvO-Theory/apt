/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015-2017  Uli Schlachter
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

package uniol.apt.util;

import java.util.Arrays;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
abstract public class AbstractEquivalenceRelationTest {

	abstract <E> AbstractEquivalenceRelation<E> createRelation(E... domain);

	IEquivalenceRelation<Integer> getOddEven() {
		return new IEquivalenceRelation<Integer>() {
			@Override
			public boolean isEquivalent(Integer e1, Integer e2) {
				return e1 % 2 == e2 % 2;
			}
		};
	}

	AbstractEquivalenceRelation<Integer> getSomePrimes() {
		Integer[] numbersToTwenty = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		int[] primes = { 2, 3, 5, 7, 11, 13, 17, 19 };
		AbstractEquivalenceRelation<Integer> result = createRelation(numbersToTwenty);
		for (int i = 0; i <= 20; i++)
			if (Arrays.binarySearch(primes, i) >= 0)
				result.joinClasses(2, i);
			else
				result.joinClasses(1, i);
		return result;
	}

	@Test
	public void testReflexive() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");

		assertThat(relation.isEquivalent("a", "a"), is(true));
	}

	@Test
	public void testTwoClasses() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("c", "d"), containsInAnyOrder("c", "d"));

		assertThat(relation.isEquivalent("c", "b"), is(false));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b"));
		assertThat(relation, containsInAnyOrder(
					containsInAnyOrder("a", "b"),
					containsInAnyOrder("c", "d")));
		assertThat(relation, hasSize(2));
	}

	@Test
	public void testFourElements() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("c", "d"), containsInAnyOrder("c", "d"));
		assertThat(relation.joinClasses("a", "d"), containsInAnyOrder("a", "b", "c", "d"));

		assertThat(relation.isEquivalent("c", "b"), is(true));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b", "c", "d"));
		assertThat(relation, contains(containsInAnyOrder("a", "b", "c", "d")));
		assertThat(relation, hasSize(1));
	}

	@Test
	public void testEqualsEmpty() {
		AbstractEquivalenceRelation<String> relation1 = createRelation("a", "b", "c", "d");
		AbstractEquivalenceRelation<String> relation2 = createRelation("a", "b", "c", "d");
		assertThat(relation1, equalTo(relation2));
		assertThat(relation1.hashCode(), equalTo(relation2.hashCode()));
	}

	@Test
	public void testEqualsNonEmpty() {
		AbstractEquivalenceRelation<String> relation1 = createRelation("a", "b", "c", "d");
		AbstractEquivalenceRelation<String> relation2 = createRelation("a", "b", "c", "d");
		relation1.joinClasses("a", "b");
		relation2.joinClasses("b", "a");
		assertThat(relation1, equalTo(relation2));
		assertThat(relation1.hashCode(), equalTo(relation2.hashCode()));
	}

	@Test
	public void testRefineEmpty() {
		AbstractEquivalenceRelation<Integer> primes = getSomePrimes();
		AbstractEquivalenceRelation<Integer> empty = createRelation(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
		assertThat(primes.refine(empty), equalTo(empty));
	}

	@Test
	public void testEmptyRefine() {
		AbstractEquivalenceRelation<Integer> primes = getSomePrimes();
		AbstractEquivalenceRelation<Integer> empty = createRelation(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
		assertThat(empty.refine(primes), sameInstance(empty));
	}

	@Test
	public void testRefineSelf() {
		AbstractEquivalenceRelation<Integer> primes = getSomePrimes();
		assertThat(primes.refine(primes), sameInstance(primes));
	}

	@Test
	public void testRefine() {
		AbstractEquivalenceRelation<Integer> primes = getSomePrimes();
		IEquivalenceRelation<Integer> numbers = getOddEven();
		AbstractEquivalenceRelation<Integer> refined = primes.refine(numbers);

		assertThat(refined.isEquivalent(3, 7), is(true));
		assertThat(refined.isEquivalent(1, 5), is(false));
		assertThat(refined.isEquivalent(11, 9), is(false));
		assertThat(refined.isEquivalent(11, 13), is(true));
		assertThat(refined.isEquivalent(15, 9), is(true));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
