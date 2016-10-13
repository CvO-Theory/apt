/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.presynthesis.pps;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.util.Pair;

/**
 * Iterator that returns all paths of an LTS in depth first order.
 *
 * @author Jonas Prellberg
 */
public class DfsPathIterable implements Iterable<Path> {
	/**
	 * Maximum considered path length after which a path is not followed
	 * further.
	 */
	private final int maxPathLength;

	private final State origin;

	/**
	 * Creates a new iterator that returns paths with the given maximum
	 * length of the transition system that the origin state belongs to.
	 *
	 * @param origin
	 *                start state of all returned paths
	 * @param maxPathLength
	 *                maximum number of arcs in a path
	 */
	public DfsPathIterable(State origin, int maxPathLength) {
		this.maxPathLength = maxPathLength;
		this.origin = origin;
	}

	private class DfsPathIterator implements Iterator<Path> {
		/**
		 * Stack for the depth first search that saves a state and a list of
		 * unused arcs from that state.
		 */
		private final Deque<Pair<State, List<Arc>>> dfsStack;

		private DfsPathIterator() {
			this.dfsStack = new LinkedList<>();
			addStateToStack(origin);
		}

		@Override
		public boolean hasNext() {
			// If every element on the stack has only 1 arc left and the
			// last has 0 there are no more paths to explore
			for (Pair<State, List<Arc>> elem : dfsStack) {
				List<Arc> arcs = elem.getSecond();
				if (arcs.size() > 1 || (elem == dfsStack.getLast() && arcs.size() > 0)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Path next() {
			while (!dfsStack.isEmpty()) {
				Pair<State, List<Arc>> elem = dfsStack.getLast();
				List<Arc> arcs = elem.getSecond();
				if (!arcs.isEmpty() && dfsStack.size() <= maxPathLength) {
					Path result = stackToPath();
					Arc next = arcs.get(0);
					addStateToStack(next.getTarget());
					return result;
				} else {
					dfsStack.removeLast();
					// Remove arc that had been used to get to the
					// state that was just removed
					if (!dfsStack.isEmpty()) {
						List<Arc> remArcs = dfsStack.getLast().getSecond();
						if (!remArcs.isEmpty()) {
							remArcs.remove(0);
						}
					}
				}
			}
			assert !hasNext();
			throw new IllegalStateException("Iterator is exhausted but next was called.");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		private void addStateToStack(State state) {
			List<Arc> arcs = new LinkedList<>(state.getPostsetEdges());
			dfsStack.addLast(new Pair<>(state, arcs));
		}

		private Path stackToPath() {
			List<Arc> path = new ArrayList<>();
			for (Pair<State, List<Arc>> elem : dfsStack) {
				Arc nextPathArc = elem.getSecond().get(0);
				path.add(nextPathArc);
			}
			return new Path(path);
		}
	}
	@Override
	public Iterator<Path> iterator() {
		return new DfsPathIterator();
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
