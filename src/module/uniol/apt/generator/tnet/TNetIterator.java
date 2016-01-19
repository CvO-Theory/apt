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

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * The iterator class that is used by {@link TNetGenerator}.
 * @author vsp, Uli Schlachter
 */
class TNetIterator implements Iterator<PetriNet> {
	// The maximum number of transitions in the generated petri nets.

	private final int maxTransitions_;
	// An iterator the provides the presets for the transitions, see description for createNet()
	private final Iterator<List<Integer>> transitionPresetSizeListIterator;
	// An iterator which generates the presets of the places, see description for createNet()
	private Iterator<Deque<Integer>> placePresetListIterator;
	// The transition preset size list that will be used for (just) the next petri net, see createNet()
	private List<Integer> transitionPresetSizeList;
	private final boolean additionalTransitions;

	/**
	 * Create a new TNetIterator. This iterator will generate t-nets without isolated elements.
	 * @param maxPlaces the maximum number of places in the generated t-nets.
	 * @param maxTransitions the limit for the number of transitions in the generated nets.
	 * @param additionalTransitions should additional transitions get generated in the place preset generation part.
	 * @param exactTransitionCount Don't generate a list with fewer than maxTransitions transitions
	 */
	TNetIterator(int maxPlaces, int maxTransitions, boolean additionalTransitions, boolean exactTransitionCount) {
		assert maxPlaces > 0;
		assert maxTransitions > 0;
		this.maxTransitions_ = maxTransitions;
		this.transitionPresetSizeListIterator = new TransitionPresetSizeListIterator(maxPlaces, maxTransitions,
				exactTransitionCount);
		this.additionalTransitions = additionalTransitions;
		updatePresetSizeList();
	}

	/**
	 * Get a new placePresetListIterator when the end of the current one is reached. This function will get the next
	 * transition preset size list and create a place preset iterator for this list.
	 */
	private void updatePresetSizeList() {
		if (!transitionPresetSizeListIterator.hasNext()) {
			// no list with the given parameters exists
			this.placePresetListIterator = null;
			return;
		}

		this.transitionPresetSizeList = transitionPresetSizeListIterator.next();

		int maxTransitions = additionalTransitions ? this.maxTransitions_ : transitionPresetSizeList.size();
		this.placePresetListIterator = new PlacePresetListIterator(transitionPresetSizeList, maxTransitions);
	}

	/**
	 * Create a new t-net with arcs as described in the arguments.
	 *
	 * Note: A t-net can have transitions with empty pre- or postsets. Empty presets are indicated by an entry with
	 * value 0 in transitionPresetSizeList. Empty postsets are generated for transitions which do not appear as an
	 * entry in placePresetList.
	 *
	 * @param transitionPresetSizeList The i-th element of this list describes the number of places in the i-th
	 * transition of the generated petri net. Since a place can only have a single outgoing arcs, this is enough to
	 * describe the presets of all transitions in the generated t-net. So this argument describes the *size* of a
	 * transition's preset. The sum of this lists elements describes the number of places in the generated net.
	 * @param placePresetList Each place in a t-net has a single transition in its preset. The i-th element of this
	 * list describes the index of the transition that is in the i-th place's preset. So, this argument actually
	 * describes the postsets of all transitions in the generated t-net. The size of this list matches the generated
	 * petri net's number of places, too.
	 * @return The crated t-net
	 */
	private static PetriNet createNet(List<Integer> transitionPresetSizeList, Deque<Integer> placePresetList) {
		PetriNet pn = new PetriNet();
		List<Place> places = new ArrayList<>();
		List<Transition> transitions = new ArrayList<>();

		for (int transitionPresetSize : transitionPresetSizeList) {
			Transition t = pn.createTransition();
			for (int i = 0; i < transitionPresetSize; i++) {
				Place p = pn.createPlace();
				pn.createFlow(p, t);
				places.add(p);
			}
			transitions.add(t);
		}

		assert places.size() == placePresetList.size();

		int placeNr = 0;
		for (int preset : placePresetList) {
			assert preset <= transitions.size();

			Transition t;
			if (preset < transitions.size()) {
				t = transitions.get(preset);
			} else {
				t = pn.createTransition();
				transitions.add(t);
			}

			Place p = places.get(placeNr++);
			pn.createFlow(t, p);
		}

		return pn;
	}

	@Override
	public boolean hasNext() {
		if (placePresetListIterator == null) {
			// No t-net with the given parameters exists
			return false;
		}

		if (placePresetListIterator.hasNext()) {
			return true;
		}

		if (transitionPresetSizeListIterator.hasNext()) {
			updatePresetSizeList();
			return true;
		}

		return false;
	}

	@Override
	public PetriNet next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		return createNet(transitionPresetSizeList, placePresetListIterator.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
