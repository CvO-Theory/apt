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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.io.parser.ParserTestUtils;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.*;

/** @author Uli Schlachter */
public class SeparationTestHelper {
	public interface SeparationFactory {
		Separation createSeparation(RegionUtility utility, String[] locationMap);
		boolean supportsImpure();

		Separation createSeparation(RegionUtility utility, PNProperties properties, String[] locationMap)
			throws UnsupportedPNPropertiesException;
	}

	final private SeparationFactory factory;

	public SeparationTestHelper(SeparationFactory factory) {
		this.factory = factory;
	}

	static public Object[] factory(SeparationFactory factory) {
		return factory(factory, true);
	}

	static public Object[] factory(SeparationFactory factory, boolean includeWord) {
		return factory(factory, includeWord, true);
	}

	static public Object[] factory(SeparationFactory factory, boolean includeWord, boolean includeNonReversible) {
		return factory(factory, includeWord, includeNonReversible, true);
	}

	static public Object[] factory(SeparationFactory factory, boolean includeWord, boolean includeNonReversible,
			boolean includePurePathTS) {
		List<Object> pairs = new ArrayList<>();
		pairs.add(new SeparationTestHelper(factory));
		if (includeNonReversible) {
			if (includePurePathTS)
				pairs.add(new PureSynthesizablePathTS(factory));
			pairs.add(new ImpureSynthesizablePathTS(factory));
			pairs.add(new CrashkursCC2Tests(factory));
			pairs.add(new TestWordCBADCBA(factory));
			pairs.add(new TestStateSeparation(factory));
			pairs.add(new TestLocationSSP(factory));
		}
		if (includeWord)
			pairs.add(new TestWordB2AB5AB6AB6(factory));
		return pairs.toArray(new Object[pairs.size()]);
	}

	static private void checkEventSeparation(SeparationFactory factory, RegionUtility utility,
			String stateName, String event) throws UnreachableException {
		State state = utility.getTransitionSystem().getNode(stateName);
		String[] locationMap = new String[utility.getNumberOfEvents()];
		Region r = factory.createSeparation(utility, locationMap).calculateSeparatingRegion(state, event);

		assertThat(r, notNullValue());

		// "event" must have a non-zero backwards weight
		assertThat(r, impureRegionWithWeightThat(event, is(greaterThan(BigInteger.ZERO)), anything()));

		// State "state" must be reachable (This only tests a necessary, but no sufficient, condition)
		assertThat(r.getMarkingForState(state), is(greaterThanOrEqualTo(BigInteger.ZERO)));

		// After reaching state "state", "event" must be disabled
		assertThat(r.getMarkingForState(state), is(lessThan(r.getBackwardWeight(event))));
	}

	abstract static public class Tester {
		private final SeparationFactory factory;
		private final String skipEvent;
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected Separation separation;

		public Tester(SeparationFactory factory, String skipEvent) {
			this.factory = factory;
			this.skipEvent = skipEvent;
		}

		public Tester(SeparationFactory factory) {
			this(factory, null);
		}

		abstract protected TransitionSystem getTS();

		@BeforeClass
		public void setup() {
			ts = getTS();

			// Add an unreachable state
			State unreachable = ts.createState("unreachable");
			ts.createArc(unreachable, unreachable, "c");

			utility = new RegionUtility(ts);

			// I'm lazy, let's hope that we always end up with this order (which is currently guaranteed
			// because TransitionSystem.getAlphabet() uses a SortedSet).
			assertThat(utility.getEventIndex("a"), is(0));
			assertThat(utility.getEventIndex("b"), is(1));
			assertThat(utility.getEventIndex("c"), is(2));

			String[] locationMap = new String[3];
			separation = factory.createSeparation(utility, locationMap);
		}

		@Test
		public void testCalculate1() {
			State s = utility.getTransitionSystem().getNode("s");
			assertThat(separation.calculateSeparatingRegion(s, "a"), is(nullValue()));
		}

		@Test
		public void testCalculateUnreachable() {
			State s = utility.getTransitionSystem().getNode("unreachable");
			assertThat(separation.calculateSeparatingRegion(s, "a"), is(nullValue()));
		}

		@DataProvider(name = "stateEventPairs")
		public Object[][] createStateEventPairs() {
			List<Object[]> pairs = new ArrayList<>();
			for (State state : ts.getNodes()) {
				if (state.getId().equals("unreachable"))
					continue;
				for (String event : ts.getAlphabet())
					if (!SeparationUtility.isEventEnabled(state, event) &&
							(!event.equals(skipEvent) || factory.supportsImpure()))
						pairs.add(new Object[] { state.getId(), event });
			}
			return pairs.toArray(new Object[][] {});
		}


		@Test(dataProvider = "stateEventPairs")
		public void testEventSeparation(String stateName, String event) throws UnreachableException {
			checkEventSeparation(factory, utility, stateName, event);
		}
	}

	static public class PureSynthesizablePathTS extends Tester {
		public PureSynthesizablePathTS(SeparationFactory factory) {
			super(factory);
		}

		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getPureSynthesizablePathTS();
		}
	}

	static public class ImpureSynthesizablePathTS extends Tester {
		public ImpureSynthesizablePathTS(SeparationFactory factory) {
			super(factory, "b");
		}

		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getImpureSynthesizablePathTS();
		}
	}

	@Test
	public void testCalculate3() throws UnreachableException {
		RegionUtility utility = new RegionUtility(TestTSCollection.getOneCycleLTS());

		checkEventSeparation(factory, utility, "s1", "c");
	}

	@Test
	public void testCalculatePlainTNnetReachabilityTS() throws UnreachableException {
		RegionUtility utility = new RegionUtility(TestTSCollection.getPlainTNetReachabilityTS());

		checkEventSeparation(factory, utility, "s2", "a");
		checkEventSeparation(factory, utility, "s5", "a");
		checkEventSeparation(factory, utility, "s3", "b");
		checkEventSeparation(factory, utility, "s5", "b");
		checkEventSeparation(factory, utility, "s0", "c");
		checkEventSeparation(factory, utility, "s3", "c");
	}

	static public class CrashkursCC2Tests {
		final private SeparationFactory factory;
		protected TransitionSystem ts;
		protected RegionUtility utility;

		public CrashkursCC2Tests(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc2-aut.apt");
			utility = new RegionUtility(ts);

			// I'm lazy, let's hope that we always end up with this order (which is currently guaranteed because
			// TransitionSystem.getAlphabet() uses a SortedSet).
			assertThat(utility.getEventIndex("t1"), is(0));
			assertThat(utility.getEventIndex("t2"), is(1));
			assertThat(utility.getEventIndex("t3"), is(2));
		}

		@Test
		public void testNoStateRestriction() throws UnreachableException {
			String[] locationMap = new String[3];
			Region region = factory.createSeparation(utility, locationMap)
				.calculateSeparatingRegion(ts.getNode("s3"), "t2");

			assertThat(region.getMarkingForState(ts.getNode("s0")),
					greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s1")),
					greaterThanOrEqualTo(region.getBackwardWeight(1)));
			assertThat(region.getMarkingForState(ts.getNode("s2")),
					greaterThanOrEqualTo(region.getBackwardWeight(1)));
		}
	}

	static public class TestWordB2AB5AB6AB6 {
		final private SeparationFactory factory;

		protected List<String> word;
		protected TransitionSystem ts;
		protected RegionUtility utility;

		public TestWordB2AB5AB6AB6(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			word = Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b",
					"a", "b", "b", "b", "b", "b", "b");
			ts = makeTS(word);
			utility = new RegionUtility(ts);
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
		public void testEventSeparation(String state, String event) throws UnreachableException {
			String[] locationMap = new String[2];
			Separation separation = factory.createSeparation(utility, locationMap);
			Region r = separation.calculateSeparatingRegion(ts.getNode(state), event);

			assertThat(r, notNullValue());
			assertThat(r.getInitialMarking(), greaterThanOrEqualTo(BigInteger.ZERO));

			for (State s : ts.getNodes())
				for (Arc arc : s.getPostsetEdges()) {
					String label = arc.getLabel();
					BigInteger backwards = r.getBackwardWeight(label);
					assertThat(r.getInitialMarking() + " : " + r + " : " + s.toString() + " : " + label,
							r.getMarkingForState(s), greaterThanOrEqualTo(backwards));
				}
			assertThat(r.getMarkingForState(ts.getNode(state)), is(lessThan(r.getBackwardWeight(event))));
		}
	}

	static public class TestWordCBADCBA {
		final private SeparationFactory factory;

		protected List<String> word;
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected String[] locationMap;

		public TestWordCBADCBA(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			word = Arrays.asList("c", "b", "a", "d", "c", "b", "a");
			ts = makeTS(word);
			utility = new RegionUtility(ts);
			locationMap = new String[4];
			locationMap[0] = "a";
			locationMap[1] = "b";
			locationMap[2] = "c";
			locationMap[3] = "d";
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
		public void testEventSeparation(String state, String event) throws UnreachableException {
			Separation separation = factory.createSeparation(utility, locationMap);
			Region r = separation.calculateSeparatingRegion(ts.getNode(state), event);

			assertThat(r, notNullValue());
			assertThat(r.getInitialMarking(), greaterThanOrEqualTo(BigInteger.ZERO));

			for (State s : ts.getNodes())
				for (Arc arc : s.getPostsetEdges()) {
					String label = arc.getLabel();
					BigInteger backwards = r.getBackwardWeight(label);
					assertThat(r.getInitialMarking() + " : " + r + " : " + s.toString() + " : " + label,
							r.getMarkingForState(s), greaterThanOrEqualTo(backwards));
				}
			assertThat(r.getMarkingForState(ts.getNode(state)), is(lessThan(r.getBackwardWeight(event))));
		}
	}

	static public class TestStateSeparation {
		final private SeparationFactory factory;

		private TransitionSystem ts;
		private RegionUtility utility;
		private String[] locationMap;

		public TestStateSeparation(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			ts = TestTSCollection.getStateSeparationFailureTS();
			utility = new RegionUtility(ts);
			locationMap = new String[2];
		}

		@Test
		public void testStateSeparationSuccess() throws UnreachableException {
			State s0 = ts.getNode("s0"), s1 = ts.getNode("s1");
			Separation separation = factory.createSeparation(utility, locationMap);
			Region r = separation.calculateSeparatingRegion(s0, s1);

			assertThat(r, is(notNullValue()));
			assertThat(r.getMarkingForState(s0), not(equalTo(r.getMarkingForState(s1))));
		}

		@Test
		public void testStateSeparationFailure() throws UnreachableException {
			State s0 = ts.getNode("fail0"), s1 = ts.getNode("fail1");
			Separation separation = factory.createSeparation(utility, locationMap);
			Region r = separation.calculateSeparatingRegion(s0, s1);

			assertThat(r, is(nullValue()));
		}
	}

	@Test
	public void testStateSeparationSatisfiesProperties() {
		// This transition system leads to a region basis with a:1 and b:2. This region is not plain, but the
		// code would add it for state separation anyway! Since this is the only entry in the basis and all
		// regions must be a linear combination of the basis, there are no plain regions at all for this TS.
		TransitionSystem ts = TestTSCollection.getTwoBThreeATS();
		State s = ts.getNode("s");
		State t = ts.getNode("t");
		State u = ts.getNode("u");

		// Jump through some hoops to get a Separation instance, if possible (this really wants to test
		// PlainPureSeparation and something "really old" which now lives in InequalitySystemSeparation, see
		// commit 6c3a08be97a3e2e9499ee3ea5c73029891a54960).
		// TODO: Handle this in a nicer way. Perhaps SeparationTestHelper was a mistake and should be split up
		// into multiple "things"?
		Separation sep = null;
		PNProperties properties = new PNProperties().setPlain(true);
		try {
			sep = factory.createSeparation(new RegionUtility(ts), properties, new String[2]);
		} catch (UnsupportedPNPropertiesException e) {
			properties = properties.setPure(true);
			try {
				sep = factory.createSeparation(new RegionUtility(ts), properties, new String[2]);
			} catch (UnsupportedPNPropertiesException f) {
				// This test cannot be applied to the factory under test
				return;
			}
		}

		assertThat(sep.calculateSeparatingRegion(s, t), nullValue());
		assertThat(sep.calculateSeparatingRegion(s, u), nullValue());
	}

	static public class TestLocationSSP {
		final private SeparationFactory factory;

		private TransitionSystem ts;
		private String[] locationMap;

		public TestLocationSSP(SeparationFactory factory) {
			this.factory = factory;
		}

		@BeforeClass
		public void setup() throws Exception {
			ts = new TransitionSystem();
			State[] states = ts.createStates("s0", "s1");
			ts.setInitialState(states[0]);
			ts.createArc(states[0], states[1], "a");
			ts.createArc(states[0], states[1], "b");

			ts.getEvent("a").putExtension("location", "a");
			ts.getEvent("b").putExtension("location", "b");
			locationMap = new String[] { "a", "b" };
		}

		@Test
		public void testLocations() throws Exception {
			State s0 = ts.getNode("s0");
			State s1 = ts.getNode("s1");
			Separation sep = factory.createSeparation(new RegionUtility(ts), locationMap);

			Region r = sep.calculateSeparatingRegion(s0, s1);
			assertThat(r, not(nullValue()));
			assertThat(r.toString(), SeparationUtility.isSeparatingRegion(r, s0, s1), is(true));

			r = sep.calculateSeparatingRegion(s1, s0);
			assertThat(r, not(nullValue()));
			assertThat(r.toString(), SeparationUtility.isSeparatingRegion(r, s0, s1), is(true));
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
