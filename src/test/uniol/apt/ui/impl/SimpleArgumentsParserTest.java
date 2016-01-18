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

package uniol.apt.ui.impl;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import uniol.apt.ui.ParametersParser;

/**
 * @author Renke Grunwald
 *
 */
public class SimpleArgumentsParserTest {

	@Test
	public void testGetModuleNames() {
		ParametersParser parser = new SimpleParametersParser();

		parser.parse(new String[]{"example_module", "arg1", "arg2"});

		assertEquals(parser.getModuleNames()[0], "example_module");

		parser.parse(new String[]{});

		assertEquals(parser.getModuleNames().length, 0);
	}

	@Test
	public void testGetModuleArguments() {
		ParametersParser parser = new SimpleParametersParser();

		parser.parse(new String[]{"example_module", "arg1", "arg2"});

		assertEquals(parser.getModuleArguments("example_module")[0], "arg1");
		assertEquals(parser.getModuleArguments("example_module")[1], "arg2");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
