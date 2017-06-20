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

import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.Event;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * This class represents results from the {@link Fairness} check.
 * @author vsp
 */
public class FairnessResult {
	// The transition system that was checked.
	public final TransitionSystem ts;

	// A counterexample for k-fairness
	public final State unfairState;
	public final Event unfairEvent;

	// The smallest k for which the Petri net is k-unfair, or null if is
	// fair.
	public final Integer k;

	// This contains the path leading to the unfairState
	public final List<Arc> sequence;

	// A cycle around the unfairState which gives us an infinite path
	public final List<Arc> cycle;

	// A path from the unfair state to a state where the unfair event is enabled
	public final List<Arc> enabling;

	/**
	 * Construct a new FairnessResult instance for a fair transition system
	 * @param ts The transition system that was examined
	 */
	FairnessResult(TransitionSystem ts) {
		this(ts, null, null, null, null, null, null);
	}

	/**
	 * Construct a new FairnessResult instance.
	 * @param ts The transition system that was examined
	 * @param state The unfairness witness state
	 * @param event The unfairness witness event
	 * @param k The smallest k for which the transition system is k-unfair
	 * @param sequence Firing sequence to the unfair state
	 * @param cycle Cycle around the unfair state
	 * @param enabling Firing sequence from the witness state to a state where the witness event is enabled
	 */
	FairnessResult(TransitionSystem ts, State state, Event event, Integer k, List<Arc> sequence,
			List<Arc> cycle, List<Arc> enabling) {
		this.ts          = ts;
		this.unfairState = state;
		this.unfairEvent = event;
		this.k           = k;
		this.sequence    = sequence;
		this.cycle       = cycle;
		this.enabling    = enabling;
	}

	/**
	 * Was the checked transition system fair?
	 * @return true if the transition system is fair.
	 */
	public boolean isFair() {
		return k == null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
