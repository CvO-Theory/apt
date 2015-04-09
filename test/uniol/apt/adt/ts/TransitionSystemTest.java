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

import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.Test;
import uniol.apt.adt.SoftMap;
import uniol.apt.adt.exception.ArcExistsException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import uniol.tests.TestUtils;

/**
 *
 * @author Manuel Gieseking
 */
@Test
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

	@Test
	public void testSoftReferences() throws Exception {
		TransitionSystem ts = getTestSystem();

		TestUtils.setField(ts, "presetNodes", new SoftMap<Object, Object>());
		TestUtils.setField(ts, "postsetNodes", new SoftMap<Object, Object>());
		TestUtils.setField(ts, "presetEdges", new SoftMap<Object, Object>());
		TestUtils.setField(ts, "postsetEdges", new SoftMap<Object, Object>());

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
		Set<Arc> ed = ts.getPresetEdges("p2");
		int counter = 0;
		for (Arc arc : ed) {
			arc.setLabel("l" + (++counter));
		}
		// Ver"andere noch ein Label
		ts.getArc(states[2].getPresetNodes().iterator().next(), states[2], "b").setLabel("a");
		return ts;
	}
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

