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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.factorization.SynthesisFactorisation;
import uniol.apt.analysis.synthesize.MissingLocationException;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;

/**
 * Helper used by {@link SeparationUtility} to handle factorisable inputs. If the given input can be factored, then this
 * class computed the individual factors, creates {@link Synthesizer} instances for each factor and combines the result
 * into a single {@link Synthesizer}.
 * @author Uli Schlachter
 */
class FactorisationSynthesizer {
	static public interface SynthesizerFactory {
		public Synthesizer create(RegionUtility utility, PNProperties properties,
				boolean onlyEventSeparation) throws MissingLocationException;
	}

	static public class DefaultSynthesizerFactory implements SynthesizerFactory {
		@Override
		public Synthesizer create(RegionUtility utility, PNProperties properties, boolean onlyEventSeparation)
				throws MissingLocationException {
			return SeparationUtility.createSynthesizerInstance(utility, properties, onlyEventSeparation,
					true, false);
		}
	}

	private final SynthesizerFactory factory;

	/**
	 * Default constructor. The created instance will use {@link DefaultSynthesizerFactory}.
	 */
	public FactorisationSynthesizer() {
		this(new DefaultSynthesizerFactory());
	}

	/**
	 * Constructor.
	 * @param factory Factory that is used for solving the computed factors.
	 */
	public FactorisationSynthesizer(SynthesizerFactory factory) {
		this.factory = factory;
	}

	/**
	 * Try to factorize the given input and create a {@link Synthesizer} for the result.
	 * @param utility This utility refers to the transition system that should be solved.
	 * @param properties The properties that the resulting Petri net should have. This is just passed on to the
	 * {@link SynthesizerFactory}.
	 * @param onlyEventSeparation Should only event separation be solved? This is just passed on to the {@link
	 * SynthesizerFactory}.
	 * @return null if the input is unsolvable, else a {@link Synthesizer} with at most one unsolvable separation
	 * problem.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	public Synthesizer createSynthesizer(RegionUtility utility, PNProperties properties,
			boolean onlyEventSeparation) throws MissingLocationException {
		Set<TransitionSystem> factors;
		try {
			factors = new SynthesisFactorisation().factorize(utility.getTransitionSystem());
		} catch (NonDeterministicException e) {
			// Definitely not PN-synthesisable; might be factorisable, who knows?
			return new NonDeterministicSynthesizer(e);
		}

		if (factors.size() <= 1)
			// Not factorisable
			return null;

		// Check if all factors are synthesizable
		Set<Region> regions = new HashSet<>();
		for (TransitionSystem factor : factors) {
			RegionUtility newUtility = new RegionUtility(factor);
			Synthesizer synt = factory.create(newUtility, properties, onlyEventSeparation);
			regions.addAll(mapRegions(synt.getSeparatingRegions(), utility));
			if (!synt.getUnsolvableEventStateSeparationProblems().isEmpty() ||
					!synt.getUnsolvableStateSeparationProblems().isEmpty()) {
				// Synthesis failed; since we are assuming quick-fail synthesis we can return a failure
				return new FailedFactorisationSynthesizer(regions, utility.getTransitionSystem(), synt);
			}
		}

		return new SuccessfulSynthesizer(regions);
	}

	static private Set<Region> mapRegions(Collection<Region> regions, RegionUtility utility) {
		Set<Region> result = new HashSet<>();
		for (Region region : regions)
			result.add(Region.Builder.copyRegionToUtility(utility, region));
		return result;
	}

	static private class SuccessfulSynthesizer implements Synthesizer {
		private final Set<Region> regions;

		private SuccessfulSynthesizer(Set<Region> regions) {
			this.regions = Collections.unmodifiableSet(regions);
		}

		@Override
		public Collection<Region> getSeparatingRegions() {
			return regions;
		}

		@Override
		public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
			return Collections.emptyMap();
		}

		@Override
		public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
			return Collections.emptySet();
		}
	}

	static private class FailedFactorisationSynthesizer implements Synthesizer {
		private final Set<Region> regions;
		private final Map<String, Set<State>> unsolvableESSP;
		private final Collection<Set<State>> unsolvableSSP;

		private FailedFactorisationSynthesizer(Set<Region> regions, TransitionSystem ts, Synthesizer failedSynthesizer) {
			this.regions = Collections.unmodifiableSet(regions);
			assert !failedSynthesizer.getUnsolvableEventStateSeparationProblems().isEmpty() ||
				!failedSynthesizer.getUnsolvableStateSeparationProblems().isEmpty();

			Map<String, Set<State>> map = failedSynthesizer.getUnsolvableEventStateSeparationProblems();
			if (!map.isEmpty()) {
				unsolvableSSP = Collections.emptySet();

				Map.Entry<String, Set<State>> entry = map.entrySet().iterator().next();
				State state = mapState(entry.getValue().iterator().next(), ts);

				Map<String, Set<State>> result = new HashMap<>();
				result.put(entry.getKey(), Collections.singleton(state));
				unsolvableESSP = Collections.unmodifiableMap(result);
			} else {
				unsolvableESSP = Collections.emptyMap();

				Set<State> result = new HashSet<>();
				for (State state : failedSynthesizer.getUnsolvableStateSeparationProblems().iterator().next())
					result.add(mapState(state, ts));
				unsolvableSSP = Collections.singleton(Collections.unmodifiableSet(result));
			}
		}

		static private State mapState(State state, TransitionSystem ts) {
			// We know that SynthesisFactorisation preserves identifiers and thus:
			return ts.getNode(state.getId());
		}

		@Override
		public Collection<Region> getSeparatingRegions() {
			return regions;
		}

		@Override
		public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
			return unsolvableESSP;
		}

		@Override
		public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
			return unsolvableSSP;
		}
	}

	static private class NonDeterministicSynthesizer implements Synthesizer {
		private final State state1;
		private final State state2;

		public NonDeterministicSynthesizer(NonDeterministicException e) {
			Set<State> set;
			if (e.isForwardsCounterExample())
				set = e.getNode().getPostsetNodesByLabel(e.getLabel());
			else
				set = e.getNode().getPresetNodesByLabel(e.getLabel());

			Iterator<State> it = set.iterator();

			assert it.hasNext();
			state1 = it.next();

			assert it.hasNext();
			state2 = it.next();

			assert !state1.equals(state2);
		}

		@Override
		public Collection<Region> getSeparatingRegions() {
			return Collections.emptySet();
		}

		@Override
		public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
			return Collections.emptyMap();
		}

		@Override
		public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
			Set<State> result = new HashSet<>();
			result.add(state1);
			result.add(state2);
			return Collections.singleton(result);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
