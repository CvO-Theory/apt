/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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

package uniol.apt.adt.subgraph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;
import static uniol.apt.adt.matcher.Matchers.subEdgeThatConnects;
import static uniol.apt.adt.subgraph.SubGraph.getSubGraphByNodeIDs;
import static uniol.apt.adt.subgraph.SubGraph.getSubGraphByNodes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * @author Uli Schlachter
 */
@SuppressWarnings("unchecked") // I hate generics
public class SubGraphTest {
	@Test
	public void testEmptyGraphByNodes() {
		PetriNet graph = new PetriNet("Some PN");
		SubGraph<PetriNet, Flow, Node> subGraph = getSubGraphByNodes(graph, Collections.<Node>emptySet());

		assertThat(subGraph.getName(), is("Some PN"));
		assertThat(subGraph.getNodes(), empty());
		assertThat(subGraph.getEdges(), empty());
		assertThat(subGraph.getOriginalGraph(), sameInstance(graph));
		assertThat(subGraph.getNodeIDs(), empty());
	}

	@Test
	public void testToString() {
		Collection<State> emptySet = Collections.emptySet();
		TransitionSystem graph = mock(TransitionSystem.class);
		when(graph.toString()).thenReturn("mock");

		assertThat(getSubGraphByNodes(graph, emptySet), hasToString("SubGraph of 'mock' with nodes []"));
	}

	@Test
	public void testEqualsGraph() {
		Collection<State> emptySet = Collections.emptySet();
		TransitionSystem graph = TestTSCollection.getSingleStateTS();
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodes(graph, emptySet);

		assertThat(subGraph, equalTo(subGraph));
		assertThat(subGraph, equalTo(getSubGraphByNodes(graph, emptySet)));
		assertThat(subGraph, not(equalTo(getSubGraphByNodes(graph, graph.getNodes()))));
		assertThat(subGraph, not(equalTo(getSubGraphByNodes(TestTSCollection.getSingleStateTS(), emptySet))));
		assertThat(subGraph.equals("42"), is(false));
	}

	@Test
	public void testEqualsNode() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodeIDs(graph, states);

		SubGraph<TransitionSystem, Arc, State> subGraph2 =
			getSubGraphByNodeIDs(graph, Arrays.asList("s1"));

		SubNode<TransitionSystem, Arc, State> s1 = subGraph.getNode("s1");
		SubNode<TransitionSystem, Arc, State> s2 = subGraph.getNode("s2");
		assertThat(s1, equalTo(s1));
		assertThat(s1, equalTo(subGraph.getNode("s1")));
		assertThat(s1, not(equalTo(s2)));
		assertThat(s1, not(equalTo(subGraph2.getNode("s1"))));
		assertThat(s1.equals("42"), is(false));
	}

	@Test
	public void testEqualsEdge() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodeIDs(graph, states);

		SubGraph<TransitionSystem, Arc, State> subGraph2 =
			getSubGraphByNodeIDs(graph, Arrays.asList("s1", "s2"));

		SubNode<TransitionSystem, Arc, State> s0 = subGraph.getNode("s0");
		SubNode<TransitionSystem, Arc, State> s1 = subGraph.getNode("s1");
		SubEdge<TransitionSystem, Arc, State> e0 = s0.getPostsetEdges().iterator().next();
		SubEdge<TransitionSystem, Arc, State> e1 = s1.getPostsetEdges().iterator().next();
		SubEdge<TransitionSystem, Arc, State> otherE = subGraph2.getNode("s1").getPostsetEdges()
			.iterator().next();
		assertThat(e0, equalTo(e0));
		assertThat(e0, equalTo(s0.getPostsetEdges().iterator().next()));
		assertThat(e0, not(equalTo(e1)));
		assertThat(e0, not(equalTo(otherE)));
		assertThat(e0.equals("42"), is(false));
	}

	@Test
	public void testHashCode() {
		Collection<State> emptySet = Collections.emptySet();
		TransitionSystem graph = TestTSCollection.getSingleStateTS();
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodes(graph, emptySet);

		assertThat(subGraph.hashCode(), equalTo(getSubGraphByNodes(graph, emptySet).hashCode()));
		assertThat(subGraph.hashCode(), not(equalTo(getSubGraphByNodes(
							graph, graph.getNodes()).hashCode())));
		assertThat(subGraph.hashCode(), not(equalTo(getSubGraphByNodes(
							TestTSCollection.getSingleStateTS(), emptySet).hashCode())));
	}

	@Test
	public void testPlainTNetReachabilityTS() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodeIDs(graph, states);

		assertThat(subGraph.getOriginalGraph(), sameInstance(graph));
		assertThat(subGraph.getNodeIDs(), containsInAnyOrder("s0", "s1", "s2", "s3"));
		assertThat(subGraph.getNodes(), containsInAnyOrder(
					nodeWithID("s0"), nodeWithID("s1"),
					nodeWithID("s2"), nodeWithID("s3")));
		assertThat(subGraph.getEdges(), containsInAnyOrder(
					subEdgeThatConnects("s0", "s1"),
					subEdgeThatConnects("s0", "s3"),
					subEdgeThatConnects("s1", "s2")));

		assertThat(subGraph.getPresetEdges("s0"), empty());
		assertThat(subGraph.getPostsetEdges("s0"), containsInAnyOrder(
					subEdgeThatConnects("s0", "s1"),
					subEdgeThatConnects("s0", "s3")));
		assertThat(subGraph.getPresetNodes("s0"), empty());
		assertThat(subGraph.getPostsetNodes("s0"), containsInAnyOrder(
					nodeWithID("s1"), nodeWithID("s3")));

		SubNode<TransitionSystem, Arc, State> s1 = subGraph.getNode("s1");
		assertThat(subGraph.getPresetEdges(s1), contains(subEdgeThatConnects("s0", "s1")));
		assertThat(subGraph.getPostsetEdges(s1), contains(subEdgeThatConnects("s1", "s2")));
		assertThat(subGraph.getPresetNodes(s1), contains(nodeWithID("s0")));
		assertThat(subGraph.getPostsetNodes(s1), contains(nodeWithID("s2")));

		SubNode<TransitionSystem, Arc, State> s2 = subGraph.getNode(graph.getNode("s2"));
		assertThat(s2.getPresetEdges(), contains(subEdgeThatConnects("s1", "s2")));
		assertThat(s2.getPostsetEdges(), empty());
		assertThat(s2.getPresetNodes(), contains(nodeWithID("s1")));
		assertThat(s2.getPostsetNodes(), empty());

		assertThat(subGraph.getPresetEdges("s3"), contains(subEdgeThatConnects("s0", "s3")));
		assertThat(subGraph.getPostsetEdges("s3"), empty());
		assertThat(subGraph.getPresetNodes("s3"), contains(nodeWithID("s0")));
		assertThat(subGraph.getPostsetNodes("s3"), empty());
	}

	@Test(expectedExceptions = NoSuchNodeException.class, expectedExceptionsMessageRegExp =
			"^Node 'foobar' does not exist in graph ''$")
	public void testInvalidNodeConstructor() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "foobar", "s3");
		getSubGraphByNodeIDs(graph, states);
	}

	@Test(expectedExceptions = NoSuchNodeException.class, expectedExceptionsMessageRegExp =
			"^Node 'foobar' does not exist in graph ''$")
	public void testInvalidNodeGetNode1() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		getSubGraphByNodeIDs(graph, states).getNode("foobar");
	}

	@Test(expectedExceptions = NoSuchNodeException.class, expectedExceptionsMessageRegExp =
			"^Node 's0' does not exist in graph ''$")
	public void testInvalidNodeGetNode2() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s1", "s2", "s3");
		getSubGraphByNodeIDs(graph, states).getNode(graph.getInitialState());
	}

	@Test(expectedExceptions = NoSuchNodeException.class, expectedExceptionsMessageRegExp =
			"^Node 's0' does not exist in graph ''$")
	public void testInvalidNodeGetNode3() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph2 = getSubGraphByNodeIDs(graph, Arrays.asList("s0"));
		getSubGraphByNodeIDs(graph, states).getPresetEdges(subGraph2.getNode("s0"));
	}

	@Test
	public void testPlainTNetReachabilityTSSubGraphIDs() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodeIDs(graph, states);

		states = Arrays.asList("s1", "s2");
		SubGraph<TransitionSystem, Arc, State> subGraph2 = getSubGraphByNodeIDs(graph, states);

		assertThat(subGraph.getFlatSubGraphByNodeIDs(states), equalTo(subGraph2));
	}

	@Test
	public void testPlainTNetReachabilityTSSubGraphNodes() {
		TransitionSystem graph = TestTSCollection.getPlainTNetReachabilityTS();
		Collection<String> states = Arrays.asList("s0", "s1", "s2", "s3");
		SubGraph<TransitionSystem, Arc, State> subGraph = getSubGraphByNodeIDs(graph, states);

		states = Arrays.asList("s1", "s2");
		SubGraph<TransitionSystem, Arc, State> subGraph2 = getSubGraphByNodeIDs(graph, states);

		assertThat(subGraph.getFlatSubGraphByNodes(
					Arrays.asList(subGraph.getNode("s1"), subGraph.getNode("s2"))),
				equalTo(subGraph2));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
