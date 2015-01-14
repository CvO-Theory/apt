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

import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.TestNetCollection;
import uniol.apt.TestTSCollection;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.util.Pair;

import org.hamcrest.collection.IsIterableWithSize;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uniol.apt.adt.matcher.Matchers.flowThatConnects;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
@SuppressWarnings("unchecked") // I hate generics
public class SynthesizePNTest {
	static private Region mockRegion(RegionUtility utility, int normalMarking,
			List<Integer> backwardWeights, List<Integer> forwardWeights)
	{
		assert backwardWeights.size() == forwardWeights.size();

		Region result = mock(Region.class);
		when(result.getRegionUtility()).thenReturn(utility);
		when(result.getNormalRegionMarking()).thenReturn(normalMarking);

		List<String> eventList = utility.getEventList();
		for (int i = 0; i < backwardWeights.size(); i++) {
			when(result.getBackwardWeight(i)).thenReturn(backwardWeights.get(i));
			when(result.getBackwardWeight(eventList.get(i))).thenReturn(backwardWeights.get(i));
			when(result.getForwardWeight(i)).thenReturn(forwardWeights.get(i));
			when(result.getForwardWeight(eventList.get(i))).thenReturn(forwardWeights.get(i));
		}

		return result;
	}

	@Test
	public void testSynthesizePetriNetEmpty() {
		PetriNet pn = SynthesizePN.synthesizePetriNet(Collections.<Region>emptySet());

		assertThat(pn.getPlaces(), is(empty()));
		assertThat(pn.getTransitions(), is(empty()));
		assertThat(pn.getEdges(), is(empty()));
	}

	@Test
	public void testSynthesizeSimplePetriNet() {
		List<String> eventList = Arrays.asList("a", "b");
		RegionUtility utility = mock(RegionUtility.class);
		when(utility.getEventList()).thenReturn(eventList);

		Set<Region> regions = new HashSet<>();
		regions.add(mockRegion(utility, 1, Arrays.asList(1, 1), Arrays.asList(0, 0)));

		PetriNet pn = SynthesizePN.synthesizePetriNet(regions);

		assertThat(pn.getPlaces(), IsIterableWithSize.<Place>iterableWithSize(1));
		assertThat(pn.getTransitions(), containsInAnyOrder(nodeWithID("a"), nodeWithID("b")));
		assertThat(pn.getEdges(), containsInAnyOrder(
					flowThatConnects(anything(), nodeWithID("a")),
					flowThatConnects(anything(), nodeWithID("b"))));
	}

	@Test
	public void testNonDeterministicTS() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility);

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		// Can't really be more specific, way too many possibilities (TODO: Optimize the code so this becomes
		// testable)
		assertThat(synth.getSeparatingRegions(), not(empty()));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("s1"), nodeWithID("s2"))));
		assertThat(synth.getFailedEventStateSeparationProblems(), empty());
	}

	@Test
	public void testPathTSPure() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility, new PNProperties(PNProperties.PURE));

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), not(empty()));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("t"), nodeWithID("u"))));
		assertThat(synth.getFailedEventStateSeparationProblems(), containsInAnyOrder(
					equalTo(new Pair<String, State>("c", ts.getNode("t"))),
					equalTo(new Pair<String, State>("b", ts.getNode("v"))),
					equalTo(new Pair<String, State>("b", ts.getNode("u"))),
					equalTo(new Pair<String, State>("b", ts.getNode("s")))));
	}

	@Test
	public void testPathTSImpure() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility);

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), not(empty()));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("t"), nodeWithID("u"))));
		assertThat(synth.getFailedEventStateSeparationProblems(), containsInAnyOrder(
					equalTo(new Pair<String, State>("c", ts.getNode("t"))),
					equalTo(new Pair<String, State>("b", ts.getNode("u")))));
	}

	@Test
	public void testPureSynthesizablePathTS() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = TestTSCollection.getPureSynthesizablePathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility);

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), IsIterableWithSize.<Region>iterableWithSize(greaterThanOrEqualTo(3)));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems(), empty());

		TransitionSystem ts2 = new CoverabilityGraph(synth.synthesizePetriNet()).toReachabilityLTS();
		assertThat(new IsomorphismLogic(ts, ts2, true).isIsomorphic(), is(true));
	}

	@Test
	public void testStateSeparationSatisfiesProperties() throws MissingLocationException {
		// This transition system leads to a region basis with a:1 and b:2. This region is not plain, but the
		// code would add it for state separation anyway! Since this is the only entry in the basis and all
		// regions must be a linear combination of the basis, there are no plain regions at all for this TS.
		TransitionSystem ts = TestTSCollection.getTwoBThreeATS();
		State s = ts.getNode("s");
		State t = ts.getNode("t");
		State u = ts.getNode("u");
		State v = ts.getNode("v");
		SynthesizePN synth = new SynthesizePN(ts, new PNProperties(PNProperties.PLAIN));

		assertThat(synth.getSeparatingRegions(), everyItem(plainRegion()));
		assertThat(synth.getFailedEventStateSeparationProblems(), containsInAnyOrder(
					equalTo(new Pair<String, State>("a", v)),
					equalTo(new Pair<String, State>("b", v)),
					equalTo(new Pair<String, State>("b", u))));
		assertThat(synth.getFailedStateSeparationProblems(), containsInAnyOrder(
					containsInAnyOrder(s, t),
					containsInAnyOrder(s, u),
					containsInAnyOrder(s, v),
					containsInAnyOrder(t, u),
					containsInAnyOrder(t, v),
					containsInAnyOrder(u, v)));
	}

	@Test
	public void testWordB2AB5AB6AB6None() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = SynthesizeWordModule.makeTS(Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b",
					"a", "b", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b"));
		SynthesizePN synth = new SynthesizePN(ts, new PNProperties());

		assertThat(synth.wasSuccessfullySeparated(), is(true));

		// Bypass the assertions in synthesizePetriNet() which already check for isomorphism
		PetriNet pn = SynthesizePN.synthesizePetriNet(synth.getSeparatingRegions());
		TransitionSystem ts2 = new CoverabilityGraph(pn).toReachabilityLTS();

		assertThat(new IsomorphismLogic(ts, ts2, true).isIsomorphic(), is(true));
	}

	@Test
	public void testWordB2AB5AB6AB6Pure() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = SynthesizeWordModule.makeTS(Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b",
					"a", "b", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b"));
		SynthesizePN synth = new SynthesizePN(ts, new PNProperties(PNProperties.PURE));

		assertThat(synth.wasSuccessfullySeparated(), is(true));

		// Bypass the assertions in synthesizePetriNet() which already check for isomorphism
		PetriNet pn = SynthesizePN.synthesizePetriNet(synth.getSeparatingRegions());
		TransitionSystem ts2 = new CoverabilityGraph(pn).toReachabilityLTS();

		assertThat(new IsomorphismLogic(ts, ts2, true).isIsomorphic(), is(true));
	}

	@Test
	static public class MinimizeRegions {
		private TransitionSystem ts;
		private RegionUtility utility;

		@BeforeClass
		public void setup() {
			ts = SynthesizeWordModule.makeTS(Arrays.asList("a", "b"));
			// Add an unreachable state, just because we can
			ts.createState();
			utility = new RegionUtility(ts);
		}

		@Test
		public void testEmpty() {
			Set<Region> regions = new HashSet<>();

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, empty());
		}

		@Test
		public void testSingleRegion() {
			Region region = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0));
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, containsInAnyOrder(region));
		}

		@Test
		public void testUselessRegion() {
			Region region = new Region(utility, Arrays.asList(1, 1), Arrays.asList(1, 1)).withInitialMarking(1);
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, empty());
		}

		@Test
		public void testNoUselessRegion() {
			Region region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0)).withInitialMarking(1);
			Region region2 = Region.createPureRegionFromVector(utility, Arrays.asList(0, -1)).withInitialMarking(1);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testUselessRegionForSSP() {
			Region region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0)).withInitialMarking(1);
			Region region2 = Region.createPureRegionFromVector(utility, Arrays.asList(0, -1)).withInitialMarking(1);
			Region region3 = Region.createPureRegionFromVector(utility, Arrays.asList(1, 1));
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2, region3));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testDuplicateRegion() {
			Region region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0)).withInitialMarking(1);
			Region region2 = Region.createPureRegionFromVector(utility, Arrays.asList(-2, 0)).withInitialMarking(2);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, anyOf(contains(region1), contains(region2)));
		}

		@Test
		public void testLessUsefulRegion() {
			// There are three SSP instances and two ESSP instances. This region solves all of them except
			// for one ESSP instance (disabling a after the first a).
			Region region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, -1)).withInitialMarking(2);
			// This region solves only two SSP and one ESSP instance (less than the above and the above
			// solves all these problems, too)
			Region region2 = Region.createPureRegionFromVector(utility, Arrays.asList(0, -1)).withInitialMarking(1);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(utility, regions);
			assertThat(regions, contains(region1));
		}
	}

	@Test
	static public class DistributedImplementation {
		private TransitionSystem ts;
		private RegionUtility utility;

		@BeforeClass
		private void setup() {
			ts = TestTSCollection.getPersistentTS();
			ts.getArc("s0", "l", "a").putExtension("location", "a");
			ts.getArc("s0", "r", "b").putExtension("location", "b");
			ts.getArc("l", "s1", "b").putExtension("location", "b");
			ts.getArc("r", "s1", "a").putExtension("location", "a");

			utility = new RegionUtility(ts);
		}

		private PetriNet setupPN(PetriNet pn) {
			pn.getTransition("t1").setLabel("a");
			pn.getTransition("t2").setLabel("b");
			return pn;
		}

		@Test
		public void testConcurrentDiamond() {
			PetriNet pn = setupPN(TestNetCollection.getConcurrentDiamondNet());

			assertThat(SynthesizePN.isDistributedImplementation(utility, pn), is(true));
		}

		@Test
		public void testConcurrentDiamondWithCommonPostset() {
			PetriNet pn = setupPN(TestNetCollection.getConcurrentDiamondNet());

			// Create a new place which has both transitions in its post-set. This tests that the
			// implementation really checks the places' pre-set and ignores their post-sets.
			pn.createPlace("post");
			pn.createFlow("t1", "post");
			pn.createFlow("t2", "post");

			assertThat(SynthesizePN.isDistributedImplementation(utility, pn), is(true));
		}

		@Test
		public void testConflictingDiamond() {
			PetriNet pn = setupPN(TestNetCollection.getConflictingDiamondNet());

			assertThat(SynthesizePN.isDistributedImplementation(utility, pn), is(false));
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
