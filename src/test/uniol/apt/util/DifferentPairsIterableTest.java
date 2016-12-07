/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import static uniol.apt.util.matcher.Matchers.*;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
public class DifferentPairsIterableTest {
	@Test
	public void testDifferentPairsIterableNonEmpty() {
		Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
		assertThat(new DifferentPairsIterable<String>(set), containsInAnyOrder(
					either(pairWith(is("a"), is("b"))).or(pairWith(is("b"), is("a"))),
					either(pairWith(is("a"), is("c"))).or(pairWith(is("c"), is("a"))),
					either(pairWith(is("b"), is("c"))).or(pairWith(is("c"), is("b")))));
	}

	@Test
	public void testDifferentPairsIterableEmpty() {
		Set<String> set = new HashSet<>();
		assertThat(new DifferentPairsIterable<>(set), emptyIterable());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
