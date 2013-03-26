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

import uniol.apt.adt.ts.ParikhVector;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.bisimulation.Pair;

/**
 * This class offers algorithms for computing smallest cycles and parikh vectors, checking if all smallest cycles having
 * the same parikh vector, or having the same or mutually disjoint parikh vectors.
 * <p/>
 * For the calculation of the parikh vectors and cycles exists the possibility to choose between two algorithms: an
 * adaption of the Floyd-Warshall algorithm for finding the shortest passes in a graph, and a method which uses the
 * depth first search to compute the smallest cycles.
 * <p/>
 * @author Chris, Manuel
 */
public class ComputeSmallestCycles {

	private CycleCounterExample counterExample; // Stored countercycles

	/**
	 * Enumeration for choosing which algorithm should be used.
	 */
	public enum Algorithm {

		DFS,
		FloydWarshall
	}

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * <p/>
	 * Uses the Floyd-Warshall algorithm.
	 * <p/>
	 * @param ts - the transitionsystem to compute the cycles from.
	 * <p/>
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts) {
		return computePVsOfSmallestCycles(ts, Algorithm.FloydWarshall);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * <p/>
	 * Uses the Floyd-Warshall algorithm.
	 * <p/>
	 * @param ts - the transition system to examine.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts) {
		return checkSameOrMutallyDisjointPVs(ts, Algorithm.FloydWarshall);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * <p/>
	 * Uses the Floyd-Warshall algorithm.
	 * <p/>
	 * @param ts - the transition system to examine.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts) {
		return checkSamePVs(ts, Algorithm.FloydWarshall);
	}

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * <p/>
	 * @param ts   - the transitionsystem to compute the cycles from.
	 * <p/>
	 * @param algo - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * <p/>
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, Algorithm algo) {
		return computePVsOfSmallestCycles(ts, algo, true);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * <p/>
	 * @param ts   - the transition system to examine.
	 * @param algo - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts, Algorithm algo) {
		return checkSameOrMutallyDisjointPVs(ts, algo, true);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * <p/>
	 * @param ts   - the transition system to examine.
	 * @param algo - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts, Algorithm algo) {
		return checkSamePVs(ts, algo, true);
	}

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * <p/>
	 * @param ts       - the transitionsystem to compute the cycles from.
	 * @param algo     - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * @param smallest - Just used if algo==DFS ! Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, Algorithm algo,
		boolean smallest) {
		if (algo == Algorithm.DFS) {
			ComputeSmallestCyclesDFS c = new ComputeSmallestCyclesDFS();
			return c.computePVsOfSmallestCycles(ts, smallest);
		} else {
			return ComputeSmallestCyclesFloydWarshall.calculate(ts);
		}
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * <p/>
	 * @param ts       - the transition system to examine.
	 * @param algo     - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * @param smallest - Just used if algo==DFS ! Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts, Algorithm algo, boolean smallest) {
		Set<Pair<List<String>, ParikhVector>> pvs;
		if (algo == Algorithm.DFS) {
			ComputeSmallestCyclesDFS c = new ComputeSmallestCyclesDFS();
			pvs = c.computePVsOfSmallestCycles(ts, smallest);
		} else {
			pvs = ComputeSmallestCyclesFloydWarshall.calculate(ts);
		}
		return checkSameOrMutallyDisjointPVs(pvs);
	}

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * <p/>
	 * @param ts       - the transition system to examine.
	 * @param algo     - the algorithm to use for computing the smallest cycles and their parikh vectors.
	 * @param smallest - Just used if algo==DFS ! Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts, Algorithm algo, boolean smallest) {
		Set<Pair<List<String>, ParikhVector>> pvs;
		if (algo == Algorithm.DFS) {
			ComputeSmallestCyclesDFS c = new ComputeSmallestCyclesDFS();
			pvs = c.computePVsOfSmallestCycles(ts, smallest);
		} else {
			pvs = ComputeSmallestCyclesFloydWarshall.calculate(ts);
		}
		return checkSamePVs(pvs);
	}

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same or mutally disjoint
	 * parikh vectors.
	 * <p/>
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * <p/>
	 * @return true if the cycles have the same or mutally disjoint parikh vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(Set<Pair<List<String>, ParikhVector>> cycles) {
		counterExample = null;
		for (Pair<List<String>, ParikhVector> pair : cycles) {
			for (Pair<List<String>, ParikhVector> pair1 : cycles) {
				if (pair1 != pair) {
					if (!pair.getSecond().sameOrMutuallyDisjoint(pair1.getSecond())) {
						counterExample = new CycleCounterExample(pair, pair1);
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same parikh vector.
	 * <p/>
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * <p/>
	 * @return true if the cycles have the same parikh vectors.
	 */
	public boolean checkSamePVs(Set<Pair<List<String>, ParikhVector>> cycles) {
		counterExample = null;
		for (Pair<List<String>, ParikhVector> pair : cycles) {
			for (Pair<List<String>, ParikhVector> pair1 : cycles) {
				if (pair1 != pair) {
					if (!pair.getSecond().equals(pair1.getSecond())) {
						counterExample = new CycleCounterExample(pair, pair1);
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
	 * <p/>
	 * @return counterexample.
	 */
	public CycleCounterExample getCounterExample() {
		return counterExample;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
