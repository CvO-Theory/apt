/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.arcThatConnectsVia;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.makeTS;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked")
public class OverapproximatePNTest {

	private State getSingleState(Set<State> in) {
		assertThat(in, hasSize(1));
		return in.iterator().next();
	}

	private List<State> checkContainsABBAA(TransitionSystem ts) {
		State initial = ts.getInitialState();
		State sA = getSingleState(initial.getPostsetNodesByLabel("a"));
		State sAB = getSingleState(sA.getPostsetNodesByLabel("b"));
		State sABB = getSingleState(sAB.getPostsetNodesByLabel("b"));
		State sABBA = getSingleState(sABB.getPostsetNodesByLabel("a"));
		State sABBAA = getSingleState(sABBA.getPostsetNodesByLabel("a"));

		Set<State> unhandled = new HashSet<>(ts.getNodes());
		List<State> result = new ArrayList<>();
		result.addAll(Arrays.asList(initial, sA, sAB, sABB, sABBA, sABBAA));
		unhandled.removeAll(result);
		result.addAll(unhandled);
		return result;
	}

	private void checkABBAANone(PetriNet pn) throws Exception {
		TransitionSystem reach = CoverabilityGraph.get(pn).toReachabilityLTS();
		List<State> states = checkContainsABBAA(reach);

		assertThat(states, hasSize(7));
		assertThat(reach.getEdges(), containsInAnyOrder(
					arcThatConnectsVia(is(states.get(0)), is(states.get(1)), is("a")),
					arcThatConnectsVia(is(states.get(1)), is(states.get(2)), is("b")),
					arcThatConnectsVia(is(states.get(2)), is(states.get(3)), is("b")),
					arcThatConnectsVia(is(states.get(3)), is(states.get(4)), is("a")),
					arcThatConnectsVia(is(states.get(4)), is(states.get(5)), is("a")),
					arcThatConnectsVia(is(states.get(2)), is(states.get(6)), is("a"))));
	}

	private void checkABBAAPure(PetriNet pn) throws Exception {
		TransitionSystem reach = CoverabilityGraph.get(pn).toReachabilityLTS();
		List<State> states = checkContainsABBAA(reach);

		assertThat(states, hasSize(7));
		assertThat(reach.getEdges(), containsInAnyOrder(
					arcThatConnectsVia(is(states.get(0)), is(states.get(1)), is("a")),
					arcThatConnectsVia(is(states.get(1)), is(states.get(2)), is("b")),
					arcThatConnectsVia(is(states.get(2)), is(states.get(3)), is("b")),
					arcThatConnectsVia(is(states.get(3)), is(states.get(4)), is("a")),
					arcThatConnectsVia(is(states.get(4)), is(states.get(5)), is("a")),
					arcThatConnectsVia(is(states.get(2)), is(states.get(6)), is("a")),
					arcThatConnectsVia(is(states.get(6)), is(states.get(4)), is("b"))));
	}

	@Test
	public void testEmptyTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		PNProperties properties = new PNProperties();
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);

		assertThat(pn.getPlaces(), empty());
		assertThat(pn.getTransitions(), empty());
	}

	@Test
	public void testABBAANone() throws Exception {
		TransitionSystem ts = makeTS(Arrays.asList("a", "b", "b", "a", "a"));
		PNProperties properties = new PNProperties();
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);
		checkABBAANone(pn);
	}

	@Test
	public void testABBAAPure() throws Exception {
		TransitionSystem ts = makeTS(Arrays.asList("a", "b", "b", "a", "a"));
		PNProperties properties = new PNProperties().setPure(true);
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);
		checkABBAAPure(pn);
	}

	@Test
	public void testABBAA3Bounded() throws Exception {
		TransitionSystem ts = makeTS(Arrays.asList("a", "b", "b", "a", "a"));
		PNProperties properties = new PNProperties().requireKBounded(3);
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);
		checkABBAANone(pn);
	}

	@Test
	public void testABBAA0Bounded() throws Exception {
		TransitionSystem ts = makeTS(Arrays.asList("a", "b", "b", "a", "a"));
		PNProperties properties = new PNProperties().requireKBounded(0);
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);

		assertThat(pn.getTransitions(), containsInAnyOrder(
					nodeWithID("a"), nodeWithID("b")));
		assertThat(pn.getPlaces(), empty());
	}

	@Test
	public void testMergeSSPFailures() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		State[] states = ts.createStates("0", "1", "2", "3", "4", "5");
		ts.setInitialState("0");
		ts.createArc("0", "1", "a");
		ts.createArc("1", "2", "b");
		ts.createArc("2", "3", "c");
		ts.createArc("0", "4", "b");
		ts.createArc("4", "5", "a");

		Collection<Set<State>> failedSSP = new ArrayList<>();
		failedSSP.add(new HashSet<>(Arrays.asList(states[2], states[5])));

		Map<String, Set<State>> failedESSP = new HashMap<>();
		failedESSP.put("c", Collections.singleton(states[5]));

		TransitionSystem fixed = OverapproximatePN.handleSeparationFailures(ts, failedSSP, failedESSP);

		assertThat(fixed.getInitialState(), is(nodeWithID("0")));
		assertThat(fixed.getNodes(), containsInAnyOrder(
					nodeWithID("0"), nodeWithID("1"), nodeWithID("3"), nodeWithID("4"),
					nodeWithID("s0")));
		assertThat(fixed.getEdges(), containsInAnyOrder(arcThatConnectsVia("0", "1", "a"),
					arcThatConnectsVia("1", "s0", "b"), arcThatConnectsVia("s0", "3", "c"),
					arcThatConnectsVia("0", "4", "b"), arcThatConnectsVia("4", "s0", "a")));
	}

	@Test
	public void testAALoop() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createStates("0", "1");
		ts.setInitialState("0");
		ts.createArc("0", "1", "a");
		ts.createArc("1", "0", "a");

		PNProperties properties = new PNProperties();
		PetriNet pn = OverapproximatePN.overapproximate(ts, properties);

		assertThat(pn.getTransitions(), contains(nodeWithID("a")));
		assertThat(pn.getPlaces(), empty());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
