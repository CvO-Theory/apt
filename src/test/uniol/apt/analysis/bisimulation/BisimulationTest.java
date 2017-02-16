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

package uniol.apt.analysis.bisimulation;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.util.matcher.Matchers.pairWith;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.impl.returns.NonBisimilarPathReturnValueTransformation;
import uniol.apt.util.Pair;

import static uniol.apt.BestNetCollection.*;
import static uniol.apt.TestTSForBisimulation.*;
import static uniol.apt.TestNetCollection.*;

/** @author Raffaela Ferrari */
public class BisimulationTest {
	private String transformErrorPath(NonBisimilarPath path) throws IOException {
		StringWriter out = new StringWriter();
		new NonBisimilarPathReturnValueTransformation().transform(out, path);
		return out.toString();
	}

	private void testBisimulation(PetriNet pn1, PetriNet pn2) throws UnboundedException {
		// The Petri nets should be bisimilar
		assertTrue(new Bisimulation().checkBisimulation(CoverabilityGraph.get(pn1).toReachabilityLTS(),
					CoverabilityGraph.get(pn2).toReachabilityLTS()), "Testing Bisimulation");
	}

	private void testNoBisimulation(PetriNet pn1, PetriNet pn2, String errorPath)
			throws ModuleException, IOException {
		// The Petri nets shouldn't be bisimilar
		Bisimulation bisimulation = new Bisimulation();
		assertFalse(bisimulation.checkBisimulation(CoverabilityGraph.get(pn1).toReachabilityLTS(),
			CoverabilityGraph.get(pn2).toReachabilityLTS()), "Testing no Bisimulation");
		assertEquals(transformErrorPath(bisimulation.getErrorPath()), errorPath);
	}

	private void testNoBisimulation(PetriNet pn1, PetriNet pn2,
		Iterable<Matcher<? super Iterable<? extends Pair<State, State>>>> matchers)
		throws ModuleException {
		// The Petri nets shouldn't be bisimilar
		Bisimulation bisimulation = new Bisimulation();
		assertFalse(bisimulation.checkBisimulation(CoverabilityGraph.get(pn1).toReachabilityLTS(),
			CoverabilityGraph.get(pn2).toReachabilityLTS()), "Testing no Bisimulation");
		assertThat(bisimulation.getErrorPath(), is(anyOf(matchers)));
	}

	private void testBisimulationForLTS(TransitionSystem lts1, TransitionSystem lts2) throws UnboundedException {
		// The LTSs should be bisimilar
		assertTrue(new Bisimulation().checkBisimulation(lts1, lts2), "Testing Bisimulation");
	}

	private void testNoBisimulationForLTS(TransitionSystem lts1, TransitionSystem lts2, String errorPath)
		throws ModuleException, IOException {
		// The LTSs shouldn't be bisimilar
		Bisimulation bisimulation = new Bisimulation();
		assertFalse(bisimulation.checkBisimulation(lts1, lts2), "Testing no Bisimulation");
		assertEquals(transformErrorPath(bisimulation.getErrorPath()), errorPath);
	}

	private void testNoBisimilationForLtsAndPn(TransitionSystem lts1, PetriNet pn2,
		Iterable<Matcher<? super Iterable<? extends Pair<State, State>>>> matchers)
		throws ModuleException {
		// The LTSs shouldn't be bisimilar
		Bisimulation bisimulation = new Bisimulation();
		assertFalse(bisimulation.checkBisimulation(lts1, CoverabilityGraph.get(pn2).toReachabilityLTS()),
				"Testing no Bisimulation");
		assertThat(bisimulation.getErrorPath(), is(anyOf(matchers)));
	}

	/*
	 * Tests for no bisimulation
	 */
	@Test
	public void testNonBisimilarNets() throws ModuleException {
		Collection<Matcher<? super Iterable<? extends Pair<State, State>>>> matchers = new ArrayList<>();
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("s0"), nodeWithID("s0")),
			pairWith(nodeWithID("s1"), nodeWithID("s1")))));
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("s0"), nodeWithID("s0")),
			pairWith(nodeWithID("s2"), nodeWithID("s1")))));
		testNoBisimulation(getNet1A(), getNet1B(), matchers);
	}

	@Test
	public void testNonBisimilarNets0() throws IOException, ModuleException {
		String errorPath = "(p0,q0);(p1,q1)";
		testNoBisimulationForLTS(getTestTS2A(), getTestTS2B(), errorPath);
	}

	@Test
	public void testNonBisimilarNets1() throws IOException, ModuleException {
		String errorPath = "(p0,q0)";
		testNoBisimulationForLTS(getTestTS3A(), getTestTS3C(), errorPath);
	}

	@Test
	public void testNonBisimilarNets2() throws IOException, ModuleException {
		String errorPath = "(p0,q0);(p0,q1)";
		testNoBisimulationForLTS(getTestTS3A(), getTestTS3D(), errorPath);
	}

	@Test
	public void testNonBisimilarNets3() throws IOException, ModuleException {
		String errorPath1 = "(p0,q0);(p1,q1);(p3,q3)";
		String errorPath2 = "(p0,q0);(p1,q2);(p2,q4)";
		Bisimulation bisimulation = new Bisimulation();
		assertFalse(bisimulation.checkBisimulation(getTestTS4A(), getTestTS4C()), "Testing no Bisimulation");
		String error = transformErrorPath(bisimulation.getErrorPath());
		if (!error.equals(errorPath1) && !error.equals(errorPath2)) {
			fail("Did not found the right error path. Expected: " + errorPath1 + " or " + errorPath2
				+ "but found: " + error);
		}
	}

	@Test
	public void testNonBisimilarNets4() throws IOException, ModuleException {
		Collection<Matcher<? super Iterable<? extends Pair<State, State>>>> matchers = new ArrayList<>();
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("s0"), nodeWithID("s0")),
			pairWith(nodeWithID("s2"), nodeWithID("s2")))));
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("s0"), nodeWithID("s0")),
			pairWith(nodeWithID("s1"), nodeWithID("s2")))));
		testNoBisimulation(getNet2A(), getNet4B(), matchers);
	}

	@Test
	public void testNonBisimilarNetAndTS() throws IOException, ModuleException {
		Collection<Matcher<? super Iterable<? extends Pair<State, State>>>> matchers = new ArrayList<>();
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("autoNode0"), nodeWithID("s0")),
			pairWith(nodeWithID("autoNode1"), nodeWithID("s2")))));
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("autoNode0"), nodeWithID("s0")),
			pairWith(nodeWithID("autoNode2"), nodeWithID("s1")))));
		matchers.add(contains(Arrays.<Matcher<? super Pair<State, State>>>asList(
			pairWith(nodeWithID("autoNode0"), nodeWithID("s0")),
			pairWith(nodeWithID("autoNode2"), nodeWithID("s2")))));
		testNoBisimilationForLtsAndPn(getTs4B(), getNet2A(), matchers);
	}

	@Test
	public void testNetWithOnlyOnePlace() throws IOException, ModuleException {
		String errorPath = "(s0,s0)";
		testNoBisimulation(getNet1A(), getNoTransitionOnePlaceNet(), errorPath);
	}

	@Test
	public void testEmptyNet() throws IOException, ModuleException {
		String errorPath = "(s0,s0)";
		testNoBisimulation(getNet1A(), getEmptyNet(), errorPath);
	}

	/*
	 * Tests for bisimulation
	 */
	@Test
	public void testBisimilarNets() throws UnboundedException {
		testBisimulation(getNet2A(), getNet2B());
	}

	@Test
	public void testBisimilarNets1() throws IOException, ModuleException {
		testBisimulationForLTS(getTestTS1A(), getTestTS1B());
	}

	@Test
	public void testBisimilarNets2() throws IOException, ModuleException {
		testBisimulationForLTS(getTestTS3A(), getTestTS3B());
	}

	@Test
	public void testBisimilarNets3() throws IOException, ModuleException {
		testBisimulationForLTS(getTestTS4A(), getTestTS4B());
	}

	@Test
	public void testEmptyNetWithEmptyNet() throws IOException, ModuleException {
		testBisimulation(getEmptyNet(), getEmptyNet());
	}

	@Test
	public void testEmptyNetWithNetWithOnePlace() throws IOException, ModuleException {
		testBisimulation(getNoTransitionOnePlaceNet(), getEmptyNet());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
