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

package uniol.apt.adt.pn;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import uniol.apt.CrashCourseNets;
import uniol.apt.adt.EdgeKey;
import uniol.apt.adt.exception.IllegalFlowException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.module.exception.ModuleException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Manuel Gieseking
 */
@SuppressWarnings("deprecation")
public class PetriNetTest {

	@Test
	public void testOrder() {
		PetriNet pn = CrashCourseNets.getCCNet1();
		Set<Place> places = pn.getPlaces();
		int i = 1;
		for (Iterator<Place> it = places.iterator(); it.hasNext(); ++i) {
			Place place = it.next();
			assertEquals("s" + i, place.getId());
		}
		pn.createPlace("s0");
		assertEquals("s0", pn.getPlaces().iterator().next().getId());
		pn.createPlace("s22");
		places = pn.getPlaces();
		i = 0;
		for (Iterator<Place> it = places.iterator(); it.hasNext(); ++i) {
			Place place = it.next();
			if (i != 3) {
				assertEquals("s" + ((i < 3) ? i : i - 1), place.getId());
			} else {
				assertEquals("s22", place.getId());
			}
		}
	}

	@Test
	public void testEnsureConsistency() {
		PetriNet pn = getTestNet();
		Marking m = pn.getInitialMarkingCopy();
		assertTrue(m.getToken("p1").getValue() == 5);
		pn.removeNode("p1");
		try {
			m.getToken("p1");
			fail("not recogniced that p1 is deleted.");
		} catch (NoSuchNodeException e) {
			assertEquals("p1", e.getNodeId());
		}
	}

	@Test
	public void testFire() {
		PetriNet pn = getTestNet();
		Marking m = new Marking(pn, 1, 2, 3, 4);
		Marking mcopy = new Marking(m);
		//[ [p4:4] [p2:3] [p1:2] [p0:1] ]
		assertEquals(m, mcopy);
		// feure t3, "andere dabei aber m nicht
		Marking changed = pn.getTransition("t3").fire(m);
		//[ [p4:4] [p2:2] [p1:2] [p0:2] ]
		assertEquals(m, mcopy);
		assertFalse(m.equals(changed));
		// feure t3 ohne mcopy zu "anderen
		Marking mnew = mcopy.fireTransitions(pn.getTransition("t3"));
		assertEquals(mnew, changed);
		assertEquals(mcopy, new Marking(pn, 1, 2, 3, 4));
		// feure t3 "andere dabei mcopy
		mcopy.fire(pn.getTransition("t3"));
		assertEquals(mcopy, changed);
	}

	@Test
	public void testFireOverflow() {
		int weight = Integer.MAX_VALUE / 2;
		PetriNet pn = new PetriNet();
		Place p = pn.createPlace();
		Transition t = pn.createTransition();
		pn.createFlow(t, p, weight);

		Marking m = pn.getInitialMarkingCopy();
		for (int i = 0; i < 8; i++)
			m = t.fire(m);
		assertEquals(m.getToken(p).getValue(), 8 * (long) weight);
	}

	@Test
	public void testCopying() {
		PetriNet pn = new PetriNet("testCopying");
		Place s0 = pn.createPlace("s0");
		s0.putExtension("extension", "peterPan");
		pn.removePlace(s0);
		assertEquals(0, pn.getPlaces().size());
		pn.createPlace(s0);
		assertEquals(1, pn.getPlaces().size());
		Place s0Copy = pn.getPlace("s0");
		assertEquals("peterPan", s0Copy.getExtension("extension"));
		Transition t = pn.createTransition("t0");
		t.putExtension("extension", "asdfasdf");
		pn.removeTransition(t);
		pn.createTransition(t);
		assertEquals("asdfasdf", pn.getTransition("t0").getExtension("extension"));
		pn.createFlow(s0Copy, t, 10);
		Marking mark = pn.getInitialMarkingCopy().setTokenCount(s0Copy, 42);
		pn.addFinalMarking(pn.getInitialMarkingCopy());
		pn.addFinalMarking(mark);
		PetriNet pnCopy = new PetriNet(pn);
		petriNetEquals(pnCopy, pn);
	}

	@Test
	public void testPrePostset() {
		PetriNet pn = new PetriNet("testPrePostset");
		pn.createPlace("s0");
		pn.createPlace("s1");
		pn.createPlace("s2");
		pn.createPlace("s3");
		pn.createPlace("s4");
		pn.createPlace("s5");
		pn.createTransition("t0");
		pn.createTransition("t1");
		pn.createFlow("s0", "t0");
		assertTrue(pn.getPresetNodes("t0").contains(pn.getNode("s0")));
		assertTrue(pn.getPostsetNodes("s0").contains(pn.getNode("t0")));
		assertTrue(pn.getPresetEdges("t0").contains(pn.getFlow("s0", "t0")));
		pn.createFlow("s1", "t0");
		pn.createFlow("s2", "t0");
		pn.createFlow("s3", "t0");
		pn.createFlow("s4", "t0");
		pn.createFlow("s5", "t0");
		assertEquals(pn.getPresetEdges("t0").size(), 6);
		assertEquals(pn.getPresetNodes("t0").size(), 6);
		assertEquals(pn.getPresetEdges("t0"), pn.getNode("t0").getPresetEdges());
	}

	static void petriNetEquals(PetriNet pn1, PetriNet pn2) {
		assertEquals(pn1.getName(), pn2.getName());
		assertNotEquals(pn1.getInitialMarkingCopy(), pn2.getInitialMarkingCopy());
		Set<Marking> finalMarkings1 = pn1.getFinalMarkings();
		Set<Marking> finalMarkings2 = pn2.getFinalMarkings();
		for (Marking marking1 : finalMarkings1) {
			boolean found = true;
			for (Marking marking2 : finalMarkings2) {
				found = true;
				for (Place place : pn1.getPlaces())
					found &= marking1.getToken(place).equals(marking2.getToken(place.getId()));
				if (found)
					break;
			}
			assertTrue(found, finalMarkings1.toString() + finalMarkings2.toString());
		}
		assertEquals(finalMarkings1.size(), finalMarkings2.size());
		assertEquals(pn1.getPlaces().size(), pn2.getPlaces().size());
		assertEquals(pn1.getTransitions().size(), pn2.getTransitions().size());
		assertEquals(pn1.getNodes().size(), pn2.getNodes().size());
		assertEquals(pn1.getEdges().size(), pn2.getEdges().size());
		for (Place obj : pn1.getPlaces()) {
			assertTrue(pn2.containsPlace(obj.getId()));
			assertEquals(obj.getInitialToken(), pn2.getPlace(obj.getId()).getInitialToken());
		}
		for (Transition obj : pn1.getTransitions()) {
			assertTrue(pn2.containsTransition(obj.getId()));
		}
		for (Node obj : pn1.getNodes()) {
			assertTrue(pn2.containsNode(obj.getId()));
		}
		for (Flow obj : pn1.getEdges()) {
			EdgeKey key = new EdgeKey(obj.getSourceId(), obj.getTargetId());
			boolean check = false;
			for (Flow flow : pn2.getEdges()) {
				EdgeKey key1 = new EdgeKey(flow.getSourceId(), flow.getTargetId());
				if (Objects.equals(key, key1)) {
					check = true;
					break;
				}
			}
			assertTrue(check);
		}
	}

	@Test
	public void testDeleting() throws ModuleException {
		PetriNet pn = getTestNet();
		Node p0 = pn.getNode("p0");
		assertTrue(p0.getPostsetNodes().isEmpty());
		assertTrue(p0.getPostsetEdges().isEmpty());
		assertEquals(p0.getPresetEdges().size(), 1);
		assertEquals(p0.getPresetNodes().size(), 1);
		assertEquals(p0.getPresetNodes().iterator().next(), pn.getNode("t3"));
		assertTrue(pn.getNode("p1").getPostsetNodes().isEmpty());
		assertTrue(pn.getNode("p1").getPresetNodes().isEmpty());
		assertTrue(pn.getNode("p1").getPresetEdges().isEmpty());
		assertTrue(pn.getNode("p1").getPostsetEdges().isEmpty());
		assertTrue(pn.getNode("t1").getPostsetEdges().isEmpty());
		assertTrue(pn.getNode("t2").getPresetEdges().isEmpty());
	}

	private PetriNet getTestNet() {
		PetriNet pn = new PetriNet("Testnet");
		// Erzeuge einige Stellen
		pn.createPlaces("p1", "p2", "p4");
		Place p3 = pn.createPlace();
		// Erzeuge einige Transitionen
		pn.createTransitions("t1", "t2", "t3");
		Transition t4 = pn.createTransition();
		// Erzeuge einige Kanten
		pn.createFlow("t1", "p1", 2);
		Flow f = pn.createFlow("p1", "t2");
		f.setWeight(3);
		pn.createFlow("t2", "p2");
		pn.createFlow("p2", "t3");
		pn.createFlow("t3", p3.getId());
		pn.createFlow(p3, t4);
		pn.createFlow(t4.getId(), "p4");
		pn.createFlow("p4", "t1");
		// L"osche einen Knoten einschlie"lich der Kanten
		pn.removeTransition(t4);
		// L"osche eine Kante
		pn.removeFlow("t1", "p1");
		// Auch dies l"oscht eine Kante
		pn.getFlow("p1", "t2").setWeight(0);
		// Setzt die initiale Marking in lexikalischer Sortierung
		pn.setInitialMarking(new Marking(pn, 1, 2, 3, 4));
		// "Andert die initiale Markierung
		pn.getPlace("p1").setInitialToken(5);
		return pn;
	}

	@Test
	public void testNetCopyFinalMarking() {
		PetriNet pn = new PetriNet("Test net");
		Place p = pn.createPlace();
		Marking final1 = pn.getInitialMarkingCopy();
		final1 = final1.setTokenCount(p, 42);
		pn.addFinalMarking(final1);

		PetriNet pnCopy = new PetriNet(pn);
		Place p2 = pnCopy.getPlace(p.getId());
		Set<Marking> finalMarkings = pnCopy.getFinalMarkings();
		assertThat(finalMarkings, hasSize(1));

		// The bug that we are testing for: final2 is a marking on pn2, but contains Token for places from net
		// pn. Thus, we must explicitly call getToken(Place) to test for this bug.
		Marking final2 = finalMarkings.iterator().next();
		assertThat(final2.getToken(p2).getValue(), equalTo(42l));
	}

	@Test(expectedExceptions = { IllegalFlowException.class })
	public void testArcBetweenPlaces() {
		PetriNet pn = new PetriNet();
		pn.createPlaces("p0", "p1");
		pn.createFlow("p0", "p1");
	}

	@Test(expectedExceptions = { IllegalFlowException.class })
	public void testArcBetweenTransitions() {
		PetriNet pn = new PetriNet();
		pn.createTransitions("t0", "t1");
		pn.createFlow("t0", "t1");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
