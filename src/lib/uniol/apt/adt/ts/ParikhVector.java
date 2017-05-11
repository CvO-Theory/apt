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

package uniol.apt.adt.ts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.TreeBag;

/**
 * Data structure for representing a Parikh vector.
 * @author Manuel Gieseking, Uli Schlachter
 */
public class ParikhVector {

	private final Bag<String> occurrenceBag = new TreeBag<>();

	// Private copy constructor. Private, because ParikhVector instances are immutable and thus no one needs a copy
	// constructor.
	private ParikhVector(ParikhVector other) {
		this.occurrenceBag.addAll(other.occurrenceBag);
	}

	/**
	 * Creates a Parikh vector from a given sequence.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(String... sequence) {
		this(Arrays.asList(sequence));
	}

	/**
	 * Creates a Parikh vector from a given sequence.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(List<String> sequence) {
		this.occurrenceBag.addAll(sequence);
	}

	/**
	 * Creates a Parikh vector from a given mapping.
	 * @param pv the mapping from labels to occurrences.
	 */
	public ParikhVector(Map<String, Integer> pv) {
		for (Map.Entry<String, Integer> entry : pv.entrySet())
			this.occurrenceBag.add(entry.getKey(), entry.getValue());
	}

	/**
	 * Combines two Parikh vectors to one new.
	 * @param p2 - the second Parikh vector to add.
	 * @return the combination of this and p2.
	 */
	public ParikhVector add(ParikhVector p2) {
		ParikhVector result = new ParikhVector(this);
		result.occurrenceBag.addAll(p2.occurrenceBag);
		return result;
	}

	/**
	 * Combines two Parikh vectors to one new.
	 * @param sequence the sequence of labels that should be added
	 * @return the combination of this and the given sequence
	 */
	public ParikhVector add(String... sequence) {
		return add(Arrays.asList(sequence));
	}

	/**
	 * Combines two Parikh vectors to one new.
	 * @param sequence the sequence of labels that should be added
	 * @return the combination of this and the given sequence
	 */
	public ParikhVector add(List<String> sequence) {
		ParikhVector result = new ParikhVector(this);
		result.occurrenceBag.addAll(sequence);
		return result;
	}

	/**
	 * Create a new ParikhVector where the given event occurs n times less often.
	 * @param event Which event should occur less often?
	 * @param n How many times should it occur less?
	 * @return The new ParikhVector or null, if the given event doesn't occur often enough in the original
	 * ParikhVector
	 */
	public ParikhVector tryRemove(String event, int n) {
		if (get(event) < n)
			return null;

		ParikhVector result = new ParikhVector(this);
		result.occurrenceBag.remove(event, n);
		return result;
	}

	/**
	 * Compute the residual of this Parikh vector with another Parikh vector. For each event <pre>t</pre>, the
	 * resulting Parikh vector will contain <pre>t</pre> exactly <pre>max{ 0, this.get(t)-other.get(t) }</pre>
	 * times. In other words, this computes the difference between the two Parikh vector, but subtraction saturates
	 * at zero.
	 * @param other The Parikh vector to subtract.
	 * @return The residual.
	 */
	public ParikhVector residual(ParikhVector other) {
		ParikhVector result = new ParikhVector(this);
		result.occurrenceBag.removeAll(other.occurrenceBag);
		return result;
	}

	/**
	 * This enum represents the result of comparing two Parikh vectors.
	 */
	public enum Comparison {
		/**
		 * The first vector was smaller than the second vector.
		 */
		LESS_THAN(-1),

		/**
		 * The first vector was larger than the second vector.
		 */
		GREATER_THAN(1),

		/**
		 * The two vectors were equal.
		 */
		EQUAL(0),

		/**
		 * The two vectors were incomparable. This means there is some component were the first vector is larger
		 * than the second and another component were the first vector is smaller than the second.
		 */
		INCOMPARABLE(0);

		private final int value;

		private Comparison(int value) {
			this.value = value;
		}

		/**
		 * Convert this comparison result to a value that is mostly compatible with {@link
		 * Comparable#compareTo}. If the two vectors were incomparable, which is not allowed by the {@link
		 * Comparable} interface, zero is also returned.
		 * @return A value smaller than, equal to, equal to, or greater than zero depending on if the same
		 * vector was smaller than, equal to, incomparable to, greater than the second vector.
		 */
		public int asInt() {
			return value;
		}
	}

	/**
	 * Compare this Parikh vector with the given Parikh vector.
	 * @param pv2 The Parikh vector to compare to.
	 * @return The result of the comparison.
	 */
	public Comparison compare(ParikhVector pv2) {
		boolean greaterThan = this.occurrenceBag.containsAll(pv2.occurrenceBag);
		boolean lessThan = pv2.occurrenceBag.containsAll(this.occurrenceBag);
		if (!lessThan && greaterThan)
			return Comparison.GREATER_THAN;
		if (lessThan && !greaterThan)
			return Comparison.LESS_THAN;
		if (lessThan && greaterThan)
			return Comparison.EQUAL;
		return Comparison.INCOMPARABLE;
	}

	/**
	 * Test if this Parikh vector is incomparable with the given Parikh vector. Two Parikh vectors are incomparable
	 * if they are neither is smaller or equal to the other.
	 * @param pv2 The Parikh vector to compare to.
	 * @return true if the two Parikh vectors are uncomparable.
	 */
	public boolean isUncomparableTo(ParikhVector pv2) {
		return !pv2.occurrenceBag.containsAll(this.occurrenceBag)
			&& !this.occurrenceBag.containsAll(pv2.occurrenceBag);
	}

	/**
	 * Checks two Parikh vectors if they are equal or mutually disjoint. Mutually disjoint means that there is no
	 * label for which both Parikh vectors have a non-zero count.
	 * @param pv2 the Parikh vector to compare with.
	 * @return true if the Parikh vectors are equal or mutually disjoint.
	 */
	public boolean sameOrMutuallyDisjoint(ParikhVector pv2) {
		return this.equals(pv2) || this.mutuallyDisjoint(pv2);
	}

	/**
	 * Check if two Parikh vectors are mutually disjoint. Mutually disjoint means that there is no label for which
	 * both Parikh vectors have a non-zero count.
	 * @param pv2 the Parikh vector to compare with.
	 * @return true if the Parikh vectors are mutually disjoint.
	 */
	public boolean mutuallyDisjoint(ParikhVector pv2) {
		return Collections.disjoint(this.occurrenceBag, pv2.occurrenceBag);
	}

	/**
	 * Returns the occurrences of the given label in this Parikh vector.
	 * @param c the label to get the occurrences from.
	 * @return the number of occurrences of the given label in this Parikh vector.
	 */
	public int get(String c) {
		return this.occurrenceBag.getCount(c);
	}

	/**
	 * Return the labels for which this Parikh vector contains a non-zero entry.
	 * @return The label set.
	 */
	public Set<String> getLabels() {
		return Collections.unmodifiableSet(this.occurrenceBag.uniqueSet());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ParikhVector)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		ParikhVector other = (ParikhVector) obj;
		return this.occurrenceBag.equals(other.occurrenceBag);
	}

	@Override
	public int hashCode() {
		return this.occurrenceBag.hashCode();
	}

	@Override
	public String toString() {
		// This makes use of a TreeBag guaranteeing a sorted order!
		StringBuilder builder = new StringBuilder("{");
		boolean first = true;
		for (String label : this.occurrenceBag.uniqueSet()) {
			if (!first)
				builder.append(", ");
			builder.append(label).append('=').append(get(label));
			first = false;
		}
		builder.append("}");
		return builder.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
