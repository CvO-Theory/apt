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

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;

/**
 * Matcher to verify that the nodes of a flow match the given matchers.
 * @author Uli Schlachter, vsp
 */
public class FlowThatConnectsMatcher extends TypeSafeDiagnosingMatcher<Flow> {

	private final Matcher<? super Node> sourceMatcher;
	private final Matcher<? super Node> targetMatcher;

	private FlowThatConnectsMatcher(Matcher<? super Node> sourceMatcher,
			Matcher<? super Node> targetMatcher) {
		this.sourceMatcher = sourceMatcher;
		this.targetMatcher = targetMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Flow with source <");
		sourceMatcher.describeTo(description);
		description.appendText("> and target <");
		targetMatcher.describeTo(description);
		description.appendText(">");
	}

	@Override
	public boolean matchesSafely(Flow flow, Description description) {
		Node source = flow.getSource();
		Node target = flow.getTarget();

		if (!sourceMatcher.matches(source)) {
			description.appendText("Flow with source <");
			sourceMatcher.describeMismatch(source, description);
			description.appendText(">");

			return false;
		}

		if (!targetMatcher.matches(target)) {
			description.appendText("Flow with target <");
			targetMatcher.describeMismatch(target, description);
			description.appendText(">");

			return false;
		}

		return true;
	}

	@Factory
	public static <T> Matcher<Flow> flowThatConnects(Matcher<? super Node> sourceMatcher,
			Matcher<? super Node> targetMatcher) {
		return new FlowThatConnectsMatcher(sourceMatcher, targetMatcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
