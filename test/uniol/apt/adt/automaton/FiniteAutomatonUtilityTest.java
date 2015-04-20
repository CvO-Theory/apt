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

import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import org.testng.annotations.Test;
import uniol.apt.adt.SoftMap;
import uniol.apt.adt.exception.ArcExistsException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import uniol.tests.TestUtils;
import uniol.tests.TestUtils;
import uniol.tests.TestUtils;
import uniol.tests.TestUtils;

import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
@Test
public class FiniteAutomatonUtilityTest {
	private void noWordInLanguage(FiniteAutomaton aut) {
		assertThat(isWordInLanguage(aut, Arrays.asList("c")), is(false));
		assertThat(isWordInLanguage(aut, Arrays.asList("a", "b", "c")), is(false));
	}

	private void wordInLanguage(FiniteAutomaton aut, boolean result, String... word) {
		assertThat(isWordInLanguage(aut, Arrays.asList(word)), is(result));
	}

	@Test
	public void testGetEmptyLanguage() {
		FiniteAutomaton automaton = getEmptyLanguage();

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(false));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, false);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testGetAtomicLanguageEpsilon() {
		FiniteAutomaton automaton = getAtomicLanguage(Symbol.EPSILON);

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(true));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testGetAtomicLanguageA() {
		FiniteAutomaton automaton = getAtomicLanguage(new Symbol("a"));

		State state = automaton.getInitialState();
		assertThat(state.isFinalState(), is(false));
		assertThat(state.getDefinedSymbols(), contains(new Symbol("a")));
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), hasSize(1));

		state = state.getFollowingStates(new Symbol("a")).iterator().next();
		assertThat(state.isFinalState(), is(true));
		assertThat(state.getDefinedSymbols(), empty());
		assertThat(state.getFollowingStates(Symbol.EPSILON), empty());
		assertThat(state.getFollowingStates(new Symbol("a")), empty());
		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testUnion2() {
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(new Symbol("b")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "b");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
		wordInLanguage(automaton, false, "b", "b");
	}

	@Test
	public void testUnion1() {
		FiniteAutomaton automaton = union(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testConcatenate1() {
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(new Symbol("b")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, true, "a", "b");
		wordInLanguage(automaton, false, "a", "b", "c");
	}

	@Test
	public void testConcatenate2() {
		FiniteAutomaton automaton = concatenate(getAtomicLanguage(new Symbol("a")), getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testKleeneStar1() {
		FiniteAutomaton automaton = kleeneStar(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "a", "a");
		wordInLanguage(automaton, true, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testKleeneStar2() {
		FiniteAutomaton automaton = kleeneStar(getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testKleenePlus1() {
		FiniteAutomaton automaton = kleenePlus(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, false);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, true, "a", "a");
		wordInLanguage(automaton, true, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testKleenePlus2() {
		FiniteAutomaton automaton = kleenePlus(getAtomicLanguage(Symbol.EPSILON));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, false, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "a", "a");
		wordInLanguage(automaton, false, "a", "a", "a", "b", "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}

	@Test
	public void testOptional() {
		FiniteAutomaton automaton = optional(getAtomicLanguage(new Symbol("a")));

		wordInLanguage(automaton, true);
		wordInLanguage(automaton, true, "a");
		wordInLanguage(automaton, false, "a", "a");
		wordInLanguage(automaton, false, "c");
		wordInLanguage(automaton, false, "a", "b");
	}
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

