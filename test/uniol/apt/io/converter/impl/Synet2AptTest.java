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

import java.io.IOException;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import java.util.List;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.converter.Synet2AptModule;
import uniol.apt.io.parser.impl.apt.APTLTSParser;
import uniol.apt.io.parser.impl.apt.APTPNParser;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleInvoker;

/**
 * Could not test anymore cauce of module reading from stdin.
 *
 * @author Manuel Gieseking
 *
 */
public class Synet2AptTest {

	/**
	 * Could not test anymore cause of module reading from stdin.
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws ModuleException
	 */
	public void testLTS() throws IOException, FormatException, ModuleException {
		Synet2AptModule mod = new Synet2AptModule();
		ModuleInvoker m = new ModuleInvoker();
		List<Object> objs = m.invoke(mod, "nets/synet-nets/synet-apt1-redmine-docs.aut");
		String fo = (String) objs.get(0);
		assertEquals(fo, "nets/synet-nets/synet-apt1-redmine-docs-aut.apt");
		TransitionSystem ts = APTLTSParser.getLTS("nets/synet-nets/synet-apt1-redmine-docs-aut.apt");
		assertNotNull(ts);

		assertEquals(ts.getNodes().size(), 4);
		assertNotNull(ts.getNode("0"));
		assertNotNull(ts.getNode("1"));
		assertNotNull(ts.getNode("2"));
		assertNotNull(ts.getNode("3"));
		assertNull(ts.getNode("4"));
		assertEquals(ts.getInitialState(), ts.getNode("0"));

		assertEquals(ts.getEdges().size(), 4);
	}

	/**
	 * Could not test anymore cause of module reading from stdin.
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws ModuleException
	 */
	public void testPN() throws IOException, FormatException, ModuleException {
		Synet2AptModule mod = new Synet2AptModule();
		ModuleInvoker m = new ModuleInvoker();
		List<Object> objs = m.invoke(mod, "nets/synet-nets/synet-docu-example.net");
		String fo = (String) objs.get(0);
		assertEquals(fo, "nets/synet-nets/synet-docu-example-net.apt");
		PetriNet pn = APTPNParser.getPetriNet(fo);
		assertNotNull(pn);
		assertEquals(5, pn.getTransitions().size());
		assertEquals(6, pn.getPlaces().size());

		//assertEquals("A", pn.getTransitionById("t").getExtension("location"));
		//assertEquals("A", pn.getPlaceById("x_0").getExtension("location"));

		Marking mark = pn.getInitialMarkingCopy();
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
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
