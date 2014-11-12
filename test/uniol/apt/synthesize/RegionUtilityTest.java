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

package uniol.apt.synthesize;

import org.hamcrest.Matcher;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.matcher.Matchers.*;

/** @author Uli Schlachter */
@Test
public class RegionUtilityTest {
	private Matcher<Iterable<? extends Integer>> parikhVector(int... args) {
		assert args.length % 2 == 0;

		Integer[] expected = new Integer[args.length / 2];
		for (int i = 0; i < args.length; i += 2) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 2)));
			expected[args[i]] = args[i + 1];
		}

		return contains(expected);
	}

	@Test
	public void testSingleStateTS() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		State s0 = ts.getNode("s0");

		RegionUtility utility = new RegionUtility(ts);

		assertThat(utility.getReachingParikhVector(s0), is(empty()));
	}

	@Test
	public void testcc1LTS() {
		TransitionSystem ts = TestTSCollection.getcc1LTS();
		State s0 = ts.getNode("s0");
		State s1 = ts.getNode("s1");
		State s2 = ts.getNode("s2");
		State s3 = ts.getNode("s3");

		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");
		int d = utility.getEventIndex("d");

		assertThat(utility.getReachingParikhVector(s0), is(parikhVector(a, 0, b, 0, c, 0, d, 0)));
		assertThat(utility.getReachingParikhVector(s1), is(parikhVector(a, 1, b, 0, c, 0, d, 0)));
		assertThat(utility.getReachingParikhVector(s2), is(parikhVector(a, 0, b, 1, c, 0, d, 0)));
		if (utility.getSpanningTree().getPredecessor(s3).equals(s1))
			assertThat(utility.getReachingParikhVector(s3), is(parikhVector(a, 1, b, 1, c, 0, d, 0)));
		else
			assertThat(utility.getReachingParikhVector(s3), is(parikhVector(a, 0, b, 1, c, 0, d, 1)));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		TransitionSystem ts = TestTSCollection.getThreeStatesTwoEdgesTS();
		State s = ts.getNode("s");
		State t = ts.getNode("t");
		State v = ts.getNode("v");

		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");

		assertThat(utility.getReachingParikhVector(s), is(parikhVector(a, 0, b, 0)));
		assertThat(utility.getReachingParikhVector(t), is(parikhVector(a, 1, b, 0)));
		assertThat(utility.getReachingParikhVector(v), is(parikhVector(a, 0, b, 1)));
	}

	@Test
	public void testPersistentTS() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		State s0 = ts.getNode("s0");
		State l = ts.getNode("l");
		State r = ts.getNode("r");
		State s1 = ts.getNode("s1");

		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");

		assertThat(utility.getReachingParikhVector(s0), is(parikhVector(a, 0, b, 0)));
		assertThat(utility.getReachingParikhVector(l), is(parikhVector(a, 1, b, 0)));
		assertThat(utility.getReachingParikhVector(r), is(parikhVector(a, 0, b, 1)));
		assertThat(utility.getReachingParikhVector(s1), is(parikhVector(a, 1, b, 1)));
	}

	@Test
	public void testNotTotallyReachableTS() {
		TransitionSystem ts = TestTSCollection.getNotTotallyReachableTS();
		State s0 = ts.getNode("s0");
		State s1 = ts.getNode("s1");
		State fail = ts.getNode("fail");

		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");

		assertThat(utility.getReachingParikhVector(s0), is(parikhVector(a, 0, b, 0)));
		assertThat(utility.getReachingParikhVector(s1), is(parikhVector(a, 1, b, 0)));
		assertThat(utility.getReachingParikhVector(fail), is(empty()));
	}

	@Test
	public void testParikhVectorForNonChords() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State s1 = ts.createState("s1");
		State s2 = ts.createState("s2");
		State s3 = ts.createState("s3");

		ts.setInitialState(s0);

		Arc a0 = ts.createArc(s0, s1, "a");
		Arc a1 = ts.createArc(s1, s2, "b");
		Arc a2 = ts.createArc(s2, s3, "a");
		Arc a3 = ts.createArc(s3, s0, "b");

		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");

		// For non chords, the Parikh vector is zero, because both states have the same path
		assertThat(utility.getParikhVectorForEdge(a0), is(parikhVector(a, 0, b, 0)));
		assertThat(utility.getParikhVectorForEdge(a1), is(parikhVector(a, 0, b, 0)));
		assertThat(utility.getParikhVectorForEdge(a2), is(parikhVector(a, 0, b, 0)));
		// This is the only non-chord in the LTS which visits all arcs
		assertThat(utility.getParikhVectorForEdge(a3), is(parikhVector(a, 2, b, 2)));
	}

	@Test
	public void testParikhVectorForChords() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		State s0 = ts.getNode("s0");
		State l = ts.getNode("l");
		State r = ts.getNode("r");
		State s1 = ts.getNode("s1");

		RegionUtility utility = new RegionUtility(ts);
		Arc chord = utility.getSpanningTree().getChords().iterator().next();

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");

		// All non-chords have vector (0, 0) anyway and the chord one is a+b-a-b, too, thanks to persistency.
		for (Arc arc : ts.getEdges())
			assertThat(utility.getParikhVectorForEdge(arc), is(parikhVector(a, 0, b, 0)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120