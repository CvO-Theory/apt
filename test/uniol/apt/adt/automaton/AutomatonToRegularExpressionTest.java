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

package uniol.apt.adt.automaton;

import uniol.apt.TestTSCollection;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static uniol.apt.adt.automaton.AutomatonToRegularExpression.automatonToRegularExpression;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
import uniol.apt.io.parser.impl.apt.AptRegexFormatParser;
@Test
public class AutomatonToRegularExpressionTest {
	private FiniteAutomaton parse(String str) {
		try {
			return AptRegexFormatParser.parseString(str);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private FiniteAutomaton roundTrip(FiniteAutomaton aut) {
		return parse(automatonToRegularExpression(aut));
	}

	@Test
	public void testEmptyLanguage() {
		FiniteAutomaton aut = getEmptyLanguage();
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("~"));
	}

	@Test
	public void testAtomicLanguageEpsilon() {
		FiniteAutomaton aut = getAtomicLanguage(Symbol.EPSILON);
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$"));
	}

	@Test
	public void testAtomicLanguageA() {
		FiniteAutomaton aut = getAtomicLanguage(new Symbol("a"));
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("a"));
	}

	@Test
	public void testConcatenate() {
		FiniteAutomaton aut = concatenate(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(new Symbol("b")));
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("ab"));
	}

	@Test
	public void testKleeneStar() {
		FiniteAutomaton aut = kleeneStar(getAtomicLanguage(new Symbol("a")));
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a|a*"));
	}

	@Test
	public void testKleenePlus() {
		FiniteAutomaton aut = kleenePlus(getAtomicLanguage(new Symbol("a")));
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("a|aa*"));
	}

	@Test
	public void testOptional() {
		FiniteAutomaton aut = optional(getAtomicLanguage(new Symbol("a")));
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a"));
	}

	private FiniteAutomaton getTestDFA() {
		// Construct the minimal dfa for ((ab)^* | (ba)^* | (ab)^+)
		FiniteAutomaton ab = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));
		FiniteAutomaton ab2 = concatenate(getAtomicLanguage(new Symbol("a")),
				getAtomicLanguage(new Symbol("b")));
		FiniteAutomaton ba = concatenate(getAtomicLanguage(new Symbol("b")),
				getAtomicLanguage(new Symbol("a")));
		return union(union(kleeneStar(ab), kleeneStar(ba)), kleenePlus(ab2));
	}

	@Test
	public void testConstructDFA() {
		FiniteAutomaton aut = getTestDFA();
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(findWordDifference(roundTrip(aut), parse("(ab)*|(ba)*")), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|ab|ab(ab)*|ba|ba(ba)*"));
	}

	@Test
	public void testConstructNegation() {
		FiniteAutomaton aut = negate(constructDFA(getTestDFA()));

		// The regex is (ab)*|(ba)*|(ba)+. This is equivalent to (ab)*|(ba)*. The negation of this is (ab)*a for
		// words that end "in the middle" of the expression (similarly (ba)*b) and words which contain aa or bb.

		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(findWordDifference(roundTrip(aut), parse("(ab)*a|(ba)*b|(a|b)*(aa|bb)(a|b)*")), is(nullValue()));
		assertThat(automatonToRegularExpression(aut),
				is("(b|b(ab)*)(ab)*(b|aa|(b|aa)(a|b)*)|(a|a(ba)*)(ba)*(a|bb|(a|bb)(a|b)*)|b|b(ab)*|a|a(ba)*"));
	}

	@Test
	public void testConstructIntersection() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		DeterministicFiniteAutomaton dfa1 = constructDFA(kleeneStar(concatenate(a, concatenate(b, a))));
		DeterministicFiniteAutomaton dfa2 = constructDFA(kleeneStar(concatenate(a, optional(union(a, b)))));
		FiniteAutomaton aut = intersection(dfa1, dfa2);

		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|ab(aab)*a"));
	}

	@Test
	public void testConstructUnion() {
		FiniteAutomaton a = getAtomicLanguage(new Symbol("a"));
		FiniteAutomaton b = getAtomicLanguage(new Symbol("b"));
		FiniteAutomaton dfa1 = constructDFA(kleeneStar(concatenate(a, concatenate(b, a))));
		FiniteAutomaton dfa2 = constructDFA(kleeneStar(concatenate(a, optional(union(a, b)))));
		FiniteAutomaton aut = union(dfa1, dfa2);

		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(findWordDifference(roundTrip(aut), parse("(a|ab)*")), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a(a|ba)*b|a|a(a|ba)*"));
	}

	@Test
	public void testPersistentTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getPersistentTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a|b|ab|ba"));
	}

	@Test
	public void testNotTotallyReachableTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getNotTotallyReachableTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a"));
	}

	@Test
	public void testNonDeterministicTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getNonDeterministicTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a"));
	}

	@Test
	public void testSingleStateSingleTransitionTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getSingleStateSingleTransitionTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|<NotA>|<NotA>*"));
	}

	@Test
	public void testSingleStateWithUnreachableTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getSingleStateWithUnreachableTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$"));
	}

	@Test
	public void testPathTS() {
		FiniteAutomaton aut = fromPrefixLanguageLTS(
				TestTSCollection.getPathTS());
		assertThat(findWordDifference(aut, roundTrip(aut)), is(nullValue()));
		assertThat(automatonToRegularExpression(aut), is("$|a|ab|abc|abca|abcab*"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
