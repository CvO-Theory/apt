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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Some auxiliary functions that make working with
 * plain Java collections slightly less clumsy.
 * <p/>
 * This implementation in Java is inspired by the 
 * Apache Commmons Collections library.
 * 
 * @author Thomas Strathmann
 */
public class CollectionUtils {

	/**
	 * Check if at least one object in a {@link Collection} satisfies
	 * a given {@link Predicate}.
	 *
	 * @param coll the collection
	 * @param p the predicate 
	 * @return true if coll contains an object that satisfies p
	 */
	public static <T> boolean exists(final Collection<T> coll, final Predicate<? super T> p) {
		if(coll != null) {
			for(final T x : coll) {
				if(p.eval(x))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the first occurrence of an object that satisfies
	 * the predicate or null.
	 * 
	 * @param coll the collection to be searched
	 * @param p the predicate to evaluate
	 * @return some object from coll satisfying p if it exists or null otherwise
	 */
	public static <T> T find(final Collection<T> coll, final Predicate<? super T> p) {
		if(coll != null) {
			for(T x : coll) {
				if(p.eval(x))
					return x;
			}
		}
		return null;
	}
	
	/**
	 * Returns a new set containing only those elements of <code>set1</code>
	 * for which there is no element in <code>set2</code> such that
	 * <code>p</code> applied to both returns <code>true</code>.
	 *  
	 * @param set1 the set from which elements of set2 shall be removed
	 * @param set2 the set of elements to be removed from set1
	 * @param p the binary predicate that is used to decide when two elements
	 * 			of the different sets are equal
	 * @return the difference of the two sets with respect to the equality implemented
	 * 		by the binary predicate
	 */
	public static <T> Set<T> difference(final Set<T> set1, final Set<T> set2, final BinaryPredicate<? super T, ? super T> p) {
		if(set1 == null)
			return null;
		if(set2 == null)
			return new HashSet<T>(set1);
		
		HashSet<T> result = new HashSet<T>();
		for(final T x1 : set1) {
			if(!exists(set2, new Predicate<T>() {
				public boolean eval(T x2) {
					return p.eval(x1, x2);
				}
			})) {
				result.add(x1);
			}
		}
		return result;
	}
	
}
