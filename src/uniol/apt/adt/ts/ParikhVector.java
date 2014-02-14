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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import uniol.apt.adt.exception.StructureException;

/**
 * Data structure for representing a parikh vector.
 * <p/>
 * At each access the consistency of the labels of the parikh vector is checked with the transitionsystem.
 * <p/>
 * @author Manuel Gieseking
 */
public class ParikhVector {

	// Mapping of labels and occurences
	private Map<String, Integer> pv = new TreeMap<>();
	private TransitionSystem ts;
	private long rev = -1;

	/**
	 * Combines two parikh vectors to one new.
	 * <p/>
	 * @param p1 - the first to add.
	 * @param p2 - the second to add.
	 * <p/>
	 * @return the combination of p1 and p2.
	 */
	public static ParikhVector add(ParikhVector p1, ParikhVector p2) {
		assert p1.ts == p2.ts;
		List<String> list = new ArrayList<>();
		for (String label : p1.getPV().keySet()) {
			int count = p1.getPV().get(label);
			for (int i = 0; i < count; ++i) {
				list.add(label);
			}
		}
		for (String label : p2.getPV().keySet()) {
			int count = p2.getPV().get(label);
			for (int i = 0; i < count; ++i) {
				list.add(label);
			}
		}
		return new ParikhVector(p1.ts, list);
	}

	/**
	 * Creates a parikh vector with a given sequence and the alphabet from the given TransitionSystem.
	 * <p/>
	 * @param ts       the TransitionSystem the pv belongs to.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(TransitionSystem ts, String... sequence) {
		this.ts = ts;
		computePv(ts.getAlphabet(), Arrays.asList(sequence));
	}

	/**
	 * Creates a parikh vector with a given sequence and the alphabet from the given TransitionSystem.
	 * <p/>
	 * @param ts       the TransitionSystem the pv belongs to.
	 * @param sequence the sequence of labels.
	 */
	public ParikhVector(TransitionSystem ts, List<String> sequence) {
		this.ts = ts;
		computePv(ts.getAlphabet(), sequence);
	}

	/**
	 * Creates a parikh vector with a given mapping and the alphabet from the given TransitionSystem.
	 * <p/>
	 * @param ts the TransitionSystem the pv belongs to.
	 * @param pv the mapping from labels to occurences.
	 */
	public ParikhVector(TransitionSystem ts, Map<String, Integer> pv) {
		this.ts = ts;
		this.pv = new TreeMap<>(pv);
	}

	/**
	 * Creates a parikh vector with a given mapping. Before using this pv it is needed to connect the pv to a
	 * TransitionSystem.
	 * <p/>
	 * @param pv the mapping from labels to occurences.
	 */
	public ParikhVector(Map<String, Integer> pv) {
		this.pv = new TreeMap<>(pv);
	}
	
	/**
	 * Creates a Parikh vector for a given LTS and set of arcs.
	 * <p/>
	 * @param ts the TransitionSystem the pv belongs to
	 * @param arcs the set of arcs whose Parikh vector is to be computed.
	 */
	public ParikhVector(TransitionSystem ts, Collection<Arc> arcs) {
		this.ts = ts;
		this.pv = new TreeMap<>();
		for (String label : ts.getAlphabet()) {
			Integer count = pv.get(label);
			if (count == null) {
				pv.put(label, 0);
			}
		}
		for(Arc a : arcs) {
			String label = a.getLabel();
			int count = pv.get(label);
			pv.put(label, ++count);
		}
	}

	/**
	 * Connects a given TransistionSystem to this parikh vector.
	 * <p/>
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
			Set<String> alphabet = ts.getAlphabet();
			for (String label : pv.keySet()) {
				if (!alphabet.contains(label)) {
					pv.remove(label);
				}
			}
			for (String label : alphabet) {
				if (!pv.containsKey(label)) {
					pv.put(label, 0);
				}
			}
			rev = ts.getLabelRev();
		}
	}

	/**
	 * Creates the mapping of labels to occurences in this parikh vector from a given alphabet and sequence.
	 * <p/>
	 * @param alphabet the alphabet for the mapping
	 * @param sequence the sequence from which the occurences are calculated
	 */
	private void computePv(Set<String> alphabet, List<String> sequence) {
		for (String string : sequence) {
			Integer i = pv.get(string);
			if (i == null) {
				i = 0;
			}
			pv.put(string, ++i);
		}
		for (String label : alphabet) {
			Integer count = pv.get(label);
			if (count == null) {
				pv.put(label, 0);
			}
		}
	}

	/**
	 * Compares one parikh vector with another parikh vector and returns true, when the parikh vector is smaller
	 * than the other parikh vector. That means true if all components are smaller than the other but the vector are
	 * not the same.
	 * <p/>
	 * @param v2 the parikh vector to compare with.
	 * <p/>
	 * @return true, when this parikh vector is smaller than v2.
	 * <p/>
	 * @throws StructureException is thrown if the labelsets of the parikh vectors are not the same.
	 */
	public boolean lessThan(ParikhVector v2) {
		ensureConsistency();
		Map<String, Integer> pv2 = v2.getPV();
		if (!Objects.equals(pv.keySet(), pv2.keySet())) {
			throw new StructureException("Parikhvectors are not operating on the same alphabet.");
		}
		boolean ret = false;
		for (String label : pv.keySet()) {
			if (pv.get(label).intValue() > pv2.get(label).intValue()) {
				return false;
			} else if (pv.get(label).intValue() != pv2.get(label).intValue()) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * Checks two parikh vectors if they are the same or mutally disjoint. Mutally disjoint means that it is false
	 * if there exists a component which is in both vectors not equal to zero.
	 * <p/>
	 * @param v2 the parikh vector to compare with.
	 * <p/>
	 * @return true if the parikh vectors are the same or mutally disjoint.
	 * <p/>
	 * @throws StructureException is thrown if the labelsets of the parikh vectors are not the same.
	 */
	public boolean sameOrMutuallyDisjoint(ParikhVector v2) {
		ensureConsistency();
		Map<String, Integer> pv2 = v2.getPV();
		if (!Objects.equals(pv.keySet(), pv2.keySet())) {
			throw new StructureException("Parikhvectors are not operating on the same alphabet.");
		}
		boolean same = true;
		boolean disjoint = true;
		for (String label : pv.keySet()) {
			if (pv.get(label).intValue() != 0 && pv2.get(label).intValue() != 0) {
				disjoint = false;
			}
			if (pv.get(label).intValue() != pv2.get(label).intValue()) {
				same = false;
			}
		}
		return disjoint || same;
	}

	/**
	 * Returns the occurences of the given label in this parikh vector.
	 * <p/>
	 * @param c the label to get the occurences from.
	 * <p/>
	 * @return the number of occurences of the given label in this parikh vector.
	 */
	public int get(String c) {
		ensureConsistency();
		return pv.get(c);
	}

	/**
	 * Returns the occurences of the labels in this parikh vector in lexical order.
	 * <p/>
	 * @return the parikh vector integers.
	 */
	public int[] getPVLexicalOrder() {
		ensureConsistency();
		int[] v = new int[pv.size()];
		int i = 0;
		for(int n : pv.values()) {
			v[i++] = n; 
		}
		return v;
	}

	/**
	 * Returns the mapping from labels to occurences of this parikh vector.
	 * <p/>
	 * @return the mapping from labels to occurences.
	 */
	public Map<String, Integer> getPV() {
		ensureConsistency();
		return Collections.unmodifiableMap(pv);
	}

	@Override
	public boolean equals(Object obj) {
		ensureConsistency();
		if (obj == null || !(obj instanceof ParikhVector)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		ParikhVector other = (ParikhVector) obj;
		return Objects.equals(this.pv, other.getPV());
	}

	@Override
	public int hashCode() {
		ensureConsistency();
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.pv);
		return hash;
	}

	@Override
	public String toString() {
		ensureConsistency();
		if (pv == null) {
			return "";
		}
		return pv.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
