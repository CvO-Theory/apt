/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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
import java.util.Collection;

import org.testng.annotations.Test;
import static uniol.apt.util.matcher.Matchers.pairWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked")
public class PairTest {
	@Test
	public void testBasics() {
		Pair<String, Integer> pair = new Pair<>("a", 42);
		assertThat(pair.getFirst(), equalTo("a"));
		assertThat(pair.getSecond(), equalTo(42));
		assertThat(pair, hasToString("(a,42)"));
	}

	@Test
	public void testNull() {
		Pair<String, Integer> pair = new Pair<>(null, null);
		assertThat(pair.getFirst(), equalTo(null));
		assertThat(pair.getSecond(), equalTo(null));
		assertThat(pair, hasToString("(null,null)"));
		// Just check that it does not throw
		assertThat(pair.hashCode(), anything());
	}

	@Test
	public void testEquals() {
		Pair<String, Integer> pair1 = new Pair<>("a", 42);

		assertThat(pair1, not(equalTo(null)));
		assertThat(pair1.equals("a"), is(false));
		assertThat(pair1.equals(42), is(false));
		assertThat(pair1, equalTo(pair1));

		Pair<String, Integer> pair2 = new Pair<>("a", 42);
		assertThat(pair1, equalTo(pair2));

		pair2 = new Pair<>("a", 1);
		assertThat(pair1, not(equalTo(pair2)));

		pair2 = new Pair<>("b", 42);
		assertThat(pair1, not(equalTo(pair2)));

		pair2 = new Pair<>("b", 1);
		assertThat(pair1, not(equalTo(pair2)));
	}

	@Test
	public void testZip() {
		Collection<String> list1 = Arrays.asList("a", null, "c");
		Collection<Integer> list2 = Arrays.asList(1, 2, null);

		assertThat(Pair.zip(list1, list2), contains(pairWith("a", 1), pairWith(null, 2), pairWith("c", null)));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testZipFail() {
		Pair.<String, String>zip(Arrays.asList("a"), Arrays.asList("b", "c"));
	}

	@Test
	public void testUnzip() {
		Collection<Pair<String, Integer>> input = Arrays.asList(new Pair<String, Integer>("a", 1), new
				Pair<String, Integer>(null, null), new Pair<String, Integer>("c", 3));
		assertThat(Pair.unzip(input), pairWith(contains("a", null, "c"), contains(1, null, 3)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
