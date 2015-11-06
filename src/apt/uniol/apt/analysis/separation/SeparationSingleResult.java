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

package uniol.apt.analysis.separation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.analysis.language.Word;

/**
 * Class to save results of separation check
 *
 * @author Daniel
 */
public class SeparationSingleResult {

	private boolean           isSeparable_;
	private boolean           isStronglyCheck_; // or weakly
	private long              k;

	private Set<String>       notExistingTransitions;
	private boolean           isNetUnbounded_;

	private boolean           testedJustASingleSequence;
	private ArrayList<String> testedSingleSequence;
	private String            possibleWaySmallNetOrder;

	private int               numberOfTestedSequences;
	private int               lenghtOfTestedSequences;
	private ArrayList<String> notSeparableExampleSequence;


	/**
	 * Class constructor.
	 *
	 */
	public SeparationSingleResult() {
		isSeparable_    = true;
		isNetUnbounded_ = false;

		this.notExistingTransitions = new HashSet<>();
	}

	/**
	 * Result to string
	 *
	 * @return string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");

		if (isNetUnbounded_) {
			sb.append("Error: Petri net is unbounded.");

		} else if (!this.notExistingTransitions.isEmpty()) {
			for (String notExistingTransition : this.notExistingTransitions) {
				sb.append("Warning: Transition " + notExistingTransition
						+ " does not exist in Petri net.\n");
			}

		} else if (this.testedJustASingleSequence) {
			sb.append("The sequence " + outputFirableSequence(testedSingleSequence) + " is ");

			if (isStronglyCheck_) {
				if (this.isSeparable_) {
					sb.append("strongly " + this.k + "-separable.\n");
					sb.append("  e.g.: " + this.possibleWaySmallNetOrder + "\n");
				} else {
					sb.append("not strongly " + this.k + "-separable.\n");
				}
			} else {
				if (this.isSeparable_) {
					sb.append("weakly " + this.k + "-separable.\n");
				} else {
					sb.append("not weakly " + this.k + "-separable.\n");
				}
			}

		} else { // not just a single sequence
			if (isStronglyCheck_) {
				if (this.isSeparable_) {
					sb.append("All " + this.numberOfTestedSequences + " firing sequences "
							+ "up to length " + lenghtOfTestedSequences
							+ " have been tested and found to be strongly "
							+ this.k + "-separable.\n");
				} else {
					sb.append("The firing sequence "
							+ outputFirableSequence(this.notSeparableExampleSequence)
							+ " of length " + this.notSeparableExampleSequence.size()
							+ " is not strongly "
							+ this.k + "-separable.\n");
				}
			} else {
				if (this.isSeparable_) {
					sb.append("All " + this.numberOfTestedSequences + " firing sequences "
							+ "up to length " + lenghtOfTestedSequences
							+ " have been tested and found to be weakly "
							+ this.k + "-separable.\n");
				} else {
					sb.append("The firing sequence "
							+ outputFirableSequence(this.notSeparableExampleSequence)
							+ " of length " + this.notSeparableExampleSequence.size()
							+ " is not weakly "
							+ this.k + "-separable.\n");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * This method outputs a firable sequence using module system output
	 *
	 * @param firableSequence sequence to output
	 * @return String
	 */
	public static String outputFirableSequence(ArrayList<String> firableSequence) {
		return new Word(firableSequence).toString();
	}

	/**
	 * getNotExistingTransitions
	 *
	 * @return notExistingTransitions
	 */
	public Set<String> getNotExistingTransitions() {
		return notExistingTransitions;
	}

	/**
	 * addNotExistingTransition
	 *
	 * @param notExistingTransition notExistingTransition to add
	 */
	public void addNotExistingTransition(String notExistingTransition) {
		this.notExistingTransitions.add(notExistingTransition);
	}

	/**
	 * isTestedJustASingleSequence
	 *
	 * @return testedJustASingleSequence
	 */
	public boolean isTestedJustASingleSequence() {
		return testedJustASingleSequence;
	}

	/**
	 * setTestedJustASingleSequence
	 *
	 * @param testedJustASingleSequence testedJustASingleSequence
	 */
	public void setTestedJustASingleSequence(boolean testedJustASingleSequence) {
		this.testedJustASingleSequence = testedJustASingleSequence;
	}

	/**
	 * getNumberOfTestedSequences
	 *
	 * @return numberOfTestedSequences
	 */
	public int getNumberOfTestedSequences() {
		return numberOfTestedSequences;
	}

	/**
	 * setNumberOfTestedSequences
	 *
	 * @param numberOfTestedSequences numberOfTestedSequences
	 */
	public void setNumberOfTestedSequences(int numberOfTestedSequences) {
		this.numberOfTestedSequences = numberOfTestedSequences;
	}

	/**
	 * isSeparable
	 *
	 * @return isSeparable
	 */
	public boolean isSeparable() {
		return isSeparable_;
	}

	/**
	 * setSeparable
	 *
	 * @param isSeparable isSeparable
	 */
	public void setSeparable(boolean isSeparable) {
		this.isSeparable_ = isSeparable;
	}

	/**
	 * getTestedSingleSequence
	 *
	 * @return testedSingleSequence
	 */
	public ArrayList<String> getTestedSingleSequence() {
		return testedSingleSequence;
	}

	/**
	 * setTestedSingleSequence
	 *
	 * @param testedSingleSequence testedSingleSequence
	 */
	public void setTestedSingleSequence(ArrayList<String> testedSingleSequence) {
		this.testedSingleSequence = testedSingleSequence;
	}

	/**
	 * getPossibleWaySmallNetOrder
	 *
	 * @return possibleWaySmallNetOrder
	 */
	public String getPossibleWaySmallNetOrder() {
		return possibleWaySmallNetOrder;
	}

	/**
	 * setPossibleWaySmallNetOrder
	 *
	 * @param possibleWaySmallNetOrder possibleWaySmallNetOrder
	 */
	public void setPossibleWaySmallNetOrder(String possibleWaySmallNetOrder) {
		this.possibleWaySmallNetOrder = possibleWaySmallNetOrder;
	}

	/**
	 * isNetUnbounded
	 *
	 * @return isNetUnbounded
	 */
	public boolean isNetUnbounded() {
		return isNetUnbounded_;
	}

	/**
	 * setNetUnbounded
	 *
	 * @param isNetUnbounded isNetUnbounded
	 */
	public void setNetUnbounded(boolean isNetUnbounded) {
		isNetUnbounded_ = isNetUnbounded;
	}

	/**
	 * isStronglyCheck
	 *
	 * @return isStronglyCheck
	 */
	public boolean isStronglyCheck() {
		return isStronglyCheck_;
	}

	/**
	 * setStronglyCheck
	 *
	 * @param isStronglyCheck isStronglyCheck
	 */
	public void setStronglyCheck(boolean isStronglyCheck) {
		isStronglyCheck_ = isStronglyCheck;
	}

	/**
	 * getK
	 *
	 * @return k
	 */
	public long getK() {
		return k;
	}

	/**
	 * setK
	 *
	 * @param k k
	 */
	public void setK(long k) {
		this.k = k;
	}

	/**
	 * getLenghtOfTestedSequences
	 *
	 * @return lenghtOfTestedSequences
	 */
	public int getLenghtOfTestedSequences() {
		return lenghtOfTestedSequences;
	}

	/**
	 * setLenghtOfTestedSequences
	 *
	 * @param lenghtOfTestedSequences lenghtOfTestedSequences
	 */
	public void setLenghtOfTestedSequences(int lenghtOfTestedSequences) {
		this.lenghtOfTestedSequences = lenghtOfTestedSequences;
	}

	/**
	 * getNotSeparableExampleSequence
	 *
	 * @return notSeparableExampleSequence
	 */
	public ArrayList<String> getNotSeparableExampleSequence() {
		return notSeparableExampleSequence;
	}

	/**
	 * setNotSeparableExampleSequence
	 *
	 * @param notSeparableExampleSequence notSeparableExampleSequence
	 */
	public void setNotSeparableExampleSequence(ArrayList<String> notSeparableExampleSequence) {
		this.notSeparableExampleSequence = notSeparableExampleSequence;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
