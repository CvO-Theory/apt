/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.analysis.cycles.lts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import uniol.apt.analysis.cycles.CycleCallback;
import uniol.apt.analysis.cycles.CycleSearch;

/**
 * This class computes smallest cycles and parikh vectors, checking if all smallest cycles having the same parikh
 * vector, or having the same or mutually disjoint parikh vectors.
 *
 * @author Chris, Manuel
 */
public class ComputeSmallestCycles {
	private CycleCounterExample counterExample; // Stored countercycles

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * @param ts   - the transitionsystem to compute the cycles from.
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Cycle> computePVsOfSmallestCycles(TransitionSystem ts) {
		return computePVsOfSmallestCycles(ts, true);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * @param ts   - the transition system to examine.
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts) {
		return checkSameOrMutallyDisjointPVs(ts, true);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * @param ts   - the transition system to examine.
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts) {
		return checkSamePVs(ts, true);
	}

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * @param ts       - the transitionsystem to compute the cycles from.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Cycle> computePVsOfSmallestCycles(TransitionSystem ts,
			final boolean smallest) {
		final Set<Cycle> cycles = new HashSet<>();
		new CycleSearch().searchCycles(ts, new CycleCallback<TransitionSystem, Arc, State>() {
			@Override
			public void cycleFound(List<State> nodes, List<Arc> edges) {
				List<String> edgeLabels = new ArrayList<>();
				for (Arc edge : edges) {
					edgeLabels.add(edge.getLabel());
				}
				ParikhVector pv = new ParikhVector(edgeLabels);
				if (smallest) {
					Iterator<Cycle> iter = cycles.iterator();
					while (iter.hasNext()) {
						Cycle cycle = iter.next();
						int comp = cycle.getParikhVector().compare(pv).asInt();
						if (comp < 0) {
							// cycle has a smaller Parikh vector
							return;
						}
						if (comp > 0) {
							// This vector is smaller than cycle.
							iter.remove();
						}
					}
				}
				cycles.add(new Cycle(nodes, pv));
			}
		});
		return cycles;
	}


	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * @param ts       - the transition system to examine.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts, boolean smallest) {
		return checkSameOrMutallyDisjointPVs(computePVsOfSmallestCycles(ts, smallest));
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * @param ts       - the transition system to examine.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts, boolean smallest) {
		return checkSamePVs(computePVsOfSmallestCycles(ts, smallest));
	}

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same or mutally disjoint
	 * parikh vectors.
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * @return true if the cycles have the same or mutally disjoint parikh vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(Collection<? extends CyclePV> cycles) {
		counterExample = null;
		for (CyclePV cycle1 : cycles) {
			for (CyclePV cycle2 : cycles) {
				if (cycle1 != cycle2) {
					if (!cycle1.getParikhVector().sameOrMutuallyDisjoint(cycle2.getParikhVector())) {
						counterExample = new CycleCounterExample(cycle1, cycle2);
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same parikh vector.
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * @return true if the cycles have the same parikh vectors.
	 */
	public boolean checkSamePVs(Collection<? extends CyclePV> cycles) {
		counterExample = null;
		for (CyclePV cycle1 : cycles) {
			for (CyclePV cycle2 : cycles) {
				if (cycle1 != cycle2) {
					if (!cycle1.getParikhVector().equals(cycle2.getParikhVector())) {
						counterExample = new CycleCounterExample(cycle1, cycle2);
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Function for returning a counter example (as requirements A8a, A8b, A10 state that two counter-examples
	 * should be found in case the condition of the parikh vectors is not met).
	 * @return counterexample.
	 */
	public CycleCounterExample getCounterExample() {
		return counterExample;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
