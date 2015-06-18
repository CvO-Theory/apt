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

package uniol.apt.io.parser.impl.apt;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.io.parser.impl.exception.LexerParserException;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
@Test
public class AptRegexFormatParserTest {
	static void test(String regex, FiniteAutomaton expected) throws Exception {
		FiniteAutomaton aut = AptRegexFormatParser.parseString(regex);
		assertThat(findWordDifference(aut, expected), nullValue());
	}

	static FiniteAutomaton getAtomic(String symbol) {
		return getAtomicLanguage(new Symbol(symbol));
	}

	@Test
	static void testEmptyLanguage() throws Exception {
		test("~", getEmptyLanguage());
	}

	@Test
	static void testEpsilonLanguage() throws Exception {
		test("$", getAtomicLanguage(Symbol.EPSILON));
	}

	@Test
	static void testALanguage() throws Exception {
		test("a1$_", concatenate(getAtomic("a"), concatenate(getAtomic("1"), getAtomic("_"))));
	}

	@Test
	static void testALanguage2() throws Exception {
		test("<a1_>", getAtomic("a1_"));
	}

	@Test
	static void testComplicatedLanguage() throws Exception {
		FiniteAutomaton autA = getAtomic("a");
		FiniteAutomaton autB = getAtomic("b");
		test("(a+\r(a\t|\nb)?$)*", kleeneStar(concatenate(kleenePlus(autA), optional(union(autA, autB)))));
	}

	@Test
	static void testComment1() throws Exception {
		test("a /* and then later we have */ b",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test
	static void testComment2() throws Exception {
		test("a /* and then / later * we have */b",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test
	static void testComment3() throws Exception {
		test("a // and then later\n//we have \r\nb",
				concatenate(getAtomic("a"), getAtomic("b")));
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testComment4() throws Exception {
		AptRegexFormatParser.parseString("/a");
	}

	@Test
	static void testComment5() throws Exception {
		test("a // Missing newline after comment", getAtomic("a"));
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testClosingParen() throws Exception {
		AptRegexFormatParser.parseString(")");
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testMissingClosingParen() throws Exception {
		AptRegexFormatParser.parseString("(a*|b+");
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testNotAllowedCharacter() throws Exception {
		AptRegexFormatParser.parseString("ab?@d");
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testBrokenID() throws Exception {
		AptRegexFormatParser.parseString("<ab");
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testBrokenID2() throws Exception {
		AptRegexFormatParser.parseString("<a<");
	}

	@Test(expectedExceptions = { LexerParserException.class })
	static void testBrokenID3() throws Exception {
		AptRegexFormatParser.parseString("<");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
