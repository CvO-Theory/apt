/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An equivalence relation where the basic set over which it is defined is explicitly given. In contrast to {@link
 * EquivalenceRelation} this allows equivalence classes with only a single entry to be preserved.
 * @param <E> The type of elements in the equivalence relation.
 * @author Uli Schlachter
 */
public class DomainEquivalenceRelation<E> extends AbstractEquivalenceRelation<E> {
	private final Set<E> domain;

	/**
	 * Create a new instance of this class for the given domain.
	 */
	public DomainEquivalenceRelation(Collection<E> domain) {
		this.domain = new HashSet<>(domain);
		// Create all equivalence classes
		for (E e : this.domain)
			getClass(e);
	}

	@Override
	protected void checkValidElement(E e) {
		if (!domain.contains(e))
			throw new IllegalArgumentException(e + " is not in the domain of this relation");
	}

	@Override
	public DomainEquivalenceRelation<E> refine(IEquivalenceRelation<? super E> relation) {
		DomainEquivalenceRelation<E> result = new DomainEquivalenceRelation<>(domain);
		if (super.refine(result, relation))
			return result;
		return this;
	}

	@Override
	public Iterator<Set<E>> iterator() {
		return new Iterator<Set<E>>() {
			private Iterator<Set<E>> iter = leaderToClass.values().iterator();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Set<E> next() {
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
