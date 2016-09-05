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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.analysis.language.LanguageEquivalence;

/** @author Uli Schlachter */
public class LimitedUnfoldingTest {
	@Test
	public void testEmptyTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getSingleStateTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(1));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testcc1LTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getcc1LTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(7));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getThreeStatesTwoEdgesTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(3));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testPersistentTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getPersistentTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(5));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testNotTotallyReachableTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getNotTotallyReachableTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(2));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testImpureSynthesizablePathTS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getImpureSynthesizablePathTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(5));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testTwoBThreeATS() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getTwoBThreeATS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(7));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test(expectedExceptions = { NonDeterministicException.class })
	public void testABAndA() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getABandA();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		// These are the results that would be calculated *if* non-deterministic inputs were supported
		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(3));
		assertThat(new Deterministic(unfold).isDeterministic(), is(true));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem), is(nullValue()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testCopyArcExtensions() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getSingleStateTSWithLoop();
		originalSystem.getArc("s0", "s0", "a").putExtension("extension", "value");
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		State s0 = unfold.getInitialState();
		Object value = "value";
		assertThat(unfold.getArc(s0, s0, "a").getExtension("extension"), is(value));
	}

	@Test
	public void testCopyStateExtensions() throws Exception {
		TransitionSystem originalSystem = TestTSCollection.getSingleStateTSWithLoop();
		originalSystem.getInitialState().putExtension("extension", "value");
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		Object value = "value";
		assertThat(unfold.getInitialState().getExtension("extension"), is(value));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
