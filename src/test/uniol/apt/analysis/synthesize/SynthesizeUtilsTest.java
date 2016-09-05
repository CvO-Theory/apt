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

package uniol.apt.analysis.synthesize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.language.Word;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.*;

/** @author Uli Schlachter */
public class SynthesizeUtilsTest {
	@Test
	public void testFormatESSPFailureEmpty() {
		Word word = new Word(Arrays.asList("a", "b"));
		Map<String, Set<State>> failures = Collections.emptyMap();

		assertThat(formatESSPFailure(word, failures, false), nullValue());
		assertThat(formatESSPFailure(word, failures, true), nullValue());
	}

	@Test
	public void testFormatESSPFailureSingle() {
		Word word = new Word(Arrays.asList("a", "b"));
		TransitionSystem ts = makeTS(word);
		State s = ts.getInitialState();

		Map<String, Set<State>> failures = new HashMap<>();
		failures.put("b", Collections.singleton(s));

		assertThat(formatESSPFailure(word, failures, false), is("[b] a, b"));
		assertThat(formatESSPFailure(word, failures, true), is("[b]ab"));
	}

	@Test
	public void testFormatESSPFailureDouble() {
		Word word = new Word(Arrays.asList("a", "b"));
		TransitionSystem ts = makeTS(word);
		State s = ts.getInitialState();
		State sA = s.getPostsetNodes().iterator().next();
		State sAB = sA.getPostsetNodes().iterator().next();

		Map<String, Set<State>> failures = new HashMap<>();
		failures.put("a", Collections.singleton(sAB));
		failures.put("b", Collections.singleton(sA));
		failures.put("c", Collections.singleton(sA));

		assertThat(formatESSPFailure(word, failures, false), is("a, [b,c] b [a]"));
		assertThat(formatESSPFailure(word, failures, true), is("a[bc]b[a]"));
	}

	@Test
	public void testFormatSSPFailureEmpty() {
		Word word = new Word(Arrays.asList("a", "b"));
		Collection<Set<State>> failures = Collections.emptyList();

		assertThat(formatSSPFailure(word, failures), nullValue());
	}

	@Test
	public void testFormatSSPFailureSingle() {
		Word word = new Word(Arrays.asList("a", "b"));
		TransitionSystem ts = makeTS(word);
		State s = ts.getInitialState();
		State sA = s.getPostsetNodes().iterator().next();
		State sAB = sA.getPostsetNodes().iterator().next();

		Set<State> failure = new HashSet<>(Arrays.asList(sA, sAB));
		Collection<Set<State>> failures = Collections.singleton(failure);

		assertThat(formatSSPFailure(word, failures), is("a, 1 b 1"));
	}

	@Test
	public void testFormatSSPFailureDouble() {
		Word word = new Word(Arrays.asList("a", "b", "c", "d"));
		TransitionSystem ts = makeTS(word);
		State s = ts.getInitialState();
		State sA = s.getPostsetNodes().iterator().next();
		State sAB = sA.getPostsetNodes().iterator().next();
		State sABC = sAB.getPostsetNodes().iterator().next();
		State sABCD = sABC.getPostsetNodes().iterator().next();

		Set<State> failure1 = new HashSet<>(Arrays.asList(s, sA));
		Set<State> failure2 = new HashSet<>(Arrays.asList(sAB, sABCD));
		Collection<Set<State>> failures = Arrays.asList(failure1, failure2);

		assertThat(formatSSPFailure(word, failures), is("1 a, 1 b, 2 c, d 2"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
