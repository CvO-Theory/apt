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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import uniol.apt.adt.ts.TransitionSystem;

/**
 * @author vsp
 */
public class SynetLTSParserTest {
	@Test
	public void testLTS() throws Exception {
		TransitionSystem ts = new SynetLTSParser().parseFile("nets/synet-nets/synet-apt1-redmine-docs.aut");

		assertEquals(ts.getNodes().size(), 4);
		assertNotNull(ts.getNode("0"));
		assertNotNull(ts.getNode("1"));
		assertNotNull(ts.getNode("2"));
		assertNotNull(ts.getNode("3"));
		assertEquals(ts.getInitialState().getId(), "0");

		assertEquals(ts.getEdges().size(), 4);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
