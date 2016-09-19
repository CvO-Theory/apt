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

import java.util.Collections;
import uniol.apt.adt.ts.ParikhVector;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

/**
 * This class offers the possibility to compute the smallest cycles and parikh vectors of a transitionsystem with a
 * algorithm using the depth first search.
 * @author Manuel Gieseking
 */
class ComputeSmallestCyclesDFS extends AbstractComputeSmallestCycles {

	private TransitionSystem tsys;
	private Set<Pair<List<String>, ParikhVector>> cycles;

	/**
	 * Computes the parikh vectors of all smallest cycles of a labeled transition system with a algorithm using the
	 * depth first search. (Requirement A10)
	 * @param ts       - the transitionsystem to examine.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 * @return a list of the smallest cycles of a given transitionsystem an their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, boolean smallest) {
		// compute cycles
		return calculate(ts, smallest);
	}

	/**
	 * Calculates the smallest cycles and their parikh vectors. It is important to notice, that in most cases if
	 * abcd is an cycle, that also bcda, cdab, etc. with the same parikh vector are cycle but won't be saved
	 * additionaly. Just in cases that two different passes coming the cycles, than they will be saved.
	 * @param ts       - the transitionsystem to examine.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 * @return a set of cycles and the belonging parikh vectors.
	 */
	private Set<Pair<List<String>, ParikhVector>> calculate(TransitionSystem ts, boolean smallest) {
		// Reset results
		this.cycles = new HashSet<>();
		this.tsys = ts;
		if (ts.getNodes().isEmpty()) {
			return Collections.unmodifiableSet(cycles);
		}
		// Calls depth first search.
		dfs(ts.getInitialState(), new Stack<String>(), new Stack<String>(), smallest);

		return Collections.unmodifiableSet(cycles);
	}

	/**
	 * Depth first search function which is recursivly used to compute the smallest cycles.
	 * @param node     - the starting node
	 * @param sequence - a stack of all the visited nodes.
	 * @param labels   - a stack with the visited labels for the parikh vectors.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 */
	private void dfs(State node, Stack<String> sequence, Stack<String> labels, boolean smallest) {
		InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

		int idx = sequence.search(node.getId());
		if (idx != -1) {
			// Node does exist in stack. This means we have found a cycle and
			// can store it to our list of cycles.
			idx = sequence.size() - idx;
			List<String> cycle = new LinkedList<>(sequence.subList(idx, sequence.size()));
			List<String> cycleParikh = new LinkedList<>(labels.subList(idx, sequence.size()));
			Pair<List<String>, ParikhVector> pair = new Pair<>(cycle, new ParikhVector(cycleParikh));
			addCycle(cycles, smallest, pair);
			return;
		}

		// The node does not exist in stack. Push it.
		sequence.push(node.getId());

		// Iterate through all postsetedges.
		for (Arc neigh : node.getPostsetEdges()) {
			labels.push(neigh.getLabel()); // Labels stack for parikh vector generation.
			// Recursive call of dfs.
			dfs(neigh.getTarget(), sequence, labels, smallest);
			labels.pop(); // Labels stack for parikh vector generation.
		}

		// Remove node from stack.
		sequence.pop();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
