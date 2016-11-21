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
 * An operation on matrices that adds a multiple of some row to another row
 * @author Uli Schlachter
 */
class MatrixOperationAddMultipleOf implements MatrixOperation {
	private final int rowToAdd;
	private final int rowToAddTo;
	private final int factor;

	/**
	 * Constructor
	 * @param rowToAdd The row that should be added to some other row.
	 * @param rowToAddTo The row that should get something added.
	 * @param factor The factor that should be applied to the row that is being added.
	 */
	public MatrixOperationAddMultipleOf(int rowToAdd, int rowToAddTo, int factor) {
		assert rowToAdd != rowToAddTo;
		this.rowToAdd = rowToAdd;
		this.rowToAddTo = rowToAddTo;
		this.factor = factor;
	}

	@Override
	public void applyTo(Matrix matrix) {
		addMultipleToRow(matrix, rowToAdd, rowToAddTo, factor);
	}

	@Override
	public void reverseApplyTo(Matrix matrix) {
		addMultipleToRow(matrix, rowToAdd, rowToAddTo, -factor);
	}

	static private void addMultipleToRow(Matrix matrix, int rowToAdd, int rowToAddTo, int factor) {
		for (int column = 0; column < matrix.getColumns(); column++) {
			int entryAdd = matrix.get(rowToAdd, column);
			int entryAddTo = matrix.get(rowToAddTo, column);
			matrix.set(rowToAddTo, column, entryAddTo + factor * entryAdd);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
