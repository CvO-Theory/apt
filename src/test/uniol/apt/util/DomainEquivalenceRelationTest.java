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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
public class DomainEquivalenceRelationTest extends AbstractEquivalenceRelationTest {
	@Override
	<E> AbstractEquivalenceRelation<E> createRelation(E... domain) {
		return new DomainEquivalenceRelation<E>(Arrays.asList(domain));
	}

	@Test
	public void testEmptyRelation() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");

		assertThat(relation, containsInAnyOrder(contains("a"), contains("b"), contains("c"), contains("d")));
		assertThat(relation, hasSize(4));
		assertThat(relation.isEquivalent("a", "b"), is(false));
		assertThat(relation.getClass("a"), contains("a"));
	}

	@Test
	public void testIterateAfterChecks() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");

		assertThat(relation.isEquivalent("a", "b"), is(false));
		assertThat(relation.isEquivalent("c", "b"), is(false));
		assertThat(relation.getClass("a"), contains("a"));
		assertThat(relation.getClass("c"), contains("c"));

		assertThat(relation, containsInAnyOrder(contains("a"), contains("b"), contains("c"), contains("d")));
		assertThat(relation, hasSize(4));
	}

	@Test
	public void testTwoElements() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("c", "c"), containsInAnyOrder("c"));

		assertThat(relation.isEquivalent("a", "b"), is(true));
		assertThat(relation.isEquivalent("b", "a"), is(true));
		assertThat(relation.isEquivalent("c", "b"), is(false));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b"));
		assertThat(relation.getClass("c"), contains("c"));
		assertThat(relation.getClass("b"), sameInstance(relation.getClass("a")));
		assertThat(relation, hasSize(3));
		assertThat(relation, containsInAnyOrder(containsInAnyOrder("a", "b"), contains("c"), contains("d")));
	}

	@Test
	public void testThreeElements() {
		AbstractEquivalenceRelation<String> relation = createRelation("a", "b", "c", "d");
		assertThat(relation.joinClasses("a", "b"), containsInAnyOrder("a", "b"));
		assertThat(relation.joinClasses("a", "c"), containsInAnyOrder("a", "b", "c"));

		assertThat(relation.isEquivalent("c", "b"), is(true));
		assertThat(relation.getClass("a"), containsInAnyOrder("a", "b", "c"));
		assertThat(relation, containsInAnyOrder(containsInAnyOrder("a", "b", "c"), contains("d")));
		assertThat(relation, hasSize(2));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testDomain() {
		new DomainEquivalenceRelation<Integer>(Arrays.asList(1)).getClass(2);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

