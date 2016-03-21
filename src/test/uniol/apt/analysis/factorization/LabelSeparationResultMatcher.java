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
 * Matcher for LabelSeparationResult that allows for witnessState1 and
 * witnessState2 to be interchangeable.
 *
 * @author Jonas Prellberg
 *
 */
public class LabelSeparationResultMatcher extends TypeSafeMatcher<LabelSeparationResult> {

	private final LabelSeparationResult result;

	private LabelSeparationResultMatcher(LabelSeparationResult result) {
		this.result = result;
	}

	@Override
	public void describeTo(Description description) {
		String s = String.format(
			"%s label separation result with witness states %s and %s",
			result.isSeparated() ? "positive" : "negative",
			result.getWitnessState1(),
			result.getWitnessState2()
		);
		description.appendText(s);
	}

	@Override
	protected boolean matchesSafely(LabelSeparationResult arg) {
		return result.isSeparated() == arg.isSeparated() && (
			(
				Objects.equals(result.getWitnessState1(), arg.getWitnessState1()) &&
				Objects.equals(result.getWitnessState2(), arg.getWitnessState2())
			) ||
			(
				Objects.equals(result.getWitnessState1(), arg.getWitnessState2()) &&
				Objects.equals(result.getWitnessState2(), arg.getWitnessState1())
			)
		);
	}

	/**
	 * Returns a matcher that compares two LabelSeparationResult. It allows
	 * for the witness states to be in a different order.
	 *
	 * @param result the LabelSeparationResult to match
	 * @return the Matcher
	 */
	public static Matcher<LabelSeparationResult> labelSeparationResultMatches(LabelSeparationResult result) {
		return new LabelSeparationResultMatcher(result);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
