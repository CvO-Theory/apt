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

import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.CrashCourseNets.getCCNet2;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.netWithSameStructureAs;

/**
 * @author Manuel Gieseking
 */
public class PnmlPNParserTest {
	@Test
	public void testCCNet2Pipe() throws Exception {
		PetriNet actual = new PnmlPNParser().parseFile("nets/crashkurs-cc2-net.pipe.pnml");
		PetriNet expected = getCCNet2();
		assertThat(actual, netWithSameStructureAs(expected));
		assertThat(actual.getName(), is("Net-One"));
	}

	@Test
	public void testCCNet2Lola() throws Exception {
		PetriNet actual = new PnmlPNParser().parseFile("nets/crashkurs-cc2-net.lola.pnml");
		PetriNet expected = getCCNet2();
		assertThat(actual, netWithSameStructureAs(expected));
		assertThat(actual.getName(), is("LoLA_Ausgabe"));
	}

	@Test
	public void testEmptyNetPipe() throws Exception {
		PetriNet actual = new PnmlPNParser().parseFile("nets/empty-net.pnml");
		PetriNet expected = new PetriNet();
		assertThat(actual, netWithSameStructureAs(expected));
		assertThat(actual.getName(), is("Net-One"));
	}

	@Test
	public void testEmptyNetEmptyID() throws Exception {
		PetriNet actual = new PnmlPNParser().parseString(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><pnml><net id=\"\" type=\"P/T net\">" +
				"</net></pnml>");
		PetriNet expected = new PetriNet();
		assertThat(actual, netWithSameStructureAs(expected));
		assertThat(actual.getName(), is(""));
	}

	@Test
	public void testNetIso() throws Exception {
		PetriNet actual = new PnmlPNParser().parseFile("nets/pnml-iso/Piscine.pnml");
		assertThat(actual.getPlaces(), hasSize(9));
		assertThat(actual.getTransitions(), hasSize(7));
		assertThat(actual.getEdges(), hasSize(20));
		assertThat(actual.getName(), is("cId-76838737524772522561"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
