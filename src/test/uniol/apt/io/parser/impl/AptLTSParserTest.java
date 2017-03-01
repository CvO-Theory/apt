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

import java.util.Set;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;

/**
 * @author vsp
 */
public class AptLTSParserTest {
	@Test
	public void testLTSTestNet() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseFile("nets/testLts-aut.apt");
		assertEquals(ts.getName(), "testnet");
		State e = ts.getNode("s2");
		assertEquals(e.getExtension("bla").toString(), "hund");
		assertEquals(e.getExtension("blub").toString(), "kater");
		assertEquals(ts.getInitialState().getId(), "s2");
		State s0 = ts.getNode("s0");
		assertEquals(ts.getEvent("a").getExtension("location").toString(), "A");
		for (Arc ed : s0.getPostsetEdges()) {
			if (ed.getTarget().equals(ts.getNode("s1"))) {
				assertEquals(ed.getLabel(), "a");
			}
		}
		assertEquals(ts.getArc("s2", "s3", "a").getExtension("foo"), 42);
	}

	private void loopAsserts(TransitionSystem ts) {
		assertEquals(ts.getName(), "42");
		assertEquals(ts.getExtension("description"), "the answer!");
		assertEquals(ts.getInitialState().getId(), "s1");
		assertEquals(ts.getNodes().size(), 1);
		assertEquals(ts.getAlphabet().size(), 1);
		assertEquals(ts.getEdges().size(), 1);
		State s1 = ts.getNode("s1");
		assertEquals(s1.getId(), "s1");
		Set<Arc> arcs = s1.getPostsetEdges();
		assertEquals(arcs.size(), 1);
		for (Arc arc : arcs) {
			assertEquals(arc.getSourceId(), "s1");
			assertEquals(arc.getTargetId(), "s1");
			assertEquals(arc.getSource().getId(), "s1");
			assertEquals(arc.getTarget().getId(), "s1");
			assertEquals(arc.getLabel(), "l1");
		}
	}

	@Test
	public void testLoop() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".type LTS\n.name \"42\"\n.description \"the answer!\"\n.states s1[initial]\n.labels l1\n.arcs s1 l1 s1");
		loopAsserts(ts);
	}

	@Test
	public void testLoopArcsFirst() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".arcs s1 l1 s1\n.type LTS\n.name \"42\"\n.description \"the answer!\"\n.states s1[initial]\n.labels l1");
		loopAsserts(ts);
	}

	@Test
	public void testLoopLabelsFirst() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".labels l1\n.type LTS\n.name \"42\"\n.description \"the answer!\"\n.states s1[initial]\n.arcs s1 l1 s1");
		loopAsserts(ts);
	}

	@Test
	public void testLoopStatesFirst() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".states s1[initial]\n.type LTS\n.name \"42\"\n.description \"the answer!\"\n.labels l1\n.arcs s1 l1 s1");
		loopAsserts(ts);
	}

	@Test
	public void testLoopOptions() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".type LTS\n.name \"42\"\n.options description=\"the answer!\"\n.states s1[initial]\n.labels l1\n.arcs s1 l1 s1");
		loopAsserts(ts);
	}

	@Test
	public void testLoopTwoOptions() throws Exception {
		TransitionSystem ts = new AptLTSParser().parseString(".type LTS\n.name \"42\"\n.options description=\"the answer!\",fortytwo=42\n.states s1[initial]\n.labels l1\n.arcs s1 l1 s1");
		loopAsserts(ts);
		assertEquals(ts.getExtension("fortytwo"), 42);
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Node 's1' already exists in graph 'doubleNodes'$")
	public void testDoubleNodes() throws Exception {
		new AptLTSParser().parseFile("nets/not-parsable-test-nets/doubleNodes-aut.apt_unparsable");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^States '(s0' and 's2|s2' and 's0)' are both marked as initial states$")
	public void testDoubleInitState() throws Exception {
		new AptLTSParser().parseFile("nets/not-parsable-test-nets/doubleInitialstate.apt_unparsable");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 4 col 5: no viable alternative at input '<EOF>'$")
	public void testMissingType() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.states foo\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 3 col 0: no viable alternative at input '\\.type'$")
	public void testTypeTwice() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.type LTS\n.type LTS\n.states foo\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 2 col 0: no viable alternative at input '\\.name'$")
	public void testNameTwice() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.name \"42\"\n.type LTS\n.states foo\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^line 3 col 0: no viable alternative at input '\\.description'$")
	public void testDescriptionTwice() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.description \"42\"\n.description \"42\"\n.type LTS\n.states foo\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Initial state not found$")
	public void testMissingInitialState() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.description \"42\"\n.type LTS\n.states foo\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^States 'bar' and 'foo' are both marked as initial states$")
	public void testDoubleInitialState() throws Exception {
		new AptLTSParser().parseString(".name \"42\"\n.description \"42\"\n.type LTS\n.states foo[initial] bar[initial]\n.labels bar\n.arcs");
	}

	@Test(expectedExceptions = { ParseException.class }, expectedExceptionsMessageRegExp = "^Unknown label found: a$")
	public void testUnknownLabel() throws Exception {
		new AptLTSParser().parseString(".type LTS\n.states foo[initial]\n.labels\n.arcs foo a foo\n");
	}

	@Test
	public void testMissingNewlineAfterComment() throws Exception {
		new AptLTSParser().parseString(".type LTS.statess[initial]// Comment without newline after");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
