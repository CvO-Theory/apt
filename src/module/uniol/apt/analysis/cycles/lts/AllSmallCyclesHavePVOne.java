/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.reversible.ReversibleTS;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.util.Pair;

/**
 * A special-case implementation for checking if all small cycles of a transition system have Parikh vector all one.
 * Compared to {@link ComputeSmallestCycles} this only checks for all-one Parikh vectors and has several preconditions:
 * Deterministic, totally reachable, reversible, persistent, backwards persistent.
 * @author Uli Schlachter
 */
public class AllSmallCyclesHavePVOne {
	private final List<Arc> counterExample;
	private final boolean cycleWithPV1Found;

	/**
	 * Check if each event occurs exactly once in each small cycle.
	 * @param ts The transition system to examine.
	 * @throws PreconditionFailedException if the given transition system is not reversible, deterministic, totally
	 * reachable, persistent or backward persistent.
	 */
	public AllSmallCyclesHavePVOne(TransitionSystem ts) throws PreconditionFailedException {
		// We require a deterministic TS
		if (!new Deterministic(ts).isDeterministic())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not deterministic");

		// We require a totally reachable TS
		if (!new TotallyReachable(ts).isTotallyReachable())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not totally reachable");

		if (ts.getAlphabet().isEmpty()) {
			// Special case: We have a totally reachable TS with an empty alphabet. This means that it only
			// consists of the initial state and there are no other state. The code below doesn't handle
			// this correctly (it expects to follow at least one edge), so this is needed.
			this.cycleWithPV1Found = true;
			this.counterExample = null;
			return;
		}

		// We also require a reversible TS
		if (!new ReversibleTS(ts).isReversible())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not reversible");

		// We require a persistent TS
		if (!new PersistentTS(ts).isPersistent())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not persistent");

		// We require a backward persistent TS
		if (!new PersistentTS(ts, true).isPersistent())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not backwards persistent");

		// By totally reachability and reversibility, every state is a home state, so s0 is a home state. By
		// corollary 4 of [1], deterministicity and persistency imply that for every cycle, there is a
		// Parikh-equivalent cycle around a home state. Thus, we are only looking at small cycles around the
		// initial state and can be sure to "see" every existing cycle. We will do this in two phases.
		// [1]: A decomposition theorem for finite persistent transition systems. Eike Best and Philippe
		// Darondeau. Acta Informatica (2009). DOI 10.1007/s00236-009-0095-6.

		// In phase one we check that (a) we find a cycle around the initial state in which every event occurs
		// exactly once, and (b) no smaller cycle exists around the initial state.
		Pair<Boolean, List<Arc>> result = checkPhase1(ts, ts.getInitialState(), new HashSet<String>(),
				new LinkedList<Arc>());
		if (result.getFirst() == false || result.getSecond() != null) {
			// Phase 1 failed
			this.cycleWithPV1Found = result.getFirst();
			this.counterExample = result.getSecond();
		} else {
			this.cycleWithPV1Found = result.getFirst();

			// Phase one succeeded. In Phase two we check if there are any small cycles with Parikh vectors
			// incomparable to the all-ones PV. An example for such a cycle would be a TS with a cycle (1,1)
			// and another cycle (0,2).
			this.counterExample = checkPhase2(ts, ts.getInitialState(), new HashBag<String>(),
					new LinkedList<Arc>(), new HashSet<State>());
		}
	}

	/**
	 * Recursive implementation of the depth-first search that looks for cycles with a Parikh vector of at most all
	 * ones.
	 * @param ts The transition system to examine
	 * @param state The next state that should be followed.
	 * @param firedEvents Set of events which were already fired on the path from the initial state.
	 * @param arcsFollowed List of arcs that were followed from the initial state to this state.
	 * @return A pair were the first element is true if a cycle with Parikh vector 1 was found and the second
	 * element is either null or a cycle with a smaller Parikh vector.
	 */
	static private Pair<Boolean, List<Arc>> checkPhase1(TransitionSystem ts, State state, Set<String> firedEvents,
			LinkedList<Arc> arcsFollowed) {
		boolean success = false;
		for (Arc arc : state.getPostsetEdges()) {
			if (firedEvents.contains(arc.getLabel()))
				continue;

			firedEvents.add(arc.getLabel());
			arcsFollowed.addLast(arc);

			State target = arc.getTarget();
			if (target.equals(ts.getInitialState())) {
				if (firedEvents.containsAll(ts.getAlphabet())) {
					// Found a suitable cycle!
					success = true;
				} else {
					// Found a counter-example
					return new Pair<Boolean, List<Arc>>(false, arcsFollowed);
				}
			} else {
				// Recurse to this new state
				Pair<Boolean, List<Arc>> result = checkPhase1(ts, target, firedEvents, arcsFollowed);
				if (result.getSecond() != null)
					return result;
				success = success || result.getFirst();
			}

			// Undo the modifications done above
			boolean r = firedEvents.remove(arc.getLabel());
			assert r == true;
			Arc last = arcsFollowed.removeLast();
			assert last == arc;
		}
		return new Pair<>(success, null);
	}

	/**
	 * Recursive implementation of the depth-first search that looks for cycles with a Parikh vector incomparable to
	 * (1, ..., 1).
	 * @param ts The transition system to examine
	 * @param state The next state that should be followed.
	 * @param firedEvents Bag of events which were already fired on the path from the initial state.
	 * @param arcsFollowed List of arcs that were followed from the initial state to this state.
	 * @param statesVisited Set of states already visited on the current path.
	 * @return Null if no incomparable cycle was found, else such a cycle.
	 */
	static private List<Arc> checkPhase2(TransitionSystem ts, State state, Bag<String> firedEvents,
			LinkedList<Arc> arcsFollowed, Set<State> statesVisited) {
		boolean newEntry = statesVisited.add(state);
		assert newEntry == true : "State " + state + " was not yet visited";

		for (Arc arc : state.getPostsetEdges()) {
			firedEvents.add(arc.getLabel());
			if (firedEvents.containsAll(ts.getAlphabet())) {
				// We fired each event at least once and so the PV would be larger than (1, ..., 1).
				firedEvents.remove(arc.getLabel(), 1);
				continue;
			}
			arcsFollowed.addLast(arc);

			State target = arc.getTarget();
			if (target.equals(ts.getInitialState())) {
				// Found a counter-example
				return arcsFollowed;
			} else {
				// Recurse to this new state
				List<Arc> result = checkPhase2(ts, target, firedEvents,
						arcsFollowed, statesVisited);
				if (result != null)
					return result;
			}

			// Undo the modifications done above
			boolean r = firedEvents.remove(arc.getLabel(), 1);
			assert r == true;
			Arc last = arcsFollowed.removeLast();
			assert last == arc;
		}

		boolean r = statesVisited.remove(state);
		assert r == true;
		return null;
	}

	/**
	 * Get the counter example that proves that smaller cycles than with Parikh vector 1 exists.
	 * @return Either an empty list or an ordered list of arcs that form a cycle around the initial state.
	 */
	public List<Arc> getCounterExample() {
		if (counterExample == null)
			return emptyList();
		return unmodifiableList(counterExample);
	}

	/**
	 * Check if a cycle with Parikh vector 1 was found.
	 * @return true if no such cycle was found.
	 */
	public boolean noPV1CycleFound() {
		return !cycleWithPV1Found;
	}

	/**
	 * Check if all small cycles have Parikh vector 1. This is equivalent to !noPV1CycleFound() &amp;&amp;
	 * getCounterExample().isEmpty().
	 * @return True if all small cycles have Parikh vector 1.
	 */
	public boolean smallCyclesHavePVOne() {
		return cycleWithPV1Found && counterExample == null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
