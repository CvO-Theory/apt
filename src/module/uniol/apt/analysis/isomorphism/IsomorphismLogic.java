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

package uniol.apt.analysis.isomorphism;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import static org.apache.commons.collections4.bidimap.UnmodifiableBidiMap.unmodifiableBidiMap;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.util.Pair;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Check if two transition systems are isomorphic. Optionally, it is possible to ignore edge labels or require them to
 * be the same in both parts.
 *
 * @author Uli Schlachter
 */
public class IsomorphismLogic {
	private final BidiMap<State, State> isomorphism;

	/**
	 * Constructor for testing if two labelled transition systems are isomorphic.
	 *
	 * @param lts1 The first LTS to test.
	 * @param lts2 The second LTS to test.
	 * @param checkLabels If true, "strong isomorphism" is tested and labels have to be identical between the LTS.
	 *                    Otherwise labels are ignored.
	 */
	public IsomorphismLogic(TransitionSystem lts1, TransitionSystem lts2, boolean checkLabels) {
		isomorphism = construct(lts1, lts2, checkLabels);
	}

	private static BidiMap<State, State> construct(TransitionSystem lts1, TransitionSystem lts2, boolean checkLabels) {
		// Check trivial case
		if (lts1.getNodes().size() != lts2.getNodes().size()) {
			return new DualHashBidiMap<>();
		}

		if (checkLabels) {
			boolean precond1 = checkPreconditions(lts1);
			boolean precond2 = checkPreconditions(lts2);
			if (precond1 != precond2)
				// Not isomorphic
				return new DualHashBidiMap<>();

			if (precond1 && precond2)
				// Both lts are totally reachable and deterministic. We can apply a special algorithm.
				return checkViaDepthSearch(lts1, lts2);
		}

		return new IsomorphismLogicComplex(lts1, lts2, checkLabels).getIsomorphism();
	}

	private static boolean checkPreconditions(TransitionSystem lts) {
		return new Deterministic(lts).isDeterministic() && new TotallyReachable(lts).isTotallyReachable();
	}

	private static BidiMap<State, State> checkViaDepthSearch(TransitionSystem lts1, TransitionSystem lts2) {
		BidiMap<State, State> result = new DualHashBidiMap<>();
		Set<String> alphabet = lts1.getAlphabet();
		if (!alphabet.equals(lts2.getAlphabet()))
			// Not isomorphic, there is an arc with a label not occurring in the other lts
			return result;

		Queue<Pair<State, State>> unhandled = new ArrayDeque<>();
		visit(result, unhandled, lts1.getInitialState(), lts2.getInitialState());

		while (!unhandled.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			Pair<State, State> pair = unhandled.remove();
			State state1 = pair.getFirst();
			State state2 = pair.getSecond();

			for (String label : alphabet) {
				State follow1 = follow(state1, label);
				State follow2 = follow(state2, label);

				if (!visit(result, unhandled, follow1, follow2))
					// Not isomorphic
					return new DualHashBidiMap<>();
			}
		}

		return result;
	}

	private static State follow(State state, String label) {
		Set<Arc> postset = state.getPostsetEdgesByLabel(label);
		assert postset.isEmpty() || postset.size() == 1 : "A deterministic LTS is not deterministic?";
		for (Arc arc : postset)
			return arc.getTarget();
		return null;
	}

	private static boolean visit(BidiMap<State, State> partialIsomorphism, Queue<Pair<State, State>> unhandled,
			State state1, State state2) {
		if (state1 == null && state2 == null)
			// Nothing to do
			return true;

		if (state1 == null || state2 == null)
			// Not isomorphic
			return false;

		State oldState1 = partialIsomorphism.getKey(state2);
		if (state1.equals(oldState1))
			// This mapping was already known
			return true;
		if (oldState1 != null)
			// We have a conflicting mapping!
			return false;

		State oldState2 = partialIsomorphism.put(state1, state2);
		if (oldState2 != null) {
			// If this assert fails, then state1 was already mapped to state2 before. However, we already
			// checked for this case above.
			assert !state2.equals(oldState2);

			// We have a conflicting mapping!
			return false;
		}

		unhandled.add(new Pair<State, State>(state1, state2));
		return true;
	}

	/**
	 * Returns result of algorithm.
	 * @return True if the inputs are isomorphic to each other
	 */
	public boolean isIsomorphic() {
		return !isomorphism.isEmpty();
	}

	public BidiMap<State, State> getIsomorphism() {
		return unmodifiableBidiMap(isomorphism);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
