/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.adt.matcher;

import java.util.Map;

import org.hamcrest.Matcher;
import org.hamcrest.FeatureMatcher;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.subgraph.SubEdge;
import uniol.apt.adt.subgraph.SubNode;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.INode;

import uniol.apt.adt.pn.Node;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Matchers to verify that adt classes match given conditions
 *
 * @author vsp, Uli Schlachter
 */
public class Matchers {

	public static <T> Matcher<Arc> edgeNodesMarkingEq(Marking sourceMark, Marking targetMark) {
		return arcThatConnects(nodeMarkingEq(sourceMark), nodeMarkingEq(targetMark));
	}

	public static <T> Matcher<CoverabilityGraphEdge> edgeCoverNodesMarkingEq(Marking sourceMark,
			Marking targetMark) {
		return edgeCoverNodesThat(coverNodeMarkingEq(sourceMark), coverNodeMarkingEq(targetMark));
	}

	public static <T> Matcher<CoverabilityGraphEdge> edgeCoverNodesThat(Matcher<CoverabilityGraphNode> source,
			Matcher<CoverabilityGraphNode> target) {
		return EdgeCoverNodesThatMatcher.edgeCoverNodesThat(source, target);
	}

	public static <T> Matcher<State> nodeMarkingThat(Matcher<? super Marking> matcher) {
		return NodeMarkingThatMatcher.nodeMarkingThat(matcher);
	}

	public static <T> Matcher<CoverabilityGraphNode> coverNodeMarkingThat(Matcher<Marking> matcher) {
		return CoverNodeMarkingThatMatcher.nodeMarkingThat(matcher);
	}

	public static <T> Matcher<State> nodeMarkingEq(Marking mark) {
		return nodeMarkingThat(is(equalTo(mark)));
	}

	public static <T> Matcher<CoverabilityGraphNode> coverNodeMarkingEq(Marking mark) {
		return coverNodeMarkingThat(is(equalTo(mark)));
	}

	public static <T> Matcher<INode<?, ?, ?>> nodeWithID(Matcher<? super String> matcher) {
		return NodeWithIDMatcher.nodeWithID(matcher);
	}

	public static <T> Matcher<INode<?, ?, ?>> nodeWithID(String id) {
		return nodeWithID(is(equalTo(id)));
	}

	public static Matcher<Arc> arcWithLabel(Matcher<? super String> matcher) {
		return ArcWithLabelMatcher.arcWithLabel(matcher);
	}

	public static Matcher<Arc> arcWithLabel(String id) {
		return arcWithLabel(is(equalTo(id)));
	}

	public static <T> Matcher<PetriNet> netWithInitialMarkingThat(Matcher<? super Marking> matcher) {
		return NetWithInitialMarkingThatMatcher.netWithInitialMarkingThat(matcher);
	}

	public static <T> Matcher<PetriNet> netWithSameStructureAs(PetriNet pn) {
		return NetWithSameStructureAsMatcher.netWithSameStructureAs(pn);
	}

	public static <T> Matcher<Flow> flowThatConnects(String sourceID, String targetID) {
		return flowThatConnects(nodeWithID(sourceID), nodeWithID(targetID));
	}

	public static <T> Matcher<Arc> arcThatConnects(String sourceID, String targetID) {
		return arcThatConnects(nodeWithID(sourceID), nodeWithID(targetID));
	}

	public static <T> Matcher<Arc> arcThatConnectsVia(String sourceID, String targetID, String label) {
		return arcThatConnectsVia(nodeWithID(sourceID), nodeWithID(targetID), is(label));
	}

	public static <T> Matcher<SubEdge<?, ?, ?>> subEdgeThatConnects(String sourceID, String targetID) {
		return subEdgeThatConnects(nodeWithID(sourceID), nodeWithID(targetID));
	}

	public static <T> Matcher<Flow> flowThatConnects(Matcher<? super Node> sourceMatcher,
			Matcher<? super Node> targetMatcher) {
		return FlowThatConnectsMatcher.flowThatConnects(sourceMatcher, targetMatcher);
	}

	public static <T> Matcher<Arc> arcThatConnects(Matcher<? super State> sourceMatcher,
			Matcher<? super State> targetMatcher) {
		return ArcThatConnectsMatcher.arcThatConnects(sourceMatcher, targetMatcher);
	}

	public static <T> Matcher<Arc> arcThatConnectsVia(Matcher<? super State> sourceMatcher,
			Matcher<? super State> targetMatcher, Matcher<? super String> labelMatcher) {
		return ArcThatConnectsMatcher.arcThatConnectsVia(sourceMatcher, targetMatcher, labelMatcher);
	}

	public static <T> Matcher<SubEdge<?, ?, ?>> subEdgeThatConnects(Matcher<? super SubNode<?, ?, ?>> sourceMatcher,
			Matcher<? super SubNode<?, ?, ?>> targetMatcher) {
		return SubEdgeThatConnectsMatcher.subEdgeThatConnects(sourceMatcher, targetMatcher);
	}

	public static <T> Matcher<Marking> markingThatIs(Map<String, Long> marking) {
		return MarkingThatIsMatcher.markingThatIs(marking);
	}

	public static <T> Matcher<Marking> markingThatIs(Marking marking) {
		return MarkingThatIsMatcher.markingThatIs(marking);
	}

	public static Matcher<TransitionSystem> tsWithInitialState(Matcher<? super State> matcher) {
		return new FeatureMatcher<TransitionSystem, State>(matcher, "initial state", "initial state") {
			@Override
			protected State featureValueOf(TransitionSystem ts) {
				return ts.getInitialState();
			}
		};
	}

	public static Matcher<TransitionSystem> tsWithInitialState(String id) {
		return tsWithInitialState(nodeWithID(id));
	}

	public static Matcher<TransitionSystem> tsWith(Matcher<Iterable<? extends Arc>> matcher) {
		return new FeatureMatcher<TransitionSystem, Iterable<Arc>>(matcher, "arcs", "arcs") {
			@Override
			protected Iterable<Arc> featureValueOf(TransitionSystem ts) {
				return ts.getEdges();
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
