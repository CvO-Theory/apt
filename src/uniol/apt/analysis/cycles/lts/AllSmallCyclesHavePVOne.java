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

		// By the above preconditions, every state is a home state, and all small cycles either have the same or
		// have disjoint Parikh vectors. Let's check if all small cycles have Parikh vector 1. For this, we
		// check that (a) we find such a cycle (b) no smaller cycle exists around the initial state.
		Pair<Boolean, List<Arc>> result = check(ts, ts.getInitialState(), new HashSet<String>(),
				new LinkedList<Arc>());
		this.cycleWithPV1Found = result.getFirst();
		this.counterExample = result.getSecond();
	}

	/**
	 * Recursive implementation of the depth-first search that looks for cycles.
	 * @param ts The transition system to examine
	 * @param state The next state that should be followed.
	 * @param firedEvents Set of events which were already fired on the path from the initial state.
	 * @param arcsFollowed List of arcs that were followed from the initial state to this state.
	 * @return A pair were the first element is true if a cycle with Parikh vector 1 was found and the second
	 * element is either null or a cycle with a smaller Parikh vector.
	 */
	static private Pair<Boolean, List<Arc>> check(TransitionSystem ts, State state, Set<String> firedEvents,
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
				Pair<Boolean, List<Arc>> result = check(ts, target, firedEvents, arcsFollowed);
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
	 * Check if all small cycles have Parikh vector 1. This is equivalent to !noPV1CycleFound() &&
	 * getCounterExample().isEmpty().
	 * @return True if all small cycles have Parikh vector 1.
	 */
	public boolean smallCyclesHavePVOne() {
		return cycleWithPV1Found && counterExample == null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
