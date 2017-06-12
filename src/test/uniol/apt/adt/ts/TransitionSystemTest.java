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

package uniol.apt.adt.ts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static uniol.apt.adt.matcher.Matchers.arcThatConnectsVia;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.adt.exception.ArcExistsException;

/**
 *
 * @author Manuel Gieseking
 */
public class TransitionSystemTest {

	@Test
	public void testAlphabet() {
		TransitionSystem ts = getTestSystem();
		Set<String> alph = ts.getAlphabet();
		Set<String> alph1 = new HashSet<>();
		alph1.add("a");
		alph1.add("c");
		alph1.add("d");
		alph1.add("l1");
		alph1.add("l2");
		assertEquals(alph, alph1);
	}

	@Test
	public void testDeleteElements() {
		TransitionSystem ts = getTestSystem();
		State p0 = ts.getNode("s0");
		assertEquals(p0.getPresetEdges().size(), 1);
		assertEquals(p0.getPresetNodes().size(), 1);
		assertEquals(p0.getPostsetNodes().size(), 1);
		assertEquals(p0.getPostsetEdges().size(), 2);
		State p2 = ts.getNode("p2");
		assertEquals(p2.getPresetEdges().size(), 2);
		assertEquals(p2.getPresetNodes().size(), 1);
		assertEquals(p2.getPostsetEdges().size(), 2);
		assertEquals(p2.getPostsetNodes().size(), 1);
		State p3 = ts.getNode("p3");
		assertEquals(p3.getPresetEdges().size(), 2);
		assertEquals(p3.getPresetNodes().size(), 1);
		assertEquals(p3.getPostsetEdges().size(), 1);
		assertEquals(p3.getPostsetNodes().size(), 1);
	}

	private static TransitionSystem getTestSystem() {
		TransitionSystem ts = new TransitionSystem("testSystem");
		// Erstelle drei Zust"ande
		State[] states = ts.createStates("p1", "p2", "p3");
		// Erstelle einen Zustand (s0) und setze ihn als
		// Startzustand
		ts.setInitialState(ts.createState());
		// Kante erstellen
		Arc a = ts.createArc("p1", "p2", "a");
		// und nachtr"aglich das Label "andern
		a.setLabel("b");
		// Ein paar weitere Kanten
		ts.createArc(states[0], states[1], "a");
		ts.createArc(states[1], states[2], "b");
		ts.createArc("p2", "p3", "c");
		State s0 = ts.getInitialState();
		ts.createArc(states[2], s0, "d");
		ts.createArc(s0, states[1], "a");
		ts.createArc(s0, states[1], "b");
		// Merke das bis hier her erstelle Alphabet in einer Extension
		ts.putExtension("initialAlphabet", new HashSet<>(ts.getAlphabet()));
		// L"osche einen Knoten einschlie"slich der Kanten
		ts.removeState("p1");
		// Durch setzen dieses Labels w"urden 2 Kanten s0 -a-> p1
		// existieren.
		try {
			ts.getArc(s0, states[1], "b").setLabel("a");
			fail("Didn't detect existing arc.");
		} catch (ArcExistsException e) {
			assertEquals(ts.getArc(s0, states[1], "b").getLabel(), "b");
		}
		// Setze die Labels von den Kanten aus dem Vorbereich von p2 um
		// (Um eine ConcurrentModificationException zu vermeiden, wird das Set kopiert)
		Set<Arc> ed = ts.getPresetEdges("p2");
		int counter = 0;
		for (Arc arc : new HashSet<>(ed)) {
			arc.setLabel("l" + (++counter));
		}
		// Ver"andere noch ein Label
		ts.getArc(states[2].getPresetNodes().iterator().next(), states[2], "b").setLabel("a");
		return ts;
	}

	@Test
	public void testGetPostsetNodesByLabel() {
		Set<State> postset;
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState();
		// Test: set is initially empty
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, hasSize(0));

		State s1 = ts.createState();
		State s2 = ts.createState();
		State s3 = ts.createState();
		ts.setInitialState(s0);

		ts.createArc(s0, s1, "a");
		// Test: arc creation updates cache
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, contains(s1));

		Arc arc2 = ts.createArc(s0, s2, "b");
		// Test: arc creation doesn't influence cache for other labels
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, contains(s1));
		postset = ts.getPostsetNodesByLabel(s0, "b");
		assertThat(postset, contains(s2));

		ts.createArc(s0, s3, "b");
		// Test: arc creation updates cache for size > 1
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, contains(s1));
		postset = ts.getPostsetNodesByLabel(s0, "b");
		assertThat(postset, containsInAnyOrder(s2, s3));

		ts.removeArc(arc2);
		// Test: arc removal updates cache
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, contains(s1));
		postset = ts.getPostsetNodesByLabel(s0, "b");
		assertThat(postset, contains(s3));

		// Test: state removal updates cache
		ts.createArc(s2, s3, "c");
		postset = ts.getPostsetNodesByLabel(s2, "c");
		assertThat(postset, contains(s3));
		ts.removeState(s2);
		postset = ts.getPostsetNodesByLabel(s2, "c");
		assertThat(postset, hasSize(0));
	}

	@Test
	public void testGetPostsetNodesByLabelWithModification() {
		Set<State> postset;
		TransitionSystem ts = new TransitionSystem();
		State s0 = ts.createState();
		State s1 = ts.createState();
		State s2 = ts.createState();
		ts.setInitialState(s0);
		Arc arc1 = ts.createArc(s0, s1, "a");
		ts.createArc(s0, s2, "a");

		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, containsInAnyOrder(s1, s2));

		arc1.setLabel("b");
		// Test: arc rename updates cache
		postset = ts.getPostsetNodesByLabel(s0, "a");
		assertThat(postset, contains(s2));
	}

	@Test
	public void testGetPresetNodesByLabel() {
		Set<State> preset;
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState();
		// Test: set is initially empty
		preset = ts.getPresetNodesByLabel(s0, "a");
		assertThat(preset, hasSize(0));

		State s1 = ts.createState();
		State s2 = ts.createState();
		State s3 = ts.createState();
		ts.setInitialState(s0);

		ts.createArc(s0, s1, "a");
		// Test: arc creation updates cache
		preset = ts.getPresetNodesByLabel(s1, "a");
		assertThat(preset, contains(s0));

		Arc arc2 = ts.createArc(s0, s2, "b");
		// Test: arc creation doesn't influence cache for other labels
		preset = ts.getPresetNodesByLabel(s1, "a");
		assertThat(preset, contains(s0));
		preset = ts.getPresetNodesByLabel(s2, "b");
		assertThat(preset, contains(s0));

		ts.createArc(s3, s1, "a");
		// Test: arc creation updates cache for size > 1
		preset = ts.getPresetNodesByLabel(s1, "a");
		assertThat(preset, containsInAnyOrder(s0, s3));

		ts.removeArc(arc2);
		// Test: arc removal updates cache
		preset = ts.getPresetNodesByLabel(s2, "b");
		assertThat(preset, hasSize(0));

		// Test: target state removal updates cache
		ts.createArc(s2, s3, "c");
		preset = ts.getPresetNodesByLabel(s3, "c");
		assertThat(preset, contains(s2));
		ts.removeState(s3);
		preset = ts.getPresetNodesByLabel(s3, "c");
		assertThat(preset, hasSize(0));

		// Test: source state removal updates cache
		ts.createArc(s2, s0, "c");
		preset = ts.getPresetNodesByLabel(s0, "c");
		assertThat(preset, contains(s2));
		ts.removeState(s2);
		preset = ts.getPresetNodesByLabel(s0, "c");
		assertThat(preset, hasSize(0));
	}

	@Test
	public void testGetPresetNodesByLabelWithModification() {
		Set<State> preset;
		TransitionSystem ts = new TransitionSystem();
		State s0 = ts.createState();
		State s1 = ts.createState();
		State s2 = ts.createState();
		ts.setInitialState(s0);
		Arc arc1 = ts.createArc(s0, s2, "a");
		ts.createArc(s1, s2, "a");

		preset = ts.getPresetNodesByLabel(s2, "a");
		assertThat(preset, containsInAnyOrder(s0, s1));

		arc1.setLabel("b");
		// Test: arc rename updates cache
		preset = ts.getPresetNodesByLabel(s2, "a");
		assertThat(preset, contains(s1));
	}

	@Test
	public void testCopyConstructor() {
		TransitionSystem ts1 = new TransitionSystem();
		ts1.createState("state");
		ts1.setInitialState("state");
		ts1.createArc("state", "state", "label");
		ts1.getEvent("label").putExtension("key", "value");

		TransitionSystem ts2 = new TransitionSystem(ts1);
		assertThat(ts2.getNodes(), contains(nodeWithID("state")));
		assertThat(ts2.getEdges(), contains(arcThatConnectsVia("state", "state", "label")));
		assertThat(ts2.getInitialState(), is(nodeWithID("state")));
		assertThat(ts2.getAlphabetEvents(), hasSize(1));
		assertThat(ts2.getEvent("label").getExtension("key"), hasToString("value"));

		for (State node : ts2.getNodes()) {
			assertThat(node.getGraph(), sameInstance(ts2));
		}
		for (Arc arc : ts2.getEdges()) {
			assertThat(arc.getGraph(), sameInstance(ts2));
			assertThat(arc.getSource().getGraph(), sameInstance(ts2));
			assertThat(arc.getTarget().getGraph(), sameInstance(ts2));
		}
	}

	@Test
	public void testPostsetAfterEdgeRemoval() {
		TransitionSystem ts = new TransitionSystem();
		State states[] = ts.createStates("s", "t");
		ts.createArc("s", "t", "a");
		ts.createArc("s", "t", "b");

		assertThat(states[0].getPostsetNodes(), contains(states[1]));
		assertThat(states[1].getPresetNodes(), contains(states[0]));

		ts.removeArc("s", "t", "a");

		assertThat(states[0].getPostsetNodes(), contains(states[1]));
		assertThat(states[1].getPresetNodes(), contains(states[0]));

		ts.removeArc("s", "t", "b");

		assertThat(states[0].getPostsetNodes(), emptyIterable());
		assertThat(states[1].getPostsetNodes(), emptyIterable());
	}

}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

