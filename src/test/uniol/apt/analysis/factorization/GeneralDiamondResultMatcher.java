/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.factorization;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for GeneralDiamondResults that allows for label1 and label2 to be
 * interchangeable.
 *
 * @author Jonas Prellberg
 *
 */
public class GeneralDiamondResultMatcher extends TypeSafeMatcher<GeneralDiamondResult> {

	private final GeneralDiamondResult result;

	private GeneralDiamondResultMatcher(GeneralDiamondResult result) {
		this.result = result;
	}

	@Override
	public void describeTo(Description description) {
		String s = String.format(
			"%s general diamond result with witness state %s and unordered labels %s (%s), %s (%s)",
			result.isGdiam() ? "positive" : "negative",
			result.getWitnessState(),
			result.getWitnessLabel1(),
			result.isWitnessLabel1Forward() ? "forward" : "backward",
			result.getWitnessLabel2(),
			result.isWitnessLabel2Forward() ? "forward" : "backward"
		);
		description.appendText(s);
	}

	@Override
	protected boolean matchesSafely(GeneralDiamondResult arg) {
		if (result.isGdiam() != arg.isGdiam()
				|| !Objects.equals(result.getWitnessState(), arg.getWitnessState())) {
			return false;
		}

		if (Objects.equals(result.getWitnessLabel1(), arg.getWitnessLabel1())) {
			return result.isWitnessLabel1Forward() == arg.isWitnessLabel1Forward()
					&& Objects.equals(result.getWitnessLabel2(), arg.getWitnessLabel2())
					&& result.isWitnessLabel2Forward() == arg.isWitnessLabel2Forward();
		}

		if (Objects.equals(result.getWitnessLabel1(), arg.getWitnessLabel2())) {
			return result.isWitnessLabel1Forward() == arg.isWitnessLabel2Forward()
					&& Objects.equals(result.getWitnessLabel2(), arg.getWitnessLabel1())
					&& result.isWitnessLabel2Forward() == arg.isWitnessLabel1Forward();
		}

		return false;
	}

	/**
	 * Returns a matcher that compares two GeneralDiamondResults. It allows
	 * for different witness label ordering.
	 *
	 * @param result the GeneralDiamondResult to match
	 * @return the Matcher
	 */
	public static Matcher<GeneralDiamondResult> gdiamResultMatches(GeneralDiamondResult result) {
		return new GeneralDiamondResultMatcher(result);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
