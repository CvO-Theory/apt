/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
 * Copyright (C) 2017 Uli Schlachter, vsp
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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.util.Pair;
import uniol.apt.util.PowerSet;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * A class to test if a LTS is a product. It allows to compute the factors as
 * well.
 *
 * @author Jonas Prellberg
 *
 */
public class Factorization {

	private final TransitionSystem ts;

	private boolean hasFactors;
	private TransitionSystem factor1;
	private TransitionSystem factor2;

	/**
	 * Creates a new Factorization instance that examines the given TS. The
	 * factorization is performed during the constructor.
	 *
	 * @param ts
	 *                the transition system to factorize
	 */
	public Factorization(TransitionSystem ts) {
		this.ts = ts;
		this.hasFactors = factorize();
	}

	/**
	 * Returns the first factor, if any.
	 *
	 * @return the first factor or null
	 */
	public TransitionSystem getFactor1() {
		return factor1;
	}

	/**
	 * Returns the second factor, if any.
	 *
	 * @return the second factor or null
	 */
	public TransitionSystem getFactor2() {
		return factor2;
	}

	/**
	 * Returns true, if factors of the LTS were found.
	 *
	 * @return true, if factorization was successful
	 */
	public boolean hasFactors() {
		return hasFactors;
	}

	/**
	 * Tries to factorize the TS and returns if it was possible.
	 *
	 * @return true, if a factorization was found
	 */
	private boolean factorize() {
		for (Collection<String> tPrimeCandidate : PowerSet.powerSet(ts.getAlphabet())) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			// Skip trivial solutions.
			if (tPrimeCandidate.isEmpty() || tPrimeCandidate.size() == ts.getAlphabet().size()) {
				continue;
			}
			if (isGdiamAndSeparated(tPrimeCandidate)) {
				createFactors(tPrimeCandidate);
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates factors with the given set of labels. It is assumed that the
	 * set of labels (T') is a T'-separation of the TS and the TS is a
	 * T'-gdiam.
	 *
	 * @param tPrime
	 *                a set of labels
	 */
	private void createFactors(Collection<String> tPrime) {
		// t1 ∪ t2 = Σ
		Set<String> t1 = new HashSet<>(tPrime);
		Set<String> t2 = new HashSet<>(ts.getAlphabet());
		t2.removeAll(tPrime);

		factor1 = createFactor(ts, t1);
		factor2 = createFactor(ts, t2);
	}

	/**
	 * Creates and returns a single factor with the given label set.
	 *
	 * @param ts the transition system whose factor should be computed
	 * @param labels the label set for this factor
	 * @return the factor TS
	 */
	static TransitionSystem createFactor(TransitionSystem ts, Set<String> labels) {
		return new TSFactor(ts, labels).factor;
	}

	static private class TSFactor {
		private final TransitionSystem factor = new TransitionSystem();
		private final Deque<Pair<State, State>> queue = new ArrayDeque<>();
		private final Map<State, State> mapped = new HashMap<>();

		private TSFactor(TransitionSystem ts, Set<String> labels) {
			this.factor.setInitialState(addState(ts.getInitialState()));

			while (!queue.isEmpty()) {
				Pair<State, State> pair = queue.pop();
				State originalState = pair.getFirst();
				State newState = pair.getSecond();

				for (Arc a : originalState.getPostsetEdges()) {
					if (labels.contains(a.getLabel())) {
						Arc newArc = factor.createArc(newState, addState(a.getTarget()),
								a.getLabel());
						newArc.copyExtensions(a);
					}
				}
				for (Arc a : originalState.getPresetEdges()) {
					if (labels.contains(a.getLabel())) {
						// If this state is already known, do nothing. If this state is new, we
						// will eventually follow its arcs in forward direction.
						addState(a.getSource());
					}
				}
			}
		}

		private State addState(State originalState) {
			State newState = mapped.get(originalState);
			if (newState == null) {
				newState = factor.createState(originalState);
				mapped.put(originalState, newState);
				queue.push(new Pair<>(originalState, newState));
			}
			return newState;
		}
	}

	private boolean isGdiamAndSeparated(Collection<String> tPrime) {
		Set<String> set = new HashSet<>(tPrime);
		return GeneralDiamond.isGdiam(ts, set) && LabelSeparation.isSeparated(ts, set);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
