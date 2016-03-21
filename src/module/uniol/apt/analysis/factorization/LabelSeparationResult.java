/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.factorization;

import uniol.apt.adt.ts.State;

/**
 * A result class for LabelSeparation that may also contain witness states.
 *
 * @author Jonas Prellberg
 *
 */
public class LabelSeparationResult {

	private final boolean isSeparated;
	private final State witnessState1;
	private final State witnessState2;

	/**
	 * Constructs a new positive LabelSeparationResult.
	 */
	public LabelSeparationResult() {
		this.isSeparated = true;
		this.witnessState1 = null;
		this.witnessState2 = null;
	}

	/**
	 * Constructs a new negative LabelSeparationResult with the two states
	 * as witnesses.
	 *
	 * @param witnessState1
	 *                first witness
	 * @param witnessState2
	 *                second witness
	 */
	public LabelSeparationResult(State witnessState1, State witnessState2) {
		this.isSeparated = false;
		this.witnessState1 = witnessState1;
		this.witnessState2 = witnessState2;
	}

	/**
	 * Returns if the LTS is T'-separated.
	 *
	 * @return true, if the LTS is T'-separated
	 */
	public boolean isSeparated() {
		return isSeparated;
	}

	/**
	 * Returns the first witness state if the LTS is not T'-separated.
	 *
	 * @return the first witness state or null if the LTS is T'-separated
	 */
	public State getWitnessState1() {
		return witnessState1;
	}

	/**
	 * Returns the second witness state if the LTS is not T'-separated.
	 *
	 * @return the second witness state or null if the LTS is T'-separated
	 */
	public State getWitnessState2() {
		return witnessState2;
	}

	@Override
	public String toString() {
		return "LabelSeparationResult [isSeparated=" + isSeparated + ", witnessState1=" + witnessState1
				+ ", witnessState2=" + witnessState2 + "]";
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
