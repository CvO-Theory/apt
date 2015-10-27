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

package uniol.apt.io.converter.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import java.util.List;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.converter.Synet2AptModule;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.AptPNParser;
import uniol.apt.io.parser.impl.AptLTSParser;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleInvoker;
import org.testng.annotations.Test;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.io.parser.impl.synet.SynetLTSParser;
import uniol.apt.io.renderer.impl.APTRenderer;

/**
 * Tests the converter, which parses a synet file and saves the content in the apt format.
 * <p/>
 * @author Manuel Gieseking
 */
public class Synet2AptTest {

	@Test
	public void testLTS() throws Exception {
		Synet2AptModule mod = new Synet2AptModule();
		ModuleInvoker m = new ModuleInvoker();
		List<Object> objs = m.invoke(mod, "nets/synet-nets/synet-apt1-redmine-docs.aut");
		String fromSynet2Apt = (String) objs.get(0);

		TransitionSystem ts1 = new AptLTSParser().parseLTS(fromSynet2Apt);
		assertNotNull(ts1);
		assertEquals(ts1.getNodes().size(), 4);
		assertNotNull(ts1.getNode("0"));
		assertNotNull(ts1.getNode("1"));
		assertNotNull(ts1.getNode("2"));
		assertNotNull(ts1.getNode("3"));
		assertEquals(ts1.getInitialState(), ts1.getNode("0"));
		assertEquals(ts1.getEdges().size(), 4);

		TransitionSystem ts = SynetLTSParser.getLTS("nets/synet-nets/synet-apt1-redmine-docs.aut");
		assertNotNull(ts);
		assertEquals(ts.getNodes().size(), 4);
		assertNotNull(ts.getNode("0"));
		assertNotNull(ts.getNode("1"));
		assertNotNull(ts.getNode("2"));
		assertNotNull(ts.getNode("3"));
		assertEquals(ts.getInitialState(), ts.getNode("0"));
		assertEquals(ts.getEdges().size(), 4);

		IsomorphismLogic iso = new IsomorphismLogic(ts1, ts, true);
		assertTrue(iso.isIsomorphic());

		APTRenderer renderer = new APTRenderer();
		String apt = renderer.render(ts);
		String[] rows = apt.split("\n");
		for (String string : rows) {
			assertTrue(fromSynet2Apt.contains(string));
		}
		rows = fromSynet2Apt.split("\n");
		for (String string : rows) {
			assertTrue(apt.contains(string));
		}
	}

	@Test
	public void testPN() throws Exception {
		Synet2AptModule mod = new Synet2AptModule();
		ModuleInvoker m = new ModuleInvoker();
		List<Object> objs = m.invoke(mod, "nets/synet-nets/synet-docu-example.net");
		String synet2apt = (String) objs.get(0);

		PetriNet pn = new AptPNParser().parsePN(synet2apt);
		assertNotNull(pn);
		assertEquals(5, pn.getTransitions().size());
		assertEquals(6, pn.getPlaces().size());

		assertEquals("A", pn.getTransition("t").getExtension("location"));
		assertEquals("A", pn.getPlace("x_0").getExtension("location"));

		Marking mark = pn.getInitialMarking();
		assertEquals(1, mark.getToken("x_5").getValue());
		assertEquals(1, mark.getToken("x_2").getValue());
		assertEquals(0, mark.getToken("x_3").getValue());

		Place x0 = pn.getPlace("x_0");
		assertEquals(x0.getPostset().size(), 2);
		assertTrue(x0.getPostset().contains(pn.getTransition("t")));
		assertTrue(x0.getPostset().contains(pn.getTransition("d")));
		assertFalse(x0.getPostset().contains(pn.getTransition("c")));

		assertEquals(x0.getPreset().size(), 1);
		assertTrue(x0.getPreset().contains(pn.getTransition("a")));
		assertFalse(x0.getPreset().contains(pn.getTransition("t")));

		PetriNet pn2 = new AptPNParser().parsePN(synet2apt);
		assertNotNull(pn2);
		assertEquals(5, pn2.getTransitions().size());
		assertEquals(6, pn2.getPlaces().size());

		assertEquals("A", pn2.getTransition("t").getExtension("location"));
		assertEquals("A", pn2.getPlace("x_0").getExtension("location"));

		mark = pn2.getInitialMarking();
		assertEquals(1, mark.getToken("x_5").getValue());
		assertEquals(1, mark.getToken("x_2").getValue());
		assertEquals(0, mark.getToken("x_3").getValue());

		x0 = pn2.getPlace("x_0");
		assertEquals(x0.getPostset().size(), 2);
		assertTrue(x0.getPostset().contains(pn2.getTransition("t")));
		assertTrue(x0.getPostset().contains(pn2.getTransition("d")));
		assertFalse(x0.getPostset().contains(pn2.getTransition("c")));

		assertEquals(x0.getPreset().size(), 1);
		assertTrue(x0.getPreset().contains(pn2.getTransition("a")));
		assertFalse(x0.getPreset().contains(pn2.getTransition("t")));

		IsomorphismLogic iso = new IsomorphismLogic(pn2, pn, true);
		assertTrue(iso.isIsomorphic());

		// Not possible to test, since not deterministic choice of which place would be named first in a flow
//		APTRenderer renderer = new APTRenderer();
//		String apt = renderer.render(pn2);
//		String[] rows = apt.split("\n");
//		for (String string : rows) {
//			assertTrue(synet2apt.contains(string));
//		}
//		rows = synet2apt.split("\n");
//		for (String string : rows) {
//			assertTrue(apt.contains(string));
//		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
