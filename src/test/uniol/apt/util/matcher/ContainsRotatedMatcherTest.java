/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.util.matcher;

import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.util.matcher.Matchers.containsRotated;

public class ContainsRotatedMatcherTest {
	@Test
	public void emptyList() {
		assertThat(Collections.emptyList(), containsRotated());
	}

	@Test
	public void emptyListExpected() {
		assertThat(Arrays.asList(3), not(containsRotated()));
	}

	@Test
	public void emptyListGiven() {
		assertThat(Collections.<Integer>emptyList(), not(containsRotated(3)));
	}

	@Test
	public void singletonList() {
		assertThat(Arrays.asList(3), containsRotated(3));
	}

	@Test
	public void tooManyMatchers() {
		assertThat(Arrays.asList(3), not(containsRotated(3, 3)));
	}

	@Test
	public void tooFewMatchers() {
		assertThat(Arrays.asList(3, 3), not(containsRotated(3)));
	}

	@Test
	public void manySameEntries() {
		assertThat(Arrays.asList(3, 3, 3, 3), containsRotated(3, 3, 3, 3));
	}

	@Test
	public void manySameEntriesOneDifferent() {
		assertThat(Arrays.asList(3, 3, 4, 3), containsRotated(3, 3, 4, 3));
	}

	@Test
	public void manySameEntriesOneDifferentRotated() {
		assertThat(Arrays.asList(3, 3, 4, 3), containsRotated(3, 4, 3, 3));
	}

	@Test
	public void twiceSameSequence() {
		assertThat(Arrays.asList(1, 2, 1, 2), containsRotated(1, 2, 1, 2));
	}

	@Test
	public void twiceSameSequenceRotated() {
		assertThat(Arrays.asList(1, 2, 1, 2), containsRotated(2, 1, 2, 1));
	}

	@Test
	public void twiceSameSequenceRotated2() {
		assertThat(Arrays.asList(3, 3, 4, 3, 3, 4), containsRotated(3, 4, 3, 3, 4, 3));
	}

	@Test
	public void sequence() {
		assertThat(Arrays.asList(1, 2, 3, 4), containsRotated(1, 2, 3, 4));
	}

	@Test
	public void sequenceRotated() {
		assertThat(Arrays.asList(1, 2, 3, 4), containsRotated(4, 1, 2, 3));
	}

	@Test
	public void sequenceReversed() {
		assertThat(Arrays.asList(1, 2, 3, 4), not(containsRotated(4, 3, 2, 1)));
	}

	@Test
	public void makeSureIGotTheGenericsRight() {
		Matcher<? super Integer> expected = anything();
		Matcher<Iterable<? extends Integer>> matcher = containsRotated(expected);
		assertThat(Arrays.asList(1), matcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
