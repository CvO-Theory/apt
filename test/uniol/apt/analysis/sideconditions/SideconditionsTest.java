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

package uniol.apt.analysis.sideconditions;

import uniol.apt.adt.pn.PetriNet;
import static uniol.apt.io.parser.ParserTestUtils.getAptPN;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author CS
 *
 */
public class SideconditionsTest {

	@Test
	public void testSideconditions() {
		PetriNet pn;
		pn = getAptPN("./nets/isolated-and-sideconditions-nets/isolated-elements-net.apt");
		assertTrue(CheckSideConditions.checkSideConditions(pn).size() == 0);

		pn = getAptPN("./nets/isolated-and-sideconditions-nets/nonsimplesideconditions-net.apt");
		assertTrue(CheckSideConditions.checkSideConditions(pn).size() == 3);

		pn = getAptPN("./nets/isolated-and-sideconditions-nets/pure-net.apt");
		assertTrue(CheckSideConditions.checkSideConditions(pn).size() == 0);

		pn = getAptPN("./nets/isolated-and-sideconditions-nets/simplesideconditions-net.apt");
		assertTrue(CheckSideConditions.checkSideConditions(pn).size() == 3);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
