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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uniol.apt.analysis.synthesize.Matchers.*;
import uniol.apt.analysis.synthesize.Matchers;

/** @author Uli Schlachter */
public class FindWordsTest {
	private static void testWords(PNProperties properties, SortedSet<String> alphabet,
			final List<List<String>> solvableWords) {
		final Collection<String> solvable = new ArrayList<>();
		final int[] currentLength = { 1 };
		FindWords.WordCallback wordCallback = new FindWords.WordCallback() {
			@Override
			public void call(List<String> wordAsList, String wordAsString, SynthesizePN synthesize) {
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
	public void testSafeABCWords() {
		PNProperties properties = new PNProperties().requireSafe();
		SortedSet<String> alphabet = new TreeSet<>(Arrays.asList("a", "b", "c"));
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
	public void testPlainPureSafeABCWords() {
		PNProperties properties = new PNProperties().setPlain(true).setPure(true).requireSafe();
		SortedSet<String> alphabet = new TreeSet<>(Arrays.asList("a", "b", "c"));
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

	private class TestDoneException extends RuntimeException {
		public static final long serialVersionUID = 0;
	}

	@Test(expectedExceptions = TestDoneException.class)
	public void testMinimalUnsolvableWords() {
		final int[] nextWord = { 0 };
		final List<String> words = Arrays.asList("abbaa", "abbbaa", "abbbbaa", "abbbaba", "abbabaa", "ababaaa");
		FindWords.LengthDoneCallback lengthDoneCallback = mock(FindWords.LengthDoneCallback.class);
		FindWords.WordCallback wordCallback = new FindWords.WordCallback() {
			@Override
			public void call(List<String> wordAsList, String wordAsString, SynthesizePN synthesize) {
				if (synthesize.wasSuccessfullySeparated())
					return;
				assertThat(wordAsString, equalTo(words.get(nextWord[0])));
				nextWord[0]++;
				if (nextWord[0] == words.size())
					throw new TestDoneException();
			}
		};

		PNProperties properties = new PNProperties();
		SortedSet<String> alphabet = new TreeSet<>(Arrays.asList("a", "b"));
		FindWords.generateList(properties, alphabet, true, wordCallback, lengthDoneCallback);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
