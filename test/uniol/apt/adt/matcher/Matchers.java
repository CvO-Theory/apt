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

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.INode;

import uniol.apt.adt.pn.Node;
import uniol.apt.analysis.bisimulation.Pair;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;

/**
 * Matchers to verify that adt classes match given conditions
 *
 * @author vsp, Uli Schlachter
 */
public class Matchers extends org.hamcrest.Matchers {

	public static <T> Matcher<Arc> edgeNodesMarkingEq(Marking sourceMark, Marking targetMark) {
		return edgeNodesThat(nodeMarkingEq(sourceMark), nodeMarkingEq(targetMark));
	}

	public static <T> Matcher<CoverabilityGraphEdge> edgeCoverNodesMarkingEq(Marking sourceMark,
			Marking targetMark) {
		return edgeCoverNodesThat(coverNodeMarkingEq(sourceMark), coverNodeMarkingEq(targetMark));
	}

	public static <T> Matcher<Arc> edgeNodesThat(Matcher<State> sourceMatcher, Matcher<State> targetMatcher) {
		return EdgeNodesThatMatcher.edgeNodesThat(sourceMatcher, targetMatcher);
	}

	public static <T> Matcher<CoverabilityGraphEdge> edgeCoverNodesThat(Matcher<CoverabilityGraphNode> source,
			Matcher<CoverabilityGraphNode> target) {
		return EdgeCoverNodesThatMatcher.edgeCoverNodesThat(source, target);
	}

	public static <T> Matcher<State> nodeMarkingThat(Matcher<Marking> matcher) {
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

	public static <T> Matcher<INode<?, ?, ?>> nodeWithID(Matcher<String> matcher) {
		return NodeWithIDMatcher.nodeWithID(matcher);
	}

	public static <T> Matcher<INode<?, ?, ?>> nodeWithID(String id) {
		return nodeWithID(is(equalTo(id)));
	}

	public static <T> Matcher<PetriNet> netWithInitialMarkingThat(Matcher<? super Marking> matcher) {
		return NetWithInitialMarkingThatMatcher.netWithInitialMarkingThat(matcher);
	}

	public static <T> Matcher<PetriNet> netWithSameStructureAs(PetriNet pn) {
		return NetWithSameStructureAsMatcher.netWithSameStructureAs(pn);
	}

	public static <T> Matcher<Flow> arcThatConnects(String sourceID, String targetID) {
		return arcThatConnects(nodeWithID(sourceID), nodeWithID(targetID));
	}

	public static <T> Matcher<Flow> arcThatConnects(Matcher<? super Node> sourceMatcher,
			Matcher<? super Node> targetMatcher) {
		return ArcThatConnectsMatcher.arcThatConnects(sourceMatcher, targetMatcher);
	}

	public static <T> Matcher<Marking> markingThatIs(Map<String, Integer> marking) {
		return MarkingThatIsMatcher.markingThatIs(marking);
	}

	public static <T> Matcher<Marking> markingThatIs(Marking marking) {
		return MarkingThatIsMatcher.markingThatIs(marking);
	}

	public static <T, U> Matcher<? super Pair<? extends T, ? extends U>> pairWith(Matcher<T> t, Matcher<U> u) {
		return PairWithMatcher.<T, U>pairWith(t, u);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
