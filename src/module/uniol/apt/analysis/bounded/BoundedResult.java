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

package uniol.apt.analysis.bounded;

import java.util.ArrayList;
import java.util.List;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class represents results from the {@link Bounded} check.
 * @author Uli Schlachter
 */
public class BoundedResult {
	// The Petri net that was checked.
	public final PetriNet pn;

	// A counterexample for (k-1)-boundedness (which is itself k-bounded). If the net is unbounded, this really is
	// an unbounded place.
	public final Place unboundedPlace;

	// The smallest k for which the Petri net is k-bounded, or null if the net is unbounded.
	public final Long k;

	// If the Petri net is bounded, this contains the firing sequence reaching the state where the unboundedPlace
	// has k tokens (the maximum). If the net is unbounded (k is null), then this contains the firing sequence that
	// reaches a state after which cycle is firable and creates a higher marking, after which cycle is again firable
	// etc.
	public final List<Transition> sequence;

	// If the net is unbounded, this is the cycle which can be used to generate infinitely many tokens on
	// unboundedPlace.
	public final List<Transition> cycle;

	/**
	 * Construct a new BoundedResult instance.
	 * @param pn The Petri net that was examined
	 * @param place Place which serves as a counter-example
	 * @param k The smallest k for which the Petri net is k-bounded
	 * @param sequenceToCovered Firing sequence reaching a special marking
	 * @param cycle Firing sequence producing arbitrarily many tokens after sequenceToCovered
	 */
	public BoundedResult(PetriNet pn, Place place, Long k, List<Transition> sequenceToCovered,
			List<Transition> cycle) {
		this.pn = pn;
		this.unboundedPlace = place;
		this.k = k;
		this.sequence = sequenceToCovered;
		this.cycle = cycle;
	}

	/**
	 * Was the checked Petri net bounded?
	 * @return true if the Petri net is bounded.
	 */
	public boolean isBounded() {
		return k != null;
	}

	/**
	 * Was the checked Petri net safe?
	 * @return true if the Petri net is safe.
	 */
	public boolean isSafe() {
		return isKBounded(1);
	}

	/**
	 * Was the checked Petri net k-bounded?
	 * @param checkK The k which should be checked.
	 * @return boolean
	 */
	public boolean isKBounded(int checkK) {
		return this.k != null && this.k <= checkK;
	}

	/**
	 * Return a sequence which is a counter-example to n-boundedness, if such a sequence exists.
	 * @param n The bound to disprove. Must be a non-negative integer.
	 * @return A suitable firing sequence or null if no such sequence exists.
	 */
	public List<Transition> getSequenceExceeding(int n) {
		if (isKBounded(n) || n < 0)
			return null;

		Token target = Token.valueOf(n);
		Marking m = pn.getInitialMarking();
		List<Transition> result = new ArrayList<>();
		// Does the initial marking already exceed bound n?
		if (m.getToken(unboundedPlace).compareTo(target) > 0)
			return result;

		// Fire sequence once...
		Transition[] array = sequence.toArray(new Transition[0]);
		m = m.fireTransitions(array);
		result.addAll(sequence);

		// ...and then go through the cycle until we exceed the target bound
		array = cycle.toArray(new Transition[0]);
		while (m.getToken(unboundedPlace).compareTo(target) <= 0) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			result.addAll(cycle);
			m = m.fireTransitions(array);
		}

		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
