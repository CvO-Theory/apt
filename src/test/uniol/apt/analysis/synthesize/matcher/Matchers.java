/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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
import java.util.List;
import java.util.ArrayList;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import uniol.apt.analysis.synthesize.Region;

/**
 * Matchers to verify that Synthesize classes match given conditions.
 *
 * @author Uli Schlachter
 */
public class Matchers {
	public static Matcher<Region> plainRegion() {
		return PlainRegionMatcher.plainRegion();
	}

	public static Matcher<Region> pureRegion() {
		return PureRegionMatcher.pureRegion();
	}

	public static Matcher<Region> impureRegion() {
		return not(pureRegion());
	}

	public static Matcher<Region> impureRegionWithWeightThat(String event,
			Matcher<? super BigInteger> backward, Matcher<? super BigInteger> forward) {
		return ImpureRegionWithWeightThatMatcher.impureRegionWithWeightThat(event, backward, forward);
	}

	public static Matcher<Region> pureRegionWithWeightThat(String event, Matcher<? super BigInteger> weight) {
		return PureRegionWithWeightThatMatcher.pureRegionWithWeightThat(event, weight);
	}

	public static Matcher<Region> impureRegionWithWeight(String event, int backward, int forward) {
		return impureRegionWithWeightThat(event, equalTo(BigInteger.valueOf(backward)),
				equalTo(BigInteger.valueOf(forward)));
	}

	public static Matcher<Region> pureRegionWithWeight(String event, int weight) {
		return pureRegionWithWeightThat(event, equalTo(BigInteger.valueOf(weight)));
	}

	public static Matcher<Region> impureRegionWithWeightsThat(List<String> events,
			List<Matcher<? super BigInteger>> weightMatchers) {
		List<Matcher<? super Region>> matchers = new ArrayList<>();
		assert 2 * events.size() == weightMatchers.size();
		for (int i = 0; i < events.size(); i++) {
			matchers.add(impureRegionWithWeightThat(events.get(i),
						weightMatchers.get(2 * i), weightMatchers.get(2 * i + 1)));
		}
		return allOf(matchers);
	}

	public static Matcher<Region> pureRegionWithWeightsThat(List<String> events,
			List<Matcher<? super BigInteger>> weightWatcher) {
		List<Matcher<? super Region>> matchers = new ArrayList<>();
		assert events.size() == weightWatcher.size();

		matchers.add(pureRegion());

		for (int i = 0; i < events.size(); i++) {
			matchers.add(pureRegionWithWeightThat(events.get(i), weightWatcher.get(i)));
		}
		return allOf(matchers);
	}

	public static Matcher<Region> impureRegionWithWeights(List<String> events, List<BigInteger> weights) {
		List<Matcher<? super BigInteger>> matchers = new ArrayList<>();
		for (int i = 0; i < weights.size(); i++) {
			matchers.add(equalTo(weights.get(i)));
		}
		return impureRegionWithWeightsThat(events, matchers);
	}

	public static Matcher<Region> pureRegionWithWeights(List<String> events, List<BigInteger> weights) {
		List<Matcher<? super BigInteger>> matchers = new ArrayList<>();
		for (int i = 0; i < weights.size(); i++) {
			matchers.add(equalTo(weights.get(i)));
		}
		return pureRegionWithWeightsThat(events, matchers);
	}

	public static Matcher<Region> regionWithInitialMarking(Matcher<? super BigInteger> markingThat) {
		return RegionWithInitialMarkingThatMatcher.regionWithInitialMarking(markingThat);
	}

	public static Matcher<Region> regionWithInitialMarking(int marking) {
		return regionWithInitialMarking(equalTo(BigInteger.valueOf(marking)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
