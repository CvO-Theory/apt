/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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
 * A predicate on two types. Usually used for user-defined
 * equalities between elements for the same or of different types.
 * 
 * @author Thomas Strathmann
 */
public interface BinaryPredicate<T1, T2> {
	
	/**
	 * Test whether the objects x1 and x2 satisfy this predicate.
	 * 
	 * @param x1 the first object to be tested
	 * @param x2 the second object to be tested
	 * @return true iff the pair (x1, x2) satisfies the predicate
	 */
	public boolean eval(T1 x1, T2 x2);

}
