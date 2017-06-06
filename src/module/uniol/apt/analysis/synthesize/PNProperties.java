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

import uniol.apt.util.MathTools;

/**
 * Properties that a (synthesized) Petri Net can/should satisfy. Instances of this class are immutable
 * @author Uli Schlachter
 */
public class PNProperties {
	// -1 means no k specified
	final static private int KBOUNDED_DEFAULT = -1;
	private int kBounded = KBOUNDED_DEFAULT;
	private int kMarking = 1;
	private boolean pure = false;
	private boolean plain = false;
	private boolean tnet = false;
	private boolean markedgraph = false;
	private boolean outputNonbranching = false;
	private boolean mergeFree = false;
	private boolean conflictFree = false;
	private boolean homogeneous = false;
	private boolean behaviourallyConflictFree = false;
	private boolean binaryConflictFree = false;
	private boolean equalConflict = false;

	/**
	 * Create a new, empty Petri net properties instance.
	 */
	public PNProperties() {
	}

	/**
	 * Create a copy of a PNProperties instance. This is not public because this class is immutable and thus doesn't
	 * need a public copy constructor.
	 * @param other The instance to copy.
	 */
	private PNProperties(PNProperties other) {
		kBounded = other.kBounded;
		kMarking = other.kMarking;
		pure = other.pure;
		plain = other.plain;
		tnet = other.tnet;
		markedgraph = other.markedgraph;
		outputNonbranching = other.outputNonbranching;
		mergeFree = other.mergeFree;
		conflictFree = other.conflictFree;
		homogeneous = other.homogeneous;
		behaviourallyConflictFree = other.behaviourallyConflictFree;
		binaryConflictFree = other.binaryConflictFree;
		equalConflict = other.equalConflict;
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
	 * Make sure that safeness is required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus safeness.
	 */
	public PNProperties requireSafe() {
		return requireKBounded(1);
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
	 * Make sure that at least k-boundedness is required.
	 * @param k the k for k-bounded
	 * @return A new PNProperties which expresses the same properties as this instance, plus k-boundedness.
	 */
	public PNProperties requireKBounded(int k) {
		assert k >= 0;
		if (isKBounded(k))
			return this;
		PNProperties result = new PNProperties(this);
		result.kBounded = k;
		return result;
	}

	/**
	 * Return true if this property description requires a k-marking for some k.
	 * @return true if there is some k set for which we require a k-marking
	 */
	public boolean isKMarking() {
		// Every marking is a 1-marking
		return kMarking != 1;
	}

	/**
	 * Return the k for k-marking. This may only be called if isKMarking() returns true.
	 * @return the k for k-marking
	 */
	public int getKForKMarking() {
		return kMarking;
	}

	/**
	 * Make sure that at least a k-marking is required.
	 * @param k the k for k-marking
	 * @return A new PNProperties which expresses the same properties as this instance, plus k-marking.
	 */
	public PNProperties requireKMarking(int k) {
		assert k >= 1;
		if (kMarking == k)
			return this;
		PNProperties result = new PNProperties(this);
		result.kMarking = MathTools.lcm(kMarking, k);
		return result;
	}

	/**
	 * Return true if this property description requires pureness.
	 * @return true if pure
	 */
	public boolean isPure() {
		return pure;
	}

	/**
	 * Create a new instance which differs from this one in the specified pureness requirement.
	 * @param value whether pureness should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus pureness.
	 */
	public PNProperties setPure(boolean value) {
		PNProperties result = new PNProperties(this);
		result.pure = value;
		return result;
	}

	/**
	 * Return true if this property description requires plainness.
	 * @return true if plain
	 */
	public boolean isPlain() {
		return plain;
	}

	/**
	 * Create a new instance which differs from this one in the specified plainness requirement.
	 * @param value whether plainness should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus plainess.
	 */
	public PNProperties setPlain(boolean value) {
		PNProperties result = new PNProperties(this);
		result.plain = value;
		return result;
	}

	/**
	 * Return true if this property description requires a T-Net.
	 * @return true if T-net
	 */
	public boolean isTNet() {
		return tnet;
	}

	/**
	 * Create a new instance which differs from this one in the specified T-Net requirement.
	 * @param value whether TNet should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus T-Net.
	 */
	public PNProperties setTNet(boolean value) {
		PNProperties result = new PNProperties(this);
		result.tnet = value;
		return result;
	}

	/**
	 * Return true if this property description requires a marked graph.
	 * @return true if marked graph
	 */
	public boolean isMarkedGraph() {
		return markedgraph;
	}

	/**
	 * Create a new instance which differs from this one in the specified marked graph requirement.
	 * @param value whether marked graph should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus marked graph.
	 */
	public PNProperties setMarkedGraph(boolean value) {
		PNProperties result = new PNProperties(this);
		result.markedgraph = value;
		return result;
	}

	/**
	 * Return true if this property description requires an output nonbranching PN.
	 * @return true if output nonbranching
	 */
	public boolean isOutputNonbranching() {
		return outputNonbranching;
	}

	/**
	 * Create a new instance which differs from this one in the specified output-nonbranching requirement.
	 * @param value whether output-nonbranching should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus ON.
	 */
	public PNProperties setOutputNonbranching(boolean value) {
		PNProperties result = new PNProperties(this);
		result.outputNonbranching = value;
		return result;
	}

	/**
	 * Return true if this property description requires an merge-free PN.
	 * @return true if merge-free
	 */
	public boolean isMergeFree() {
		return mergeFree;
	}

	/**
	 * Create a new instance which differs from this one in the specified merge-free requirement.
	 * @param value whether merge-free should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus merge-free.
	 */
	public PNProperties setMergeFree(boolean value) {
		PNProperties result = new PNProperties(this);
		result.mergeFree = value;
		return result;
	}

	/**
	 * Return true if this property description requires a conflict free PN.
	 * @return true if conflict free
	 */
	public boolean isConflictFree() {
		return conflictFree;
	}

	/**
	 * Create a new instance which differs from this one in the specified conflict-freeness requirement.
	 * @param value whether conflict freeness should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus CF.
	 */
	public PNProperties setConflictFree(boolean value) {
		PNProperties result = new PNProperties(this);
		result.conflictFree = value;
		return result;
	}

	/**
	 * Return true if this property description requires a homogeneous PN.
	 * @return true if homogeneous.
	 */
	public boolean isHomogeneous() {
		return homogeneous;
	}

	/**
	 * Create a new instance which differs from this one in the specified homogeneous requirement.
	 * @param value whether homogeneous should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus homogeneous.
	 */
	public PNProperties setHomogeneous(boolean value) {
		PNProperties result = new PNProperties(this);
		result.homogeneous = value;
		return result;
	}

	/**
	 * Return true if this property description requires a behaviourally conflict-free PN.
	 * @return true if BCF.
	 */
	public boolean isBehaviourallyConflictFree() {
		return behaviourallyConflictFree;
	}

	/**
	 * Create a new instance which differs from this one in the specified behaviourally conflict-freeness
	 * requirement.
	 * @param value whether BCF should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus BCF.
	 */
	public PNProperties setBehaviourallyConflictFree(boolean value) {
		PNProperties result = new PNProperties(this);
		result.behaviourallyConflictFree = value;
		return result;
	}

	/**
	 * Return true if this property description requires a binary conflict-free PN.
	 * @return true if BiCF.
	 */
	public boolean isBinaryConflictFree() {
		return binaryConflictFree;
	}

	/**
	 * Create a new instance which differs from this one in the specified binary conflict-freeness requirement.
	 * @param value whether BiCF should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus BiCF.
	 */
	public PNProperties setBinaryConflictFree(boolean value) {
		PNProperties result = new PNProperties(this);
		result.binaryConflictFree = value;
		return result;
	}

	/**
	 * Return true if this properties description requires an equal-conflict PN.
	 * @return true if EC.
	 */
	public boolean isEqualConflict() {
		return equalConflict;
	}

	/**
	 * Create a new instance which differs from this one in the specified equal-conflictness requirement.
	 * @param value whether EC should be required.
	 * @return A new PNProperties which expresses the same properties as this instance, plus EC.
	 */
	public PNProperties setEqualConflict(boolean value) {
		PNProperties result = new PNProperties(this);
		result.equalConflict = value;
		return result;
	}

	/**
	 * Test if this properties instance a superset of another instance.
	 * @param other The PNProperties instance to compare with.
	 * @return True if all requirements done by the other instance are also enforced by this.
	 */
	public boolean containsAll(PNProperties other) {
		if (other.isKBounded()) {
			if (!isKBounded() || getKForKBounded() > other.getKForKBounded())
				return false;
		}
		if (other.isKMarking()) {
			if (!isKMarking() || getKForKMarking() % other.getKForKMarking() != 0)
				return false;
		}
		if (other.isPure() && !isPure())
			return false;
		if (other.isPlain() && !isPlain())
			return false;
		if (other.isTNet() && !isTNet())
			return false;
		if (other.isMarkedGraph() && !isMarkedGraph())
			return false;
		if (other.isOutputNonbranching() && !isOutputNonbranching())
			return false;
		if (other.isMergeFree() && !isMergeFree())
			return false;
		if (other.isConflictFree() && !isConflictFree())
			return false;
		if (other.isHomogeneous() && !isHomogeneous())
			return false;
		if (other.isBehaviourallyConflictFree() && !isBehaviourallyConflictFree())
			return false;
		if (other.isBinaryConflictFree() && !isBinaryConflictFree())
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
		if (markedgraph)
			hashCode |= 1 << 3;
		if (outputNonbranching)
			hashCode |= 1 << 4;
		if (mergeFree)
			hashCode |= 1 << 5;
		if (conflictFree)
			hashCode |= 1 << 6;
		if (homogeneous)
			hashCode |= 1 << 7;
		if (behaviourallyConflictFree)
			hashCode |= 1 << 8;
		if (binaryConflictFree)
			hashCode |= 1 << 9;
		if (equalConflict)
			hashCode |= 1 << 10;
		if (isKBounded())
			hashCode |= getKForKBounded() << 11;
		if (isKMarking())
			hashCode ^= getKForKMarking() << 12;
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PNProperties))
			return false;
		PNProperties other = (PNProperties) o;
		if (kBounded != other.kBounded)
			return false;
		if (kMarking != other.kMarking)
			return false;
		if (pure != other.pure)
			return false;
		if (plain != other.plain)
			return false;
		if (tnet != other.tnet)
			return false;
		if (markedgraph != other.markedgraph)
			return false;
		if (outputNonbranching != other.outputNonbranching)
			return false;
		if (mergeFree != other.mergeFree)
			return false;
		if (conflictFree != other.conflictFree)
			return false;
		if (homogeneous != other.homogeneous)
			return false;
		if (behaviourallyConflictFree != other.behaviourallyConflictFree)
			return false;
		if (binaryConflictFree != other.binaryConflictFree)
			return false;
		if (equalConflict != other.equalConflict)
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
		if (isKMarking())
			tmpList.add(getKForKMarking() + "-marking");
		if (isPure())
			tmpList.add("pure");
		if (isPlain())
			tmpList.add("plain");
		if (isTNet())
			tmpList.add("tnet");
		if (isMarkedGraph())
			tmpList.add("marked-graph");
		if (isOutputNonbranching())
			tmpList.add("output-nonbranching");
		if (isMergeFree())
			tmpList.add("merge-free");
		if (isConflictFree())
			tmpList.add("conflict-free");
		if (isHomogeneous())
			tmpList.add("homogeneous");
		if (isBehaviourallyConflictFree())
			tmpList.add("behaviourally-conflict-free");
		if (isBinaryConflictFree())
			tmpList.add("binary-conflict-free");

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
