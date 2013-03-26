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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.exception.StructureException;

/**
 * Test class for testing removing of TS elements.
 * @author Chris
 *
 */
public class RemoveTSElementsTest {

	/**
	 * Test for removing a state from a TS.
	 */
	@Test
	public void removeStateTest() {
		TransitionSystem ts = getTestTS();
		ts.removeState("s1");
		try {
			ts.getNode("s1");
			fail("State not deleted");
		} catch (NoSuchNodeException e) {
			assertEquals(ts.getNodes().size(), 3);
			assertEquals(ts.getEdges().size(), 2);
			return;
		}
		fail("Not correctly removed elements.");
	}

	/**
	 * Test for removing an arc from a TS.
	 */
	@Test
	public void removeArcsTest() {
		TransitionSystem ts = getTestTS();
		ts.removeArc("s1", "s2", "a");
		try {
			ts.getArc("s1", "s2", "a");
			fail("Arc not deleted");
		} catch (NoSuchEdgeException e) {
			assertEquals(ts.getEdges().size(), 4);
			return;
		}
		fail("Not correctly removed elements.");
	}

	/**
	 * Test for checking if the initial state Gets
	 * reset.
	 */
	@Test
	public void removeInitialStateTest() {
		TransitionSystem ts = getTestTS();
		ts.setInitialState("s1");
		ts.removeArc("s1", "s2", "a");
		ts.removeState("s1");

		try {
			ts.getInitialState();
		} catch (StructureException e) {
			assertEquals("Initial state is not set in graph ''.", e.getMessage());
			return;
		}
		fail("Not found that initial state is deleted.");
	}

	/**
	 * Test for readding elements to a TS.
	 * Also useful for checking the equals method.
	 */
	@Test
	public void readdElementsTest() {
		TransitionSystem ts = getTestTS();
		ts.setInitialState(ts.getNode("s1"));
		ts.removeState("s1");
		ts.removeArc("s2", "s3", "a");

		assertEquals(ts.getNodes().size(), 3);
		assertEquals(ts.getEdges().size(), 1);

		try {
			ts.createState("s1");

			ts.createArc("s1", "s2", "a");
			ts.createArc("s4", "s1", "a");
			ts.createArc("s2", "s3", "a");

			ts.createArc("s1", "s2", "b");

			ts.setInitialState(ts.getNode("s1"));

			TransitionSystem ts2 = getTestTS();
			assertFalse(ts.equals(ts2));

			ts2.setInitialState(ts2.getNode("s1"));
			assertFalse(ts.equals(ts2));

		} catch (IllegalArgumentException | NodeExistsException | ArcExistsException | NoSuchNodeException e) {
			fail("Could not add elements.");
		}
	}

	/**
	 * gives us a small test TS consisting of 4 nodes connected in a cycle.
	 * @return a test net.
	 */
	private TransitionSystem getTestTS() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");
		ts.createState("s4");

		ts.createArc("s1", "s2", "a");
		ts.createArc("s2", "s3", "a");
		ts.createArc("s3", "s4", "a");
		ts.createArc("s4", "s1", "a");

		ts.createArc("s1", "s2", "b");
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
