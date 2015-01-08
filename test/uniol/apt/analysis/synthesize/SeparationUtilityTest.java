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
import java.util.Collections;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.apt.APTLTSParser;
import uniol.apt.util.equations.InequalitySystem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
		protected SeparationUtility pureSeparationUtility;
		protected SeparationUtility impureSeparationUtility;

		abstract protected TransitionSystem getTS();

		@BeforeClass
		public void setup() throws MissingLocationException {
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

			pureSeparationUtility = new SeparationUtility(utility, regionBasis, new PNProperties(PNProperties.PURE));
			impureSeparationUtility = new SeparationUtility(utility, regionBasis, new PNProperties());
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
			assertThat(pureSeparationUtility.getSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
		}

		@Test
		public void testCalculate1Impure() {
			State s = utility.getTransitionSystem().getNode("s");
			assertThat(impureSeparationUtility.getSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
		}

		@Test
		public void testStateSeparationUnreachable() {
			assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, ts.getNode("unreachable"), ts.getInitialState()), nullValue());
		}

		@Test
		public void testEventSeparationUnreachable() {
			assertThat(SeparationUtility.findSeparatingRegion(utility, regionBasis, ts.getNode("unreachable"), "a"), is(nullValue()));
		}

		@Test
		public void testCalculateUnreachable() {
			State s = utility.getTransitionSystem().getNode("unreachable");
			assertThat(pureSeparationUtility.getSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
		}

		@Test
		public void testCalculateUnreachableImpure() {
			State s = utility.getTransitionSystem().getNode("unreachable");
			assertThat(impureSeparationUtility.getSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
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
		public void testEventSeparation(String stateName, String event) throws MissingLocationException {
			State state = utility.getTransitionSystem().getNode(stateName);
			if (SeparationUtility.isEventEnabled(state, event) || stateName.equals("unreachable"))
				return;
			checkEventSeparation(utility, regionBasis, stateName, event, true);
		}

		@Test(dataProvider = "stateEventPairs")
		public void testEventSeparationImpure(String stateName, String event) throws MissingLocationException {
			State state = utility.getTransitionSystem().getNode(stateName);
			if (SeparationUtility.isEventEnabled(state, event) || stateName.equals("unreachable"))
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

	private static void checkEventSeparation(RegionUtility utility, List<Region> basis, String stateName, String event) throws MissingLocationException {
		checkEventSeparation(utility, basis, stateName, event, true);
	}

	private static void checkEventSeparation(RegionUtility utility, List<Region> basis, String stateName, String event, boolean pure) throws MissingLocationException {
		State state = utility.getTransitionSystem().getNode(stateName);
		PNProperties properties = new PNProperties();
		if (pure)
			properties.add(PNProperties.PURE);

		Region r = new SeparationUtility(utility, basis, properties).getSeparatingRegion(Collections.<Region>emptyList(), state, event);

		// "event" must have a non-zero backwards weight
		assertThat(r, impureRegionWithWeightThat(event, is(greaterThan(0)), anything()));

		// State "state" must be reachable (This only tests a necessary, but no sufficient, condition)
		assertThat(r.getMarkingForState(state), is(greaterThanOrEqualTo(0)));

		// After reaching state "state", "event" must be disabled
		assertThat(r.getMarkingForState(state), is(lessThan(r.getBackwardWeight(event))));
	}

	@Test
	public void testCalculate3() throws MissingLocationException {
		RegionUtility utility = new RegionUtility(TestTSCollection.getOneCycleLTS());
		List<Region> basis = new ArrayList<>();

		// Event order does not matter here, all events behave the same
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 1, 0, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 1, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0, 1)));

		checkEventSeparation(utility, basis, "s1", "c");
	}

	@Test
	public void testCalculate3Impure() throws MissingLocationException {
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
		public void testNoStateRestrictionPure() throws MissingLocationException {
			Region region = new SeparationUtility(utility, regionBasis, new PNProperties(PNProperties.PURE))
				.getSeparatingRegion(regionBasis, ts.getNode("s3"), "t2");

			assertThat(region.getMarkingForState(ts.getNode("s0")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s1")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s2")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}

		@Test
		public void testNoStateRestrictionImpure() throws MissingLocationException {
			Region region = new SeparationUtility(utility, regionBasis)
				.getSeparatingRegion(regionBasis, ts.getNode("s3"), "t2");

			assertThat(region.getMarkingForState(ts.getNode("s0")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s1")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s2")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}
	}

	@Test
	public void testLocations() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		ts.getArc("s0", "l", "a").putExtension("location", "X");
		ts.getArc("s0", "r", "b").putExtension("location", "Y");
		ts.getArc("l", "s1", "b").putExtension("location", "Y");
		ts.getArc("r", "s1", "a").putExtension("location", "X");

		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts)), arrayContaining("X", "Y"));
	}

	@Test(expectedExceptions = MissingLocationException.class)
	public void testMissingLocation() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		ts.getArc("s0", "l", "a").putExtension("location", "X");
		ts.getArc("r", "s1", "a").putExtension("location", "X");

		SeparationUtility.getLocationMap(new RegionUtility(ts));
	}

	@Test
	public static class testWordB2AB5AB6AB6 {
		protected List<String> word;
		protected TransitionSystem ts;
		protected SeparationUtility utility;

		@BeforeClass
		public void setup() throws Exception {
			word = Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b", "a", "b", "b",
					"b", "b", "b", "b");
			ts = SynthesizeWordModule.makeTS(word);
			RegionUtility reg_util = new RegionUtility(ts);

			List<Region> regionBasis = new ArrayList<>();
			regionBasis.add(Region.createPureRegionFromVector(reg_util, Arrays.asList(1, 0)));
			regionBasis.add(Region.createPureRegionFromVector(reg_util, Arrays.asList(0, 1)));

			utility = new SeparationUtility(reg_util, regionBasis);
		}

		@DataProvider(name = "stateDisabledEventPairs")
		public Object[][] createStateEventPairs() {
			List<Object[]> pairs = new ArrayList<>();
			for (State state : ts.getNodes())
				for (String event : ts.getAlphabet())
					if (!SeparationUtility.isEventEnabled(state, event))
						pairs.add(new Object[] { state.getId(), event });
			return pairs.toArray(new Object[][] {});
		}

		@Test(dataProvider = "stateDisabledEventPairs")
		public void testEventSeparation(String state, String event) throws MissingLocationException {
			Region r = utility.getSeparatingRegion(Collections.<Region>emptyList(), ts.getNode(state), event);

			assertThat(r, notNullValue());
			assertThat(r.getInitialMarking(), greaterThanOrEqualTo(0));

			for (State s : ts.getNodes())
				for (Arc arc : s.getPostsetEdges()) {
					String label = arc.getLabel();
					int backwards = r.getBackwardWeight(label);
					assertThat(r.getInitialMarking() + " : " + r + " : " + s.toString() + " : " + label,
							r.getMarkingForState(s), greaterThanOrEqualTo(backwards));
				}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
