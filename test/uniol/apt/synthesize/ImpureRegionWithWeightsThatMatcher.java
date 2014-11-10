/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

package uniol.apt.synthesize;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matcher to verify that a region satisfies some constraints.
 * @author Uli Schlachter, vsp
 */
public class ImpureRegionWithWeightsThatMatcher extends TypeSafeDiagnosingMatcher<Region> {

	private final String event;
	private final Matcher<Integer> backwardMatcher;
	private final Matcher<Integer> forwardMatcher;

	private ImpureRegionWithWeightsThatMatcher(String event, Matcher<Integer> backwardMatcher, Matcher<Integer> forwardMatcher) {
		this.event = event;
		this.backwardMatcher = backwardMatcher;
		this.forwardMatcher = forwardMatcher;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("region weight('");
		description.appendText(event);
		description.appendText("')=(");
		backwardMatcher.describeTo(description);
		description.appendText(", ");
		forwardMatcher.describeTo(description);
		description.appendText(")");
	}

	@Override
	public boolean matchesSafely(Region region, Description description) {
		boolean matches = true;

		description.appendText("region weight('");
		description.appendText(event);
		description.appendText("')=(");

		backwardMatcher.describeMismatch(region.getBackwardWeight(event), description);
		if (!backwardMatcher.matches(region.getBackwardWeight(event)))
			matches = false;

		description.appendText(",  ");
		backwardMatcher.describeMismatch(region.getForwardWeight(event), description);
		if (!forwardMatcher.matches(region.getForwardWeight(event)))
			matches = false;

		description.appendText(")");

		return matches;
	}

	@Factory
	public static Matcher<Region> impureRegionWithWeightsThat(String event, Matcher<Integer> backward, Matcher<Integer> forward) {
		return new ImpureRegionWithWeightsThatMatcher(event, backward, forward);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
