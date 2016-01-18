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

import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * This is an interface for  algorithms for computing smallest cycles and parikh vectors, checking if all smallest
 * cycles having the same parikh vector, or having the same or mutually disjoint parikh vectors.
 * @author Chris, Manuel
 */
public interface ComputeSmallestCycles {
	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * <p/>
	 * @param ts   - the transitionsystem to compute the cycles from.
	 * <p/>
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts);

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * <p/>
	 * @param ts   - the transition system to examine.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts);

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * <p/>
	 * @param ts   - the transition system to examine.
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts);

	/**
	 * Computes the parikh vectors of all smallest cycles of an labeled transition system. (Requirement A10)
	 * <p/>
	 * @param ts       - the transitionsystem to compute the cycles from.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return a list of the smallest cycles and their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, boolean smallest);

	/**
	 * Checks a labeled transition system if all smallest cycles have the same or mutally disjoint parikh vectors.
	 * (Requirement A8b)
	 * <p/>
	 * @param ts       - the transition system to examine.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same or mutally disjoint parikh
	 *         vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts, boolean smallest);

	/**
	 * Checks a labeled transition system if all smallest cycles have the same parikh vector. (Requirement A8a)
	 * <p/>
	 * @param ts       - the transition system to examine.
	 * @param smallest - Flag which tells if all or just the smallest should be saved.
	 *                 (Storage vs. Time)
	 * <p/>
	 * @return true if the smallest cycles of the given transitionsystem have the same parikh vectors.
	 */
	public boolean checkSamePVs(TransitionSystem ts, boolean smallest);

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same or mutally disjoint
	 * parikh vectors.
	 * <p/>
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * <p/>
	 * @return true if the cycles have the same or mutally disjoint parikh vectors.
	 */
	public boolean checkSameOrMutallyDisjointPVs(Set<Pair<List<String>, ParikhVector>> cycles);

	/**
	 * Checks whether in the given set of cycles and parikh vectors all cycles have the same parikh vector.
	 * <p/>
	 * @param cycles - a list of cycles and parikh vectors which should be examined.
	 * <p/>
	 * @return true if the cycles have the same parikh vectors.
	 */
	public boolean checkSamePVs(Set<Pair<List<String>, ParikhVector>> cycles);

	/**
	 * Function for returning a counter example (as requirements A8a, A8b, A10 state that two counter-examples
	 * should be found in case the condition of the parikh vectors is not met).
	 * <p/>
	 * @return counterexample.
	 */
	public CycleCounterExample getCounterExample();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
