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

package uniol.apt.adt;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.commons.collections4.iterators.UnmodifiableIterator.unmodifiableIterator;

/**
 * Given a collection which really behaves like a set (read: has no duplicate entries), implement a read-only set.
 * @param <E> The type of elements of the set.
 * @author Uli Schlachter
 */
public class CollectionToUnmodifiableSetAdapter<E> extends AbstractSet<E> {
	private final Collection<E> collection;

	/**
	 * Create a new adapter for the given Collection. This does not create a copy of the collection, but instead
	 * behaves as a view into that collection.
	 * @param c The collection to adapt into a set.
	 */
	public CollectionToUnmodifiableSetAdapter(Collection<E> c) {
		this.collection = Collections.unmodifiableCollection(c);
	}

	@Override
	public boolean contains(Object o) {
		return collection.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return collection.isEmpty();
	}

	@Override
	public int size() {
		return collection.size();
	}

	@Override
	public Iterator<E> iterator() {
		return unmodifiableIterator(collection.iterator());
	}

	@Override
	public String toString() {
		return collection.toString();
	}

	@Override
	public boolean equals(Object o) {
		return collection.equals(o);
	}

	@Override
	public int hashCode() {
		return collection.hashCode();
	}

	// We could also implement toArray(), but is that really worth it?
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
