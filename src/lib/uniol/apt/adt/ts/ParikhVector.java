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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import uniol.apt.adt.exception.StructureException;

/**
 * Data structure for representing a parikh vector.
 * At each access the consistency of the labels of the parikh vector is checked with the transitionsystem.
 * @author Manuel Gieseking
 */
public class ParikhVector {

	private TransitionSystem ts;
	// Mapping of labels and occurrences
	private List<String> labelList;
	private List<Integer> occurrenceList;
	private long rev = -1;

	/**
	 * Combines two parikh vectors to one new.
	 * @param p1 - the first to add.
	 * @param p2 - the second to add.
	 * @return the combination of p1 and p2.
	 */
	public static ParikhVector add(ParikhVector p1, ParikhVector p2) {
		assert p1.ts == p2.ts;
		p1.ensureConsistency();
		p2.ensureConsistency();
		Map<String, Integer> pv = new TreeMap<>();
		for (int i = 0; i < p1.labelList.size(); i++)
			pv.put(p1.labelList.get(i), p1.occurrenceList.get(i) + p2.occurrenceList.get(i));
		return new ParikhVector(p1.ts, pv);
	}

	/**
	 * Creates a parikh vector with a given sequence and the alphabet from the given TransitionSystem.
	 * @param ts       the TransitionSystem the pv belongs to.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(TransitionSystem ts, String... sequence) {
		this.ts = ts;
		computePv(Arrays.asList(sequence));
	}

	/**
	 * Creates a parikh vector with a given sequence and the alphabet from the given TransitionSystem.
	 * @param ts       the TransitionSystem the pv belongs to.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(TransitionSystem ts, List<String> sequence) {
		this.ts = ts;
		computePv(sequence);
	}

	/**
	 * Creates a parikh vector with a given mapping and the alphabet from the given TransitionSystem.
	 * @param ts the TransitionSystem the pv belongs to.
	 * @param pv the mapping from labels to occurrences.
	 */
	public ParikhVector(TransitionSystem ts, Map<String, Integer> pv) {
		this(pv);
		this.ts = ts;
	}

	/**
	 * Creates a parikh vector with a given mapping. Before using this pv it is needed to connect the pv to a
	 * TransitionSystem.
	 * @param pv the mapping from labels to occurrences.
	 */
	public ParikhVector(Map<String, Integer> pv) {
		this.labelList = new ArrayList<>(pv.keySet());
		Collections.sort(this.labelList);

		this.occurrenceList = new ArrayList<>(pv.size());
		for (String label : labelList)
			this.occurrenceList.add(pv.get(label));
	}

	/**
	 * Connects a given TransistionSystem to this parikh vector.
	 * @param tranSys the TransitionSystem the pv belongs to.
	 */
	public void connectToTransitionSystem(TransitionSystem tranSys) {
		this.ts = tranSys;
	}

	/**
	 * Used for ensuring the consistency of the parikh vector to the alphabet of the transitionsystem. The function
	 * checks the label revision variable of the transitionsystem and incase the parikh vector has an earlier
	 * revision, the hashmap of the parikh vector gets updated.
	 */
	private void ensureConsistency() {
		if (rev != ts.getLabelRev()) {
			List<String> oldLabelList = this.labelList;
			List<Integer> oldOccurrenceList = this.occurrenceList;

			this.labelList = new ArrayList<>(ts.getAlphabet());
			this.occurrenceList = new ArrayList<>(this.labelList.size());
			Collections.sort(this.labelList);

			for (String label : this.labelList) {
				int idx = oldLabelList.indexOf(label);
				if (idx == -1)
					this.occurrenceList.add(0);
				else
					this.occurrenceList.add(oldOccurrenceList.get(idx));
			}
			rev = ts.getLabelRev();
		}
	}

	/**
	 * Creates the mapping of labels to occurrences in this parikh vector from a given alphabet and sequence.
	 * @param sequence the sequence from which the occurrences are calculated
	 */
	private void computePv(List<String> sequence) {
		this.labelList = new ArrayList<>(ts.getAlphabet());
		this.occurrenceList = new ArrayList<>(labelList.size());
		Collections.sort(this.labelList);
		for (String label : labelList)
			this.occurrenceList.add(Collections.frequency(sequence, label));
		this.rev = ts.getLabelRev();
	}

	/**
	 * Compares one parikh vector with another parikh vector and returns true, when the parikh vector is smaller
	 * than the other parikh vector. That means true if all components are smaller than the other but the vector are
	 * not the same.
	 * @param pv2 the parikh vector to compare with.
	 * @return true, when this parikh vector is smaller than v2.
	 * @throws StructureException is thrown if the labelsets of the parikh vectors are not the same.
	 */
	public boolean lessThan(ParikhVector pv2) {
		ensureConsistency();
		if (!Objects.equals(this.labelList, pv2.labelList)) {
			throw new StructureException("Parikhvectors are not operating on the same alphabet.");
		}
		boolean ret = false;
		for (int i = 0; i < this.labelList.size(); i++) {
			if (this.occurrenceList.get(i) > pv2.occurrenceList.get(i))
				return false;
			if (this.occurrenceList.get(i) != pv2.occurrenceList.get(i))
				ret = true;
		}
		return ret;
	}

	/**
	 * Checks two parikh vectors if they are the same or mutally disjoint. Mutally disjoint means that it is false
	 * if there exists a component which is in both vectors not equal to zero.
	 * @param pv2 the parikh vector to compare with.
	 * @return true if the parikh vectors are the same or mutally disjoint.
	 * @throws StructureException is thrown if the labelsets of the parikh vectors are not the same.
	 */
	public boolean sameOrMutuallyDisjoint(ParikhVector pv2) {
		ensureConsistency();
		if (!Objects.equals(this.labelList, pv2.labelList)) {
			throw new StructureException("Parikhvectors are not operating on the same alphabet.");
		}
		boolean same = true;
		boolean disjoint = true;
		for (int i = 0; i < this.labelList.size(); i++) {
			disjoint &= this.occurrenceList.get(i) == 0 || pv2.occurrenceList.get(i) == 0;
			same &= this.occurrenceList.get(i) == pv2.occurrenceList.get(i);
		}
		return disjoint || same;
	}

	/**
	 * Returns the occurrences of the given label in this parikh vector.
	 * @param c the label to get the occurrences from.
	 * @return the number of occurrences of the given label in this parikh vector.
	 */
	public int get(String c) {
		ensureConsistency();
		int idx = this.labelList.indexOf(c);
		if (idx < 0)
			return 0;
		return this.occurrenceList.get(idx);
	}

	/**
	 * Returns the occurrences of the labels in this parikh vector in lexical order.
	 * @return the parikh vector integers.
	 */
	public Integer[] getPVLexicalOrder() {
		ensureConsistency();

		// The labelList should always be sorted, check this!
		List<String> copy = Collections.emptyList();
		assert (copy = new ArrayList<>(labelList)) != null;
		Collections.sort(copy);
		assert copy.equals(labelList) : "The label list is not sorted: " + labelList;

		return occurrenceList.toArray(new Integer[occurrenceList.size()]);
	}

	/**
	 * Returns the mapping from labels to occurrences of this parikh vector.
	 * @return the mapping from labels to occurrences.
	 */
	public Map<String, Integer> getPV() {
		ensureConsistency();
		Map<String, Integer> pv = new TreeMap<>();
		for (int i = 0; i < this.labelList.size(); i++)
			pv.put(labelList.get(i), occurrenceList.get(i));
		return Collections.unmodifiableMap(pv);
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
		ensureConsistency();
		other.ensureConsistency();
		return Objects.equals(this.labelList, other.labelList)
			&& Objects.equals(this.occurrenceList, other.occurrenceList);
	}

	@Override
	public int hashCode() {
		ensureConsistency();
		return this.occurrenceList.hashCode();
	}

	@Override
	public String toString() {
		ensureConsistency();
		return getPV().toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
