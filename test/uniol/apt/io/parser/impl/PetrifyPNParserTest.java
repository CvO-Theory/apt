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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static uniol.apt.TestNetCollection.getNoTransitionOnePlaceNet;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.annotations.Test;

import uniol.apt.CrashCourseNets;
import uniol.apt.TestNetCollection;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.io.parser.impl.petrify.PetrifyPNParser;
import uniol.apt.io.renderer.impl.PetrifyRenderer;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.NetIsNotParsableException;

/**
 * Test wether the Petrify renderer and parser work together.
 * <p/>
 * @author Sören
 * <p/>
 */
public class PetrifyPNParserTest {

	@Test
	public void test() throws FileNotFoundException, ModuleException {

		PetriNet pn = CrashCourseNets.getCCNet1();

		PetriNet onePlace = getNoTransitionOnePlaceNet();

		PetriNet onePlaceOneTrans = TestNetCollection.getTokenGeneratorNet();

		try {
			testNets(pn);
			testNets(onePlace);
			testonePlaceOneTrans(onePlaceOneTrans);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void testonePlaceOneTrans(PetriNet pn) throws ModuleException, IOException {
		PetrifyRenderer renderer = new PetrifyRenderer();
		PetrifyPNParser p = new PetrifyPNParser();
		String s1 = renderer.render(pn);
		p.parse(renderer.render(pn));
		PetriNet test = p.getPN();
		String s2 = renderer.render(test);
		assertEquals(s1, s2);
	}

	private void testNets(PetriNet pn) throws IOException, NetIsNotParsableException, ModuleException,
		UnboundedException {
		PetrifyRenderer renderer = new PetrifyRenderer();
		PetrifyPNParser p = new PetrifyPNParser();
		p.parse(renderer.render(pn));
		PetriNet test = p.getPN();

		IsomorphismLogic logic = new IsomorphismLogic(pn, test, false);
		assertTrue(logic.isIsomorphic());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
