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

package uniol.apt.generator.tnet;

import java.util.Iterator;

import uniol.apt.adt.pn.PetriNet;

/**
 * A generator which creates all t-nets with less than the given number of places and transitions. This generator does
 * not generate isolated transitions. The {@link TNetIterator} implements the actual logic of this class.
 *
 * This generator can get wrapped by the {@link uniol.apt.generator.isolated.IsolatedTransitionsGenerator} to get t-nets
 * which can contain isolated elements and by the {link uniol.apt.generator.marking.MarkingNetGenerator} to get
 * t-systems.
 *
 * @author vsp, Uli Schlachter
 */
public class TNetGenerator implements Iterable<PetriNet> {
	private final int maxPlaces;
	private final int maxTransitions;
	private final boolean additionalTransitions;
	private final boolean exactTransitionCount;

	/**
	 * Create a TNetGenerator for all t-nets with at most maxPlaces places. There is no limit for the number of
	 * transitions in the generated petri nets. However, in a t-net each place has exactly one transition in its
	 * preset and one in its postset. Since no isolated elements are allowed, this means that 2*maxPlaces is a
	 * limit for the transition count.
	 * @param maxPlaces the limit for the number of places in the generated petri nets.
	 */
	public TNetGenerator(int maxPlaces) {
		this(maxPlaces, 2 * maxPlaces, true, false);
	}

	/**
	 * Create a TNetGenerator for all t-nets with at most maxPlaces places. There is no limit for the number of
	 * transitions in the generated petri nets. However, in a t-net each place has exactly one transition in its
	 * preset and one in its postset. Since no isolated elements are allowed, this means that 2*maxPlaces is a
	 * limit for the transition count.
	 * @param maxPlaces the limit for the number of places in the generated petri nets.
	 * @param additionalTransitions should additional transitions get generated in the place preset generation part.
	 */
	public TNetGenerator(int maxPlaces, boolean additionalTransitions) {
		this(maxPlaces, 2 * maxPlaces, additionalTransitions, false);
	}

	/**
	 * Create a TNetGenerator for all t-nets with at most maxPlaces places. There is no limit for the number of
	 * transitions in the generated petri nets. However, in a t-net each place has exactly one transition in its
	 * preset and one in its postset. Since no isolated elements are allowed, this means that 2*maxPlaces is a
	 * limit for the transition count.
	 * @param maxPlaces the limit for the number of places in the generated petri nets.
	 * @param additionalTransitions should additional transitions get generated in the place preset generation part.
	 * @param exactTransitionCount Don't generate a list with fewer than maxTransitions transitions in the first
	 * step.
	 */
	public TNetGenerator(int maxPlaces, boolean additionalTransitions, boolean exactTransitionCount) {
		this(maxPlaces, 2 * maxPlaces, additionalTransitions, exactTransitionCount);
	}


	/**
	 * Create a TNetGenerator which generates all t-nets smaller than the given size limits.
	 * @param maxPlaces the maximum number of places in the generated nets.
	 * @param maxTransitions the limit for the transition count for the generated nets.
	 */
	public TNetGenerator(int maxPlaces, int maxTransitions) {
		this(maxPlaces, maxTransitions, true, false);
	}

	/**
	 * Create a TNetGenerator which generates all t-nets smaller than the given size limits.
	 * @param maxPlaces the maximum number of places in the generated nets.
	 * @param maxTransitions the limit for the transition count for the generated nets.
	 * @param additionalTransitions should additional transitions get generated in the place preset generation part.
	 * @param exactTransitionCount Don't generate a list with fewer than maxTransitions transitions in the first
	 * step.
	 */
	public TNetGenerator(int maxPlaces, int maxTransitions, boolean additionalTransitions,
			boolean exactTransitionCount) {
		if (maxTransitions <= 0)
			throw new IllegalArgumentException("maxTransition must be positive.");
		if (maxPlaces <= 0)
			throw new IllegalArgumentException("maxPlaces must be positive.");

		this.maxPlaces             = maxPlaces;
		this.maxTransitions        = maxTransitions;
		this.additionalTransitions = additionalTransitions;
		this.exactTransitionCount  = exactTransitionCount;
	}

	@Override
	public Iterator<PetriNet> iterator() {
		return new TNetIterator(maxPlaces, maxTransitions, additionalTransitions, exactTransitionCount);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
