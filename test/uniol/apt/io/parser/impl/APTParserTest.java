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

package uniol.apt.io.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;
import uniol.apt.CrashCourseNets;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.apt.APTLTSParser;
import uniol.apt.io.parser.impl.apt.APTParser;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.StructureException;

/**
 * @author Manuel Gieseking
 * <p/>
 */
public class APTParserTest {

	@Test
	public void testAPTParser() throws IOException, FormatException, ParseException {
		APTParser parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-net.apt");
		assertNotNull(parser.getPn());
		assertNull(parser.getTs());
		parser.parse("nets/crashkurs-cc1-aut.apt");
		assertNotNull(parser.getTs());
		assertNull(parser.getPn());
	}

	@Test
	public void testLTSTestNet() throws IOException, FormatException {
		try {
			TransitionSystem ts = APTLTSParser.getLTS("nets/testLts-aut.apt");
			assertEquals("testnet", ts.getName());
			State e = ts.getNode("s2");
			assertEquals("hund", e.getExtension("bla").toString());
			assertEquals("kater", e.getExtension("blub").toString());
			assertEquals(ts.getNode("s2"), ts.getInitialState());
			State s0 = ts.getNode("s0");
			for (Arc ed : s0.getPostsetEdges()) {
				if (ed.getTarget().equals(ts.getNode("s1"))) {
					assertEquals("a", ed.getLabel());
					assertEquals("A", ed.getExtension("location").toString());
				}
			}
		} catch (LexerParserException ex) {
			// yes it's intended. Just for testing...
			//ex.printStackTrace();
			//System.err.println(ex.getLexerParserMessage());
			fail();
		}
	}

	@Test
	public void testLTS() throws IOException, FormatException {
		TransitionSystem ts = APTLTSParser.getLTS("nets/crashkurs-cc1-aut.apt");
		assertNotNull(ts);
	}

	@Test
	public void testLTSandPN() throws IOException, FormatException, ParseException {
		APTParser parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-aut.apt");
		assertNull(parser.getPn());
		assertNotNull(parser.getTs());
		parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-net.apt");
		assertNotNull(parser.getPn());
		assertNull(parser.getTs());
	}

	@Test
	public void testCrashCourseNets() throws IOException, FormatException {
		assertNotNull(CrashCourseNets.getCCNet1());
		assertNotNull(CrashCourseNets.getCCNet2());
		assertNotNull(CrashCourseNets.getCCNet2inf());
		assertNotNull(CrashCourseNets.getCCNet3());
		assertNotNull(CrashCourseNets.getCCNet4());
		assertNotNull(CrashCourseNets.getCCNet5());
		assertNotNull(CrashCourseNets.getCCNet6());
		assertNotNull(CrashCourseNets.getCCNet7());
		assertNotNull(CrashCourseNets.getCCNet8());
		assertNotNull(CrashCourseNets.getCCNet9());
		assertNotNull(CrashCourseNets.getCCNet10());
		assertNotNull(CrashCourseNets.getCCNet11());
		assertNotNull(CrashCourseNets.getCCNet12());
		assertNotNull(CrashCourseNets.getCCNet13());
		assertNotNull(CrashCourseNets.getCCNet14());
	}

	@Test
	public void testDoubleNodes() throws IOException, FormatException {
		// test LTS
		// double nodes
		try {
			APTLTSParser.getLTS("nets/not-parsable-test-nets/doubleNodes_shouldNotBeParsable-aut.apt");
			fail("not detected adding two nodes with same id.");
		} catch (LexerParserException e) {
			assertEquals(e.getLexerMsg(), "line 11:0 Node s1 already exists.");
			assertEquals(e.getParserMsg(), "line 11:0 Node s1 already exists.");
		}
		// double initial states
		try {
			APTLTSParser.getLTS("nets/not-parsable-test-nets/doubleInitialstate_shouldNotBeParsable-aut.apt");
			fail("not detected StructureException: initial state is set multiple times");
		} catch (StructureException se) {
			assertEquals(se.getMessage(), "initial state is set multiple times.");
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
