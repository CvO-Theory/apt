/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import uniol.apt.analysis.synthesize.Matchers;
import uniol.apt.module.exception.ModuleException;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
public class SynthesizeModuleTest {
	// I'm too lazy to type
	private PNProperties parse(String arg) throws ModuleException {
		return SynthesizeModule.parseProperties(arg);
	}

	@Test
	public void testNone() throws Exception {
		assertThat(parse("none"), emptyIterable());
	}

	@Test
	public void testPure() throws Exception {
		assertThat(parse("pure"), contains(PNProperties.PURE));
	}

	@Test
	public void testSafe() throws Exception {
		assertThat(parse("Safe"), Matchers.<PNProperties.PNProperty>contains(PNProperties.SAFE));
	}

	@Test
	public void testPlain() throws Exception {
		assertThat(parse("PLAIN"), contains(PNProperties.PLAIN));
	}

	@Test
	public void testTNet() throws Exception {
		assertThat(parse("tNeT"), contains(PNProperties.TNET));
	}

	@Test
	public void test3Bounded() throws Exception {
		assertThat(parse("3-bounded"), Matchers.<PNProperties.PNProperty>contains(PNProperties.kBounded(3)));
	}

	@Test
	public void testComma() throws Exception {
		assertThat(parse("safe,none,pure"), containsInAnyOrder(PNProperties.SAFE, PNProperties.PURE));
	}

	@Test
	public void testSpaces() throws Exception {
		assertThat(parse(" none "), emptyIterable());
	}

	@DataProvider(name = "unparsable")
	private Object[][] createUnparsable() {
		return new Object[][] {
			{ "e" },
			{ "no ne" },
			{ "0-bounded" },
			{ "-3-bounded" },
			{ "solves the halting problem" },
			{ "impure" },
		};
	}

	@Test(dataProvider = "unparsable", expectedExceptions = ModuleException.class)
	public void testUnparsable(String arg) throws Exception {
		parse(arg);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
