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

package uniol.apt.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Calculate the power set of a collection.
 * @param <E> The type of the elements.
 * @author Uli Schlachter
 */
public class PowerSet<E> extends AbstractCollection<Collection<E>> {
	final private List<E> c;

	/**
	 * Constructor
	 *
	 * @param c The collection whose power set should be generated. This collection must have stable iteration
	 * order so that iterating multiple times over it always produces elements in the same order! For lists this is
	 * already the case. Other types of collections will be copied into an ArrayList to guarantee stable iteration
	 * order.
	 */
	public PowerSet(Collection<E> c) {
		if (c instanceof List) {
			this.c = (List<E>) c;
		} else {
			this.c = new ArrayList<>(c);
		}
	}

	@Override
	public int size() {
		return (int) Math.pow(2, c.size());
	}

	@Override
	public Iterator<Collection<E>> iterator() {
		return new PowerSetIterator<E>(c);
	}

	/**
	 * Iterator that produces the power set of a given list.
	 * @author Uli Schlachter
	 */
	static public class PowerSetIterator<E> implements Iterator<Collection<E>> {
		final private List<E> c;
		final private Iterator<BitSet> iter;

		/**
		 * Constructor
		 * @param c The list whose power set should be generated
		 */
		public PowerSetIterator(List<E> c) {
			this.c = c;
			this.iter = new BitSetIterator(c.size());
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Collection<E> next() {
			BitSet next = iter.next();
			Collection<E> result = new ArrayList<>(next.cardinality());
			int i = 0;
			for (E e : c)
				if (next.get(i++))
					result.add(e);

			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Iterable that creates BitSetIterator instances.
	 * @author Uli Schlachter
	 */
	static public class BitSetIterable extends AbstractCollection<BitSet> {
		final private int size;

		/**
		 * Constructor
		 * @param size The number of bits that should be in each BitSet
		 */
		public BitSetIterable(int size) {
			this.size = size;
		}

		@Override
		public int size() {
			return (int) Math.pow(2, size);
		}

		@Override
		public Iterator<BitSet> iterator() {
			return new BitSetIterator(size);
		}
	}

	/**
	 * Iterator that iterates through all possible BitSets of a given length.
	 * @author Uli Schlachter
	 */
	static public class BitSetIterator implements Iterator<BitSet> {
		final private int size;
		private BitSet state;

		/**
		 * Constructor
		 * @param size The number of bits that should be in each BitSet
		 */
		public BitSetIterator(int size) {
			if (size < 0)
				throw new IllegalArgumentException("Size may not be negative, got " + size);
			this.size = size;
			this.state = new BitSet(size);
		}

		@Override
		public boolean hasNext() {
			return state != null;
		}

		@Override
		public BitSet next() {
			BitSet result = state.get(0, size);

			int lastClear = state.previousClearBit(size - 1);
			if (lastClear >= 0)
				state.flip(lastClear, size);
			else
				state = null;

			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Produce the power set of a collection
	 * @param c The collection whose power set should be generated
	 * @return An iterable containing the power set of the given list
	 * @param <E> The type of the elements.
	 */
	static public <E> PowerSet<E> powerSet(Collection<E> c) {
		return new PowerSet<E>(c);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
