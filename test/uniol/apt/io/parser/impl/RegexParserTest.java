/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.io.parser.ParseException;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
public class RegexParserTest {
	static private void test(String regex, FiniteAutomaton expected) throws Exception {
		FiniteAutomaton aut = RegexParser.parseRegex(regex);
		assertThat(findWordDifference(aut, expected), nullValue());
	}

	static private FiniteAutomaton getAtomic(String symbol) {
		return getAtomicLanguage(new Symbol(symbol));
	}

	@Test
	public void testEmptyLanguage() throws Exception {
		test("~", getEmptyLanguage());
	}

	@Test
	public void testEpsilonLanguage() throws Exception {
		test("$", getAtomicLanguage(Symbol.EPSILON));
	}

	@Test
	public void testALanguage() throws Exception {
		test("a1$_", concatenate(getAtomic("a"), concatenate(getAtomic("1"), getAtomic("_"))));
	}

	@Test
	public void testALanguage2() throws Exception {
		test("<a1_>", getAtomic("a1_"));
	}

	@Test
	public void testComplicatedLanguage() throws Exception {
		FiniteAutomaton autA = getAtomic("a");
		FiniteAutomaton autB = getAtomic("b");
		test("(a+\r(a\t|\nb)?$)*", kleeneStar(concatenate(kleenePlus(autA), optional(union(autA, autB)))));
	}

	@Test
	public void testComment1() throws Exception {
		test("a /* and then later we have */ b",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test
	public void testComment2() throws Exception {
		test("a /* and then / later * we have */b",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test
	public void testComment3() throws Exception {
		test("a // and then later\n//we have \r\nb",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testComment4() throws Exception {
		RegexParser.parseRegex("/a");
	}

	@Test
	public void testComment5() throws Exception {
		test("a // Missing newline after comment", getAtomic("a"));
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testClosingParen() throws Exception {
		RegexParser.parseRegex(")");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testClosingParenAfterExpr() throws Exception {
		RegexParser.parseRegex("(ab)*)");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testMissingClosingParen() throws Exception {
		RegexParser.parseRegex("(a*|b+");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testNotAllowedCharacter() throws Exception {
		RegexParser.parseRegex("ab?@d");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testBrokenID() throws Exception {
		RegexParser.parseRegex("<ab");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testBrokenID2() throws Exception {
		RegexParser.parseRegex("<a<");
	}

	@Test(expectedExceptions = { ParseException.class })
	public void testBrokenID3() throws Exception {
		RegexParser.parseRegex("<");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
