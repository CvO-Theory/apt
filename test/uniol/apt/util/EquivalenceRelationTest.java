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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.matcher.Matchers.*;

/** @author Uli Schlachter */
@Test
@SuppressWarnings("unchecked") // I hate generics
public class EquivalenceRelationTest {
	@Test
	public void testEmptyRelation() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();

		assertThat(relation, emptyIterable());
		assertThat(relation, hasSize(0));
		assertThat(relation.isEquivalent("a", "b"), is(false));
		assertThat(relation.getClass("a"), contains("a"));
	}

	@Test
	public void testIterateAfterChecks() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();

		assertThat(relation.isEquivalent("a", "b"), is(false));
		assertThat(relation.isEquivalent("c", "b"), is(false));
		assertThat(relation.getClass("a"), contains("a"));
		assertThat(relation.getClass("c"), contains("c"));

		assertThat(relation, emptyIterable());
		assertThat(relation, hasSize(0));
	}

	@Test
	public void testReflexive() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();

		assertThat(relation.isEquivalent("a", "a"), is(true));
	}

	@Test
	public void testTwoElements() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));

		assertThat(relation.isEquivalent("a", "b"), is(true));
		assertThat(relation.isEquivalent("b", "a"), is(true));
		assertThat(relation.isEquivalent("c", "b"), is(false));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b"));
		assertThat(relation.getClass("c"), contains("c"));
		assertThat(relation.getClass("b"), sameInstance(relation.getClass("a")));
		assertThat(relation, contains(containsInAnyOrder("a", "b")));
		assertThat(relation, hasSize(1));
	}

	@Test
	public void testThreeElements() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("a", "c"), containsInAnyOrder("a", "b", "c"));

		assertThat(relation.isEquivalent("c", "b"), is(true));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b", "c"));
		assertThat(relation, contains(containsInAnyOrder("a", "b", "c")));
		assertThat(relation, hasSize(1));
	}

	@Test
	public void testTwoClasses() {
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();
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
		EquivalenceRelation<String> relation = new EquivalenceRelation<>();
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("c", "d"), containsInAnyOrder("c", "d"));
		assertThat(relation.joinClasses("a", "d"), containsInAnyOrder("a", "b", "c", "d"));

		assertThat(relation.isEquivalent("c", "b"), is(true));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b", "c", "d"));
		assertThat(relation, contains(containsInAnyOrder("a", "b", "c", "d")));
		assertThat(relation, hasSize(1));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
