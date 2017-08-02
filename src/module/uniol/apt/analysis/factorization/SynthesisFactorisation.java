/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Jonas Prellberg, vsp
 * Copyright (C) 2017  Uli Schlachter, vsp
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

package uniol.apt.analysis.factorization;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.DomainEquivalenceRelation;
import uniol.apt.util.Pair;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * An optimized version of {@link Factorization} for the special-case of Petri net synthesis. This class does not
 * actually find proper factors. Instead, the following holds: If the input is PN-solvable, then this class finds proper
 * factors. If the input is not PN-solvable, then this class produces non-PN-solvable lts.
 * @author Jonas Prellberg, vsp, Uli Schlachter
 */
public class SynthesisFactorisation {
	private static abstract class IntermediateState {

		private State state;
		protected String label;

		protected IntermediateState(State state, String label) {
			this.state = state;
			this.label = label;
		}

		protected State getState() {
			return this.state;
		}

		protected String getLabel() {
			return this.label;
		}

		protected abstract State getSuccessorOfState(State s);
	}

	private static class IntermediateForwardState extends IntermediateState {
		protected IntermediateForwardState(State state, String label) {
			super(state, label);
		}

		protected State getSuccessorOfState(State s) {
			for (State state : s.getPostsetNodesByLabel(this.label))
				return state;
			return null;
		}
	}

	private static class IntermediateBackwardState extends IntermediateState {
		protected IntermediateBackwardState(State state, String label) {
			super(state, label);
		}

		protected State getSuccessorOfState(State s) {
			for (State state : s.getPresetNodesByLabel(this.label))
				return state;
			return null;
		}
	}

	public Set<TransitionSystem> factorize(TransitionSystem ts) throws NonDeterministicException {
		DomainEquivalenceRelation<String> eq = new DomainEquivalenceRelation<>(ts.getAlphabet());

		// Step 0: Check preconditions
		new Deterministic(ts, true).throwIfNonDeterministic();
		new Deterministic(ts, false).throwIfNonDeterministic();

		// Step 1: Use gdiam and local separation to construct an equivalence relation
		for (State s : ts.getNodes()) {
			for (Pair<IntermediateState, IntermediateState> arcPair : new DifferentPairsIterable<>(getArcsWithDirection(s))) {
				IntermediateState imState1 = arcPair.getFirst();
				IntermediateState imState2 = arcPair.getSecond();

				// Short-cut: If they are already equivalent, we do not have to check
				if (eq.isEquivalent(imState1.getLabel(), imState2.getLabel()))
					continue;

				// check local separation
				if (imState1.getState().equals(imState2.getState()) && !s.equals(imState1.getState())) {
					eq.joinClasses(imState1.getLabel(), imState2.getLabel());
				} else {
					// Local separation holds; next check gdiam
					State state1 = imState1.getSuccessorOfState(imState2.getState());
					State state2 = imState2.getSuccessorOfState(imState1.getState());
					if (state1 == null || !state1.equals(state2)) {
						eq.joinClasses(imState1.getLabel(), imState2.getLabel());
					}
				}
			}

			// Not factorizable?
			if (eq.size() == 1)
				return Collections.singleton(ts);
		}

		// Step 2: Create candidate factors
		Set<TransitionSystem> factors = new HashSet<>();

		for (Set<String> labelClass : eq) {
			factors.add(createFactor(ts, labelClass));
		}

		debugFormat("Found %d candidate factors", factors.size());
		return factors;
	}

	private static Set<IntermediateState> getArcsWithDirection(State s) {
		Set<IntermediateState> result = new HashSet<>();
		for (Arc arc : s.getPostsetEdges())
			result.add(new IntermediateForwardState(arc.getTarget(), arc.getLabel()));
		for (Arc arc : s.getPresetEdges())
			result.add(new IntermediateBackwardState(arc.getSource(), arc.getLabel()));
		return result;
	}

	private TransitionSystem createFactor(TransitionSystem ts, Set<String> labels) {
		return Factorization.createFactor(ts, labels);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
