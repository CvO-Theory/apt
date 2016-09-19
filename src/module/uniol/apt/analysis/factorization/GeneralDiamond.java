/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

/**
 * Allows to check a LTS for the general diamond property.
 *
 * @author Jonas Prellberg
 */
public class GeneralDiamond {

	private GeneralDiamond() {
	}

	/**
	 * Shorthand for
	 * <code>{@link #checkGdiam(TransitionSystem, Set)}.isGdiam()</code>.
	 *
	 * @param ts
	 *                the TS to check
	 * @param labelSubset
	 *                the label set to check
	 * @return true, if the TS is a <code>labelSubset</code>-gdiam
	 */
	public static boolean isGdiam(TransitionSystem ts, Set<String> labelSubset) {
		return checkGdiam(ts, labelSubset).isGdiam();
	}

	/**
	 * Checks if the given <code>LTS = (S, →, T, s0)</code> is a
	 * <code>T'</code>-gdiam, i.e. if for each pair of labels
	 * <code>a ∈ labelSubset, b ∈ T\T'</code> the general diamond property
	 * holds.
	 *
	 * @param ts
	 *                the TS to check
	 * @param tPrime
	 *                the label set to check
	 * @return true, if the TS is a <code>labelSubset</code>-gdiam
	 */
	public static GeneralDiamondResult checkGdiam(TransitionSystem ts, Set<String> tPrime) {
		Set<String> notLabelSubset = new HashSet<>(ts.getAlphabet());
		notLabelSubset.removeAll(tPrime);
		for (String t : tPrime) {
			for (String u : notLabelSubset) {
				GeneralDiamondResult r = checkGdiam(ts, t, u);
				if (!r.isGdiam()) {
					return r;
				}
			}
		}
		return new GeneralDiamondResult();
	}

	/**
	 * Checks if for the given LTS and labels a and b the general diamond
	 * property holds. Shorthand for
	 * <code>{@link #checkGdiam(TransitionSystem, String, String)}.isGdiam()</code>
	 * .
	 *
	 * @param ts
	 *                the TS to check
	 * @param a
	 *                the first label
	 * @param b
	 *                the second label
	 * @return true, if the TS presents the general diamond property for a
	 *         and b
	 */
	public static boolean isGdiam(TransitionSystem ts, String a, String b) {
		return checkGdiam(ts, a, b).isGdiam();
	}

	/**
	 * Checks if for the given LTS and labels a and b the general diamond
	 * property holds.
	 *
	 * @param ts
	 *                the TS to check
	 * @param a
	 *                the first label
	 * @param b
	 *                the second label
	 * @return a an object containing the result and a counter-example if
	 *         the LTS is not a T'-gdiam
	 */
	public static GeneralDiamondResult checkGdiam(TransitionSystem ts, String a, String b) {
		for (State s : ts.getNodes()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Set<Pair<State, Boolean>> s1Set = getGenerallyReachable(s, a);
			Set<Pair<State, Boolean>> s2Set = getGenerallyReachable(s, b);
			for (Pair<State, Boolean> p1 : s1Set) {
				for (Pair<State, Boolean> p2 : s2Set) {
					Set<State> s1SetPrime = getReachableWithDirection(
						p1.getFirst(), b, p2.getSecond()
					);
					Set<State> s2SetPrime = getReachableWithDirection(
						p2.getFirst(), a, p1.getSecond()
					);
					if (Collections.disjoint(s1SetPrime, s2SetPrime)) {
						return new GeneralDiamondResult(
							s, a, b, p1.getSecond(), p2.getSecond()
						);
					}
				}
			}
		}
		return new GeneralDiamondResult();
	}

	/**
	 * Returns a set of pairs. Each pair contains a state that is directly
	 * (single step) reachable from s with the given label regardless of arc
	 * direction. The boolean signifies if the used arc was traveled in
	 * forward (true) or backward (false) direction.
	 *
	 * @param s
	 *                starting state
	 * @param label
	 *                allowed labeling for arcs
	 * @return set of pairs of state and arc direction to get to that state
	 */
	private static Set<Pair<State, Boolean>> getGenerallyReachable(State s, String label) {
		Set<Pair<State, Boolean>> result = new HashSet<>();
		for (State sPost : s.getPostsetNodesByLabel(label)) {
			result.add(new Pair<>(sPost, true));
		}
		for (State sPre : s.getPresetNodesByLabel(label)) {
			result.add(new Pair<>(sPre, false));
		}
		return result;
	}

	/**
	 * Returns a set of states that is directly (single step) reachable from
	 * s with the given label while using arcs in the specified direction.
	 *
	 * @param s
	 *                starting state
	 * @param label
	 *                allowed labeling for arcs
	 * @param forward
	 *                true, if arcs must be followed in proper direction.
	 *                false, if arcs must be followed in reverse direction
	 * @return set of reachable states
	 */
	private static Set<State> getReachableWithDirection(State s, String label, boolean forward) {
		if (forward) {
			return s.getPostsetNodesByLabel(label);
		} else {
			return s.getPresetNodesByLabel(label);
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
