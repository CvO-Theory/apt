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
 * An implementation of the {@link Matrix}-interface based on arrays.
 * @author Uli Schlachter
 */
public class ArrayMatrix extends AbstractMatrix implements Matrix {
	final private int[][] elements;

	/**
	 * Create a copy of the given matrix.
	 * @param matrix The matrix to copy.
	 */
	public ArrayMatrix(Matrix matrix) {
		int rows = matrix.getRows();
		int columns = matrix.getColumns();

		elements = new int[rows][];
		for (int row = 0; row < rows; row++) {
			elements[row] = new int[columns];
			for (int column = 0; column < columns; column++)
				elements[row][column] = matrix.get(row, column);
		}
	}

	private ArrayMatrix(int[][] elements) {
		this.elements = elements;
	}

	@Override
	public int getColumns() {
		return elements[0].length;
	}

	@Override
	public int getRows() {
		return elements.length;
	}

	@Override
	public int get(int row, int column) {
		return elements[row][column];
	}

	@Override
	public void set(int row, int column, int value) {
		elements[row][column] = value;
	}

	/**
	 * Create an identity matrix with the given dimensions.
	 * @param rows The number of rows that should be created.
	 * @param columns The number of columns that should be created.
	 * @return An identity matrix with the given dimensions.
	 */
	public static Matrix createIdentityMatrix(int rows, int columns) {
		if (rows < 1 || columns < 1)
			throw new IllegalArgumentException();

		int[][] elements = new int[rows][];
		for (int row = 0; row < rows; row++) {
			elements[row] = new int[columns];
			if (row < columns)
				elements[row][row] = 1;
		}

		return new ArrayMatrix(elements);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
