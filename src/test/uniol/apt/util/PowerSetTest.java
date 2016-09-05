/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import static java.util.Arrays.asList;

import org.testng.annotations.Test;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
public class PowerSetTest {
	static private Matcher<Collection<? extends Integer>> empty() {
		// Just a helper method to defeat generics and the Java compiler...
		return org.hamcrest.Matchers.empty();
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testBitSetNegativeSize() {
		new PowerSet.BitSetIterator(-42);
	}

	@Test
	public void testBitSetZero() {
		Iterator<BitSet> iter = new PowerSet.BitSetIterator(0);

		assertThat(iter.hasNext(), is(true));
		BitSet element = iter.next();
		assertThat(element, equalTo(new BitSet(0)));

		assertThat(iter.hasNext(), is(false));
	}

	@Test
	public void testBitSetOne() {
		Iterator<BitSet> iter = new PowerSet.BitSetIterator(1);

		assertThat(iter.hasNext(), is(true));
		BitSet element = iter.next();
		assertThat(element, equalTo(new BitSet(1)));

		assertThat(iter.hasNext(), is(true));
		element = iter.next();
		BitSet expected = new BitSet(1);
		expected.set(0);
		assertThat(element, equalTo(expected));

		assertThat(iter.hasNext(), is(false));
	}

	@Test
	public void testBitSetThree() {
		Collection<BitSet> collection = new PowerSet.BitSetIterable(3);

		assertThat(collection, hasSize(8));
		// Cheap check that all elements are really different:
		assertThat(new HashSet<>(collection), hasSize(8));
	}

	@Test
	public void testEmptyList() {
		PowerSet<Integer> power = PowerSet.powerSet(Collections.<Integer>emptyList());
		assertThat(power, hasSize(1));
		assertThat(power, contains(empty()));
	}

	@Test
	public void testOneElementList() {
		PowerSet<Integer> power = PowerSet.powerSet(asList(0));
		assertThat(power, hasSize(2));
		assertThat(power, containsInAnyOrder(contains(0), empty()));
	}

	@Test
	public void testTwoElementList() {
		PowerSet<Integer> power = PowerSet.powerSet(asList(0, 1));
		assertThat(power, hasSize(4));
		assertThat(power, containsInAnyOrder(contains(0, 1), contains(0), contains(1), empty()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
