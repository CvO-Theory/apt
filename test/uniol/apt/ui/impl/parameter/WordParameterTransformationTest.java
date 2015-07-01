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

package uniol.apt.ui.impl.parameter;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import uniol.apt.analysis.language.Word;
import uniol.apt.module.exception.ModuleException;

/** @author Uli Schlachter */
public class WordParameterTransformationTest {
	@Test(expectedExceptions = ModuleException.class)
	public void testEmptyWord() throws Exception {
		new WordParameterTransformation().transform("");
	}

	@Test(expectedExceptions = ModuleException.class)
	public void testEpsilonWord() throws Exception {
		new WordParameterTransformation().transform("a,b,,d");
	}

	@Test
	public void testABCDWord() throws Exception {
		Word expected = new Word();
		expected.add("a");
		expected.add("b");
		expected.add("c");
		expected.add("d");
		Word actual = new WordParameterTransformation().transform("a,b,c,d");
		assertEquals(actual, expected);
	}

	@Test
	public void testABCDLongWord() throws Exception {
		Word expected = new Word();
		expected.add("ab");
		expected.add("cd");
		Word actual = new WordParameterTransformation().transform("ab,cd");
		assertEquals(actual, expected);
	}

	@Test
	public void testPrefixWord() throws Exception {
		Word expected = new Word();
		expected.add("a");
		expected.add("aa");
		expected.add("aaa");
		Word actual = new WordParameterTransformation().transform("a,aa,aaa");
		assertEquals(actual, expected);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
