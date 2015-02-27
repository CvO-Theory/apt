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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.map.LazyMap;
import static org.apache.commons.collections4.iterators.PeekingIterator.peekingIterator;

/**
 * Instances of this class represent an equivalence relation over an unknown set. Initially, all elements are only
 * equivalent to themselves, but the equivalence classes of two elements can be joined.
 * @author Uli Schlachter
 */
public class EquivalenceRelation<E> extends AbstractCollection<Set<E>> implements Collection<Set<E>> {
	private class ToUnitSetTransformer implements Transformer<E, Set<E>> {
		@Override
		public Set<E> transform(E e) {
			Set<E> result = new HashSet<>();
			result.add(e);
			EquivalenceRelation.this.allClasses.add(result);
			return result;
		}
	}

	private final Map<E, Set<E>> elementToClass = LazyMap.lazyMap(new HashMap<E, Set<E>>(), new ToUnitSetTransformer());
	private final Set<Set<E>> allClasses = new HashSet<>();

	/**
	 * Join the equivalence classes of two elements.
	 * @param e1 The first element to join classes with
	 * @param e2 The other element to join classes with
	 * @return the new class containing both elements
	 */
	public Set<E> joinClasses(E e1, E e2) {
		Set<E> class1 = elementToClass.get(e1);
		Set<E> class2 = elementToClass.get(e2);

		if (class1.contains(e2))
			// Already in same class
			return class1;

		// Make class1 refer to the smaller of the two classes.
		if (class1.size() > class2.size()) {
			Set<E> tmp = class1;
			class1 = class2;
			class2 = tmp;
		}

		allClasses.remove(class1);
		allClasses.remove(class2);
		class2.addAll(class1);
		allClasses.add(class2);

		for (E e : class1)
			elementToClass.put(e, class2);

		return class2;
	}

	/**
	 * Get the equivalence class of the given element.
	 * @param e the element whose class is needed
	 * @return The element's equivalence class
	 */
	public Set<E> getClass(E e) {
		return elementToClass.get(e);
	}

	/**
	 * Check if the given elements are in the same equivalence class.
	 * @param e1 The first element to check
	 * @param e2 The other element to check
	 * @return true if both elements are equivalent
	 */
	public boolean isEquivalent(E e1, E e2) {
		return elementToClass.get(e1).contains(e2);
	}

	@Override
	public int size() {
		// Remove all classes which have only a single entry
		Iterator<Set<E>> iter = allClasses.iterator();
		while (iter.hasNext()) {
			Set<E> klass = iter.next();
			if (klass.size() == 1) {
				elementToClass.remove(klass.iterator().next());
				iter.remove();
			}
		}
		return allClasses.size();
	}

	@Override
	public Iterator<Set<E>> iterator() {
		return new Iterator<Set<E>>() {
			private PeekingIterator<Set<E>> iter = peekingIterator(allClasses.iterator());

			@Override
			public boolean hasNext() {
				// Skip all sets which have just a single entry
				Set<E> next = iter.peek();
				while (next != null && next.size() == 1) {
					iter.next();
					next = iter.peek();
				}
				return iter.hasNext();
			}

			@Override
			public Set<E> next() {
				hasNext();
				return Collections.unmodifiableSet(iter.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
