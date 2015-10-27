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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Token;
import uniol.apt.io.parser.ParseException;

/**
 * @author vsp
 */
public class AptPNParserTest {
	@Test
	public void testPNTestNet() throws Exception {
		PetriNet net = new AptPNParser().parsePNFile("nets/testPN-net.apt");
		assertEquals(net.getName(), "cc1.net");
		assertEquals(net.getExtension("description"), "asdfpeterpan");
		assertEquals(net.getPlaces().size(), 4);
		assertEquals(net.getTransitions().size(), 4);
		assertEquals(net.getEdges().size(), 8);
		Flow f = net.getFlow("s2", "t1");
		assertEquals(f.getWeight(), 6);
		assertEquals(net.getPlace("s1").getInitialToken().getValue(), 4);
		assertEquals(net.getPlace("s3").getInitialToken().getValue(), 2);
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
	public void testMarkedSideCondition() throws Exception {
		PetriNet net = new AptPNParser().parsePN(".type PN\n.places p1\n.transitions t1\n.flows t1:{p1}->{p1}\n.initial_marking {p1}");
		sideConditionAsserts(net);
	}

	@Test
	public void testMarkedSideConditionPlacesFirst() throws Exception {
		PetriNet net = new AptPNParser().parsePN(".places p1\n.type PN\n.transitions t1\n.flows t1:{p1}->{p1}\n.initial_marking {p1}");
		sideConditionAsserts(net);
	}

	@Test
	public void testMarkedSideConditionTransitionsFirst() throws Exception {
		PetriNet net = new AptPNParser().parsePN(".transitions t1\n.type PN\n.places p1\n.flows t1:{p1}->{p1}\n.initial_marking {p1}");
		sideConditionAsserts(net);
	}

	@Test
	public void testMarkedSideConditionFlowsFirst() throws Exception {
		PetriNet net = new AptPNParser().parsePN(".flows t1:{p1}->{p1}\n.type PN\n.places p1\n.transitions t1\n.initial_marking {p1}");
		sideConditionAsserts(net);
	}

	@Test
	public void testMarkedSideConditionMarkingFirst() throws Exception {
		PetriNet net = new AptPNParser().parsePN(".initial_marking {p1}\n.type PN\n.places p1\n.transitions t1\n.flows t1:{p1}->{p1}");
		sideConditionAsserts(net);
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testDoubleNodes() throws Exception {
		new AptPNParser().parsePNFile("nets/not-parsable-test-nets/doubleNodes_shouldNotBeParsable-net.apt");
	}

	@Test
	public void testInitalMarking() throws Exception {
		PetriNet pn = new AptPNParser().parsePNFile("nets/doubleMarking.apt");
		Marking im = pn.getInitialMarking();
		Token s1 = im.getToken("s1");
		Token s3 = im.getToken("s3");
		assertEquals(s1.getValue(), 4);
		assertEquals(s3.getValue(), 1);
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testUnknownAttributeNet() throws Exception {
		new AptPNParser().parsePNFile("nets/not-parsable-test-nets/unknown-attribute.apt");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testMissingNewlineAfterComment() throws Exception {
		new AptPNParser().parsePN(".type PN// Comment without newline after");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testMissingType() throws Exception {
		new AptPNParser().parsePN(".places foo\n.transitions bar");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testTypeTwice() throws Exception {
		new AptPNParser().parsePN(".type PN\n.places foo\n.transitions bar\n.type LPN");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testNameTwice() throws Exception {
		new AptPNParser().parsePN(".type PN\n.name \"foo\"\n.places foo\n.transitions bar\n.name \"bar\"");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testDesciptionTwice() throws Exception {
		new AptPNParser().parsePN(".type PN\n.description \"foo\"\n.places foo\n.transitions bar\n.description \"bar\"");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testInitialMarkingTwice() throws Exception {
		new AptPNParser().parsePN(".type PN\n.initialMarking {foo}\n.places foo\n.transitions bar\n.initialMarking");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
