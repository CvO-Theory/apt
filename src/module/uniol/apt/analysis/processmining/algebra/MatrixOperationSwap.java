/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.processmining.algebra;

/**
 * Swap two rows in a matrix.
 * @author Uli Schlachter
 */
class MatrixOperationSwap implements MatrixOperation {
	private final int row1;
	private final int row2;

	/**
	 * Constructor
	 * @param row1 The first row participating in the swap.
	 * @param row2 The second row participating in the swap.
	 */
	public MatrixOperationSwap(int row1, int row2) {
		assert row1 != row2;
		this.row1 = row1;
		this.row2 = row2;
	}

	@Override
	public void applyTo(Matrix matrix) {
		for (int column = 0; column < matrix.getColumns(); column++) {
			int entry1 = matrix.get(row1, column);
			int entry2 = matrix.get(row2, column);
			matrix.set(row1, column, entry2);
			matrix.set(row2, column, entry1);
		}
	}

	@Override
	public void reverseApplyTo(Matrix matrix) {
		applyTo(matrix);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
