/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015-2017  Uli Schlachter
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Base class for equivalence relations.
 * @param <E> The type of elements in the equivalence relation.
 * @author Uli Schlachter
 */
public abstract class AbstractEquivalenceRelation<E> extends AbstractCollection<Set<E>>
		implements Collection<Set<E>>, IEquivalenceRelation<E> {
	// All equivalence classes have a "leader" which all other elements refer to. This leader is found by
	// recursively following parents. The leader itself does not have a parent. This approach is used to speed up
	// joinClasses(). This map contains the parents.
	private final Map<E, E> elementToParent = new HashMap<>();

	// A leader refers to a set containing its equivalence class via this map. Non-leaders aren't contained.
	protected final Map<E, Set<E>> leaderToClass = new HashMap<>();

	/**
	 * Refine this equivalence relation via another relation. This function splits classes in this equivalence
	 * relation where not all elements are in the same class in the given relation. The result will be the
	 * equivalence relation where two elements are in the same class iff they are in the same class in this and in
	 * the given relation.
	 * @param relation The relation to use for refinement.
	 * @return The refined equivalence relation or this relation if no refinement was necessary.
	 */
	abstract public AbstractEquivalenceRelation<E> refine(IEquivalenceRelation<? super E> relation);

	/**
	 * Extension point to restrict the valid entries of the equivalence class.
	 */
	protected void checkValidElement(E e) {
	}

	/**
	 * Refine this equivalence relation via another relation. This function splits classes in this equivalence
	 * relation where not all elements are in the same class in the given relation. All elements which are
	 * equivalent in both classes will be joined via calls to {@link joinClasses} on the given new relation.
	 * @param newRelation The relation where classes should be joined.
	 * @param relation The relation to use for refinement.
	 * @return The refined equivalence relation or this relation if no refinement was necessary.
	 */
	protected boolean refine(AbstractEquivalenceRelation<? super E> newRelation, IEquivalenceRelation<? super E> relation) {
		boolean hadSplit = false;
		for (Set<E> klass : this) {
			Set<E> unhandled = new HashSet<>(klass);
			while (!unhandled.isEmpty()) {
				// Pick some element and figure out its equivalence class
				Iterator<E> it = unhandled.iterator();
				E e1 = it.next();
				it.remove();

				while (it.hasNext()) {
					E e2 = it.next();
					if (relation.isEquivalent(e1, e2)) {
						it.remove();
						newRelation.joinClasses(e1, e2);
					} else
						hadSplit = true;
				}
			}
		}

		return hadSplit;
	}

	/**
	 * Join the equivalence classes of two elements.
	 * @param e1 The first element to join classes with
	 * @param e2 The other element to join classes with
	 * @return the new class containing both elements
	 */
	public Set<E> joinClasses(E e1, E e2) {
		// Identify all elements by their leader
		e1 = getLeader(e1);
		e2 = getLeader(e2);

		Set<E> class1 = getClass(e1);
		Set<E> class2 = getClass(e2);

		if (class1.contains(e2))
			// Already in same class
			return class1;

		// Make class1 refer to the smaller of the two classes.
		if (class1.size() > class2.size()) {
			Set<E> classTmp = class1;
			class1 = class2;
			class2 = classTmp;

			E eTmp = e1;
			e1 = e2;
			e2 = eTmp;
		}

		// Now actually merge the classes. e1 is no longer a leader!
		class2.addAll(class1);
		leaderToClass.remove(e1);
		elementToParent.put(e1, e2);

		return class2;
	}

	/**
	 * Get the leader of the element's equivalence class or the element itself. Each equivalence class has a leader
	 * that uniquely identifies it. This method finds the leader.
	 * @param e The element whose leader should be returned
	 * @return The leader of the element or the element itself if it doesn't have an equivalence class.
	 */
	private E getLeader(E e) {
		checkValidElement(e);
		E parent = e;
		E next = elementToParent.get(parent);
		while (next != null) {
			parent = next;
			next = elementToParent.get(parent);
		}

		// Remember this result to speed up following lookups
		if (e != parent)
			elementToParent.put(e, parent);

		return parent;
	}

	/**
	 * Get the equivalence class of the given element if it exists.
	 * @param e the element whose class is needed
	 * @return The element's equivalence class or null if it doesn't have one.
	 */
	private Set<E> getClassIfExists(E e) {
		return leaderToClass.get(getLeader(e));
	}

	/**
	 * Get the equivalence class of the given element.
	 * @param e the element whose class is needed
	 * @return The element's equivalence class
	 */
	public Set<E> getClass(E e) {
		Set<E> result = getClassIfExists(e);
		if (result == null) {
			result = new HashSet<>();
			result.add(e);
			leaderToClass.put(e, result);
		}
		return result;
	}

	@Override
	public boolean isEquivalent(E e1, E e2) {
		if (e1.equals(e2))
			return true;
		Set<E> klass = getClassIfExists(e1);
		return klass != null && klass.contains(e2);
	}

	@Override
	public int size() {
		return leaderToClass.size();
	}

	@Override
	abstract public Iterator<Set<E>> iterator();

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof AbstractEquivalenceRelation))
			return false;
		AbstractEquivalenceRelation<?> rel = (AbstractEquivalenceRelation<?>) o;
		if (rel.size() != size())
			return false;
		return containsAll(rel);
	}

	@Override
	public int hashCode() {
		int result = 0;
		for (Set<E> set : this)
			result += set.hashCode();
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
