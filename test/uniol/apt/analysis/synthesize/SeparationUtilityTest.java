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
import uniol.apt.io.parser.impl.apt.APTLTSParser;
import uniol.apt.util.equations.InequalitySystem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
public class SeparationUtilityTest {
	public static abstract class Tester {
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;
		protected Region region1;
		protected Region region2;
		protected Region region3;

		abstract protected TransitionSystem getTS();

		@BeforeClass
		public void setup() {
			ts = getTS();

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

		@Test
		public void testCalculate1() {
			State s = utility.getTransitionSystem().getNode("s");
			InequalitySystem system = SeparationUtility.makeInequalitySystem(utility, regionBasis);
			assertThat(SeparationUtility.calculateSeparatingPureRegion(utility, regionBasis, system, s, "a"), is(nullValue()));
		}

		@Test
		public void testCalculate1Impure() {
			State s = utility.getTransitionSystem().getNode("s");
			InequalitySystem system = SeparationUtility.makeInequalitySystem(utility, regionBasis);
			assertThat(SeparationUtility.calculateSeparatingImpureRegion(utility, regionBasis, system, s, "a", false), is(nullValue()));
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
			InequalitySystem system = SeparationUtility.makeInequalitySystem(utility, regionBasis);
			assertThat(SeparationUtility.calculateSeparatingPureRegion(utility, regionBasis, system, ts.getNode("unreachable"), "a"), nullValue());

			system = SeparationUtility.makeInequalitySystem(utility, regionBasis);
			assertThat(SeparationUtility.calculateSeparatingImpureRegion(utility, regionBasis, system, ts.getNode("unreachable"), "a", false), nullValue());
		}

		@DataProvider(name = "stateEventPairs")
		public Object[][] createStateEventPairs() {
			List<Object[]> pairs = new ArrayList<>();
			for (State state : ts.getNodes())
				for (String event : ts.getAlphabet())
					pairs.add(new Object[] { state.getId(), event });
			return pairs.toArray(new Object[][] {});
		}

		@Test(dataProvider = "stateEventPairs")
		public void testEventSeparation(String stateName, String event) {
			State state = utility.getTransitionSystem().getNode(stateName);
			if (SeparationUtility.getFollowingState(state, event) != null || stateName.equals("unreachable"))
				return;
			checkEventSeparation(utility, regionBasis, stateName, event, true);
		}

		@Test(dataProvider = "stateEventPairs")
		public void testEventSeparationImpure(String stateName, String event) {
			State state = utility.getTransitionSystem().getNode(stateName);
			if (SeparationUtility.getFollowingState(state, event) != null || stateName.equals("unreachable"))
				return;
			checkEventSeparation(utility, regionBasis, stateName, event, false);
		}
	}

	@Test
	public static class PureSynthesizablePathTSTest extends Tester {
		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getPureSynthesizablePathTS();
		}
	}

	@Test
	public static class ImpureSynthesizablePathTSTest extends Tester {
		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getImpureSynthesizablePathTS();
		}
	}

	private static void checkEventSeparation(RegionUtility utility, List<Region> basis, String stateName, String event) {
		checkEventSeparation(utility, basis, stateName, event, true);
	}

	private static void checkEventSeparation(RegionUtility utility, List<Region> basis, String stateName, String event, boolean pure) {
		State state = utility.getTransitionSystem().getNode(stateName);
		InequalitySystem system = SeparationUtility.makeInequalitySystem(utility, basis);
		Region r;
		if (pure)
			r = SeparationUtility.calculateSeparatingPureRegion(utility, basis, system, state, event);
		else
			r = SeparationUtility.calculateSeparatingImpureRegion(utility, basis, system, state, event, false);

		// "event" must have a non-zero backwards weight
		assertThat(r, impureRegionWithWeightThat(event, is(greaterThan(0)), anything()));

		// State "state" must be reachable (This only tests a necessary, but no sufficient, condition)
		assertThat(r.getNormalRegionMarkingForState(state), is(greaterThanOrEqualTo(0)));

		// After reaching state "state", "event" must be disabled
		assertThat(r.getNormalRegionMarkingForState(state), is(lessThan(r.getBackwardWeight(event))));
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
	public void testCalculate3Impure() {
		RegionUtility utility = new RegionUtility(TestTSCollection.getOneCycleLTS());
		List<Region> basis = new ArrayList<>();

		// Event order does not matter here, all events behave the same
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 1, 0, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 1, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0, 1)));

		checkEventSeparation(utility, basis, "s1", "c", false);
	}

	@Test
	public static class CrashkursCC2Tests {
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;

		@BeforeClass
		public void setup() throws Exception {
			ts = APTLTSParser.getLTS("nets/crashkurs-cc2-aut.apt", true);
			utility = new RegionUtility(ts);

			// I'm lazy, let's hope that we always end up with this order (which is currently guaranteed because
			// TransitionSystem.getAlphabet() uses a SortedSet).
			assertThat(utility.getEventIndex("t1"), is(0));
			assertThat(utility.getEventIndex("t2"), is(1));
			assertThat(utility.getEventIndex("t3"), is(2));

			regionBasis = new ArrayList<>();
			regionBasis.add(Region.createPureRegionFromVector(utility, Arrays.asList(1, 0, -1)));
			regionBasis.add(Region.createPureRegionFromVector(utility, Arrays.asList(0, 1, 0)));
		}

		@Test
		public void testNoStateRestrictionPure() {
			Region region = SeparationUtility.findOrCalculateSeparatingRegion(utility, regionBasis,
					regionBasis, ts.getNode("s3"), "t2", new PNProperties(PNProperties.PURE));

			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s0")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s1")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s2")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}

		@Test
		public void testNoStateRestrictionImpure() {
			Region region = SeparationUtility.findOrCalculateSeparatingRegion(utility, regionBasis,
					regionBasis, ts.getNode("s3"), "t2", new PNProperties());

			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s0")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s1")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getNormalRegionMarkingForState(ts.getNode("s2")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}
	}

	public void testRequireKBoundedness() {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		RegionUtility utility = new RegionUtility(ts);

		InequalitySystem system = mock(InequalitySystem.class);
		when(system.getNumberOfVariables()).thenReturn(utility.getEventList().size() + 0);

		SeparationUtility.requireKBoundedness(utility, system, 42);

		// From state s to state v
		verify(system).addInequality(42, ">=", 0, 1);

		// From state s to state t
		verify(system).addInequality(42, ">=", 1, 0);

		// From state v to state s
		verify(system).addInequality(42, ">=", 0, -1);

		// From state v to state t
		verify(system).addInequality(42, ">=", 1, -1);

		// From state t to state s
		verify(system).addInequality(42, ">=", -1, 0);

		// From state t to state v
		verify(system).addInequality(42, ">=", -1, 1);

		verify(system, atLeastOnce()).getNumberOfVariables();
		verifyNoMoreInteractions(system);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
