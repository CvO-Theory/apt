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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Provides methods to check if a given LTS is T'-separated for some label set
 * T'.
 *
 * @author Jonas Prellberg
 */
public class LabelSeparation {

	private LabelSeparation() {
	}

	/**
	 * Shorthand for
	 * <code>{@link #checkSeparated(TransitionSystem, Set)}.isSeparated()</code>
	 * .
	 *
	 * @param ts
	 *                the LTS to check
	 * @param tPrime
	 *                a set of labels T'
	 * @return true, if the LTS is T'-separated
	 */
	public static boolean isSeparated(TransitionSystem ts, Set<String> tPrime) {
		return checkSeparated(ts, tPrime).isSeparated();
	}

	/**
	 * Checks if the LTS is T'-separated.
	 *
	 * The LTS = (S, →, T, s0) is T'-separated if for all states s1 ≠ s2 it
	 * is not possible to go from s1 to s2 while using only labels and
	 * reverse labels from T' as well as only using labels and reverse
	 * labels outside of T'.
	 *
	 * @param ts
	 *                the LTS to check
	 * @param tPrime
	 *                a set of labels T'
	 * @return a LabelSeparationResult with witnesses if the check failed
	 */
	public static LabelSeparationResult checkSeparated(TransitionSystem ts, Set<String> tPrime) {
		/*
		 * Idea: Build two new LTS with (1) all arcs with labels from T'
		 * removed and (2) all arcs with labels not from T' removed.
		 *
		 * Then if s1 and s2 are in the same weakly connected component,
		 * there is a path (ignoring arc direction) from s1 to s2.
		 */
		TransitionSystem ts1 = TransitionSystemFilter.removeArcsByLabel(new TransitionSystem(ts), tPrime);
		TransitionSystem ts2 = TransitionSystemFilter.retainArcsByLabel(new TransitionSystem(ts), tPrime);

		Set<? extends Set<State>> wcc1 = Connectivity.getWeaklyConnectedComponents(ts1);
		Set<? extends Set<State>> wcc2 = Connectivity.getWeaklyConnectedComponents(ts2);

		Set<Set<String>> wcc1Ids = componentStateSetToIdSet(wcc1);
		Set<Set<String>> wcc2Ids = componentStateSetToIdSet(wcc2);

		// Iterate over pairs of component ids
		for (Set<String> ids1 : wcc1Ids) {
			for (Set<String> ids2 : wcc2Ids) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				// Compute intersection of two components' ids.
				Set<String> intersection = new HashSet<>(ids1);
				intersection.retainAll(ids2);
				/*
				 * If this intersection contains two (or more)
				 * states s1 and s2 the TS is not T'-separated:
				 *
				 * (1) It exists an undirected path between s1
				 * and s2 using only labels from T' because both
				 * states are in ids2.
				 *
				 * (2) It exists an undirected path between s1
				 * and s2 using only labels from T\T' because
				 * both states are in ids1.
				 */
				if (intersection.size() > 1) {
					Iterator<String> iter = intersection.iterator();
					State s1 = ts.getNode(iter.next());
					State s2 = ts.getNode(iter.next());
					return new LabelSeparationResult(s1, s2);
				}
			}
		}

		return new LabelSeparationResult();
	}

	/**
	 * Transforms a set of set of states (as is the result of a
	 * {@link Connectivity#getWeaklyConnectedComponents} call) to a set of
	 * set of ids. This allows for easier contains-checks.
	 *
	 * @param components
	 *                the component results
	 * @return set of set of state ids
	 */
	private static Set<Set<String>> componentStateSetToIdSet(Set<? extends Set<State>> components) {
		Set<Set<String>> result = new HashSet<>();
		for (Set<State> component : components) {
			Set<String> componentIds = new HashSet<>();
			for (State state : component) {
				componentIds.add(state.getId());
			}
			result.add(componentIds);
		}
		return result;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
