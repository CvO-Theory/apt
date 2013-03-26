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

//import static org.junit.Assert.*;
//import org.junit.Test;
import java.util.Objects;
import org.testng.annotations.Test;
import uniol.apt.adt.EdgeKey;
import static org.testng.Assert.*;

import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;

/**
 * Test-class for removing elements from a PetriNet.
 * @author Chris
 */
public class RemovePNElementsTest {

	/**
	 * Test for removing a flow from a PN.
	 */
	@Test
	public void removeFlowTest() {
		PetriNet pn = getTestNet();
		pn.removeFlow("t1", "p1");
		try {
			pn.getFlow("t1", "p1");
			fail("Edge not deleted");
		} catch (NoSuchEdgeException e) {
			assertEquals(pn.getEdges().size(), 7);
			return;
		}
		fail("Not correctly removed elements.");
	}

	/**
	 * Test for removing a place from a PN.
	 */
	@Test
	public void removePlaceTest() {
		PetriNet pn = getTestNet();
		pn.removePlace("p1");
		try {
			pn.getPlace("p1");
			fail("Place not deleted");
		} catch (NoSuchNodeException e) {
			assertEquals(pn.getEdges().size(), 6);
			assertEquals(pn.getPlaces().size(), 3);
			return;
		}
		fail("Not correctly removed elements.");
	}

	/**
	 * Test for removing a transition from a PN.
	 */
	@Test
	public void removeTransitionTest() {
		PetriNet pn = getTestNet();
		pn.removeTransition("t1");
		try {
			pn.getTransition("t1");
			fail("Transition not deleted");
		} catch (NoSuchNodeException e) {
			assertEquals(pn.getEdges().size(), 6);
			assertEquals(pn.getTransitions().size(), 3);
			return;
		}
		fail("Not correctly removed elements.");
	}

	/**
	 * Test for readding elements from a PN.
	 * Also useful for checking the equals method.
	 */
	@Test
	public void readdElementsTest() {
		PetriNet pn = getTestNet();

		pn.removePlace("p1");
		pn.removeTransition("t1");
		pn.removeFlow("p3", "t4");

		assertEquals(pn.getPlaces().size(), 3);
		assertEquals(pn.getTransitions().size(), 3);
		assertEquals(pn.getEdges().size(), 4);

		try {
			pn.createPlace("p1");
			pn.createTransition("t1");
			pn.createFlow("p3", "t4");
			pn.createFlow("t1", "p1");
			pn.createFlow("p1", "t2");
			pn.createFlow("p4", "t1");
		} catch (IllegalArgumentException | NodeExistsException | NoSuchNodeException e) {
			fail("Could not add elements.");
		}
		PetriNet net = getTestNet();
		assertEquals(pn.getPlaces().size(), net.getPlaces().size());
		assertEquals(pn.getTransitions().size(), net.getTransitions().size());
		assertEquals(pn.getNodes().size(), net.getNodes().size());
		assertEquals(pn.getEdges().size(), net.getEdges().size());
		for (Place obj : pn.getPlaces()) {
			assertTrue(net.containsPlace(obj.getId()));
		}
		for (Transition obj : pn.getTransitions()) {
			assertTrue(net.containsTransition(obj.getId()));
		}
		for (Node obj : pn.getNodes()) {
			assertTrue(net.containsNode(obj.getId()));
		}
		for (Flow obj : pn.getEdges()) {
			EdgeKey key = new EdgeKey(obj.getSourceId(), obj.getTargetId());
			boolean check = false;
			for (Flow flow : net.getEdges()) {
				EdgeKey key1 = new EdgeKey(flow.getSourceId(), flow.getTargetId());
				if (Objects.equals(key, key1)) {
					check = true;
					break;
				}
			}
			assertTrue(check);
		}
	}

	/**
	 * gives us a small 4 seasons test net.
	 * @return a test net.
	 */
	private PetriNet getTestNet() {
		PetriNet pn = new PetriNet();
		pn.createPlace("p1");
		pn.createPlace("p2");
		pn.createPlace("p3");
		pn.createPlace("p4");
		pn.createTransition("t1");
		pn.createTransition("t2");
		pn.createTransition("t3");
		pn.createTransition("t4");
		pn.createFlow("t1", "p1");
		pn.createFlow("p1", "t2");
		pn.createFlow("t2", "p2");
		pn.createFlow("p2", "t3");
		pn.createFlow("t3", "p3");
		pn.createFlow("p3", "t4");
		pn.createFlow("t4", "p4");
		pn.createFlow("p4", "t1");
		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
