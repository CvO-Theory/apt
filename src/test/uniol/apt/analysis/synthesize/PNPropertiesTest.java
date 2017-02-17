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

package uniol.apt.analysis.synthesize;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class PNPropertiesTest {
	static private Matcher<? super PNProperties> containsAll(PNProperties expected) {
		final PNProperties f = expected;
		return new TypeSafeDiagnosingMatcher<PNProperties>() {
			@Override
			public void describeTo(Description description) {
				description.appendText(f.toString());
			}

			@Override
			public boolean matchesSafely(PNProperties properties, Description description) {
				return properties.containsAll(f);
			}
		};
	}

	@Test
	public void testNotKBounded() {
		assertThat(new PNProperties().isKBounded(), is(false));
	}

	@Test
	public void testAddKBoundedAndSafe() {
		PNProperties properties = new PNProperties().requireKBounded(7).requireSafe();
		assertThat(properties.isKBounded(), is(true));
		assertThat(properties.getKForKBounded(), is(1));
	}

	@Test
	public void testAddSafeAndKBounded() {
		PNProperties properties = new PNProperties().requireSafe().requireKBounded(7);
		assertThat(properties.isKBounded(), is(true));
		assertThat(properties.getKForKBounded(), is(1));
	}

	@Test
	public void testContainsAll() {
		PNProperties properties = new PNProperties();
		PNProperties properties2 = new PNProperties();
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.requireSafe();
		assertThat(properties, not(containsAll(properties2)));

		properties2 = new PNProperties();
		properties = properties.requireSafe();
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.requireKBounded(42);
		assertThat(properties, containsAll(properties2));

		properties = properties.setPlain(true);
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.setPure(true);
		assertThat(properties, not(containsAll(properties2)));

		properties = properties.setPure(true);
		assertThat(properties, containsAll(properties2));

		properties = properties.setMarkedGraph(true);
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.setHomogeneous(true);
		assertThat(properties, not(containsAll(properties2)));

		properties = properties.setHomogeneous(true);
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.setBehaviourallyConflictFree(true);
		assertThat(properties, not(containsAll(properties2)));

		properties = properties.setBehaviourallyConflictFree(true);
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.setBinaryConflictFree(true);
		assertThat(properties, not(containsAll(properties2)));

		properties = properties.setBinaryConflictFree(true);
		assertThat(properties, containsAll(properties2));

		properties = properties.setMergeFree(true);
		assertThat(properties, containsAll(properties2));
	}

	@Test
	public void testContainsAllKMarking() {
		PNProperties properties = new PNProperties();
		PNProperties properties2 = new PNProperties();
		assertThat(properties, containsAll(properties2));

		properties = properties.requireKMarking(2);
		assertThat(properties, containsAll(properties2));

		properties2 = properties2.requireKMarking(8);
		assertThat(properties, not(containsAll(properties2)));
		assertThat(properties2, containsAll(properties));

		properties = new PNProperties().requireKMarking(8);
		assertThat(properties, containsAll(properties2));
	}

	@Test
	public void testEquals() {
		PNProperties properties = new PNProperties();
		PNProperties properties2 = new PNProperties();
		assertThat(properties, equalTo(properties2));
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));

		properties2 = properties2.requireSafe();
		assertThat(properties, not(equalTo(properties2)));

		properties = properties.requireSafe();
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));

		properties = properties.setPlain(true);
		assertThat(properties, not(equalTo(properties2)));

		properties2 = properties2.setPlain(true);
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));

		properties = properties.setHomogeneous(true);
		assertThat(properties, not(equalTo(properties2)));

		properties2 = properties2.setHomogeneous(true);
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));
	}

	@Test
	public void testEqualsKMarking() {
		PNProperties properties = new PNProperties();
		PNProperties properties2 = new PNProperties();
		assertThat(properties, equalTo(properties2));
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));

		properties = properties.requireKMarking(2);
		properties2 = properties2.requireKMarking(3);
		assertThat(properties, not(equalTo(properties2)));

		properties = properties.requireKMarking(3);
		assertThat(properties, not(equalTo(properties2)));

		properties2 = properties2.requireKMarking(2);
		assertThat(properties, equalTo(properties2));
		assertThat(properties.hashCode(), equalTo(properties2.hashCode()));
	}

	@Test
	public void testToString() {
		PNProperties properties = new PNProperties();
		assertThat(properties, hasToString("[]"));

		properties = properties.requireKBounded(42);
		assertThat(properties, hasToString("[42-bounded]"));

		properties = properties.requireSafe();
		assertThat(properties, hasToString("[safe]"));

		properties = properties.setConflictFree(true);
		assertThat(properties, hasToString("[safe, conflict-free]"));

		properties = properties.setOutputNonbranching(true);
		assertThat(properties, hasToString("[safe, output-nonbranching, conflict-free]"));

		properties = properties.setConflictFree(false);
		assertThat(properties, hasToString("[safe, output-nonbranching]"));

		properties = properties.setMarkedGraph(true);
		assertThat(properties, hasToString("[safe, marked-graph, output-nonbranching]"));

		properties = properties.setHomogeneous(true);
		assertThat(properties, hasToString("[safe, marked-graph, output-nonbranching, homogeneous]"));

		properties = properties.requireKMarking(5);
		assertThat(properties, hasToString("[safe, 5-marking, marked-graph, output-nonbranching, homogeneous]"));

		properties = properties.requireKMarking(7);
		assertThat(properties, hasToString("[safe, 35-marking, marked-graph, output-nonbranching, homogeneous]"));

		properties = properties.setBehaviourallyConflictFree(true);
		assertThat(properties, hasToString("[safe, 35-marking, marked-graph, output-nonbranching, homogeneous, behaviourally-conflict-free]"));

		properties = properties.setBehaviourallyConflictFree(false);
		properties = properties.setBinaryConflictFree(true);
		assertThat(properties, hasToString("[safe, 35-marking, marked-graph, output-nonbranching, homogeneous, binary-conflict-free]"));

		properties = properties.setMergeFree(true);
		properties = properties.setBinaryConflictFree(false);
		assertThat(properties, hasToString("[safe, 35-marking, marked-graph, output-nonbranching, merge-free, homogeneous]"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
