/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import uniol.apt.analysis.synthesize.Region;

/**
 * Matcher to verify that a Region has a matching initial marking.
 * @author Uli Schlachter
 */
public class RegionWithInitialMarkingThatMatcher extends FeatureMatcher<Region, BigInteger> {
	private RegionWithInitialMarkingThatMatcher(Matcher<? super BigInteger> matcher) {
		super(matcher, "region with initial marking", "initial marking");
	}

	@Override
	protected BigInteger featureValueOf(Region region) {
		return region.getInitialMarking();
	}

	@Factory
	public static <T> Matcher<Region> regionWithInitialMarking(Matcher<? super BigInteger> matcher) {
		return new RegionWithInitialMarkingThatMatcher(matcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
