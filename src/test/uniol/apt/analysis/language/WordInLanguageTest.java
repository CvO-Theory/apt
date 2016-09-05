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

package uniol.apt.analysis.language;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matcher;

import uniol.apt.adt.INode;
import uniol.apt.adt.pn.Transition;

import static org.hamcrest.Matchers.*;
import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import java.util.Arrays;
import java.util.List;

/** @author Uli Schlachter */
public class WordInLanguageTest {
	private void checkExpectedSequence(List<Transition> sequence, String[][] expectedSequences) {
		Collection<Matcher<? super Iterable<? extends INode<?, ?, ?>>>> matchers
			= new ArrayList<>();
		for (String[] expected : expectedSequences) {
			List<Matcher<? super INode<?, ?, ?>>> match = new ArrayList<>();
			for (String id : expected) {
				match.add(nodeWithID(id));
			}
			matchers.add(contains(match));
		}

		assertThat(sequence, anyOf(matchers));
	}

	@Test
	public void testEmptyNetEmptyWord() {
		WordInLanguage test = new WordInLanguage(getEmptyNet());
		List<Transition> seq = test.checkWord(Arrays.<String>asList());
		assertNotNull(seq);
		assertEquals(seq.size(), 0);
	}

	@Test
	public void testEmptyNetWord() {
		WordInLanguage test = new WordInLanguage(getEmptyNet());
		List<Transition> seq = test.checkWord(Arrays.asList("a"));
		assertNull(seq);
	}

	@Test
	public void testEmptyWord() {
		WordInLanguage test = new WordInLanguage(getABCLanguageNet());
		List<Transition> seq = test.checkWord(Arrays.<String>asList());
		assertNotNull(seq);
		assertEquals(seq.size(), 0);
	}

	@Test
	public void testWordInLanguage() {
		String[][] expected = {
			{ "a", "c", "b", "c", "a" }
		};

		WordInLanguage test = new WordInLanguage(getNonPersistentNet());
		List<Transition> seq = test.checkWord(Arrays.asList("a", "c", "b", "c", "a"));
		assertNotNull(seq);
		checkExpectedSequence(seq, expected);
	}

	@Test
	public void testSinglePossibility() {
		String[][] expected = {
			{ "ta1", "ta1", "ta2", "tb1", "tb2", "tc" }
		};

		WordInLanguage test = new WordInLanguage(getABCLanguageNet());
		List<Transition> seq = test.checkWord(Arrays.asList("a", "a", "a", "b", "b", "c"));
		assertNotNull(seq);
		checkExpectedSequence(seq, expected);
	}

	@Test
	public void testMultiplePossibilities() {
		String[][] expected = {
			{ "ta1", "ta1", "ta2", "tb1", "tb1" },
			{ "ta1", "ta1", "ta2", "tb1", "tb2" },
		};

		WordInLanguage test = new WordInLanguage(getABCLanguageNet());
		List<Transition> seq = test.checkWord(Arrays.asList("a", "a", "a", "b", "b"));
		assertNotNull(seq);
		checkExpectedSequence(seq, expected);
	}

	@Test
	public void testWordNotInLanguage() {
		WordInLanguage test = new WordInLanguage(getABCLanguageNet());
		List<Transition> seq = test.checkWord(Arrays.asList("a", "a", "b", "b", "c", "a"));
		assertNull(seq);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
