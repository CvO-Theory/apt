/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Properties that a (synthesized) Petri Net can/should satisfy.
 * @author Uli Schlachter
 */
public class PNProperties {
	// -1 means no k specified
	final static private int KBOUNDED_DEFAULT = -1;
	private int kBounded = KBOUNDED_DEFAULT;
	private boolean pure = false;
	private boolean plain = false;
	private boolean tnet = false;
	private boolean outputNonbranching = false;
	private boolean conflictFree = false;

	/**
	 * Create a new, empty Petri net properties instance.
	 */
	public PNProperties() {
	}

	/**
	 * Create a copy of a PNProperties instance.
	 */
	public PNProperties(PNProperties other) {
		kBounded = other.kBounded;
		pure = other.pure;
		plain = other.plain;
		tnet = other.tnet;
		outputNonbranching = other.outputNonbranching;
		conflictFree = other.conflictFree;
	}

	/**
	 * Return true if this property description requires k-boundedness for some k.
	 * @return true if there is some k set for which we require k-boundedness
	 */
	public boolean isKBounded() {
		return kBounded != KBOUNDED_DEFAULT;
	}

	/**
	 * Return true if this property description requires k-boundedness.
	 * @param k The k for k-boundedness
	 * @return true if k-bounded
	 */
	public boolean isKBounded(int k) {
		return isKBounded() && kBounded <= k;
	}

	/**
	 * Return true if this property description requires safeness.
	 * @return true if safe
	 */
	public boolean isSafe() {
		return isKBounded(1);
	}

	/**
	 * Make sure that that this instance requires safeness.
	 */
	public void requireSafe() {
		requireKBounded(1);
	}

	/**
	 * Return the k for k-bounded. This may only be called if isKBounded() returns true.
	 * @return the k for k-bounded
	 */
	public int getKForKBounded() {
		assert isKBounded();
		return kBounded;
	}

	/**
	 * Make sure this instance requires at least k-boundedness.
	 * @param k the k for k-bounded
	 */
	public void requireKBounded(int k) {
		assert k >= 0;
		if (!isKBounded(k))
			kBounded = k;
	}

	/**
	 * Set the k for k-bounded.
	 * @param k The new value
	 */
	public void setKBounded(int k) {
		assert k >= 0;
		kBounded = k;
	}

	/**
	 * Return true if this property description requires pureness.
	 * @return true if pure
	 */
	public boolean isPure() {
		return pure;
	}

	/**
	 * Set the pureness value of this property description.
	 * @param value whether pureness should be required.
	 */
	public void setPure(boolean value) {
		pure = value;
	}

	/**
	 * Return true if this property description requires plainness.
	 * @return true if plain
	 */
	public boolean isPlain() {
		return plain;
	}

	/**
	 * Set the plainness value of this property description.
	 * @param value whether plainness should be required.
	 */
	public void setPlain(boolean value) {
		plain = value;
	}

	/**
	 * Return true if this property description requires a T-Net.
	 * @return true if T-net
	 */
	public boolean isTNet() {
		return tnet;
	}

	/**
	 * Set the TNet value of this property description.
	 * @param value whether TNet should be required.
	 */
	public void setTNet(boolean value) {
		tnet = value;
	}

	/**
	 * Return true if this property description requires an output nonbranching PN.
	 * @return true if output nonbranching
	 */
	public boolean isOutputNonbranching() {
		return outputNonbranching;
	}

	/**
	 * Set the output-nonbranching value of this property description.
	 * @param value whether output-nonbranching should be required.
	 */
	public void setOutputNonbranching(boolean value) {
		outputNonbranching = value;
	}

	/**
	 * Return true if this property description requires a conflict free PN.
	 * @return true if conflict free
	 */
	public boolean isConflictFree() {
		return conflictFree;
	}

	/**
	 * Set the conflict freeness value of this property description.
	 * @param value whether conflict freeness should be required.
	 */
	public void setConflictFree(boolean value) {
		conflictFree = value;
	}

	/**
	 * Test if this properties instance a superset of another instance.
	 */
	public boolean containsAll(PNProperties other) {
		if (other.isKBounded()) {
			if (!isKBounded() || getKForKBounded() > other.getKForKBounded())
				return false;
		}
		if (other.isPure() && !isPure())
			return false;
		if (other.isPlain() && !isPlain())
			return false;
		if (other.isTNet() && !isTNet())
			return false;
		if (other.isOutputNonbranching() && !isOutputNonbranching())
			return false;
		if (other.isConflictFree() && !isConflictFree())
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (pure)
			hashCode |= 1 << 0;
		if (plain)
			hashCode |= 1 << 1;
		if (tnet)
			hashCode |= 1 << 2;
		if (outputNonbranching)
			hashCode |= 1 << 3;
		if (conflictFree)
			hashCode |= 1 << 4;
		if (isKBounded())
			hashCode |= getKForKBounded() << 5;
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PNProperties))
			return false;
		PNProperties other = (PNProperties) o;
		if (kBounded != other.kBounded)
			return false;
		if (pure != other.pure)
			return false;
		if (plain != other.plain)
			return false;
		if (tnet != other.tnet)
			return false;
		if (outputNonbranching != other.outputNonbranching)
			return false;
		if (conflictFree != other.conflictFree)
			return false;
		return true;
	}

	@Override
	public String toString() {
		List<String> tmpList = new ArrayList<>();

		if (isSafe())
			tmpList.add("safe");
		else if (isKBounded())
			tmpList.add(getKForKBounded() + "-bounded");
		if (isPure())
			tmpList.add("pure");
		if (isPlain())
			tmpList.add("plain");
		if (isTNet())
			tmpList.add("tnet");
		if (isOutputNonbranching())
			tmpList.add("output-nonbranching");
		if (isConflictFree())
			tmpList.add("conflict-free");

		if (tmpList.isEmpty())
			return "[]";

		Iterator<String> iter = tmpList.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(iter.next());
		while (iter.hasNext()) {
			sb.append(", ");
			sb.append(iter.next());
		}

		sb.append(']');
		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
