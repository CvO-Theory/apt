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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.math.BigInteger.ZERO;

import uniol.apt.TestNetCollection;
import uniol.apt.TestTSCollection;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.util.Pair;

import org.hamcrest.Matcher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uniol.apt.adt.matcher.Matchers.flowThatConnects;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;
import static uniol.apt.util.matcher.Matchers.pairWith;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
public class SynthesizePNTest {
	static private Region mockRegion(RegionUtility utility, int initialMarking,
			List<Integer> backwardWeights, List<Integer> forwardWeights) {
		assert backwardWeights.size() == forwardWeights.size();

		Region result = mock(Region.class);
		when(result.getRegionUtility()).thenReturn(utility);
		when(result.getInitialMarking()).thenReturn(BigInteger.valueOf(initialMarking));

		List<String> eventList = utility.getEventList();
		for (int i = 0; i < backwardWeights.size(); i++) {
			BigInteger back = BigInteger.valueOf(backwardWeights.get(i));
			BigInteger forw = BigInteger.valueOf(forwardWeights.get(i));
			when(result.getBackwardWeight(i)).thenReturn(back);
			when(result.getBackwardWeight(eventList.get(i))).thenReturn(back);
			when(result.getForwardWeight(i)).thenReturn(forw);
			when(result.getForwardWeight(eventList.get(i))).thenReturn(forw);
		}

		return result;
	}

	static private List<BigInteger> asBigIntegerList(int... list) {
		List<BigInteger> result = new ArrayList<>(list.length);
		for (int i = 0; i < list.length; i++)
			result.add(BigInteger.valueOf(list[i]));
		return result;
	}

	@Test
	public void testSynthesizePetriNetEmpty() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);
		PetriNet pn = SynthesizePN.synthesizePetriNet(utility, Collections.<Region>emptySet());

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

		PetriNet pn = SynthesizePN.synthesizePetriNet(utility, regions);

		assertThat(pn.getPlaces(), hasSize(1));
		assertThat(pn.getTransitions(), containsInAnyOrder(nodeWithID("a"), nodeWithID("b")));
		assertThat(pn.getEdges(), containsInAnyOrder(
					flowThatConnects(anything(), nodeWithID("a")),
					flowThatConnects(anything(), nodeWithID("b"))));
	}

	@Test
	public void testSingleStateTSWithLoop() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getSingleStateTSWithLoop();
		RegionUtility utility = new RegionUtility(ts);
		PetriNet pn = SynthesizePN.synthesizePetriNet(utility, Collections.<Region>emptySet());

		assertThat(pn.getPlaces(), is(empty()));
		assertThat(pn.getTransitions(), contains(nodeWithID("a")));
		assertThat(pn.getEdges(), is(empty()));
	}

	@Test
	public void testNonDeterministicTS() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).build();

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		assertThat(synth.getSeparatingRegions(), contains(
					allOf(regionWithInitialMarking(1), pureRegionWithWeight("a", -1))));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("s1"), nodeWithID("s2"))));
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	@Test
	public void testNonDeterministicTSNoSSP() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		SynthesizePN synth = new SynthesizePN(new RegionUtility(ts), new PNProperties(), true, null, false,
				new HashSet<Region>());

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		assertThat(synth.getSeparatingRegions(), contains(
					allOf(regionWithInitialMarking(1), pureRegionWithWeight("a", -1))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	@Test
	public void testACBCCLoopTSOutputNonbranching() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getACBCCLoopTS();
		PNProperties properties = new PNProperties().setOutputNonbranching(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// We know that there is a solution with three regions. Test that this really found an ON feasible set.
		assertThat(synth.getSeparatingRegions(), containsInAnyOrder(
				allOf(pureRegionWithWeightThat("b", greaterThanOrEqualTo(ZERO)),
						pureRegionWithWeightThat("c", greaterThanOrEqualTo(ZERO))),
				allOf(pureRegionWithWeightThat("a", greaterThanOrEqualTo(ZERO)),
						pureRegionWithWeightThat("c", greaterThanOrEqualTo(ZERO))),
				allOf(pureRegionWithWeightThat("a", greaterThanOrEqualTo(ZERO)),
						pureRegionWithWeightThat("b", greaterThanOrEqualTo(ZERO)))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	@Test
	public void testACBCCLoopTSTNet() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getACBCCLoopTS();
		PNProperties properties = new PNProperties().setTNet(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// We know that there is a solution with four regions.
		// Test that this really found a T-Net feasible set.
		assertThat(synth.getSeparatingRegions(), containsInAnyOrder(
				allOf(pureRegionWithWeightThat("a", equalTo(ZERO)),
						pureRegionWithWeightThat("b", greaterThan(ZERO)),
						pureRegionWithWeightThat("c", lessThan(ZERO))),
				allOf(pureRegionWithWeightThat("a", equalTo(ZERO)),
						pureRegionWithWeightThat("b", lessThan(ZERO)),
						pureRegionWithWeightThat("c", greaterThan(ZERO))),
				allOf(pureRegionWithWeightThat("b", equalTo(ZERO)),
						pureRegionWithWeightThat("a", greaterThan(ZERO)),
						pureRegionWithWeightThat("c", lessThan(ZERO))),
				allOf(pureRegionWithWeightThat("b", equalTo(ZERO)),
						pureRegionWithWeightThat("a", lessThan(ZERO)),
						pureRegionWithWeightThat("c", greaterThan(ZERO)))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	@Test
	public void testACBCCLoopTSMarkedGraph() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getACBCCLoopTS();
		PNProperties properties = new PNProperties().setMarkedGraph(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// We know that there is a solution with four regions.
		// Test that this really found a marked graph feasible set.
		assertThat(synth.getSeparatingRegions(), containsInAnyOrder(
				allOf(pureRegionWithWeightThat("a", equalTo(ZERO)),
						pureRegionWithWeightThat("b", greaterThan(ZERO)),
						pureRegionWithWeightThat("c", lessThan(ZERO))),
				allOf(pureRegionWithWeightThat("a", equalTo(ZERO)),
						pureRegionWithWeightThat("b", lessThan(ZERO)),
						pureRegionWithWeightThat("c", greaterThan(ZERO))),
				allOf(pureRegionWithWeightThat("b", equalTo(ZERO)),
						pureRegionWithWeightThat("a", greaterThan(ZERO)),
						pureRegionWithWeightThat("c", lessThan(ZERO))),
				allOf(pureRegionWithWeightThat("b", equalTo(ZERO)),
						pureRegionWithWeightThat("a", lessThan(ZERO)),
						pureRegionWithWeightThat("c", greaterThan(ZERO)))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	@Test
	public void testPathTSPure() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		PNProperties properties = new PNProperties().setPure(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), not(empty()));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("t"), nodeWithID("u"))));
		assertThat(synth.getFailedEventStateSeparationProblems().toString(),
				synth.getFailedEventStateSeparationProblems().size(), is(2));
		assertThat(synth.getFailedEventStateSeparationProblems(), allOf(
				hasEntry(is("b"), containsInAnyOrder(
						ts.getNode("v"), ts.getNode("u"), ts.getNode("s"))),
				hasEntry(is("c"), contains(ts.getNode("t")))));
	}

	@Test
	public void testPathTSImpure() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).build();

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), not(empty()));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("t"), nodeWithID("u"))));
		assertThat(synth.getFailedEventStateSeparationProblems().toString(),
				synth.getFailedEventStateSeparationProblems().size(), is(2));
		assertThat(synth.getFailedEventStateSeparationProblems(), allOf(
					hasEntry(is("b"), contains(ts.getNode("u"))),
					hasEntry(is("c"), contains(ts.getNode("t")))));
	}

	@Test
	public void testPureSynthesizablePathTS() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = TestTSCollection.getPureSynthesizablePathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(utility).build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), hasSize(greaterThanOrEqualTo(3)));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());

		TransitionSystem ts2 = CoverabilityGraph.get(synth.synthesizePetriNet()).toReachabilityLTS();
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

		PNProperties properties = new PNProperties().setPlain(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.getSeparatingRegions(), everyItem(plainRegion()));
		assertThat(synth.getFailedEventStateSeparationProblems().toString(),
				synth.getFailedEventStateSeparationProblems().size(), is(2));
		assertThat(synth.getFailedEventStateSeparationProblems(), allOf(
					hasEntry(is("a"), contains(v)),
					hasEntry(is("b"), containsInAnyOrder(u, v))));
		assertThat(synth.getFailedStateSeparationProblems(), contains(containsInAnyOrder(s, t, u, v)));
	}

	@Test
	public void testWordB2AB5AB6AB6None() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = makeTS(Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b",
					"a", "b", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b"));
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));

		// Bypass the assertions in synthesizePetriNet() which already check for isomorphism
		PetriNet pn = SynthesizePN.synthesizePetriNet(synth.getUtility(), synth.getSeparatingRegions());
		TransitionSystem ts2 = CoverabilityGraph.get(pn).toReachabilityLTS();

		assertThat(new IsomorphismLogic(ts, ts2, true).isIsomorphic(), is(true));
	}

	@Test
	public void testWordB2AB5AB6AB6Pure() throws MissingLocationException, UnboundedException {
		TransitionSystem ts = makeTS(Arrays.asList("b", "b", "a", "b", "b", "b", "b", "b",
					"a", "b", "b", "b", "b", "b", "b", "a", "b", "b", "b", "b", "b", "b"));
		PNProperties properties = new PNProperties().setPure(true);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).setProperties(properties)
				.build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));

		// Bypass the assertions in synthesizePetriNet() which already check for isomorphism
		PetriNet pn = SynthesizePN.synthesizePetriNet(synth.getUtility(), synth.getSeparatingRegions());
		TransitionSystem ts2 = CoverabilityGraph.get(pn).toReachabilityLTS();

		assertThat(new IsomorphismLogic(ts, ts2, true).isIsomorphic(), is(true));
	}

	@Test
	public void testABandB() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getABandB();
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).build();

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		assertThat(synth.getSeparatingRegions(), contains(allOf(regionWithInitialMarking(1),
				pureRegionWithWeight("b", -1), impureRegionWithWeight("a", 1, 1))));
		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("s"), nodeWithID("t"))));
		assertThat(synth.getFailedEventStateSeparationProblems().toString(),
				synth.getFailedEventStateSeparationProblems().size(), is(1));
		assertThat(synth.getFailedEventStateSeparationProblems(),
				hasEntry(equalTo("a"), contains(nodeWithID("t"))));
	}

	@Test
	public void testABandBNoSSP() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getABandB();
		SynthesizePN synth = new SynthesizePN(new RegionUtility(ts), new PNProperties(), true, null, false,
				new HashSet<Region>());

		assertThat(synth.wasSuccessfullySeparated(), is(false));
		assertThat(synth.getSeparatingRegions(), contains(allOf(regionWithInitialMarking(1),
				pureRegionWithWeight("b", -1), impureRegionWithWeight("a", 1, 1))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().toString(),
				synth.getFailedEventStateSeparationProblems().size(), is(1));
		assertThat(synth.getFailedEventStateSeparationProblems(),
				hasEntry(equalTo("a"), contains(nodeWithID("t"))));
	}

	@Test
	public void testABandBUnfolded() throws MissingLocationException {
		TransitionSystem ts = TestTSCollection.getABandBUnfolded();
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(ts).build();

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		assertThat(synth.getSeparatingRegions(), containsInAnyOrder(
					allOf(regionWithInitialMarking(1),
							pureRegionWithWeight("b", -1),
							impureRegionWithWeight("a", 1, 1)),
					allOf(regionWithInitialMarking(1),
							pureRegionWithWeight("b", 0),
							pureRegionWithWeight("a", -1))));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}

	static public class MinimizeRegions {
		private TransitionSystem ts;
		private RegionUtility utility;

		@BeforeClass
		public void setup() {
			ts = makeTS(Arrays.asList("a", "b"));
			// Add an unreachable state, just because we can
			ts.createState();
			utility = new RegionUtility(ts);
		}

		@Test
		public void testEmpty() {
			Set<Region> regions = new HashSet<>();

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, empty());
		}

		@Test
		public void testEmptyESSP() {
			Set<Region> regions = new HashSet<>();

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, empty());
		}

		@Test
		public void testSingleRegion() {
			Region region = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withNormalRegionInitialMarking();
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, containsInAnyOrder(region));
		}

		@Test
		public void testSingleRegionESSP() {
			Region region = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withNormalRegionInitialMarking();
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, containsInAnyOrder(region));
		}

		@Test
		public void testUselessRegion() {
			Region region = new Region.Builder(utility, asBigIntegerList(1, 1), asBigIntegerList(1, 1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, empty());
		}

		@Test
		public void testUselessRegionESSP() {
			Region region = new Region.Builder(utility, asBigIntegerList(1, 1), asBigIntegerList(1, 1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, empty());
		}

		@Test
		public void testNoUselessRegion() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testNoUselessRegionESSP() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testUselessRegionForSSP() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Region region3 = Region.Builder.createPure(utility, asBigIntegerList(1, 1))
					.withNormalRegionInitialMarking();
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2, region3));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testUselessRegionForSSPESSP() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Region region3 = Region.Builder.createPure(utility, asBigIntegerList(1, 1))
					.withNormalRegionInitialMarking();
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2, region3));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, containsInAnyOrder(region1, region2));
		}

		@Test
		public void testDuplicateRegion() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(-2, 0))
					.withInitialMarking(BigInteger.valueOf(2));
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, anyOf(contains(region1), contains(region2)));
		}

		@Test
		public void testDuplicateRegionESSP() {
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, 0))
					.withInitialMarking(BigInteger.ONE);
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(-2, 0))
					.withInitialMarking(BigInteger.valueOf(2));
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, anyOf(contains(region1), contains(region2)));
		}

		@Test
		public void testLessUsefulRegion() {
			// There are three SSP instances and two ESSP instances. This region solves all of them except
			// for one ESSP instance (disabling a after the first a).
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, -1))
					.withInitialMarking(BigInteger.valueOf(2));
			// This region solves only two SSP and one ESSP instance (less than the above and the above
			// solves all these problems, too)
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, false);
			assertThat(regions, contains(region1));
		}

		@Test
		public void testLessUsefulRegionESSP() {
			// There are three SSP instances and two ESSP instances. This region solves all of them except
			// for one ESSP instance (disabling a after the first a).
			Region region1 = Region.Builder.createPure(utility, asBigIntegerList(-1, -1))
					.withInitialMarking(BigInteger.valueOf(2));
			// This region solves only two SSP and one ESSP instance (less than the above and the above
			// solves all these problems, too)
			Region region2 = Region.Builder.createPure(utility, asBigIntegerList(0, -1))
					.withInitialMarking(BigInteger.ONE);
			Set<Region> regions = new HashSet<>(Arrays.asList(region1, region2));

			SynthesizePN.minimizeRegions(ts, regions, true);
			assertThat(regions, contains(region1));
		}
	}

	static public class DistributedImplementation {
		private TransitionSystem ts;
		private RegionUtility utility;

		@BeforeClass
		private void setup() {
			ts = TestTSCollection.getPersistentTS();
			ts.getEvent("a").putExtension("location", "a");
			ts.getEvent("b").putExtension("location", "b");

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

			assertThat(SynthesizePN.isDistributedImplementation(utility, new PNProperties(), pn), is(true));
		}

		@Test
		public void testConcurrentDiamondWithCommonPostset() {
			PetriNet pn = setupPN(TestNetCollection.getConcurrentDiamondNet());

			// Create a new place which has both transitions in its post-set. This tests that the
			// implementation really checks the places' pre-set and ignores their post-sets.
			pn.createPlace("post");
			pn.createFlow("t1", "post");
			pn.createFlow("t2", "post");

			assertThat(SynthesizePN.isDistributedImplementation(utility, new PNProperties(), pn), is(true));
		}

		@Test
		public void testConflictingDiamond() {
			PetriNet pn = setupPN(TestNetCollection.getConflictingDiamondNet());

			assertThat(SynthesizePN.isDistributedImplementation(utility, new PNProperties(), pn), is(false));
		}
	}

	@DataProvider(name = "TNets")
	private Object[][] createTNets() {
		return new Object[][]{
				{TestNetCollection.getEmptyNet()},
				{TestNetCollection.getNoTransitionOnePlaceNet()},
				{TestNetCollection.getOneTransitionNoPlaceNet()},
				{TestNetCollection.getTokenGeneratorNet()},
				{TestNetCollection.getConcurrentDiamondNet()},
				{TestNetCollection.getDeadTransitionNet()},
				{TestNetCollection.getACBCCLoopNet()}};
	}

	@DataProvider(name = "NonTNets")
	private Object[][] createNonTNets() {
		return new Object[][]{
				{TestNetCollection.getDeadlockNet()},
				{TestNetCollection.getNonPersistentNet()},
				{TestNetCollection.getPersistentBiCFNet()},
				{TestNetCollection.getConflictingDiamondNet()},
				{TestNetCollection.getABCLanguageNet()}};
	}

	@Test(dataProvider = "TNets")
	public void testGoodTNet(PetriNet pn) {
		assertThat(SynthesizePN.isGeneralizedTNet(pn), is(true));
	}

	@Test(dataProvider = "NonTNets")
	public void testBadTNet(PetriNet pn) {
		assertThat(SynthesizePN.isGeneralizedTNet(pn), is(false));
	}

	@DataProvider(name = "MarkedGraphs")
	private Object[][] createMarkedGraphs() {
		return new Object[][]{
				{TestNetCollection.getEmptyNet()},
				{TestNetCollection.getOneTransitionNoPlaceNet()},
				{TestNetCollection.getACBCCLoopNet()}};
	}

	@DataProvider(name = "NonMarkedGraphs")
	private Object[][] createNonMarkedGraphs() {
		return new Object[][]{
				{TestNetCollection.getDeadTransitionNet()},
				{TestNetCollection.getNoTransitionOnePlaceNet()},
				{TestNetCollection.getConcurrentDiamondNet()},
				{TestNetCollection.getTokenGeneratorNet()},
				{TestNetCollection.getDeadlockNet()},
				{TestNetCollection.getNonPersistentNet()},
				{TestNetCollection.getPersistentBiCFNet()},
				{TestNetCollection.getConflictingDiamondNet()},
				{TestNetCollection.getABCLanguageNet()}};
	}

	@Test(dataProvider = "MarkedGraphs")
	public void testGoodMarkedGraph(PetriNet pn) {
		assertThat(SynthesizePN.isGeneralizedMarkedGraph(pn), is(true));
	}

	@Test(dataProvider = "NonMarkedGraphs")
	public void testBadMarkedGraph(PetriNet pn) {
		assertThat(SynthesizePN.isGeneralizedMarkedGraph(pn), is(false));
	}

	@DataProvider(name = "ESSPInstances")
	private Object[][] createESSPInstances() {
		TransitionSystem ts1 = TestTSCollection.getThreeStatesTwoEdgesTS();
		List<Pair<State, String>> instances1 = new ArrayList<>();
		instances1.add(new Pair<>(ts1.getNode("t"), "a"));
		instances1.add(new Pair<>(ts1.getNode("t"), "b"));
		instances1.add(new Pair<>(ts1.getNode("v"), "a"));
		instances1.add(new Pair<>(ts1.getNode("v"), "b"));

		TransitionSystem ts2 = new TransitionSystem();
		ts2.setInitialState(ts2.createState("s0"));
		ts2.createState("s1");
		ts2.createState("s2");
		ts2.createArc("s0", "s1", "a");
		ts2.createArc("s1", "s2", "b");
		List<Pair<State, String>> instances2 = new ArrayList<>();
		instances2.add(new Pair<>(ts2.getNode("s0"), "b"));
		instances2.add(new Pair<>(ts2.getNode("s1"), "a"));
		instances2.add(new Pair<>(ts2.getNode("s2"), "a"));
		instances2.add(new Pair<>(ts2.getNode("s2"), "b"));

		return new Object[][] {
			{ ts1, instances1 },
			{ ts2, instances2 }
		};
	}

	@Test(dataProvider = "ESSPInstances")
	public void testESSPInstances(TransitionSystem ts, List<Pair<State, String>> instances) {
		List<Matcher<? super Pair<State, String>>> matchers = new ArrayList<>();
		for (Pair<State, String> pair : instances)
			matchers.add(pairWith(is(pair.getFirst()), is(pair.getSecond())));

		assertThat(new SynthesizePN.EventStateSeparationProblems(ts), containsInAnyOrder(matchers));
	}

	@Test
	public void testOverflows() throws Exception {
		// Should be more than the number of bits in a long
		int size = 70;
		SynthesizePN synth = SynthesizePN.Builder
				.createForIsomorphicBehaviour(TestTSCollection.getOverflowTS(size)).build();
		assertThat(synth.getFailedEventStateSeparationProblems().size(), equalTo(size - 1));
		assertThat(synth.getFailedStateSeparationProblems(), emptyIterable());
	}

	@Test
	public void testUnreachableSSP() throws Exception {
		// This tests for a very specific bug that caused unsolvable SSP instances to be overlooked for
		// unreachable states.
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("s0", "s1", "s2");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s2", "s2", "a");

		RegionUtility utility = new RegionUtility(ts);
		Region region = new Region.Builder(utility).addWeightOn(0, BigInteger.ONE.negate()).withInitialMarking(BigInteger.ONE);
		SynthesizePN synth = SynthesizePN.Builder.createForIsomorphicBehaviour(utility)
			.addRegion(region).build();

		assertThat(synth.getFailedStateSeparationProblems(),
				contains(containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"), nodeWithID("s2"))));
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
		assertThat(synth.wasSuccessfullySeparated(), is(false));
	}

	@Test
	public void testFactorisable() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.setInitialState(ts.createState("s"));
		ts.createArc("s", "s", "a");
		ts.createArc("s", "s", "b");

		SynthesizePN synth = new SynthesizePN(new RegionUtility(ts), new PNProperties(), false, null, true,
				Collections.<Region>emptySet());

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		assertThat(synth.getSeparatingRegions(), empty());
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems().entrySet(), empty());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
