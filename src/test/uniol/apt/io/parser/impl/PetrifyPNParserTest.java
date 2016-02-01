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

package uniol.apt.io.parser.impl;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import uniol.apt.io.parser.ParseException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import static org.testng.Assert.assertEquals;

/**
 * Test wether the Petrify parser works.
 * @author Sören
 */
public class PetrifyPNParserTest {
	@Test
	public void testOnePlaceOneTrans() throws Exception {
		PetriNet pn = new PetrifyPNParser().parseString(".inputs t1\n.graph\nt1 p1\n.marking  { } \n.end\n");
		assertEquals(pn.getTransitions().size(), 1);
		assertEquals(pn.getPlaces().size(), 1);
		assertEquals(pn.getEdges().size(), 1);
		assertEquals(pn.getTransition("t1").getPostsetNodes().iterator().next().getId(), "p1");
	}

	@Test
	public void testIsolatedPlace() throws Exception {
		PetriNet pn = new PetrifyPNParser().parseString(".inputs\n.graph\nfoo\n.marking{}\n.end\n");
		assertEquals(pn.getTransitions().size(), 0);
		assertEquals(pn.getPlaces().size(), 1);
	}

	@Test
	public void testEmptyPN() throws Exception {
		PetriNet pn = new PetrifyPNParser().parseString(".inputs\n.graph\n\n.marking{}\n.end\n");
		assertEquals(pn.getTransitions().size(), 0);
		assertEquals(pn.getPlaces().size(), 0);
	}

	@Test
	public void testLabel() throws Exception {
		PetriNet pn = new PetrifyPNParser().parseString(".inputs a\n.graph\np0 a\na/0 a/1\n.marking{p0}\n.end\n");
		assertEquals(pn.getTransitions().size(), 2);
		assertEquals(pn.getPlaces().size(), 2);

		Place p0 = pn.getPlace("p0");
		Place pImpl = pn.getPlace("<a,a/1>");
		Transition ta = pn.getTransition("a");
		Transition ta1 = pn.getTransition("a/1");

		assertEquals(p0.getInitialToken().getValue(), 1);
		assertEquals(pImpl.getInitialToken().getValue(), 0);

		assertEquals(ta.getLabel(), "a");
		assertEquals(ta1.getLabel(), "a");

		assertEquals(p0.getPresetEdges().size(), 0);
		assertEquals(p0.getPostsetEdges().size(), 1);
		assertEquals(ta.getPresetEdges().size(), 1);
		assertEquals(ta.getPostsetEdges().size(), 1);
		assertEquals(pImpl.getPresetEdges().size(), 1);
		assertEquals(pImpl.getPostsetEdges().size(), 1);
		assertEquals(ta1.getPresetEdges().size(), 1);
		assertEquals(ta1.getPostsetEdges().size(), 0);

		assertEquals(p0.getPostsetNodes().iterator().next().getId(), ta.getId());
		assertEquals(ta.getPostsetNodes().iterator().next().getId(), pImpl.getId());
		assertEquals(pImpl.getPostsetNodes().iterator().next().getId(), ta1.getId());
	}

	@Test
	public void testABBAA() throws Exception {
		// Actual output of petrify (except for comments) when synthesizing a solution to the word "abbaa"
		PetriNet pn = new PetrifyPNParser().parseString(".model abbaa.g\n.inputs  a b\n.graph\na a/2\nb b/1\n"
				+ "a/1 b\nb/1 a\np0 a/1\n.marking { p0 }\n.end\n");
		assertEquals(pn.getTransitions().size(), 5);
		assertEquals(pn.getPlaces().size(), 5);
		assertEquals(pn.getEdges().size(), 9);

		Place place = pn.getPlace("p0");
		assertEquals(place.getInitialToken().getValue(), 1);

		for (String nextTransition : Arrays.asList("a/1", "b", "b/1", "a", "a/2")) {
			Transition t = pn.getTransition(nextTransition);
			assertEquals(t.getPresetEdges().size(), 1);
			assertEquals(t.getPresetNodes(), Collections.singleton(place));
			if ("a/2".equals(nextTransition)) {
				assertEquals(t.getPostsetEdges().size(), 0);
			} else {
				assertEquals(t.getPostsetEdges().size(), 1);
				place = t.getPostsetEdges().iterator().next().getPlace();
				assertEquals(place.getInitialToken().getValue(), 0);
			}
		}
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Tried to create arc between two places 'p0' and 'p1'$")
	public void testArcBetweenPlaces() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs\n.graph\np0 p1\n.marking { }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Duplicate initial marking for place '<a,b>'$")
	public void testDuplicateMarking() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs a b\n.graph\na b\nb a\n.marking { <a, b> <a, b> }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Duplicate initial marking for place 'p'$")
	public void testDuplicateMarking2() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs a b\n.graph\np a\np b\n.marking { p p }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Arc with sourceId 'a' and targetId 'p' already exists in graph ''$")
	public void testDuplicateArc() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs a b\n.graph\na p\na p\n.marking { p }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^There is no implicit place between 'a' and 'b' whose initial marking can be set$")
	public void testPlaceDoesNotExist() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs a b\n.graph\n.marking { <a, b> }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^A non-existent event was split in 'c/1'$")
	public void testSplitNonExistentEvent() throws Exception {
		PetrifyPNParser p = new PetrifyPNParser();
		p.parseString(".inputs a b\n.graph\na/0 b c/1\n.marking { <a, b> }\n.end\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
