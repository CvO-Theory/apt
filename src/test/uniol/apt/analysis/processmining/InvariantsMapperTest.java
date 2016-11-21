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

package uniol.apt.analysis.processmining;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.ts.ParikhVector;

/** @author Uli Schlachter */
public class InvariantsMapperTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvariantNotOverAlphabet() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b"));
		Collection<List<String>> invariants = Arrays.asList(Arrays.asList("a", "a", "d"));
		new InvariantsMapper(alphabet, invariants);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testWordNotOverAlphabet() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b"));
		Collection<List<String>> invariants = Arrays.asList(
					Arrays.asList("a", "a", "b"));
		new InvariantsMapper(alphabet, invariants).transform(new ParikhVector("d"));
	}

	@Test
	public void testEmptyAlphabet() {
		Set<String> alphabet = Collections.emptySet();
		Collection<List<String>> invariants = Collections.emptySet();
		new InvariantsMapper(alphabet, invariants);
	}

	@Test
	public void testNoInvariants() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b"));
		Collection<List<String>> invariants = Collections.emptySet();
		InvariantsMapper mapper = new InvariantsMapper(alphabet, invariants);

		assertThat(mapper.transform(new ParikhVector()), contains(0, 0));
		assertThat(mapper.transform(new ParikhVector("a", "b", "a")), contains(2, 1));
		assertThat(mapper.transform(new ParikhVector("a", "b", "a", "b")), contains(2, 2));
	}

	@Test
	public void testAAInvariant() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b"));
		Collection<List<String>> invariants = Arrays.asList(Arrays.asList("a", "a"));
		InvariantsMapper mapper = new InvariantsMapper(alphabet, invariants);

		assertThat(mapper.transform(new ParikhVector()), contains(0));
		assertThat(mapper.transform(new ParikhVector("a", "b", "a")), contains(1));
		assertThat(mapper.transform(new ParikhVector("a", "b", "a", "b")), contains(2));
	}

	@Test
	public void testABCCInvariant() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b", "c"));
		Collection<List<String>> invariants = Arrays.asList(Arrays.asList("a", "b", "c", "c"));
		InvariantsMapper mapper = new InvariantsMapper(alphabet, invariants);

		// This contains sets of sets of words. Words in the same "inner set" are equivalent, other words are
		// not equivalent.
		Collection<List<List<String>>> words = Arrays.asList(
				Arrays.asList(
					Arrays.<String>asList(),
					Arrays.asList("a", "b", "c", "c")),
				Arrays.asList(
					Arrays.asList("a"),
					Arrays.asList("a", "b", "c", "c", "a")),
				Arrays.asList(
					Arrays.asList("b"),
					Arrays.asList("a", "b", "c", "c", "b")),
				Arrays.asList(
					Arrays.asList("c"),
					Arrays.asList("a", "b", "c", "c", "c")));
		testWords(mapper, words);
	}

	@Test
	public void testSomeExample() {
		Set<String> alphabet = new TreeSet<>(Arrays.asList("a", "b", "c", "d"));
		Collection<List<String>> invariants = Arrays.asList(
					Arrays.asList("b", "b", "c", "c", "c", "c", "d"),
					Arrays.asList("a", "a", "d"),
					Arrays.asList("a", "b", "c", "c", "d"));
		InvariantsMapper mapper = new InvariantsMapper(alphabet, invariants);

		// This contains sets of sets of words. Words in the same "inner set" are equivalent, other words are
		// not equivalent.
		Collection<List<List<String>>> words = Arrays.asList(
				Arrays.asList(
					Arrays.<String>asList(),
					Arrays.asList("c", "c", "a", "d", "b"),
					Arrays.asList("c", "b", "c", "c", "c", "d", "b"),
					Arrays.asList("b", "c", "c", "d", "a", "b", "c", "a", "c", "d", "a", "d", "b", "c", "c"),
					Arrays.asList("c", "a", "c", "a", "d", "a", "d", "b"),
					Arrays.asList("c", "a", "a", "d", "c", "a", "d", "b")),
				Arrays.asList(
					Arrays.asList("a"),
					Arrays.asList("a", "c", "c", "a", "d", "b"),
					Arrays.asList("a", "c", "b", "c", "c", "c", "d", "b"),
					Arrays.asList("a", "b", "c", "c", "d", "a", "b", "c", "a", "c", "d", "a", "d", "b", "c", "c"),
					Arrays.asList("a", "c", "a", "c", "a", "d", "a", "d", "b"),
					Arrays.asList("a", "c", "a", "a", "d", "c", "a", "d", "b")),
				Arrays.asList(
					Arrays.asList("b"),
					Arrays.asList("b", "c", "c", "a", "d", "b"),
					Arrays.asList("b", "c", "b", "c", "c", "c", "d", "b"),
					Arrays.asList("b", "b", "c", "c", "d", "a", "b", "c", "a", "c", "d", "a", "d", "b", "c", "c"),
					Arrays.asList("b", "c", "a", "c", "a", "d", "a", "d", "b"),
					Arrays.asList("b", "c", "a", "a", "d", "c", "a", "d", "b")),
				Arrays.asList(
					Arrays.asList("a", "b"),
					Arrays.asList("a", "b", "c", "c", "a", "d", "b"),
					Arrays.asList("a", "b", "c", "b", "c", "c", "c", "d", "b"),
					Arrays.asList("a", "b", "b", "c", "c", "d", "a", "b", "c", "a", "c", "d", "a", "d", "b", "c", "c"),
					Arrays.asList("a", "b", "c", "a", "c", "a", "d", "a", "d", "b"),
					Arrays.asList("a", "b", "c", "a", "a", "d", "c", "a", "d", "b")));
		testWords(mapper, words);
	}

	static private void testWords(InvariantsMapper mapper, Collection<List<List<String>>> words) {
		for (Collection<List<String>> innerWords1 : words) {
			for (List<String> word1 : innerWords1) {
				// Test that words in the same class are equivalent
				Object o1 = mapper.transform(new ParikhVector(word1));
				for (List<String> word2 : innerWords1) {
					Object o2 = mapper.transform(new ParikhVector(word2));
					assertThat(word1 + " vs " + word2, o2, equalTo(o1));
				}
				// Test that words in different classes are not equivalent
				for (Collection<List<String>> innerWords2 : words) {
					if (innerWords1 == innerWords2)
						continue;
					for (List<String> word2 : innerWords2) {
						Object o2 = mapper.transform(new ParikhVector(word2));
						assertThat(word1 + " vs " + word2, o2, not(equalTo(o1)));
					}
				}
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
