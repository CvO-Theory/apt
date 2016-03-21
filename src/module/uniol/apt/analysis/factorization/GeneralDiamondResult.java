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
 * Result class for GeneralDiamond class that may contain a witness.
 *
 * @author Jonas Prellberg
 *
 */
public class GeneralDiamondResult {

	private final boolean isGdiam;
	private final State witnessState;
	private final String witnessLabel1;
	private final String witnessLabel2;
	private final boolean witnessLabel1Forward;
	private final boolean witnessLabel2Forward;

	/**
	 * Creates a new positive GeneralDiamondResult with witness values left
	 * empty.
	 */
	public GeneralDiamondResult() {
		this.isGdiam = true;
		this.witnessState = null;
		this.witnessLabel1 = "";
		this.witnessLabel2 = "";
		this.witnessLabel1Forward = true;
		this.witnessLabel2Forward = true;
	}

	/**
	 * Creates a new negative GeneralDiamondResult with witness values that
	 * specify why the LTS is not a T'-gdiam.
	 *
	 * @param witnessState
	 *                the state caused the failing check
	 * @param label1
	 *                the first label that caused the failing check
	 * @param label2
	 *                the second label that caused the failing check
	 * @param label1Forward
	 *                direction of the first label
	 * @param label2Forward
	 *                direction of the second label
	 */
	public GeneralDiamondResult(State witnessState, String label1, String label2, boolean label1Forward,
			boolean label2Forward) {
		this.isGdiam = false;
		this.witnessState = witnessState;
		this.witnessLabel1 = label1;
		this.witnessLabel2 = label2;
		this.witnessLabel1Forward = label1Forward;
		this.witnessLabel2Forward = label2Forward;
	}

	/**
	 * Returns the state that failed the gdiam-check.
	 *
	 * @return the witness state
	 */
	public State getWitnessState() {
		return witnessState;
	}

	/**
	 * Returns the first of the two labels that were tested when the
	 * gdiam-check failed.
	 *
	 * @return the first witness label
	 */
	public String getWitnessLabel1() {
		return witnessLabel1;
	}

	/**
	 * Returns the second of the two labels that were tested when the
	 * gdiam-check failed.
	 *
	 * @return the second witness label
	 */
	public String getWitnessLabel2() {
		return witnessLabel2;
	}

	/**
	 * Returns the direction in which the first label was followed.
	 *
	 * @return true, if the first label was followed in forward direction
	 */
	public boolean isWitnessLabel1Forward() {
		return witnessLabel1Forward;
	}

	/**
	 * Returns the direction in which the second label was followed.
	 *
	 * @return true, if the second label was followed in forward direction
	 */
	public boolean isWitnessLabel2Forward() {
		return witnessLabel2Forward;
	}

	/**
	 * Returns if the LTS is a T'-gdiam.
	 *
	 * @return true, if the LTS is a T'-gdiam
	 */
	public boolean isGdiam() {
		return isGdiam;
	}

	/**
	 * Returns a formatted string that contains label and direction for the
	 * first label.
	 *
	 * @return a string of the format "label (direction)"
	 */
	public String getWitnessLabel1String() {
		return String.format("%s (%s)", witnessLabel1, witnessLabel1Forward ? "forward" : "backward");
	}

	/**
	 * Returns a formatted string that contains label and direction for the
	 * second label.
	 *
	 * @return a string of the format "label (direction)"
	 */
	public String getWitnessLabel2String() {
		return String.format("%s (%s)", witnessLabel2, witnessLabel2Forward ? "forward" : "backward");
	}

	@Override
	public String toString() {
		return "GeneralDiamondResult [isGdiam=" + isGdiam + ", witnessState=" + witnessState + ", label1="
				+ witnessLabel1 + ", label2=" + witnessLabel2 + ", label1Forward="
				+ witnessLabel1Forward + ", label2Forward=" + witnessLabel2Forward + "]";
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
