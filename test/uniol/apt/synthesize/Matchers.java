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

package uniol.apt.synthesize;

import java.util.List;
import java.util.ArrayList;

import org.hamcrest.Matcher;

/**
 * Matchers to verify that Synthesize classes match given conditions.
 *
 * @author Uli Schlachter
 */
public class Matchers extends org.hamcrest.Matchers {
	public static Matcher<Region> pureRegion() {
		return PureRegionMatcher.pureRegion();
	}

	public static Matcher<Region> impureRegion() {
		return not(pureRegion());
	}

	public static Matcher<Region> impureRegionWithWeightsThat(String event, Matcher<Integer> backward, Matcher<Integer> forward) {
		return ImpureRegionWithWeightsThatMatcher.impureRegionWithWeightsThat(event, backward, forward);
	}

	public static Matcher<Region> pureRegionWithWeightThat(String event, Matcher<Integer> weight) {
		return PureRegionWithWeightsThatMatcher.pureRegionWithWeightsThat(event, weight);
	}

	public static Matcher<Region> impureRegionWithWeights(String event, int backward, int forward) {
		return impureRegionWithWeightsThat(event, equalTo(backward), equalTo(forward));
	}

	public static Matcher<Region> pureRegionWithWeight(String event, int weight) {
		return pureRegionWithWeightThat(event, equalTo(weight));
	}

	public static Matcher<Region> impureRegionWithWeightsThat(List<String> events, List<Matcher<Integer>> weightMatchers) {
		List<Matcher<? super Region>> matchers = new ArrayList<>();
		assert 2 * events.size() == weightMatchers.size();
		for (int i = 0; i < events.size(); i++) {
			matchers.add(impureRegionWithWeightsThat(events.get(i), weightMatchers.get(2*i), weightMatchers.get(2*i + 1)));
		}
		return allOf(matchers);
	}

	public static Matcher<Region> pureRegionWithWeightsThat(List<String> events, List<Matcher<Integer>> weightWatcher) {
		List<Matcher<? super Region>> matchers = new ArrayList<>();
		assert events.size() == weightWatcher.size();

		matchers.add(pureRegion());

		for (int i = 0; i < events.size(); i++) {
			matchers.add(pureRegionWithWeightThat(events.get(i), weightWatcher.get(i)));
		}
		return allOf(matchers);
	}

	public static Matcher<Region> impureRegionWithWeights(List<String> events, List<Integer> weights) {
		List<Matcher<Integer>> matchers = new ArrayList<>();
		for (int i = 0; i < weights.size(); i++) {
			matchers.add(equalTo(weights.get(i)));
		}
		return impureRegionWithWeightsThat(events, matchers);
	}

	public static Matcher<Region> pureRegionWithWeights(List<String> events, List<Integer> weights) {
		List<Matcher<Integer>> matchers = new ArrayList<>();
		for (int i = 0; i < weights.size(); i++) {
			matchers.add(equalTo(weights.get(i)));
		}
		return pureRegionWithWeightsThat(events, matchers);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
