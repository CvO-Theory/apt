/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;

import uniol.apt.analysis.exception.PreconditionFailedException;

/** @author Uli Schlachter */
public class FindWordsTest {
	private static void testWords(PNProperties properties, SortedSet<Character> alphabet,
			final List<List<String>> solvableWords) throws PreconditionFailedException {
		final Collection<String> solvable = new ArrayList<>();
		final int[] currentLength = { 1 };
		FindWords.WordCallback wordCallback = new FindWords.WordCallback() {
			@Override
			public void call(List<Character> wordAsList, String wordAsString, SynthesizePN synthesize) {
				assertThat(wordAsList, hasSize(currentLength[0]));
				if (synthesize.wasSuccessfullySeparated())
					solvable.add(wordAsString);
			}
		};
		FindWords.LengthDoneCallback lengthDoneCallback = new FindWords.LengthDoneCallback() {
			@Override
			public void call(int length) {
				assertThat(length, equalTo(currentLength[0]));
				assertThat(length, lessThanOrEqualTo(solvableWords.size()));
				assertThat(solvable, containsInAnyOrder(solvableWords.get(length - 1).toArray()));
				solvable.clear();
				currentLength[0]++;
			}
		};

		FindWords.generateList(properties, alphabet, true, wordCallback, lengthDoneCallback);

		assertThat(currentLength[0], equalTo(solvableWords.size() + 1));
	}

	@Test
	public void testSafeABCWords() throws Exception {
		PNProperties properties = new PNProperties().requireSafe();
		SortedSet<Character> alphabet = new TreeSet<>(Arrays.asList('a', 'b', 'c'));
		List<List<String>> solvableWords = Arrays.asList(
				Arrays.asList("a"),
				Arrays.asList("ab"),
				Arrays.asList("aba", "abc"),
				Arrays.asList("abac", "abca", "abcb"),
				Arrays.asList("abaca", "abacb", "abcab", "abcac", "abcba"),
				Arrays.asList("abacba", "abcbab"),
				Arrays.asList("abacaba"),
				Arrays.<String>asList());
		testWords(properties, alphabet, solvableWords);
	}

	@Test
	public void testPlainPureSafeABCWords() throws Exception {
		PNProperties properties = new PNProperties().setPlain(true).setPure(true).requireSafe();
		SortedSet<Character> alphabet = new TreeSet<>(Arrays.asList('a', 'b', 'c'));
		List<List<String>> solvableWords = Arrays.asList(
				Arrays.asList("a"),
				Arrays.asList("ab"),
				Arrays.asList("aba", "abc"),
				Arrays.asList("abac", "abca", "abcb"),
				Arrays.asList("abaca", "abcab", "abcba"),
				Arrays.asList("abcbab"),
				Arrays.asList("abacaba"),
				Arrays.<String>asList());
		testWords(properties, alphabet, solvableWords);
	}

	static private class TestDoneException extends RuntimeException {
		public static final long serialVersionUID = 0;
	}

	@Test(expectedExceptions = TestDoneException.class)
	public void testMinimalUnsolvableWords() throws Exception {
		final int[] nextLength = { 1 };
		final List<String[]> words = Arrays.asList(
				new String[] {}, new String[] {}, new String[] {}, new String[] {}, new String[] {},
				new String[] { "abbaa" }, new String[] { "abbbaa" },
				new String[] { "abbbbaa", "abbbaba", "abbabaa", "ababaaa"}
				);
		final Set<String> unsolvableWords = new TreeSet<>();
		FindWords.WordCallback wordCallback = new FindWords.WordCallback() {
			@Override
			public void call(List<Character> wordAsList, String wordAsString, SynthesizePN synthesize) {
				if (!synthesize.wasSuccessfullySeparated())
					assertThat(unsolvableWords.add(wordAsString), is(true));
			}
		};
		FindWords.LengthDoneCallback lengthDoneCallback = new FindWords.LengthDoneCallback() {
			@Override
			public void call(int length) {
				assertThat(length, equalTo(nextLength[0]));
				assertThat(unsolvableWords, containsInAnyOrder(words.get(length)));
				unsolvableWords.clear();
				nextLength[0]++;

				if (nextLength[0] == words.size())
					throw new TestDoneException();
			}
		};

		PNProperties properties = new PNProperties();
		SortedSet<Character> alphabet = new TreeSet<>(Arrays.asList('a', 'b'));
		FindWords.generateList(properties, alphabet, true, wordCallback, lengthDoneCallback);
	}

	@Test(expectedExceptions = PreconditionFailedException.class)
	public void testUnsupportedProperties() throws Exception {
		PNProperties properties = new PNProperties().setPlain(true).requireKMarking(2);
		SortedSet<Character> alphabet = new TreeSet<>(Arrays.asList('a', 'b', 'c'));
		FindWords.generateList(properties, alphabet, true, null, null);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
