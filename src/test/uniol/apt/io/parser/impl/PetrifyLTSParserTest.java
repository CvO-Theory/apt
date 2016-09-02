/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       Uli Schlachter
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

import uniol.apt.io.parser.ParseException;
import uniol.apt.adt.ts.TransitionSystem;

import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;
import static uniol.apt.adt.matcher.Matchers.arcThatConnects;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the PetrifyLTSParser.
 * @author Uli Schlachter
 */
@SuppressWarnings("unchecked")
public class PetrifyLTSParserTest {
	private void verifyAbcLts(TransitionSystem lts) {
		assertThat(lts.getNodes(), containsInAnyOrder(nodeWithID("s0"), nodeWithID("s1"), nodeWithID("s2")));
		assertThat(lts.getAlphabet(), containsInAnyOrder("a", "b", "c"));
		assertThat(lts.getEdges(), containsInAnyOrder(
					arcThatConnects("s0", "s1"),
					arcThatConnects("s1", "s2"),
					arcThatConnects("s2", "s0")));
	}

	@Test
	public void testABCLTS1() throws Exception {
		verifyAbcLts(new PetrifyLTSParser().parseString(
				".inputs a b c\n.state graph\ns0 a s1 b s2 c s0\n.marking  {s0} \n.end\n"));
	}

	@Test
	public void testABCLTS2() throws Exception {
		verifyAbcLts(new PetrifyLTSParser().parseString(
				".inputs a b c\n.state graph\ns0 a s1\ns2 c s0\ns1 b s2\n.marking  {s0} \n.end\n"));
	}

	@Test
	public void testIsolatedState() throws Exception {
		TransitionSystem lts = new PetrifyLTSParser().parseString(
				".state graph\ns0\ns1\n.marking{ s0 }\n.end\n");
		assertThat(lts.getNodes(), contains(nodeWithID("s0"), nodeWithID("s1")));
		assertThat(lts.getAlphabet(), empty());
		assertThat(lts.getEdges(), empty());
		assertThat(lts.getInitialState(), nodeWithID("s0"));
	}

	@Test
	public void testEmptyLTS() throws Exception {
		TransitionSystem lts = new PetrifyLTSParser().parseString(
				" #bar\n.model   a_7-g # foo\n.state graph\ns0\n.marking{ s0 }\n.end\n");
		assertThat(lts.getName(), is("a_7-g"));
		assertThat(lts.getNodes(), contains(nodeWithID("s0")));
		assertThat(lts.getAlphabet(), empty());
		assertThat(lts.getEdges(), empty());
		assertThat(lts.getInitialState(), nodeWithID("s0"));
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Duplicate input 'a'$")
	public void testDuplicateInput() throws Exception {
		PetrifyLTSParser p = new PetrifyLTSParser();
		p.parseString(".inputs a a\n.state graph\ns0 a s1\n.marking { s0 }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 4 col 14: extraneous input 's1' expecting '}'$")
	public void testDuplicateMarking() throws Exception {
		PetrifyLTSParser p = new PetrifyLTSParser();
		p.parseString(".inputs a\n.state graph\ns0 a s1\n.marking { s0 s1 }\n.end\n");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Unknown event 'a'$")
	public void testEventDoesNotExist() throws Exception {
		PetrifyLTSParser p = new PetrifyLTSParser();
		p.parseString(".inputs\n.state graph\ns0 a s1\n.marking {s0}\n.end\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
