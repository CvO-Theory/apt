/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * Compute smallest cycles or cycles which do not contain any state twice via Johnson's algorithm.
 * This implementation is based on "Finding All the Elementary Circuits of a Directed Graph" from Donald B. Johnson in
 * SIAM J. Comput., 4(1), 77–84. (8 pages) (DOI: 10.1137/0204007).
 * @author vsp
 */
class ComputeSmallestCyclesJohnson extends AbstractComputeSmallestCycles {
	// Get the adjacencies list of the strongly connected component of the state minState in the states list. All
	// states below minState are ignored. The returned adjacencies list contains for each state its postset as pairs
	// of arc label and arc target.
	private List<Collection<Pair<String, Integer>>> constructAdjacencies(TransitionSystem ts, List<State> states,
			int minState) {
		int[] dfsNums = new int[states.size()];
		int[] minNums = new int[states.size()];
		Arrays.fill(dfsNums, -1);
		Arrays.fill(minNums, -1);
		Deque<Integer> callers  = new ArrayDeque<>();
		Deque<Integer> stack    = new ArrayDeque<>();
		List<Collection<Pair<String, Integer>>> adjacencies = new ArrayList<>();
		for (int i = 0; i < states.size(); i++)
			adjacencies.add(new ArrayList<Pair<String, Integer>>());

		// Calculate the SCC via Tarjan's algorithm
		int node = minState;
		int counter = 0;
		visitNode(node, dfsNums, minNums, counter++, stack);
		do {
			boolean done = true;
			for (State curState : states.get(node).getPostsetNodes()) {
				Integer cur = states.indexOf(curState);
				assert cur != -1;
				if (cur < minState)
					// Ignore nodes below minState
					continue;
				if (dfsNums[cur] == -1) {
					callers.addLast(node);
					node = cur;
					visitNode(node, dfsNums, minNums, counter++, stack);
					done = false;
					break;
				} else if (stack.contains(cur) && dfsNums[node] > dfsNums[cur]) {
					minNums[node] = Math.min(minNums[node], dfsNums[cur]);
				}
			}
			if (done) {
				if (dfsNums[node] == minNums[node]) {
					// We found a strongly connected component, ensure that all nodes in it have the
					// same minNums[i] value so that we can later identify components based on this.
					Integer cur = -1;
					while (cur != node) {
						cur = stack.removeLast();
						assert minNums[cur] >= minNums[node];
						minNums[cur] = minNums[node];
					}
				}

				Integer next = callers.pollLast();
				if (next == null)
					next = -1;
				else
					minNums[next] = Math.min(minNums[next], minNums[node]);
				node = next;
			}
		} while (node != -1);

		assert callers.isEmpty();
		assert stack.isEmpty();

		// Now construct the adjacencies list of the SCC of minState
		for (int i = minState; i < states.size(); i++) {
			if (minNums[i] != minNums[minState])
				continue;
			for (Arc arc : ts.getNode(states.get(i).getId()).getPostsetEdges()) {
				Integer target = states.indexOf(arc.getTarget());
				assert target != -1;
				if (minNums[target] == minNums[minState])
					adjacencies.get(i).add(new Pair<>(arc.getLabel(), target));
			}
		}

		return adjacencies;
	}

	private void visitNode(int node, int[] dfsNums, int[] minNums, int counter, Deque<Integer> stack) {
		assert dfsNums[node] == -1;
		assert minNums[node] == -1;

		dfsNums[node] = counter;
		minNums[node] = counter;
		stack.addLast(node);
	}

	/**
	 * Computes the parikh vectors of all smallest cycles of a labeled transition system with a algorithm using
	 * Johnson's algorithm. (Requirement A10)
	 * @param ts       - the transitionsystem to examine.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 * @return a list of the smallest cycles of a given transitionsystem an their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, boolean smallest) {
		List<State> states = new ArrayList<>(ts.getNodes());

		int s = 0;
		Set<Pair<List<String>, ParikhVector>> cycles = new HashSet<>();
		while (s < states.size()) {
			List<Collection<Pair<String, Integer>>> adjacencies = constructAdjacencies(ts, states, s);

			// As a side effect, this adds cycles through s to 'cycles'
			new DoDfs(adjacencies, states, s, cycles, smallest);
			s++;
		}

		return cycles;
	}

	// Do a DFS for circles going through s. We are currently in state 'state'. This is called CIRCUIT() in the
	// paper.
	static private class DoDfs {
		private final List<Collection<Pair<String, Integer>>> adjacencies;
		private final List<State> states;
		private final int s;
		private final Deque<String> sStack;
		private final Deque<String> lStack;
		private final boolean[] blocked;
		private final List<Set<Integer>> b;
		private final Set<Pair<List<String>, ParikhVector>> cycles;
		private final boolean smallest;

		public DoDfs(List<Collection<Pair<String, Integer>>> adjacencies, List<State> states, int s,
				Set<Pair<List<String>, ParikhVector>> cycles, boolean smallest) {
			this.adjacencies = adjacencies;
			this.states = states;
			this.s = s;
			this.cycles = cycles;
			this.smallest = smallest;

			this.blocked = new boolean[states.size()];
			Arrays.fill(blocked, s, states.size(), false);

			this.b = new ArrayList<>();
			for (int i = 0; i < states.size(); i++) {
				if (i >= s)
					b.add(new HashSet<Integer>());
				else
					// Not part of the graph, should not be accessed
					b.add(null);
			}

			this.sStack = new ArrayDeque<>();
			this.lStack = new ArrayDeque<>();

			doDfs(s);

			assert sStack.isEmpty();
			assert lStack.isEmpty();
		}

		private boolean doDfs(int state) {
			boolean foundCycle = false;

			blocked[state] = true;
			sStack.addLast(states.get(state).getId());
			for (Pair<String, Integer> arc : adjacencies.get(state)) {
				lStack.addLast(arc.getFirst());
				if (arc.getSecond() == s) {
					// cycle found
					List<String> sCycle = new ArrayList<>(sStack);
					List<String> lCycle = new ArrayList<>(lStack);
					addCycle(cycles, smallest, new Pair<>(sCycle, new ParikhVector(lCycle)));
					foundCycle = true;
				} else if (!blocked[arc.getSecond()]) {
					foundCycle |= doDfs(arc.getSecond());
				}
				lStack.removeLast();
			}
			sStack.removeLast();

			if (foundCycle) {
				unblock(state, blocked, b);
			} else {
				for (Pair<String, Integer> arc : adjacencies.get(state)) {
					b.get(state).add(arc.getSecond());
				}
			}

			return foundCycle;
		}
	}

	static private void unblock(int state, boolean[] blocked, List<Set<Integer>> b) {
		blocked[state] = false;
		for (Integer prev : b.get(state)) {
			if (blocked[prev])
				unblock(prev, blocked, b);
		}
		b.set(state, new HashSet<Integer>());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
