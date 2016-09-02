/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

package uniol.apt.io.parser.impl;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.io.parser.ParseException;

/**
 * @author vsp
 */
public class SynetPNParserTest {
	@Test
	public void testPN() throws Exception {
		PetriNet pn = new SynetPNParser().parseFile("nets/synet-nets/synet-docu-example.net");
		assertEquals(pn.getTransitions().size(), 5);
		assertEquals(pn.getPlaces().size(), 6);

		assertEquals(pn.getTransition("t").getExtension("location"), "A");
		assertEquals(pn.getPlace("x_0").getExtension("location"), "A");

		Marking mark = pn.getInitialMarking();
		assertEquals(mark.getToken("x_5").getValue(), 1);
		assertEquals(mark.getToken("x_2").getValue(), 1);
		assertEquals(mark.getToken("x_3").getValue(), 0);

		Place x0 = pn.getPlace("x_0");
		assertEquals(x0.getPostset().size(), 2);
		assertTrue(x0.getPostset().contains(pn.getTransition("t")));
		assertTrue(x0.getPostset().contains(pn.getTransition("d")));

		assertEquals(x0.getPreset().size(), 1);
		assertTrue(x0.getPreset().contains(pn.getTransition("a")));
	}

	private void sideConditionAsserts(PetriNet net) {
		assertEquals(1, net.getPlaces().size());
		assertEquals(1, net.getTransitions().size());
		assertEquals(2, net.getEdges().size());
		Flow f = net.getFlow("p1", "t1");
		assertEquals(f.getWeight(), 1);
		f = net.getFlow("t1", "p1");
		assertEquals(f.getWeight(), 1);
		assertEquals(net.getPlace("p1").getInitialToken().getValue(), 1);
	}

	@Test
	public void testSideCondition() throws Exception {
		PetriNet pn = new SynetPNParser().parseString(
				"transition t1 place p1 := 1 flow p1 <- 1 -- t1 flow p1 ---> t1");
		sideConditionAsserts(pn);
	}

	@Test
	public void testSideConditionWithLocation() throws Exception {
		PetriNet pn = new SynetPNParser().parseString(
				"transition t1 :: l place p1 := 1 :: l flow p1 <- 1 -- t1 flow p1 ---> t1 location l");
		sideConditionAsserts(pn);
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 1 pos 0 Missing Location$")
	public void testSideConditionWithPartialLocation1() throws Exception {
		PetriNet pn = new SynetPNParser().parseString(
				"transition t1 place p1 := 1 :: l flow p1 <- 1 -- t1 flow p1 ---> t1 location l");
		sideConditionAsserts(pn);
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 1 pos 19 Missing Location$")
	public void testSideConditionWithPartialLocation2() throws Exception {
		PetriNet pn = new SynetPNParser().parseString(
				"transition t1 :: l place p1 := 1 flow p1 <- 1 -- t1 flow p1 ---> t1 location l");
		sideConditionAsserts(pn);
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 1 pos 17 Unknown Location 'l'$")
	public void testSideConditionWithUndeclaredLocation() throws Exception {
		PetriNet pn = new SynetPNParser().parseString(
				"transition t1 :: l place p1 := 1 :: l flow p1 <- 1 -- t1 flow p1 ---> t1");
		sideConditionAsserts(pn);
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Location 'A' already exists$")
	public void testDoubleLocation() throws Exception {
		new SynetPNParser().parseString("location A\nlocation A");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
