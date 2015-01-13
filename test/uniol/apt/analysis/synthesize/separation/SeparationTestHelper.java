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

package uniol.apt.analysis.synthesize.separation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.SynthesizeWordModule;
import uniol.apt.io.parser.impl.apt.APTLTSParser;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
public class SeparationTestHelper {
	public interface SeparationFactory {
		Separation createSeparation(RegionUtility utility, List<Region> basis);
	}

	final private SeparationFactory factory;

	public SeparationTestHelper(SeparationFactory factory) {
		this.factory = factory;
	}

	static public Object[] factory(SeparationFactory factory) {
		return new Object[] {
			new SeparationTestHelper(factory),
			new PureSynthesizablePathTSTest(factory),
			new ImpureSynthesizablePathTSTest(factory),
			new CrashkursCC2Tests(factory),
			new TestWordB2AB5AB6AB6(factory)
		};
	}

	static private void checkEventSeparation(SeparationFactory factory, RegionUtility utility, List<Region> basis,
			String stateName, String event) {
		State state = utility.getTransitionSystem().getNode(stateName);
		Region r = factory.createSeparation(utility, basis).calculateSeparatingRegion(
				Collections.<Region>emptyList(), state, event);

		// "event" must have a non-zero backwards weight
		assertThat(r, impureRegionWithWeightThat(event, is(greaterThan(0)), anything()));

		// State "state" must be reachable (This only tests a necessary, but no sufficient, condition)
		assertThat(r.getMarkingForState(state), is(greaterThanOrEqualTo(0)));

		// After reaching state "state", "event" must be disabled
		assertThat(r.getMarkingForState(state), is(lessThan(r.getBackwardWeight(event))));
	}

	abstract static public class Tester {
		private final SeparationFactory factory;
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;
		protected Region region1;
		protected Region region2;
		protected Region region3;

		public Tester(SeparationFactory factory) {
			this.factory = factory;
		}

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
		public void testCalculate1() {
			Separation separation = factory.createSeparation(utility, regionBasis);
			State s = utility.getTransitionSystem().getNode("s");
			assertThat(separation.calculateSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
		}

		@Test
		public void testCalculateUnreachable() {
			Separation separation = factory.createSeparation(utility, regionBasis);
			State s = utility.getTransitionSystem().getNode("unreachable");
			assertThat(separation.calculateSeparatingRegion(Collections.<Region>emptyList(), s, "a"), is(nullValue()));
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
			if (SeparationUtility.isEventEnabled(state, event) || stateName.equals("unreachable"))
				return;
			checkEventSeparation(factory, utility, regionBasis, stateName, event);
		}
	}

	static public class PureSynthesizablePathTSTest extends Tester {
		public PureSynthesizablePathTSTest(SeparationFactory factory) {
			super(factory);
		}

		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getPureSynthesizablePathTS();
		}
	}

	static public class ImpureSynthesizablePathTSTest extends Tester {
		public ImpureSynthesizablePathTSTest(SeparationFactory factory) {
			super(factory);
		}

		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getImpureSynthesizablePathTS();
		}
	}

	@Test
	public void testCalculate3() {
		RegionUtility utility = new RegionUtility(TestTSCollection.getOneCycleLTS());
		List<Region> basis = new ArrayList<>();

		// Event order does not matter here, all events behave the same
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 1, 0, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 1, 0)));
		basis.add(Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0, 1)));

		checkEventSeparation(factory, utility, basis, "s1", "c");
	}

	static public class CrashkursCC2Tests {
		final private SeparationFactory factory;
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;

		public CrashkursCC2Tests(SeparationFactory factory) {
			this.factory = factory;
		}

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
		public void testNoStateRestriction() {
			Region region = factory.createSeparation(utility, regionBasis)
				.calculateSeparatingRegion(regionBasis, ts.getNode("s3"), "t2");

			assertThat(region.getMarkingForState(ts.getNode("s0")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s1")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s2")), greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}
	}

	static public class TestWordB2AB5AB6AB6 {
		final private SeparationFactory factory;

		protected List<String> word;
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;

		public TestWordB2AB5AB6AB6(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			word = Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b", "a", "b", "b",
					"b", "b", "b", "b");
			ts = SynthesizeWordModule.makeTS(word);
			utility = new RegionUtility(ts);

			regionBasis = new ArrayList<>();
			regionBasis.add(Region.createPureRegionFromVector(utility, Arrays.asList(1, 0)));
			regionBasis.add(Region.createPureRegionFromVector(utility, Arrays.asList(0, 1)));
		}

		@DataProvider(name = "stateDisabledEventPairs")
		private Object[][] createStateEventPairs() {
			List<Object[]> pairs = new ArrayList<>();
			for (State state : ts.getNodes())
				for (String event : ts.getAlphabet())
					if (!SeparationUtility.isEventEnabled(state, event))
						pairs.add(new Object[] { state.getId(), event });
			return pairs.toArray(new Object[][] {});
		}

		@Test(dataProvider = "stateDisabledEventPairs")
		public void testEventSeparation(String state, String event) {
			Separation separation = factory.createSeparation(utility, regionBasis);
			Region r = separation.calculateSeparatingRegion(Collections.<Region>emptyList(), ts.getNode(state), event);

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
