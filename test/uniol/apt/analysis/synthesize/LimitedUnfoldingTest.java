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
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.analysis.language.LanguageEquivalence;

/** @author Uli Schlachter */
@Test
public class LimitedUnfoldingTest {
	@Test
	public void testEmptyTS() {
		TransitionSystem originalSystem = TestTSCollection.getSingleStateTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(1));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testcc1LTS() {
		TransitionSystem originalSystem = TestTSCollection.getcc1LTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(7));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		TransitionSystem originalSystem = TestTSCollection.getThreeStatesTwoEdgesTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(3));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testPersistentTS() {
		TransitionSystem originalSystem = TestTSCollection.getPersistentTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(5));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testNotTotallyReachableTS() {
		TransitionSystem originalSystem = TestTSCollection.getNotTotallyReachableTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(2));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}

	@Test
	public void testImpureSynthesizablePathTS() {
		TransitionSystem originalSystem = TestTSCollection.getImpureSynthesizablePathTS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(5));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(true));
	}

	@Test
	public void testTwoBThreeATS() {
		TransitionSystem originalSystem = TestTSCollection.getTwoBThreeATS();
		TransitionSystem unfold = LimitedUnfolding.calculateLimitedUnfolding(originalSystem);

		assertThat(unfold.getInitialState(), not(nullValue()));
		assertThat(unfold.getNodes(), hasSize(7));
		assertThat(LanguageEquivalence.checkLanguageEquivalence(unfold, originalSystem, false), is(empty()));
		assertThat(new IsomorphismLogic(unfold, originalSystem, true).isIsomorphic(), is(false));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
