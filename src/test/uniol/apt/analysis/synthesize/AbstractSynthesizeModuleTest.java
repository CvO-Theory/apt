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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.analysis.synthesize.AbstractSynthesizeModule.Options;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;

/** @author Uli Schlachter */
public class AbstractSynthesizeModuleTest {
	// I'm too lazy to type
	private PNProperties parse(String arg) throws ModuleException {
		return parse(arg, Collections.<String>emptyList(), Collections.<String>emptyList());
	}

	// I'm too lazy to type
	private PNProperties parse(String arg, Collection<String> supportedExtraOptions,
			Collection<String> expectedExtraOptions) throws ModuleException {
		Set<String> extra = new HashSet<>(expectedExtraOptions);
		Options options = Options.parseProperties(arg, supportedExtraOptions);
		assertThat(options.extraOptions, is(extra));
		return options.properties;
	}

	@Test
	public void testNone() throws Exception {
		assertThat(parse("none"), equalTo(new PNProperties()));
	}

	@Test
	public void testPure() throws Exception {
		PNProperties properties = new PNProperties().setPure(true);
		assertThat(parse("pure"), equalTo(properties));
	}

	@Test
	public void testSafe() throws Exception {
		PNProperties properties = new PNProperties().requireSafe();
		assertThat(parse("Safe"), equalTo(properties));
	}

	@Test
	public void testPlain() throws Exception {
		PNProperties properties = new PNProperties().setPlain(true);
		assertThat(parse("PLAIN"), equalTo(properties));
	}

	@Test
	public void testTNet() throws Exception {
		PNProperties properties = new PNProperties().setTNet(true);
		assertThat(parse("tNeT"), equalTo(properties));
	}

	@Test
	public void testMarkedGraph() throws Exception {
		PNProperties properties = new PNProperties().setMarkedGraph(true).setPlain(true);
		assertThat(parse("marked-Graph"), equalTo(properties));
	}

	@Test
	public void testMarkedGraph2() throws Exception {
		PNProperties properties = new PNProperties().setMarkedGraph(true).setPlain(true);
		assertThat(parse("mg"), equalTo(properties));
	}

	@Test
	public void testMarkedGraph3() throws Exception {
		PNProperties properties = new PNProperties().setMarkedGraph(true);
		assertThat(parse("generalIzed-marked-graph"), equalTo(properties));
	}

	@Test
	public void testMarkedGraph3_en_GB() throws Exception {
		PNProperties properties = new PNProperties().setMarkedGraph(true);
		assertThat(parse("generalIsed-marked-graph"), equalTo(properties));
	}

	@Test
	public void testMarkedGraph4() throws Exception {
		PNProperties properties = new PNProperties().setMarkedGraph(true);
		assertThat(parse("gmg"), equalTo(properties));
	}

	@Test
	public void test3Bounded() throws Exception {
		PNProperties properties = new PNProperties().requireKBounded(3);
		assertThat(parse("3-bounded"), equalTo(properties));
	}

	@Test
	public void test3Marking() throws Exception {
		PNProperties properties = new PNProperties().requireKMarking(3);
		assertThat(parse("3-marking"), equalTo(properties));
	}

	@Test
	public void testOutputNonbranching() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true);
		assertThat(parse("generalized-output-nonbranching"), equalTo(properties));
	}

	@Test
	public void testOutputNonbranching_en_GB() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true);
		assertThat(parse("generalised-output-nonbranching"), equalTo(properties));
	}

	@Test
	public void testOutputNonbranching2() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true).setPlain(true);
		assertThat(parse("output-nonbranching"), equalTo(properties));
	}

	@Test
	public void testON() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true);
		assertThat(parse("gON"), equalTo(properties));
	}

	@Test
	public void testON2() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true).setPlain(true);
		assertThat(parse("ON"), equalTo(properties));
	}

	@Test
	public void testConflictFree() throws Exception {
		PNProperties properties = new PNProperties().setConflictFree(true);
		assertThat(parse("Conflict-Free"), equalTo(properties));
	}

	@Test
	public void testCF() throws Exception {
		PNProperties properties = new PNProperties().setConflictFree(true);
		assertThat(parse("CF"), equalTo(properties));
	}

	@Test
	public void testHomogeneous() throws Exception {
		PNProperties properties = new PNProperties().setHomogeneous(true);
		assertThat(parse("homogeneous"), equalTo(properties));
	}

	@Test
	public void testExtraArgs() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true).setPlain(true);
		assertThat(parse("foo,bAr,on,baz", Arrays.asList("foo", "bar", "foobar", "baz"),
					Arrays.asList("foo", "bar", "baz")),
				equalTo(properties));
	}

	@Test
	public void testComma() throws Exception {
		PNProperties properties = new PNProperties().requireSafe().setPure(true);
		assertThat(parse("safe,none,pure"), equalTo(properties));
	}

	@Test
	public void testSpaces() throws Exception {
		assertThat(parse(" none "), equalTo(new PNProperties()));
	}

	@DataProvider(name = "unparsable")
	private Object[][] createUnparsable() {
		return new Object[][] {
			{ "e" },
			{ "no ne" },
			{ "0-bounded" },
			{ "-3-bounded" },
			{ "0-marking" },
			{ "-3-marking" },
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
