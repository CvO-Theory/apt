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

/**
 * Class to save results of separation check
 *
 * @author Daniel
 */
public class SeparationResult {
	/**
	 * For every k. See singleResults for result of just one test
	 */
	private boolean separable = true;

	private ArrayList<SeparationSingleResult> singleResults;

	private boolean isStronglyCheck_; // or weakly

	private long kMax;
	private long k;

	private ArrayList<String> testedSequence;
	private boolean           testedJustASingleSequence;
	private int               maxLength;

	private boolean noMarks;
	private boolean kGreaterKMax;
	private boolean kNoDivisorOfKMax;
	private boolean noSequenceOfNet;
	private boolean noCounterExampleFound;

	/**
	 * Class constructor.
	 *
	 */
	public SeparationResult() {
		this.singleResults = new ArrayList<>();

		this.noMarks = false;
		this.kGreaterKMax = false;
		this.kNoDivisorOfKMax = false;
		this.noSequenceOfNet = false;
		this.noCounterExampleFound = false;

	}

	/**
	 * Result to string
	 *
	 * @return string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\n");


		/*if (this.singleResults.get(0).isNetUnbounded()) {
				sb.append("Error: Net is unbounded.");
				} else*/ if (this.testedJustASingleSequence) {

			if (this.noMarks) {
				sb.append("The initial marking is a k-marking, for every k>" + kMax + ".\n\n"
						+ "The sequence "
						+ SeparationSingleResult.outputFirableSequence(testedSequence));

				if (isStronglyCheck_) {
					sb.append(" is " + "strongly k-separable, for every k>=1.\n");
				} else {
					sb.append(" is " + "weakly k-separable, for every k>=1.\n");
				}

			} else if (this.kGreaterKMax) {
				sb.append("The initial marking is a " + kMax + "-marking "
						+ "but not an L-marking for any L>" + kMax + ".\n"
						+ "However, the chosen k=" + k + " is >" + kMax + ".");

			} else if (this.kNoDivisorOfKMax) {
				sb.append("The initial marking is a " + kMax + "-marking "
						+ "but not an L-marking for any L>" + kMax + ".\n"
						+ "k=" + k + " is not a divisor of " + kMax + ".");

			} else { // k is ok
				sb.append("The initial marking is a " + kMax
						+ "-marking but not an L-marking for any L>" + kMax + ".\n\n");

				if (this.noSequenceOfNet) {
					sb.append("The sequence "
							+ SeparationSingleResult.outputFirableSequence(testedSequence)
							+ " is not a firing sequence of the Petri net.");
				} else { // is a sequence

					// single results
					for (SeparationSingleResult singleResult : this.singleResults) {
						sb.append(singleResult.toString());
					}

					if (this.noCounterExampleFound) {
						sb.append("\nThe sequence "
							+ SeparationSingleResult.outputFirableSequence(
									testedSequence) + " is ");
						if (isStronglyCheck_) {
							sb.append("strongly d-separable, "
									+ "for any divisor d of " + k + ".\n");
						} else {
							sb.append("weakly d-separable, "
									+ "for any divisor d of " + k + ".\n");
						}
					}

				}
			}

		} else { // not just a single sequence

			if (this.noMarks) {
				sb.append("The initial marking is a k-marking for every k>" + kMax + ".\n\n"
						+ "All 1 firing sequence "
						+ "up to length " + maxLength);

				if (isStronglyCheck_) {
					sb.append(" have been tested and found to be strongly k-separable.\n");
				} else {
					sb.append(" have been tested and found to be weakly k-separable.\n");
				}

			} else if (this.kGreaterKMax) {
				sb.append("The initial marking is a " + kMax + "-marking "
						+ "but not an L-marking for any L>" + kMax + ".\n"
						+ "However, the chosen k=" + k + " is >" + kMax + ".");

			} else if (this.kNoDivisorOfKMax) {
				sb.append("The initial marking is a " + kMax + "-marking "
						+ "but not an L-marking for any L>" + kMax + ".\n"
						+ "k=" + k + " is not a divisor of " + kMax + ".");

			} else { // k is ok
				sb.append("The initial marking is a " + kMax
						+ "-marking but not an L-marking for any L>" + kMax + ".\n\n");

				// single results
				for (SeparationSingleResult singleResult : this.singleResults) {
					sb.append(singleResult.toString());
				}

				if (this.noCounterExampleFound) {
					sb.append("\nAll firing sequences " + "up to length " + maxLength
							+ " have been tested and found to be ");
					if (isStronglyCheck_) {
						sb.append("strongly d-separable, for any divisor d of " + k + ".\n");
					} else {
						sb.append("weakly d-separable, for any divisor d of " + k + ".\n");
					}
				}
			}
		}

		return sb.toString();
	}

	/**
	 * isNoMarks
	 *
	 * @return noMarks
	 */
	public boolean isNoMarks() {
		return noMarks;
	}

	/**
	 * setNoMarks
	 *
	 * @param noMarks noMarks
	 */
	public void setNoMarks(boolean noMarks) {
		this.noMarks = noMarks;
	}

	/**
	 * isSeparable
	 *
	 * @return separable
	 */
	public boolean isSeparable() {
		return separable;
	}

	/**
	 * setSeparable
	 *
	 * @param separable separable
	 */
	public void setSeparable(boolean separable) {
		this.separable = separable;
	}

	/**
	 * getkMax
	 *
	 * @return kMax
	 */
	public long getkMax() {
		return kMax;
	}

	/**
	 * setkMax
	 *
	 * @param kMax kMax
	 */
	public void setkMax(long kMax) {
		this.kMax = kMax;
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
	 * getTestedSequence
	 *
	 * @return testedSequence
	 */
	public ArrayList<String> getTestedSequence() {
		return testedSequence;
	}

	/**
	 * setTestedSequence
	 *
	 * @param testedSequence testedSequence
	 */
	public void setTestedSequence(ArrayList<String> testedSequence) {
		this.testedSequence = testedSequence;
	}

	/**
	 * iskGreaterKMax
	 *
	 * @return kGreaterKMax
	 */
	public boolean iskGreaterKMax() {
		return kGreaterKMax;
	}

	/**
	 * setkGreaterKMax
	 *
	 * @param kGreaterKMax kGreaterKMax
	 */
	public void setkGreaterKMax(boolean kGreaterKMax) {
		this.kGreaterKMax = kGreaterKMax;
	}

	/**
	 * iskNoDivisorOfKMax
	 *
	 * @return kNoDivisorOfKMax
	 */
	public boolean iskNoDivisorOfKMax() {
		return kNoDivisorOfKMax;
	}

	/**
	 * setkNoDivisorOfKMax
	 *
	 * @param kNoDivisorOfKMax kNoDivisorOfKMax
	 */
	public void setkNoDivisorOfKMax(boolean kNoDivisorOfKMax) {
		this.kNoDivisorOfKMax = kNoDivisorOfKMax;
	}

	/**
	 * isNoSequenceOfNet
	 *
	 * @return noSequenceOfNet
	 */
	public boolean isNoSequenceOfNet() {
		return noSequenceOfNet;
	}

	/**
	 * setNoSequenceOfNet
	 *
	 * @param noSequenceOfNet noSequenceOfNet
	 */
	public void setNoSequenceOfNet(boolean noSequenceOfNet) {
		this.noSequenceOfNet = noSequenceOfNet;
	}

	/**
	 * getSingleResults
	 *
	 * @return singleResults
	 */
	public ArrayList<SeparationSingleResult> getSingleResults() {
		return singleResults;
	}

	/**
	 * addSingleResult
	 *
	 * @param singleResult singleResult to add
	 */
	public void addSingleResult(SeparationSingleResult singleResult) {
		this.singleResults.add(singleResult);
	}

	/**
	 * isNoCounterExampleFound
	 *
	 * @return noCounterExampleFound
	 */
	public boolean isNoCounterExampleFound() {
		return noCounterExampleFound;
	}

	/**
	 * setNoCounterExampleFound
	 *
	 * @param noCounterExampleFound noCounterExampleFound
	 */
	public void setNoCounterExampleFound(boolean noCounterExampleFound) {
		this.noCounterExampleFound = noCounterExampleFound;
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
	 * getMaxLength
	 *
	 * @return maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * setMaxLength
	 *
	 * @param maxLength maxLength
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
