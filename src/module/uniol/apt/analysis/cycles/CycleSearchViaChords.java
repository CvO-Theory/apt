/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017 Uli Schlachter
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

package uniol.apt.analysis.cycles;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.util.SpanningTree;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Compute the Parikh vectors of the smallest cycles of a totally reachable, deterministic, and persistent LTS with
 * disjoint small cycles.
 * With these preconditions, we can pick an arbitrary home state (there always must be one) and compute a spanning tree
 * starting in this state. The Parikh vectors of its chords which are not the zero vector, are exactly the Parikh
 * vectors of small cycles.
 * @author Uli Schlachter
 */
public class CycleSearchViaChords {
	public Set<ParikhVector> searchCycles(TransitionSystem ts) throws PreconditionFailedException {
		if (!new Deterministic(ts).isDeterministic())
			throw new NonDeterministicException(ts);
		if (!new TotallyReachable(ts).isTotallyReachable())
			throw new PreconditionFailedException("Transition system " + ts.getName() +
					" is not totally reachable, only totally reachable inputs are allowed");
		if (!new PersistentTS(ts).isPersistent())
			throw new PreconditionFailedException("Transition system " + ts.getName() +
					" is not persistent, only persistent inputs are allowed");

		return findCyclesAround(findHomeState(ts));
	}

	private State findHomeState(TransitionSystem ts) {
		// TODO: This is partial reimplementation of Tarjan's algorithm /
		// Connectivity#getStronglyConnectedComponents() amended to find the final SSC
		Map<State, Integer> dfsNumbers = new HashMap<>();
		Map<State, Integer> minNumbers = new HashMap<>();
		Deque<State> callers = new ArrayDeque<>();
		Set<State> stackAsSet = new HashSet<>();
		int counter = 0;

		State node = ts.getInitialState();
		counter = visitNode(node, dfsNumbers, minNumbers, counter, stackAsSet);
		while (true) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			boolean done = true;
			for (State current : node.getPostsetNodes()) {
				if (!dfsNumbers.containsKey(current)) {
					// 'current' was not visited yet
					callers.addLast(node);
					node = current;

					counter = visitNode(node, dfsNumbers, minNumbers, counter, stackAsSet);
					done = false;
					break;
				} else if (stackAsSet.contains(current)
						&& minNumbers.get(node) > minNumbers.get(current)) {
					// Set our own minNumbers to current's depth search number if it is smaller
					minNumbers.put(node, Math.min(minNumbers.get(node), dfsNumbers.get(current)));
				}
			}

			if (done) {
				if (dfsNumbers.get(node).equals(minNumbers.get(node))) {
					// We are the root of the first component found. Since all components reachable
					// from a given component a found before said component is found, the first
					// component must be the final SSC. Thus, we can just return our current state.
					return node;
				}

				State next = callers.removeLast();
				// Set our own minNumber to current's number if that one is smaller
				minNumbers.put(next, Math.min(minNumbers.get(next), minNumbers.get(node)));
				node = next;
			}
		}
	}

	private static int visitNode(State node, Map<State, Integer> dfsNumbers,
			Map<State, Integer> minNumbers, int counter, Set<State> stackAsSet) {
		// Node should not have been visited before.
		assert !dfsNumbers.containsKey(node);
		assert !minNumbers.containsKey(node);

		counter++;
		dfsNumbers.put(node, counter);
		minNumbers.put(node, counter);
		boolean added = stackAsSet.add(node);
		assert added;

		return counter;
	}

	private Set<ParikhVector> findCyclesAround(State homeState) throws PreconditionFailedException {
		SpanningTree<TransitionSystem, Arc, State> tree = SpanningTree.get(homeState.getGraph(), homeState);

		Set<ParikhVector> cyclesSeen = new HashSet<>();

		for (Arc chord : tree.getChords()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			ParikhVector pv = getPV(tree, chord);
			cyclesSeen.add(pv);
		}

		// Remove the empty Parikh vector, in case it was found
		cyclesSeen.remove(new ParikhVector());

		return cyclesSeen;
	}

	// Calculate PV(chord.getSource()) + PV(chord.getLabel()) - PV(chord.getTarget()) where the Parikh vector of a
	// state is the Parikh vector of its reaching path according to the spanning tree.
	private ParikhVector getPV(SpanningTree<TransitionSystem, Arc, State> tree, Arc chord)
			throws PreconditionFailedException {
		ParikhVector result = new ParikhVector(chord.getLabel());
		for (Arc arc : tree.getEdgePathFromStart(chord.getSource()))
			result = result.add(arc.getLabel());
		for (Arc arc : tree.getEdgePathFromStart(chord.getTarget())) {
			result = result.tryRemove(arc.getLabel(), 1);
			if (result == null)
				throw new PreconditionFailedException(
						"The given ts does not have the disjoint small cycle property");
		}
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
