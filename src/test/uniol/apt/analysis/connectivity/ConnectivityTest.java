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

package uniol.apt.analysis.connectivity;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matcher;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.module.Module;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleInvoker;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author Uli Schlachter, vsp */
public class ConnectivityTest {

	private CycleGenerator cycleGenerator;
	private BistatePhilNetGenerator biStatePhilNetGenerator;
	private TristatePhilNetGenerator triStatePhilNetGenerator;
	private QuadstatePhilNetGenerator quadStatePhilNetGenerator;

	@BeforeClass
	public void setup() {
		cycleGenerator = new CycleGenerator();
		biStatePhilNetGenerator = new BistatePhilNetGenerator();
		triStatePhilNetGenerator = new TristatePhilNetGenerator();
		quadStatePhilNetGenerator = new QuadstatePhilNetGenerator();
	}

	@AfterClass
	public void teardown() {
		cycleGenerator = null;
		biStatePhilNetGenerator = null;
		triStatePhilNetGenerator = null;
		quadStatePhilNetGenerator = null;
	}

	private boolean callModule(IGraph<?, ?, ?> graph, Module mod) {
		try {
			ModuleInvoker m = new ModuleInvoker();
			Collection<Object> objs = m.invoke(mod, graph);
			Boolean ret = (Boolean) objs.iterator().next();
			return ret;
		} catch (ModuleException ex) {
			throw new RuntimeException(ex);
		}
	}

	private <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>>
			void testGraph(G graph, String[] isolated, String[][] weak, String[][] strong) {
		// First we need to collect our matchers
		Collection<Matcher<? super INode<?, ?, ?>>> isolatedMatchers = new ArrayList<>();
		Collection<Matcher<? super Iterable<? extends INode<?, ?, ?>>>> weakMatchers = new ArrayList<>();
		Collection<Matcher<? super Iterable<? extends INode<?, ?, ?>>>> strongMatchers = new ArrayList<>();

		for (String id : isolated) {
			isolatedMatchers.add(nodeWithID(id));
		}
		for (String[] component : weak) {
			Collection<Matcher<? super INode<?, ?, ?>>> matcher = new ArrayList<>();
			for (String id : component) {
				matcher.add(nodeWithID(id));
			}
			weakMatchers.add(containsInAnyOrder(matcher));
		}
		for (String[] component : strong) {
			Collection<Matcher<? super INode<?, ?, ?>>> matcher = new ArrayList<>();
			for (String id : component) {
				matcher.add(nodeWithID(id));
			}
			strongMatchers.add(containsInAnyOrder(matcher));
		}

		// Now do the actual test
		assertThat(Connectivity.findIsolatedElements(graph), containsInAnyOrder(isolatedMatchers));
		assertThat(Connectivity.getWeaklyConnectedComponents(graph), containsInAnyOrder(weakMatchers));
		assertThat(Connectivity.getStronglyConnectedComponents(graph), containsInAnyOrder(strongMatchers));
		assertEquals(weak.length <= 1, Connectivity.isWeaklyConnected(graph));
		assertEquals(strong.length <= 1, Connectivity.isStronglyConnected(graph));
		assertEquals(weak.length <= 1, callModule(graph, new WeakConnectivityModule()));
		assertEquals(strong.length <= 1, callModule(graph, new StrongConnectivityModule()));
	}

	private <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>>
			void testStronglyConnected(G graph) {
		Collection<String> ids = new ArrayList<>();
		for (INode<?, ?, ?> node : graph.getNodes()) {
			ids.add(node.getId());
		}
		String[] array = ids.toArray(new String[0]);
		String[] isolated = {};
		String[][] components = {array};
		testGraph(graph, isolated, components, components);
	}

	@Test
	public void testIsolatedPlaceTransition() {
		PetriNet result = new PetriNet();
		result.createPlace("p");
		result.createTransition("t");

		String[] isolated = {"p", "t"};
		String[][] weak = {{"p"}, {"t"}};
		String[][] strong = weak;
		testGraph(result, isolated, weak, strong);
	}

	@Test
	public void testTwoComponents() {
		PetriNet result = new PetriNet();
		Place p1 = result.createPlace("p1");
		Place p2 = result.createPlace("p2");
		Transition t1 = result.createTransition("t1");
		Transition t2 = result.createTransition("t2");
		result.createFlow(p1, t1);
		result.createFlow(t2, p2);

		String[] isolated = {};
		String[][] weak = {{"p1", "t1"}, {"p2", "t2"}};
		String[][] strong = {{"p1"}, {"p2"}, {"t1"}, {"t2"}};
		testGraph(result, isolated, weak, strong);
	}

	@Test
	public void testLargerGraph() {
		// Graph is from http://en.wikipedia.org/wiki/File:Scc.png
		TransitionSystem lts = new TransitionSystem();

		State a = lts.createState("a");
		State b = lts.createState("b");
		State c = lts.createState("c");
		State d = lts.createState("d");
		State e = lts.createState("e");
		State f = lts.createState("f");
		State g = lts.createState("g");
		State h = lts.createState("h");

		lts.createArc(a, b, "");
		lts.createArc(b, e, "");
		lts.createArc(b, c, "");
		lts.createArc(c, d, "");
		lts.createArc(c, g, "");
		lts.createArc(d, c, "");
		lts.createArc(d, h, "");
		lts.createArc(e, a, "");
		lts.createArc(e, f, "");
		lts.createArc(f, g, "");
		lts.createArc(g, f, "");
		lts.createArc(h, g, "");
		lts.createArc(h, d, "");

		String[] isolated = {};
		String[][] weak = {{"a", "b", "c", "d", "e", "f", "g", "h"}};
		String[][] strong = {{"a", "b", "e"}, {"c", "d", "h"}, {"f", "g"}};
		testGraph(lts, isolated, weak, strong);
	}

	@Test
	public void testEmptyNet() {
		PetriNet pn = getEmptyNet();

		String[] isolated = {};
		String[][] weak = {};
		String[][] strong = {};
		testGraph(pn, isolated, weak, strong);
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		PetriNet pn = getNoTransitionOnePlaceNet();

		String[] isolated = { "p1" };
		String[][] weak = {{ "p1" }};
		String[][] strong = {{ "p1" }};
		testGraph(pn, isolated, weak, strong);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 10)
	public void testCycle(int size) {
		testStronglyConnected(cycleGenerator.generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 10)
	public void testBistatePhilNet(int size) {
		testStronglyConnected(biStatePhilNetGenerator.generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 10)
	public void testTristatePhilNet(int size) {
		testStronglyConnected(triStatePhilNetGenerator.generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 10)
	public void testQuadstatePhilNet(int size) {
		testStronglyConnected(quadStatePhilNetGenerator.generateNet(size));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
