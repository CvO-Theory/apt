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
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.List;

/**
 * Iterator over the options to distribute places in transition presets. Options are represented as lists of the sizes
 * of the transitions presets. This lists are sorted descending because permutations of this list would lead to
 * isomorphic Petri nets.
 * @author vsp, Uli Schlachter
 */
class TransitionPresetSizeListIterator implements Iterator<List<Integer>> {
	private final int minPlaces;
	private final int maxPlaces;
	private final int maxTransitions;
	private final boolean exactTransitionCount;

	private int curPlaces;
	private Iterator<List<Integer>> subIterator;

	/**
	 * Constructor of this iterator.
	 *
	 * @param maxPlaces The maximum number of places which may get distributed in the transition presets.
	 * @param maxTransitions Maximum number of transitions which may be used.
	 */
	TransitionPresetSizeListIterator(int maxPlaces, int maxTransitions) {
		this(1, maxPlaces, maxTransitions, false);
	}

	/**
	 * Constructor of this iterator.
	 *
	 * @param maxPlaces The maximum number of places which may get distributed in the transition presets.
	 * @param maxTransitions Maximum number of transitions which may be used.
	 * @param exactTransitionCount Don't generate a list with fewer than maxTransitions transitions.
	 */
	TransitionPresetSizeListIterator(int maxPlaces, int maxTransitions, boolean exactTransitionCount) {
		this(1, maxPlaces, maxTransitions, exactTransitionCount);
	}

	/**
	 * Constructor of this iterator.
	 *
	 * @param minPlaces The minimum number of places which every transition preset must have.
	 * @param maxPlaces The maximum number of places which may get distributed in the transition presets.
	 * @param maxTransitions Maximum number of transitions which may be used.
	 * @param exactTransitionCount Don't generate a list with fewer than maxTransitions transitions.
	 */
	private TransitionPresetSizeListIterator(int minPlaces, int maxPlaces, int maxTransitions,
			boolean exactTransitionCount) {
		// this assertions should always hold when called from TNetIterator ...
		assert minPlaces > 0;
		assert maxPlaces > 0;
		assert maxTransitions > 0;
		assert minPlaces <= maxPlaces;

		this.minPlaces            = minPlaces;
		this.maxPlaces            = maxPlaces;
		this.maxTransitions       = maxTransitions;
		this.exactTransitionCount = exactTransitionCount;
		this.curPlaces            = minPlaces;
		createSubIterator();
	}

	/**
	 * Helper method for recursive iterating. This gets called at creation time and when this iterator instance
	 * changes its own state which is when the dependant iterators haven't next elements.
	 */
	private void createSubIterator() {
		if (2 * curPlaces > maxPlaces || maxTransitions <= 1) {
			// the remaining places can not get divided in two parts which hold the conditions
			// => we are (now) the last iterator
			this.subIterator = null;
			this.curPlaces   = minPlaces;
		} else {
			this.subIterator = new TransitionPresetSizeListIterator(curPlaces, maxPlaces - curPlaces,
						maxTransitions - 1, exactTransitionCount);
		}
	}

	@Override
	public boolean hasNext() {
		while (subIterator != null && !subIterator.hasNext()) {
			curPlaces++;
			createSubIterator();
		}
		if (subIterator == null) {
			if (exactTransitionCount && maxTransitions > 1) {
				return false;
			}
			return curPlaces <= maxPlaces;
		}
		return true;
	}
	@Override
	public List<Integer> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		List<Integer> list;
		if (subIterator == null) {
			list = new LinkedList<>();
			list.add(curPlaces++);
		} else {
			list = subIterator.next();
			list.add(curPlaces);
		}

		return list;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
