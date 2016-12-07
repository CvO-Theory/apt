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

package uniol.apt.util.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import uniol.apt.util.Pair;

/**
 * Matcher to verify that the fields of a pair match the given matchers.
 * @author Uli Schlachter, vsp
 */
public class PairWithMatcher<T, U> extends TypeSafeDiagnosingMatcher<Pair<? extends T, ? extends U>> {
	private final Matcher<T> firstMatcher;
	private final Matcher<U> secondMatcher;

	private PairWithMatcher(Matcher<T> firstMatcher, Matcher<U> secondMatcher) {
		this.firstMatcher = firstMatcher;
		this.secondMatcher = secondMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Pair with <");
		firstMatcher.describeTo(description);
		description.appendText("> and <");
		secondMatcher.describeTo(description);
		description.appendText(">");
	}

	@Override
	public boolean matchesSafely(Pair<? extends T, ? extends U> pair, Description description) {
		if (!firstMatcher.matches(pair.getFirst())) {
			description.appendText("Pair with first <");
			firstMatcher.describeMismatch(pair.getFirst(), description);
			description.appendText(">");

			return false;
		}

		if (!secondMatcher.matches(pair.getSecond())) {
			description.appendText("Pair with second <");
			firstMatcher.describeMismatch(pair.getSecond(), description);
			description.appendText(">");

			return false;
		}

		return true;
	}

	@Factory
	public static <T, U> Matcher<? super Pair<? extends T, ? extends U>> pairWith(Matcher<T> firstMatcher,
			Matcher<U> secondMatcher) {
		return new PairWithMatcher<T, U>(firstMatcher, secondMatcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
