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

import org.hamcrest.Factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import static org.hamcrest.Matchers.equalTo;

public class ContainsRotatedMatcher<T> extends TypeSafeDiagnosingMatcher<Iterable<? extends T>> {
	private final List<Matcher<? super T>> expected;

	private ContainsRotatedMatcher(List<Matcher<? super T>> expected) {
		this.expected = expected;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Iterable containing ")
			.appendList("[", ",", "]", expected)
			.appendText(" in rotated order");
	}

	@Override
	public boolean matchesSafely(Iterable<? extends T> values, Description description) {
		int numEntries = expected.size();
		List<T> alreadyMatched = new ArrayList<>();
		boolean[] isRotationPossible = new boolean[numEntries];
		Arrays.fill(isRotationPossible, true);

		int position = -1;
		for (T obj : values) {
			position++;

			// Check all rotations that are still possible if this entry matches
			boolean hadMatch = false;
			for (int rotation = 0; rotation < numEntries; rotation++) {
				if (!isRotationPossible[rotation])
					continue;

				int index = (rotation + position) % numEntries;
				if (position >= numEntries || !expected.get(index).matches(obj)) {
					isRotationPossible[rotation] = false;
					continue;
				}

				hadMatch = true;
			}

			if (!hadMatch) {
				description.appendText("not matched: ")
					.appendValue(obj)
					.appendText(" (after matching ")
					.appendValue(alreadyMatched)
					.appendText(")");
				return false;
			}

			alreadyMatched.add(obj);
		}

		// We now know that all objects from the iterable match.
		// Now check if too few objects were provided.
		if (numEntries > position + 1) {
			// There must have been some missing entries; find one of them.
			int rotation = 0;
			while (!isRotationPossible[rotation]) {
				rotation++;
				assert rotation < numEntries;
			}

			// At this rotation, the next (position+1) entries were matched.
			// Thus, we can say that the entry afterwards is missing.
			int index = (rotation + (position + 1) + 1) % numEntries;
			description.appendText("no item was ")
				.appendDescriptionOf(expected.get(index))
				.appendText(" at rotation ")
				.appendValue(rotation);
			return false;
		}

		return true;
	}

	/**
	 * Create a rotation-aware matcher for {@link Iterable}s that matches when the examined {@link Iterable}
	 * produces objects that match the given matchers in the correct order, or with some offset.
	 *
	 * For example, all possible rotations of the list "1,2,3,4" are "1,2,3,4", "2,3,4,1", "3,4,1,2", and "4,1,2,3".
	 * All of these four lists are considered to be the same by this matcher.
	 * @param expected The list of matchers that are expected to match.
	 */
	@Factory
	public static <T> Matcher<Iterable<? extends T>> containsRotated(List<Matcher<? super T>> expected) {
		return new ContainsRotatedMatcher<T>(expected);
	}

	/**
	 * Create a rotation-aware matcher for {@link Iterable}s that matches when the examined {@link Iterable}
	 * produces objects that match the given matchers in the correct order, or with some offset.
	 *
	 * For example, all possible rotations of the list "1,2,3,4" are "1,2,3,4", "2,3,4,1", "3,4,1,2", and "4,1,2,3".
	 * All of these four lists are considered to be the same by this matcher.
	 * @param expected The list of matchers that are expected to match.
	 */
	@Factory
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Matcher<Iterable<? extends T>> containsRotated(Matcher<? super T>... expected) {
		return containsRotated(Arrays.asList(expected));
	}

	/**
	 * Create a rotation-aware matcher for {@link Iterable}s that matches when the examined {@link Iterable}
	 * produces objects that are equal to the given order, in the correct order, or with some offset.
	 *
	 * For example, all possible rotations of the list "1,2,3,4" are "1,2,3,4", "2,3,4,1", "3,4,1,2", and "4,1,2,3".
	 * All of these four lists are considered to be the same by this matcher.
	 * @param expected The list of matchers that are expected to match.
	 */
	@Factory
	@SafeVarargs
	public static <T> Matcher<Iterable<? extends T>> containsRotated(T... expected) {
		List<Matcher<? super T>> expectedMatchers = new ArrayList<>();
		for (T obj : expected) {
			expectedMatchers.add(equalTo(obj));
		}
		return containsRotated(expectedMatchers);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
