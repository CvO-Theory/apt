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
		isomorphism = new IsomorphismLogicComplex(lts1, lts2, checkLabels).getIsomorphism();
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
