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

package uniol.apt.analysis.petrify;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import org.testng.SkipException;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.PetrifyNotFoundException;
import static uniol.apt.io.parser.ParserTestUtils.getAptLTS;

/**
 * Tests if the given LTS is parsable.
 * @author Sören
 *
 */
public class PetrifyLTSSynthesizeTest {

	@Test
	public void test() throws IOException, ModuleException {
		try {
			String path = "nets/crashkurs-cc1-aut.apt";
			TransitionSystem lts = getAptLTS(path);
			PetrifyLTSSynthesize syn = new PetrifyLTSSynthesize(lts);
			assertTrue(syn.check());
		} catch (PetrifyNotFoundException e) {
			throw new SkipException("petrify not found");
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
