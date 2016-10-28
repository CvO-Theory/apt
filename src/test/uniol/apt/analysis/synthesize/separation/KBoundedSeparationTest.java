/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;

import static uniol.apt.TestTSCollection.*;
import static org.hamcrest.Matchers.*;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked")
public class KBoundedSeparationTest {
	static private Set<Region> calculateRegions(TransitionSystem ts, int k, boolean pure)
		throws UnsupportedPNPropertiesException {
		PNProperties properties = new PNProperties().requireKBounded(k);
		if (pure)
			properties = properties.setPure(pure);
		return new KBoundedSeparation(ts, properties).getRegions();
	}

	static private List<BigInteger> asBigIntegerList(int... list) {
		List<BigInteger> result = new ArrayList<>(list.length);
		for (int i = 0; i < list.length; i++)
			result.add(BigInteger.valueOf(list[i]));
		return result;
	}

	@Test
	public void testSingleStateTSWithLoop() throws Exception {
		TransitionSystem ts = getSingleStateTSWithLoop();

		assertThat(calculateRegions(ts, 42, false),
				contains(impureRegionWithWeights(Arrays.asList("a"), asBigIntegerList(1, 1))));
		assertThat(calculateRegions(ts, 42, true),
				contains(impureRegionWithWeights(Arrays.asList("a"), asBigIntegerList(0, 0))));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() throws Exception {
		TransitionSystem ts = getThreeStatesTwoEdgesTS();

		assertThat(calculateRegions(ts, 42, false),
				containsInAnyOrder(
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 1, 0, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(1, 0, 1, 0))));
		assertThat(calculateRegions(ts, 42, true),
				containsInAnyOrder(
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 1, 0, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(1, 0, 1, 0))));
	}

	@Test
	public void testTwoStateCycleSameLabelTS() throws Exception {
		TransitionSystem ts = getTwoStateCycleSameLabelTS();

		assertThat(calculateRegions(ts, 42, false),
				contains(impureRegionWithWeights(Arrays.asList("a"), asBigIntegerList(1, 1))));
		assertThat(calculateRegions(ts, 42, true),
				contains(impureRegionWithWeights(Arrays.asList("a"), asBigIntegerList(0, 0))));
	}

	@Test
	public void testPathTS() throws Exception {
		TransitionSystem ts = getPathTS();

		assertThat(calculateRegions(ts, 2, false),
				containsInAnyOrder(
					// minimal regions
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 0, 0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 0, 0, 0, 1, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 1, 1, 1, 1, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 0, 0, 0, 0, 1)),
					// non-minimal regions
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 1, 1, 1, 1, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 2, 2, 2, 2, 0))));
		assertThat(calculateRegions(ts, 2, true),
				containsInAnyOrder(
					// minimal regions
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 0, 0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 0, 0, 0, 1, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 1, 0, 0, 1, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 0, 0, 0, 0, 1)),
					// non-minimal regions
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 1, 0, 0, 0, 0)),
					impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 2, 0, 0, 2, 0))
					));
	}

	@Test
	public void testABandB() throws Exception {
		TransitionSystem ts = getABandB();

		assertThat(calculateRegions(ts, 42, false),
				containsInAnyOrder(
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(1, 1, 1, 0))));
		assertThat(calculateRegions(ts, 42, true),
				containsInAnyOrder(
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 0, 0, 1)),
					impureRegionWithWeights(Arrays.asList("a", "b"), asBigIntegerList(0, 0, 1, 0))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
