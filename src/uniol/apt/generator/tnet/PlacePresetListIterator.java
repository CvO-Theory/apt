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

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.List;

/**
 * A Iterator over the possible combinations of place presets. This iterator produces isomorphic petri nets. The only
 * countermeasures against this are that places which are in the preset of the same transition get ascending ordered
 * transitions as preset and that the numbers of new transitions must be continuously starting beyond the number of the
 * highest old transition.
 * @author vsp, Uli Schlachter
 */
class PlacePresetListIterator implements Iterator<Deque<Integer>> {
	private final List<Integer> transitionPresetSizeList;
	private final int index;
	private final int subIndex;
	private final int maxTransitions;
	private final int maxTransitionNr;

	private int cur;
	private Iterator<Deque<Integer>> subIterator;

	/**
	 * Constructor of this iterator.
	 *
	 * @param transitionPresetSizeList List of transition preset sizes like {@link TransitionPresetSizeListIterator}
	 *        generates it.
	 * @param maxTransitions How many transitions may get used by any place.
	 */
	PlacePresetListIterator(List<Integer> transitionPresetSizeList, int maxTransitions) {
		this(transitionPresetSizeList, 0, 0, maxTransitions, 0,
				Math.min(maxTransitions - 1, transitionPresetSizeList.size()));
	}

	/**
	 * Extended constructor of this iterator.
	 *
	 * @param transitionPresetSizeList List of transition preset sizes like {@link TransitionPresetSizeListIterator}
	 *        generates it.
	 * @param index Number of the transitionPresetSizeList entry to which this iterator belongs.
	 * @param subIndex Which place from the transition preset referenced by index is represented by this iterator
	 *        instance.
	 * @param maxTransitions How many transitions may get used by any place.
	 * @param minTransitionNr The minimum transition number which may get used for this place.
	 * @param maxTransitionNr The maximum transition number which may get used for this place.
	 */
	private PlacePresetListIterator(List<Integer> transitionPresetSizeList, int index, int subIndex,
			int maxTransitions, int minTransitionNr, int maxTransitionNr) {
		assert maxTransitions > 0;
		assert index < transitionPresetSizeList.size();
		assert subIndex < transitionPresetSizeList.get(index);

		this.transitionPresetSizeList = transitionPresetSizeList;
		this.index                    = index;
		this.subIndex                 = subIndex;
		this.maxTransitions           = maxTransitions;
		this.maxTransitionNr          = maxTransitionNr;
		this.cur                      = minTransitionNr;

		createSubIterator();
	}

	/**
	 * Helper method for recursive iterating. This gets called at creation time and when this iterator instance
	 * changes its own state which is when the dependant iterators haven't next elements.
	 */
	private void createSubIterator() {
		int newMaxTransitionNr = Math.min(maxTransitions - 1, Math.max(maxTransitionNr, cur + 1));
		if (subIndex < transitionPresetSizeList.get(index) - 1) {
			this.subIterator =
				new PlacePresetListIterator(transitionPresetSizeList, index, subIndex + 1,
						maxTransitions, cur, newMaxTransitionNr);
		} else if (index < transitionPresetSizeList.size() - 1) {
			this.subIterator =
				new PlacePresetListIterator(transitionPresetSizeList, index + 1, 0,
						maxTransitions, 0, newMaxTransitionNr);
		} else {
			this.subIterator = null;
		}
	}

	@Override
	public boolean hasNext() {
		if (subIterator != null && !subIterator.hasNext()) {
			cur++;
			createSubIterator();
		}
		return cur <= maxTransitionNr;
	}
	@Override
	public Deque<Integer> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		Deque<Integer> list;
		if (subIterator == null) {
			list = new LinkedList<>();
			list.addFirst(cur++);
		} else {
			list = subIterator.next();
			list.addFirst(cur);
		}

		return list;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
