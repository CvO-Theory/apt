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

package uniol.apt.check;

import java.util.ArrayList;

/**
 * This class manages the parameter for check
 *
 * @author Daniel
 *
 */
public class CheckParameters extends ArrayList<Integer> {

	/**
	 * Enum to describe modify type
	 */
	public enum ParameterModifyType {
		BruteForce, Score
	}

	private static final long serialVersionUID = 43;

	private int minValue;

	private ParameterModifyType modifyMode;

	private boolean active;

	/**
	 * Class constructor
	 */
	public CheckParameters() {
		minValue   = 0;
		modifyMode = ParameterModifyType.BruteForce;

		active = true;
	}

	/**
	 * Set number of parameters
	 *
	 * Note: first set min value
	 *
	 * @param number number of parameters
	 */
	public void setNumberOfParameters(int number) {
		for (int i = 0; i < number; i++) {
			this.add(this.minValue);
		}
	}


	/**
	 * Modify parameter for next run
	 *
	 * @param score score information
	 * @param markingFactor all k (from k-marking, k-separation) multiplied
	 */
	public void modify(int score, int markingFactor) {

		if (active) {

			int greatestNumber = this.getGreatestNumber();

			if (modifyMode == ParameterModifyType.BruteForce) {

				// from 1 -> 2 or from 1,1 to 2,1 or from 1,1,1 to 2,1,1
				// or from 2,2,2 to 3,1,1... (if minValue = 1)
				if (this.areAllNumbersEqual()) {
					this.set(0, greatestNumber + 1);
					for (int index = 1; index < this.size(); index++) {
						this.set(index, this.minValue);
					}
				} else { // increment
					this.increment(greatestNumber);

					// without greatestNumber: this was already tested.
					while (!this.contains(greatestNumber)) {
						this.increment(greatestNumber);
					}
				}
			} else if (modifyMode == ParameterModifyType.Score) {
				this.set(0, score);
				this.set(1, markingFactor);
			}
		}
	}

	private void increment(int greatestNumber) {
		// increment
		this.set(0, this.get(0) + 1);

		// handle overflow
		for (int index = 1; index < this.size(); index++) {
			if (this.get(index - 1) > greatestNumber) {
				this.set(index - 1, minValue);
				this.set(index, this.get(index) + 1);
			}
		}
	}

	private int getGreatestNumber() {
		int greatestNumber = minValue;

		for (int number : this) {
			if (number > greatestNumber) {
				greatestNumber = number;
			}
		}

		return greatestNumber;
	}

	private boolean areAllNumbersEqual() {

		int refNumber = this.get(0);

		for (int number : this) {
			if (number != refNumber) {
				return false;
			}
		}

		return true;
	}


	/**
	 * Get min value
	 *
	 * @return min value
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * Set min value
	 *
	 * @param minValue min value
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	/**
	 * get modify mode
	 *
	 * @return modify mode
	 */
	public ParameterModifyType getModifyMode() {
		return modifyMode;
	}

	/**
	 * set modify mode
	 *
	 * @param modifyMode modify mode
	 */
	public void setModifyMode(ParameterModifyType modifyMode) {
		this.modifyMode = modifyMode;
	}

	/**
	 * is active?
	 *
	 * @return active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set active
	 *
	 * Note: no changes will happen if not active
	 *
	 * @param active active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
