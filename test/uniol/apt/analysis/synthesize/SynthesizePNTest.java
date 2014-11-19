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

import uniol.apt.TestTSCollection;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.ts.State;
import uniol.apt.util.Pair;

import org.hamcrest.collection.IsIterableWithSize;
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
	public void testNonDeterministicTS() {
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
	public void testPathTSPure() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility, false);

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
	public void testPathTSImpure() {
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
	public void testPureSynthesizablePathTS() {
		TransitionSystem ts = TestTSCollection.getPureSynthesizablePathTS();
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = new SynthesizePN(utility);

		assertThat(synth.wasSuccessfullySeparated(), is(true));
		// Can't really be more specific, way too many possibilities
		assertThat(synth.getSeparatingRegions(), IsIterableWithSize.<Region>iterableWithSize(greaterThanOrEqualTo(3)));
		assertThat(synth.getFailedStateSeparationProblems(), empty());
		assertThat(synth.getFailedEventStateSeparationProblems(), empty());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
