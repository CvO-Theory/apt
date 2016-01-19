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

/**
 * Implementations of this interface can decide if two objects are equivalent to each other in some sense. This is
 * mainly used for {@link EquivalenceRelation#refine}.
 * @param <E> The type of elements that can be compared.
 * @author Uli Schlachter
 */
public interface IEquivalenceRelation<E> {
	/**
	 * Check if the given elements are in the same equivalence class.
	 * @param e1 The first element to check
	 * @param e2 The other element to check
	 * @return true if both elements are equivalent
	 */
	public boolean isEquivalent(E e1, E e2);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
