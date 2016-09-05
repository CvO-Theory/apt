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

package uniol.apt.analysis.synthesize.matcher;

import java.math.BigInteger;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import uniol.apt.analysis.synthesize.Region;

/**
 * Matcher to verify that a region is plain.
 * @author Uli Schlachter, vsp
 */
public class PlainRegionMatcher extends TypeSafeDiagnosingMatcher<Region> {

	private PlainRegionMatcher() {
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("plain region");
	}

	@Override
	public boolean matchesSafely(Region region, Description description) {
		boolean matches = true;

		description.appendText("{");

		for (String event : region.getTransitionSystem().getAlphabet()) {
			BigInteger backward = region.getBackwardWeight(event);
			BigInteger forward = region.getForwardWeight(event);

			if (backward.compareTo(BigInteger.ONE) <= 0 && forward.compareTo(BigInteger.ONE) <= 0)
				continue;

			if (!matches)
				description.appendText(", ");

			description.appendText("" + backward).appendText(":");
			description.appendText(event).appendText(":");
			description.appendText("" + forward);

			matches = false;
		}

		description.appendText("}");

		return matches;
	}

	@Factory
	public static Matcher<Region> plainRegion() {
		return new PlainRegionMatcher();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
