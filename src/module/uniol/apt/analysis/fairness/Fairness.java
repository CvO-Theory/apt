/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017       vsp
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

package uniol.apt.analysis.fairness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.Event;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.cycles.lts.Cycle;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;
import uniol.apt.util.SpanningTree;

/**
 * Check if a transition system is fair;
 * A transition system is fair if for every infinite firing sequence every infintely often k-activated event is fired
 * infinitely often.
 *
 * @author vsp
 */
public class Fairness {
	/**
	 * Check if a transition system is fair
	 * @param ts The transitions system to check.
	 * @return An instance of FairnessResult describing the result. This function never returns null.
	 */
	static public FairnessResult checkFairness(TransitionSystem ts) {
		return checkFairness(ts, 0);
	}

	/**
	 * Check if a transition system is k-fair
	 * @param ts The transitions system to check.
	 * @param k Break if a situation which is k-unfair is found.
	 * @return An instance of FairnessResult describing the result. This function never returns null.
	 */
	static public FairnessResult checkFairness(TransitionSystem ts, int k) {
		Fairness dfc = new Fairness(ts);
		for (Event e : ts.getAlphabetEvents()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			FairnessResult result = dfc.checkEvent(e, k);
			if (!result.isFair())
				return result;
		}
		return new FairnessResult(ts);
	}

	/**
	 * Check if a transition system is fair regarding a specific event
	 * @param ts The transition system to check.
	 * @param e The event to check
	 * @return An instance of FairnessResult describing the result. This function never returns null.
	 */
	static public FairnessResult checkFairness(TransitionSystem ts, Event e) {
		return checkFairness(ts, 0, e);
	}

	/**
	 * Check if a transition system is k-fair regarding a specific event
	 * @param ts The transition system to check.
	 * @param k Break if a situation which is k-unfair is found.
	 * @param e The event to check
	 * @return An instance of FairnessResult describing the result. This function never returns null.
	 */
	static public FairnessResult checkFairness(TransitionSystem ts, int k, Event e) {
		return new Fairness(ts).checkEvent(e, k);
	}

	private final TransitionSystem ts;
	private final Set<Cycle> cycles;

	/**
	 * Construct an instance to check fairness on a given {@link TransitionSystem}
	 *
	 * @param ts The transition system on which fairness should be checked.
	 */
	public Fairness(TransitionSystem ts) {
		this.ts = ts;

		// calculate the elementary cycles which are reachable from the initial state
		Set<State> unreachable = SpanningTree.get(ts, ts.getInitialState()).getUnreachableNodes();
		this.cycles = new HashSet<>();
		for (Cycle c : new ComputeSmallestCycles().computePVsOfSmallestCyclesViaCycleSearch(ts, false)) {
			if (!unreachable.contains(c.getNodes().iterator().next())) {
				// All states of a cycle are reachable iff any state on it is reachable
				this.cycles.add(c);
			}
		}
	}

	/**
	 * Check if a given event is k-unfair
	 *
	 * @param e The event to check
	 * @param k The value for k in k-unfairness
	 * @return An instance of FairnessResult describing the result. This function never returns null.
	 */
	public FairnessResult checkEvent(Event e, int k) {
		// Step 1: Calculate for each state how many events must at least get fired to enable the given event
		// how many events do we need to fire?
		Map<State, Integer> unfairness = new HashMap<>();
		// which arc do we need to fire next to finally enable the event?
		Map<State, Arc> successors = new HashMap<>();
		int d = 0;
		Set<State> prevDepthStates = new HashSet<>();
		for (Arc a : this.ts.getEdges()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (e.equals(a.getEvent())) {
				State src = a.getSource();
				unfairness.put(src, 0);
				prevDepthStates.add(src);
			}
		}
		while (!prevDepthStates.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			d++;
			Set<State> curDepthStates = new HashSet<>();
			for (State s : prevDepthStates) {
				for (Arc a : s.getPresetEdges()) {
					State src = a.getSource();
					if (unfairness.containsKey(src))
						continue;
					unfairness.put(src, d);
					successors.put(src, a);
					curDepthStates.add(src);
				}
			}
			prevDepthStates = curDepthStates;
		}

		// Step 2: Search every cycle which doesn't contain the given event for the smallest distance to
		// enabling the event
		State witness = null;
		int witnessDistance = Integer.MAX_VALUE;
		List<Arc> witnessCycle = null;
		int witnessPos = Integer.MAX_VALUE;
		for (Cycle c : this.cycles) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (c.getParikhVector().get(e.getLabel()) > 0)
				continue;
			int i = -1;
			for (State s : c.getNodes()) {
				i++;
				Integer distance = unfairness.get(s);
				if (distance == null)
					continue;
				if (witness == null || distance < witnessDistance) {
					witness         = s;
					witnessCycle    = new ArrayList<>(c.getArcs());
					witnessDistance = distance;
					witnessPos      = i;
				}
			}
			if (witnessDistance <= k)
				break;
		}

		if (witness == null)
			return new FairnessResult(this.ts);

		Collections.rotate(witnessCycle, -witnessPos);
		List<Arc> sequence = SpanningTree.get(this.ts, this.ts.getInitialState()).getEdgePathFromStart(witness);
		List<Arc> enabling = new ArrayList<>();

		for (Arc a = successors.get(witness); a != null; a = successors.get(a.getTarget())) {
			enabling.add(a);
		}

		return new FairnessResult(this.ts, witness, e, witnessDistance, sequence, witnessCycle, enabling);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
