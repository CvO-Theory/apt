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
import uniol.apt.analysis.exception.NonDisjointCyclesException;
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
		new Deterministic(ts).throwIfNonDeterministic();
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
		State commonAncestor = findCommonAncestor(tree, chord.getSource(), chord.getTarget());
		ParikhVector pv1 = getPV(tree, chord.getSource(), commonAncestor).add(chord.getLabel());
		ParikhVector pv2 = getPV(tree, chord.getTarget(), commonAncestor);
		// Check that pv1 >= pv2, i.e. no negative entries would occur in pv1 - pv2
		ParikhVector.Comparison comp = pv1.compare(pv2);
		if (!comp.equals(ParikhVector.Comparison.GREATER_THAN) &&
				!comp.equals(ParikhVector.Comparison.EQUAL)) {
			// There must be at least one x so that pv1(x) < pv2(x). Thus, pv2-pv1 is non-empty.
			// Also, our spanning tree computes shortest paths. Thus, we have |pv1-t| <= |pv2|.
			// Thus, since there is some event that occurs more often in pv2 as in pv1, there must be
			// another event that occurs less often in pv2 as in pv1 and pv1-pv2 is non-empty.
			// This motivates the following assert:
			assert comp.equals(ParikhVector.Comparison.INCOMPARABLE);
			// By definition of the residual, these two residuals have disjoint support. We can now use
			// Keller's theorem to find a state s reached from chord.getTarget() via both residuals. We
			// complete this into a cycle by finding any path back to chord.getTarget().
			// TODO: Is such a cycle necessarily small? I don't know, but some hint at the correct
			// counter-example is better than no counter-example at all.
			ParikhVector residual1 = pv1.residual(pv2);
			ParikhVector residual2 = pv2.residual(pv1);
			State residualsTarget = followPV(chord.getTarget(), residual1);
			ParikhVector restOfCycle = findPath(residualsTarget, chord.getTarget());
			throw new NonDisjointCyclesException(chord.getGraph(),
					residual1.add(restOfCycle),
					residual2.add(restOfCycle));
		}
		return pv1.residual(pv2);
	}

	// Find the last common ancestor of the given states in the spanning tree
	private State findCommonAncestor(SpanningTree<TransitionSystem, Arc, State> tree, State state1, State state2) {
		Set<State> predecessors1 = new HashSet<>();
		Set<State> predecessors2 = new HashSet<>();
		predecessors1.add(state1);
		predecessors2.add(state2);

		while (true) {
			if (state1 != null) {
				state1 = tree.getPredecessor(state1);
				if (predecessors2.contains(state1))
					return state1;
				predecessors1.add(state1);
			}

			if (state2 != null) {
				state2 = tree.getPredecessor(state2);
				if (predecessors1.contains(state2))
					return state2;
				predecessors2.add(state2);
			}
		}
	}

	// Get the Parikh vector that reaches the given state in the given tree.
	private ParikhVector getPV(SpanningTree<TransitionSystem, Arc, State> tree, State state) {
		return getPV(tree, state, null);
	}

	// Get the Parikh vector that goes from upTo to state in the given tree.
	// This function assumes that such a path exists!
	private ParikhVector getPV(SpanningTree<TransitionSystem, Arc, State> tree, State state, State upTo) {
		Map<String, Integer> result = new HashMap<>();
		Arc arc = tree.getPredecessorEdge(state);
		// Since we already checked total reachability: arc == null means we reached the initial state
		while (arc != null && !state.equals(upTo)) {
			String label = arc.getLabel();
			Integer value = result.get(label);
			if (value == null)
				result.put(label, 1);
			else
				result.put(label, value + 1);
			state = arc.getSource();
			arc = tree.getPredecessorEdge(state);
		}
		return new ParikhVector(result);
	}

	// Find the state reached by 'firing' the given Parikh vector. This assumes that such a state exists!
	private State followPV(State state, ParikhVector pv) {
		// By determinism and persistency, we can follow things in an arbitrary order and are still guaranteed
		// to find the state we are looking for
		for (String t : pv.getLabels()) {
			Set<State> targets = state.getPostsetNodesByLabel(t);
			for (State target : targets)
				return followPV(target, pv.tryRemove(t, 1));
		}
		// We are assuming that the path is possible. Thus, we can only get here when we are done.
		assert new ParikhVector().equals(pv) : pv;
		return state;
	}

	private ParikhVector findPath(State from, State to) {
		SpanningTree<TransitionSystem, Arc, State> tree = SpanningTree.get(from.getGraph(), from);
		assert tree.isReachable(to);
		return getPV(tree, to);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
