/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.apache.commons.collections4.iterators.EmptyIterator.emptyIterator;

/**
 * Iterable for iterating over all pairs of different elements of a given collection. Note that it is assumed that the
 * order of the elements does not matter, so only one of (a, b) and (b, a) will be returned.
 * @param <E> The type of elements that are investigated.
 * @author Uli Schlachter
 */
public class DifferentPairsIterable<E> implements Iterable<Pair<E, E>> {
	private final Collection<E> collection;

	/**
	 * Construct a new instance of this iterable for the given base collection.
	 * @param collection The collection whose pairs should be returned.
	 */
	public DifferentPairsIterable(Collection<E> collection) {
		this.collection = collection;
	}

	@Override
	public Iterator<Pair<E, E>> iterator() {
		return new Iterator<Pair<E, E>>() {
			private Deque<E> remainingElements = new ArrayDeque<>(collection);
			private Iterator<E> iter = emptyIterator();
			private E currentElement = null;

			@Override
			public boolean hasNext() {
				while (!iter.hasNext()) {
					if (remainingElements.isEmpty())
						return false;

					currentElement = remainingElements.remove();

					iter = remainingElements.iterator();
				}

				return true;
			}

			@Override
			public Pair<E, E> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return new Pair<>(currentElement, iter.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
