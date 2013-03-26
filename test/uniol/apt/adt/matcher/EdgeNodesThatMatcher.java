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

package uniol.apt.adt.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;

/**
 * Matcher to verify that the nodes of an arc match the given matchers.
 *
 * @author vsp
 */
public class EdgeNodesThatMatcher extends TypeSafeDiagnosingMatcher<Arc> {
	private final Matcher<State> sourceMatcher;
	private final Matcher<State> targetMatcher;

	private EdgeNodesThatMatcher(Matcher<State> sourceMatcher, Matcher<State> targetMatcher) {
		this.sourceMatcher = sourceMatcher;
		this.targetMatcher = targetMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Edge with source <");
		sourceMatcher.describeTo(description);
		description.appendText("> and target <");
		targetMatcher.describeTo(description);
		description.appendText(">");
	}

	@Override
	public boolean matchesSafely(Arc edge, Description description) {
		State source = edge.getSource();
		State target = edge.getTarget();

		if (!sourceMatcher.matches(source)) {
			description.appendText("Edge with source <");
			sourceMatcher.describeMismatch(source, description);
			description.appendText(">");

			return false;
		}

		if (!targetMatcher.matches(target)) {
			description.appendText("Edge with target <");
			targetMatcher.describeMismatch(target, description);
			description.appendText(">");

			return false;
		}

		return true;
	}

	@Factory
	public static <T> Matcher<Arc> edgeNodesThat(Matcher<State> sourceMatcher, Matcher<State> targetMatcher) {
		return new EdgeNodesThatMatcher(sourceMatcher, targetMatcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
