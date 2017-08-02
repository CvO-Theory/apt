/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.adt.ts.State;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;

/** @author Uli Schlachter */
public class FactorisationSynthesizerTest {
	private final PNProperties properties = new PNProperties();

	@Test
	public void testNotFactorisable() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);
		assertThat(new FactorisationSynthesizer(null).createSynthesizer(utility, properties, false),
				nullValue());
	}

	@Test
	public void testNonDeterministic() throws Exception {
		// This fails already early: Factorisation throws a NonDeterministicException
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		RegionUtility utility = new RegionUtility(ts);
		FactorisationSynthesizer synthesizer = new FactorisationSynthesizer(null);
		Synthesizer result = synthesizer.createSynthesizer(utility, properties, true);

		assertThat(result.getSeparatingRegions(), empty());
		assertThat(result.getUnsolvableEventStateSeparationProblems().entrySet(), emptyIterable());
		assertThat(result.getUnsolvableStateSeparationProblems(),
				contains(containsInAnyOrder(ts.getNode("s1"), ts.getNode("s2"))));
	}

	@Test
	public void testNonBackwardsDeterministic() throws Exception {
		// This fails already early: Factorisation throws a NonDeterministicException
		TransitionSystem ts = TestTSCollection.getNonBackwardsDeterministicTS();
		RegionUtility utility = new RegionUtility(ts);
		FactorisationSynthesizer synthesizer = new FactorisationSynthesizer(null);
		Synthesizer result = synthesizer.createSynthesizer(utility, properties, true);

		assertThat(result.getSeparatingRegions(), empty());
		assertThat(result.getUnsolvableEventStateSeparationProblems().entrySet(), emptyIterable());
		assertThat(result.getUnsolvableStateSeparationProblems(),
				contains(containsInAnyOrder(ts.getNode("s1"), ts.getNode("s2"))));
	}

	class SuccessfulSynthesizerFactory implements FactorisationSynthesizer.SynthesizerFactory {
		private final TransitionSystem regionTS = TestTSCollection.getSingleStateTS();
		private final RegionUtility regionUtility = new RegionUtility(regionTS);
		private final Region r1 = new Region.Builder(regionUtility).withInitialMarking(BigInteger.valueOf(1));
		private final Region r2 = new Region.Builder(regionUtility).withInitialMarking(BigInteger.valueOf(2));
		private final Region r3 = new Region.Builder(regionUtility).withInitialMarking(BigInteger.valueOf(3));

		@Override
		public Synthesizer create(RegionUtility utility, PNProperties props, boolean onlyEventSeparation) {
			assertThat(props, sameInstance(properties));
			assertThat(onlyEventSeparation, is(true));
			assertThat(utility.getTransitionSystem().getNodes(), hasSize(2));
			assertThat(utility.getTransitionSystem().getNode("s0"), not(nullValue()));

			for (State state : utility.getTransitionSystem().getNodes()) {
				if ("l".equals(state.getId()))
					return mockSynthesizer(r1, r2);
				if ("r".equals(state.getId()))
					return mockSynthesizer(r3);
			}

			throw new RuntimeException("This should not be reachable");
		}

		private Synthesizer mockSynthesizer(Region... regions) {
			final List<Region> list = Arrays.asList(regions);
			return new Synthesizer() {
				@Override
				public Collection<Region> getSeparatingRegions() {
					return list;
				}

				@Override
				public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
					return Collections.emptyMap();
				}

				@Override
				public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
					return Collections.emptySet();
				}
			};
		}
	}

	@Test
	public void testSuccessful() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		RegionUtility utility = new RegionUtility(ts);
		SuccessfulSynthesizerFactory factory = new SuccessfulSynthesizerFactory();
		FactorisationSynthesizer synthesizer = new FactorisationSynthesizer(factory);

		Synthesizer result = synthesizer.createSynthesizer(utility, properties, true);

		assertThat(result.getUnsolvableEventStateSeparationProblems().entrySet(), emptyIterable());
		assertThat(result.getUnsolvableStateSeparationProblems(), emptyIterable());

		Region r1 = new Region.Builder(utility).withInitialMarking(BigInteger.valueOf(1));
		Region r2 = new Region.Builder(utility).withInitialMarking(BigInteger.valueOf(2));
		Region r3 = new Region.Builder(utility).withInitialMarking(BigInteger.valueOf(3));
		assertThat(result.getSeparatingRegions(), containsInAnyOrder(r1, r2, r3));
	}

	class FailingESSPSynthesizerFactory implements FactorisationSynthesizer.SynthesizerFactory {
		@Override
		public Synthesizer create(RegionUtility utility, PNProperties props, boolean onlyEventSeparation) {
			assertThat(props, sameInstance(properties));
			assertThat(onlyEventSeparation, is(false));
			assertThat(utility.getTransitionSystem().getNodes(), hasSize(2));
			assertThat(utility.getTransitionSystem().getNode("s0"), not(nullValue()));

			final State failingState = utility.getTransitionSystem().getNode("s0");

			return new Synthesizer() {
				@Override
				public Collection<Region> getSeparatingRegions() {
					return Collections.emptyList();
				}

				@Override
				public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
					Map<String, Set<State>> result = new HashMap<>();
					result.put("a", Collections.singleton(failingState));
					return result;
				}

				@Override
				public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
					return Collections.emptySet();
				}
			};
		}
	}

	@Test
	public void testESSPFailure() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		RegionUtility utility = new RegionUtility(ts);
		FailingESSPSynthesizerFactory factory = new FailingESSPSynthesizerFactory();
		FactorisationSynthesizer synthesizer = new FactorisationSynthesizer(factory);

		Synthesizer result = synthesizer.createSynthesizer(utility, properties, false);
		assertThat(result.getSeparatingRegions(), empty());
		assertThat(result.getUnsolvableStateSeparationProblems(), emptyIterable());
		assertThat(result.getUnsolvableEventStateSeparationProblems().entrySet(), hasSize(1));
		assertThat(result.getUnsolvableEventStateSeparationProblems(), hasEntry("a", Collections.singleton(ts.getNode("s0"))));
	}

	class FailingSSPSynthesizerFactory implements FactorisationSynthesizer.SynthesizerFactory {
		@Override
		public Synthesizer create(RegionUtility utility, PNProperties props, boolean onlyEventSeparation) {
			assertThat(props, sameInstance(properties));
			assertThat(onlyEventSeparation, is(false));
			assertThat(utility.getTransitionSystem().getNodes(), hasSize(2));
			assertThat(utility.getTransitionSystem().getNode("s0"), not(nullValue()));

			final Set<State> result = new HashSet<>(utility.getTransitionSystem().getNodes());

			return new Synthesizer() {
				@Override
				public Collection<Region> getSeparatingRegions() {
					return Collections.emptyList();
				}

				@Override
				public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
					return Collections.emptyMap();
				}

				@Override
				public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
					return Collections.singleton(result);
				}
			};
		}
	}

	@Test
	public void testSSPFailure() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		RegionUtility utility = new RegionUtility(ts);
		FailingSSPSynthesizerFactory factory = new FailingSSPSynthesizerFactory();
		FactorisationSynthesizer synthesizer = new FactorisationSynthesizer(factory);

		Synthesizer result = synthesizer.createSynthesizer(utility, properties, false);
		assertThat(result.getSeparatingRegions(), empty());
		assertThat(result.getUnsolvableEventStateSeparationProblems().entrySet(), emptyIterable());
		assertThat(result.getUnsolvableStateSeparationProblems(), contains(
					either(containsInAnyOrder(ts.getNode("s0"), ts.getNode("l"))).
					or(containsInAnyOrder(ts.getNode("s0"), ts.getNode("r")))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
