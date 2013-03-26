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

package uniol.apt.analysis.cycles.lts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uniol.apt.adt.ts.ParikhVector;

/**
 * A datastructure for saving a parikh vector and a sequence. It is not checked if they are belonging together but it is
 * intended.
 * <p/>
 * @author Manuel Gieseking
 */
public class PVwithSequence {

	private ParikhVector pv;
	private List<String> sequence;

	/**
	 * Creates a new PVwithSequence by saving the giving parikh vector and sequence.
	 * <p/>
	 * @param pv       - the parikh vector to save.
	 * @param sequence - the string sequence to save.
	 */
	public PVwithSequence(ParikhVector pv, List<String> sequence) {
		this.pv = pv;
		this.sequence = sequence;
	}

	/**
	 * Adds to given PVwithSequences together to a new one. That means their sequences will be concatenate and their
	 * parikh vectors added.
	 * <p/>
	 * @param first  - the first parikh vector and sequence to add. The left side of the concatenation.
	 * @param second - the second parikh vector and sequence to add. The right side of the concatenation.
	 * <p/>
	 * @return a new generated parikh vector with sequence, with the addition of the parikh vectors and
	 *         concatenation of the sequences of the given parikh vectors with there sequences.
	 */
	public static PVwithSequence add(PVwithSequence first, PVwithSequence second) {
		ParikhVector p = ParikhVector.add(first.getPv(), second.getPv());
		List<String> list = new ArrayList<>();
		list.addAll(first.getSequence());
		list.addAll(second.getSequence());
		return new PVwithSequence(p, list);
	}

	/**
	 * Glues to given PVwithSequences together to a new one. That means their parikh vectors will be added and there
	 * sequences will be concatenated, but the last component of the first sequence is replaced by the first
	 * component of the second sequence.
	 * <p/>
	 * @param first  - the first parikh vector and sequence to glue. The left side of the concatenation.
	 * @param second - the second parikh vector and sequence to glue. The right side of the concatenation.
	 * <p/>
	 * @return a new generated parikh vector with sequence, with the addition of the parikh vectors and gluing
	 *         concatenation of the sequences of the given parikh vectors with there sequences.
	 */
	public static PVwithSequence glue(PVwithSequence first, PVwithSequence second) {
		ParikhVector p = ParikhVector.add(first.getPv(), second.getPv());
		List<String> list = new ArrayList<>();
		list.addAll(first.getSequence());
		list.remove(list.size() - 1);
		list.addAll(second.getSequence());
		return new PVwithSequence(p, list);
	}

	/**
	 * Returns the parikh vector.
	 * <p/>
	 * @return the parikh vector.
	 */
	public ParikhVector getPv() {
		return pv;
	}

	/**
	 * Sets the parikh vector.
	 * <p/>
	 * @param pv - the parikh vector to set.
	 */
	public void setPv(ParikhVector pv) {
		this.pv = pv;
	}

	/**
	 * Returns the sequence of strings.
	 * <p/>
	 * @return the sequence of strings.
	 */
	public List<String> getSequence() {
		return sequence;
	}

	/**
	 * Sets the sequence of strings.
	 * <p/>
	 * @param sequence - the sequence of strings to set.
	 */
	public void setSequence(List<String> sequence) {
		this.sequence = sequence;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.pv);
		hash = 19 * hash + Objects.hashCode(this.sequence);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PVwithSequence other = (PVwithSequence) obj;
		if (!Objects.equals(this.pv, other.pv)) {
			return false;
		}
		if (!Objects.equals(this.sequence, other.sequence)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PVwithSequence{" + "pv=" + pv + ", sequence=" + sequence + '}';
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
