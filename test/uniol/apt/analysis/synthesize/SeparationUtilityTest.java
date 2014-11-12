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

package uniol.apt.analysis.synthesize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
public class SeparationUtilityTest {
	private TransitionSystem ts;
	private RegionUtility utility;
	private List<Region> regionBasis;
	private Region region1;
	private Region region2;
	private Region region3;

	@BeforeClass
	public void setup() {
		ts = TestTSCollection.getPureSynthesizablePathTS();

		// Add an unreachable state
		State unreachable = ts.createState("unreachable");
		ts.createArc(unreachable, unreachable, "c");

		utility = new RegionUtility(ts);

		// I'm lazy, let's hope that we always end up with this order (which is currently guaranteed because
		// TransitionSystem.getAlphabet() uses a SortedSet).
		assertThat(utility.getEventIndex("a"), is(0));
		assertThat(utility.getEventIndex("b"), is(1));
		assertThat(utility.getEventIndex("c"), is(2));

		region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0));
		region2 = Region.createPureRegionFromVector(utility, Arrays.asList(0, -1, 0));
		region3 = Region.createPureRegionFromVector(utility, Arrays.asList(0, 0, -1));

		regionBasis = new ArrayList<>();
		regionBasis.add(region1);
		regionBasis.add(region2);
		regionBasis.add(region3);
	}

	@Test
	public void testStateSelfSeparation() {
		// A state cannot be separated from itself
		for (State state : utility.getTransitionSystem().getNodes()) {
			assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, state, state), is(nullValue()));
		}
	}

	@Test
	public void testStateSeparation() {
		for (State s1 : utility.getTransitionSystem().getNodes()) {
			if (!utility.getSpanningTree().isReachable(s1))
				continue;

			for (State s2 : utility.getTransitionSystem().getNodes()) {
				if (s1 == s2 || !utility.getSpanningTree().isReachable(s2))
					continue;
				// All pairs of (reachable) states are separable
				assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, s1, s2), is(not(nullValue())));
			}
		}
	}

	@Test
	public void testEventSeparation1() {
		State w = utility.getTransitionSystem().getNode("w");
		assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, w, "c"), equalTo(region3));
	}

	@Test
	public void testEventSeparation2() {
		State s = utility.getTransitionSystem().getNode("s");
		assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, s, "c"), is(nullValue()));
	}

	private static void checkEventSeparation(RegionUtility utility, List<Region> basis, String stateName, String event) {
		State state = utility.getTransitionSystem().getNode(stateName);
		Region r = SeparationUtility.calculateSeparatingRegion(utility, basis, state, event);

		// "event" must have a non-zero backwards weight
		assertThat(r, impureRegionWithWeightThat(event, is(greaterThan(0)), anything()));

		// State "state" must be reachable
		assertThat(r.getNormalRegionMarking() + r.evaluateParikhVector(utility.getReachingParikhVector(state)), is(greaterThanOrEqualTo(0)));

		// After reaching state "state", "event" must be disabled
		assertThat(r.getNormalRegionMarking() + r.evaluateParikhVector(utility.getReachingParikhVector(state)), is(lessThan(r.getBackwardWeight(event))));
	}

	@Test
	public void testCalculate1() {
		State s = utility.getTransitionSystem().getNode("s");
		assertThat(SeparationUtility.calculateSeparatingRegion(utility, regionBasis, s, "a"), is(nullValue()));
	}

	@Test
	public void testCalculate2() {
		checkEventSeparation(utility, regionBasis, "t", "b");
	}

	@Test
	public void testCalculate3() {
		RegionUtility utility = new RegionUtility(TestTSCollection.getOneCycleLTS());
		List<Region> basis = new ArrayList<>();

		// Event order does not matter here, all events behave the same
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 1, 0, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 1, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0, 1)));

		checkEventSeparation(utility, basis, "s1", "c");
	}

	@Test
	public void testStateSeparationUnreachable() {
		assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, ts.getNode("unreachable"), ts.getInitialState()), nullValue());
	}

	@Test
	public void testEventSeparationUnreachable() {
		assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, ts.getNode("unreachable"), "a"), nullValue());
	}

	@Test
	public void testCalculateUnreachable() {
		assertThat(SeparationUtility.calculateSeparatingRegion(utility, regionBasis, ts.getNode("unreachable"), "a"), nullValue());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
