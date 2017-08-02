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

package uniol.apt.analysis.synthesize;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Calculate a limited unfolding of a transition system, as defined in definition 2.42 in "Petri Net Synthesis" by
 * Badouel, Bernardinello and Darondeau.
 * @author Uli Schlachter
 */
public class LimitedUnfolding  {
	private LimitedUnfolding() {
	}

	static final private String NEW_STATE_KEY = "NEW_STATE";
	static final public String ORIGINAL_STATE_KEY = "ORIGINAL_STATE";

	// A state of the depth-first search
	static private class DFSState {
		public final State oldState;
		public final Iterator<Arc> oldStatePostset;

		public DFSState(State oldState) {
			this.oldState = oldState;
			this.oldStatePostset = oldState.getPostsetEdges().iterator();
		}

		public Arc getNextArc() {
			if (!oldStatePostset.hasNext())
				return null;
			return oldStatePostset.next();
		}
	}

	/**
	 * Calculate a limited unfolding.
	 * This function does a depth-first iteration through the transition system. For each state that it reaches it
	 * checks if the state was already reached on the current path from the root (aka: we found a loop). If yes, a
	 * corresponding loop is created in the unfolding. Else, the postset of this new state is examined.
	 * @param ts The transition system to unfold.
	 * @return The limited unfolding.
	 * @throws NonDeterministicException When the input ts is non-deterministic.
	 */
	static public TransitionSystem calculateLimitedUnfolding(TransitionSystem ts) throws NonDeterministicException {
		new Deterministic(ts).throwIfNonDeterministic();

		TransitionSystem unfolding = new TransitionSystem("Limited unfolding of " + ts.getName());
		Deque<DFSState> stack = new LinkedList<>();
		unfolding.setInitialState(createState(unfolding, stack, ts.getInitialState()));

		while (!stack.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			// Recurse via one element from the current state's postset
			Arc arc = stack.getFirst().getNextArc();
			if (arc == null) {
				// We handled the complete postset, done with this state
				stack.getFirst().oldState.removeExtension(NEW_STATE_KEY);
				stack.removeFirst();
				continue;
			}

			// Figure out where this arc goes to in the unfolding
			State state = getNewState(stack.getFirst().oldState);
			State newTarget = getNewState(arc.getTarget());
			if (newTarget == null) {
				// We didn't examine this path in our current path from the root yet.
				// This puts the new state at the front of the stack, so that the next iteration
				// will handle the target state instead. Thus, we are really doing a depth-first
				// search and at any given point in time only the nodes that are on a single
				// path from the initial state to the current state have a NEW_STATE_KEY
				// extension set.
				newTarget = createState(unfolding, stack, arc.getTarget());
			}
			// Create the new arc
			unfolding.createArc(state, newTarget, arc.getLabel()).copyExtensions(arc);
		}

		return unfolding;
	}

	static private State createState(TransitionSystem unfolding, Deque<DFSState> stack, State next) {
		State newState = unfolding.createState();
		newState.copyExtensions(next);
		next.putExtension(NEW_STATE_KEY, newState);
		newState.putExtension(ORIGINAL_STATE_KEY, next);
		newState.putExtension("original_state", next.getId(), ExtensionProperty.WRITE_TO_FILE);
		stack.addFirst(new DFSState(next));
		return newState;
	}

	static private State getNewState(State state) {
		try {
			return (State) state.getExtension(NEW_STATE_KEY);
		} catch (StructureException e) {
			// No such extension
			return null;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
