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

package uniol.apt.analysis.trapsAndSiphons;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matcher;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static uniol.apt.TestNetsForSiphonsAndTraps.*;
import static uniol.apt.BestNetCollection.*;
import static uniol.apt.TestNetCollection.*;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.INode;

import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * Tests for TrapsAndSiphonsLogic.java
 *
 * @author Maike Schwammberger, Uli Schlachter
 */
public class TrapsAndSiphonsTest {

	TrapsAndSiphonsLogic logic1;
	TrapsAndSiphonsLogic logic2;

	@AfterClass
	public void teardown() {
		logic1 = null;
		logic2 = null;
	}

	private Collection<Matcher<? super Iterable<? extends INode<?, ?, ?>>>> getMatchersFor(String[][] ids) {
		Collection<Matcher<? super Iterable<? extends INode<?, ?, ?>>>> matchers = new ArrayList<>();

		for (String[] group : ids) {
			Collection<Matcher<? super INode<?, ?, ?>>> matcher = new ArrayList<>();
			for (String id : group) {
				matcher.add(nodeWithID(id));
			}
			matchers.add(containsInAnyOrder(matcher));
		}
		return matchers;
	}

	private void testTrapSiphons(PetriNet pn, String[][] minimalTraps,
		String[][] minimalSiphons) {
		logic1 = new TrapsAndSiphonsLogic(pn, true, false);
		logic2 = new TrapsAndSiphonsLogic(pn, false, true);

		if (minimalTraps != null) {
			assertThat(logic2.getResult(), containsInAnyOrder(getMatchersFor(minimalTraps)));
		}
		if (minimalSiphons != null) {
			assertThat(logic1.getResult(), containsInAnyOrder(getMatchersFor(minimalSiphons)));
		}
	}

	@Test
	public void testEmptyNet() {
		String[][] minimalTraps = {};
		String[][] minimalSiphons = {};
		testTrapSiphons(getEmptyNet(), minimalTraps, minimalSiphons);
	}

	@Test
	public void testOneTransitionNet() {
		String[][] minimalTraps = {};
		String[][] minimalSiphons = {};
		testTrapSiphons(getOneTransitionNoPlaceNet(), minimalTraps, minimalSiphons);
	}

	@Test
	public void testOnePlaceNet() {
		String[][] minimalTraps = {
			{"p1"}, };
		String[][] minimalSiphons = {
			{"p1"}, };
		testTrapSiphons(getNoTransitionOnePlaceNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a net with multiple transitions.
	 * This net is plain, bounded, reversible and persistent.
	 */
	@Test
	public void testPersFig2Net() {
		String[][] minimalTraps = {
			{"s1", "s2", "s4"},
			{"s2", "s3", "s4"}, };
		String[][] minimalSiphons = {
			{"s1", "s2"},
			{"s2", "s3"},
			{"s3", "s4"}, };
		testTrapSiphons(getNetPersFig2(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a net which is bounded, reversible and persistent.
	 */
	@Test
	public void testPersFig4Net() {
		String[][] minimalTraps = {
			{"s1", "s2"}, };
		String[][] minimalSiphons = {
			{"s1", "s2"}, };
		testTrapSiphons(getNetPersFig4(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a net with a self-loop and no other transitions.
	 */
	@Test
	public void test32Net() {
		String[][] minimalTraps = {
			{"x"}, };
		String[][] minimalSiphons = {
			{"x"}, };
		testTrapSiphons(getNet32(), minimalTraps, minimalSiphons);
	}

	@Test
	public void testSiphonAndTrapNet() {
		String[][] minimalTraps = {
			{"s1"}, };
		String[][] minimalSiphons = {
			{"s2"}, };
		testTrapSiphons(getSiphonAndTrapNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a non-fc-net.
	 */
	@Test
	public void testNonFCTrapSiphonNet() {
		String[][] minimalTraps = {
			{"p0", "p2", "p3", "p5", "p7", "p8"},
			{"p0", "p2", "p3", "p6"},
			{"p0", "p2", "p3", "p7", "p9"},
			{"p1", "p2", "p3", "p4"}, };
		String[][] minimalSiphons = {
			{"p0", "p2", "p3", "p5", "p7", "p8"},
			{"p0", "p2", "p3", "p6"},
			{"p0", "p3", "p7", "p9"},
			{"p1", "p2", "p3", "p4"}
		};
		testTrapSiphons(getNonFCTrapSiphonNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a fc-net.
	 */
	@Test
	public void testFCTrapSiphonNet() {
		String[][] minimalTraps = {
			{"s1", "s2", "s3", "s6", "s7", "s8", },
			{"s1", "s2", "s5", "s6", "s7", "s8", },
			{"s2", "s3", "s4", "s7", "s8", },
			{"s2", "s4", "s5", "s7", "s8", }, };
		String[][] minimalSiphons = {
			{"s2", "s3", "s4", "s7", "s8"},
			{"s5", "s7", "s8", },
			{"s1", "s2", "s3", "s6", "s7", "s8"}, };
		testTrapSiphons(getFCTrapSiphonNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a totally connected net.
	 */
	@Test
	public void testTotallyConnectedNet() {
		String[][] minimalTraps = {
			{"s0"},
			{"s1"},
			{"s2"}, };
		String[][] minimalSiphons = {
			{"s0"},
			{"s1"},
			{"s2"}, };
		testTrapSiphons(getTotallyConnectedNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * Test for a not connected net
	 * (minimal traps and siphons are the same as seen with
	 * totally-connected-net.apt).
	 */
	@Test
	public void testNotConnectedNet() {
		String[][] minimalTraps = {
			{"s0"},
			{"s1"},
			{"s2"}, };
		String[][] minimalSiphons = {
			{"s0"},
			{"s1"},
			{"s2"}, };
		testTrapSiphons(getNotConnectedNet(), minimalTraps, minimalSiphons);
	}

	/**
	 * pn to test, if siphons in pn N are really equal to traps in its' dual net N'.
	 */
	@Test
	public void testDualityNet1() {
		String[][] minimalTraps = {
			{"s2"}, };
		String[][] minimalSiphons = {
			{"s0"}, };
		testTrapSiphons(getDualityTestNet1(), minimalTraps, minimalSiphons);
	}

	/**
	 * Dual net to net in the method above.
	 */
	@Test
	public void testDualityNet2() {
		String[][] minimalTraps = {
			{"s0"}, };
		String[][] minimalSiphons = {
			{"s2"}, };
		testTrapSiphons(getDualityTestNet2(), minimalTraps, minimalSiphons);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
