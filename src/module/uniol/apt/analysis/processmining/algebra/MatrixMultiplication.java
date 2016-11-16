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
 * An implementation of matrix multiplication.
 * @author Uli Schlachter
 */
public class MatrixMultiplication extends AbstractMatrix implements Matrix {
	private final Matrix lhs;
	private final Matrix rhs;

	private MatrixMultiplication(Matrix lhs, Matrix rhs) {
		if (lhs.getColumns() != rhs.getRows())
			throw new IllegalArgumentException();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public int getColumns() {
		return rhs.getColumns();
	}

	@Override
	public int getRows() {
		return lhs.getRows();
	}

	@Override
	public int get(int row, int column) {
		int result = 0;
		for (int i = 0; i < lhs.getColumns(); i++)
			result += lhs.get(row, i) * rhs.get(i, column);
		return result;
	}

	@Override
	public void set(int row, int column, int value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Produce the multiplication of the given matrices
	 * @param first The first matrix to multiply with.
	 * @param other The following matrices participating in the multiplication.
	 * @return The multiplication of the arguments
	 */
	public static Matrix multiply(Matrix first, Matrix... other) {
		Matrix result = first;
		for (Matrix matrix : other)
			result = new MatrixMultiplication(result, matrix);
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
