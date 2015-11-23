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

import static org.hamcrest.Matchers.anything;

/**
 * Matcher to verify that the nodes of an arc match the given matchers.
 * @author Uli Schlachter, vsp
 */
public class ArcThatConnectsMatcher extends TypeSafeDiagnosingMatcher<Arc> {

	private final Matcher<? super State> sourceMatcher;
	private final Matcher<? super State> targetMatcher;
	private final Matcher<? super String> labelMatcher;

	private ArcThatConnectsMatcher(Matcher<? super State> sourceMatcher,
			Matcher<? super State> targetMatcher, Matcher<? super String> labelMatcher) {
		this.sourceMatcher = sourceMatcher;
		this.targetMatcher = targetMatcher;
		this.labelMatcher = labelMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Arc with source <");
		sourceMatcher.describeTo(description);
		description.appendText(">, target <");
		targetMatcher.describeTo(description);
		description.appendText("> and label <");
		labelMatcher.describeTo(description);
		description.appendText(">");
	}

	@Override
	public boolean matchesSafely(Arc arc, Description description) {
		State source = arc.getSource();
		State target = arc.getTarget();
		String label = arc.getLabel();

		if (!sourceMatcher.matches(source)) {
			description.appendText("Arc with source <");
			sourceMatcher.describeMismatch(source, description);
			description.appendText(">");

			return false;
		}

		if (!targetMatcher.matches(target)) {
			description.appendText("Arc with target <");
			targetMatcher.describeMismatch(target, description);
			description.appendText(">");

			return false;
		}

		if (!labelMatcher.matches(label)) {
			description.appendText("Arc with label <");
			labelMatcher.describeMismatch(label, description);
			description.appendText(">");

			return false;
		}

		return true;
	}

	@Factory
	public static <T> Matcher<Arc> arcThatConnectsVia(Matcher<? super State> sourceMatcher,
			Matcher<? super State> targetMatcher, Matcher<? super String> labelMatcher) {
		return new ArcThatConnectsMatcher(sourceMatcher, targetMatcher, labelMatcher);
	}

	@Factory
	public static <T> Matcher<Arc> arcThatConnects(Matcher<? super State> sourceMatcher,
			Matcher<? super State> targetMatcher) {
		return arcThatConnectsVia(sourceMatcher, targetMatcher, anything());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
