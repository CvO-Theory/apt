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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.analysis.processmining.algebra.TransposedMatrix.transpose;
import static uniol.apt.analysis.processmining.algebra.MatrixMultiplication.multiply;
import static uniol.apt.util.DebugUtil.debugFormat;
import static uniol.apt.util.DebugUtil.debug;

/**
 * Calculate the smith normal form of a matrix including the invertible transformations. Given a matrix M, this computes
 * matrices L, M' and R so that L and R are invertible, M' is a diagonal matrix whose diagonal entries are "ascendingly
 * dividing" and M=L * M' * R holds.
 * @author Uli Schlachter
 */
public class SmithNormalForm {
	private final int rows;
	private final int columns;
	private final List<MatrixOperation> rowOperations = new ArrayList<>();
	private final List<MatrixOperation> columnOperations = new ArrayList<>();
	private final List<Integer> diagonalEntries = new ArrayList<>();

	/**
	 * Create the smith normal form of the given matrix.
	 * @param input The matrix to use
	 */
	public SmithNormalForm(Matrix input) {
		rows = input.getRows();
		columns = input.getColumns();
		new Calculator(input);
	}

	// Helper class that does all the work of calculating the Smith normal form
	private class Calculator {
		private final Matrix input;
		private final Matrix matrix;

		private Calculator(Matrix input) {
			this.input = input;
			this.matrix = new ArrayMatrix(input);
			testInvariants();

			debug("Generating Smith normal form of ", matrix);

			// Step 1: Generate a diagonal form
			diagonalise();

			// Step 2: Make sure the entries in the diagonal are properly ordered
			// (Increasing order so that entry a_i divides entry a_{i+1}; all zeroes at the end)
			orderDiagonalIncreasingly();

			// Extract the resulting diagonal entries and asserts that they are increasingly dividing
			int last = 0;
			for (int i = 0; i < Math.min(rows, columns); i++) {
				int entry = matrix.get(i, i);
				diagonalEntries.add(entry);

				assert entry >= 0 : entry;

				// Either this is the first entry (no last), or we already reached "all the zeros" or
				// things are dividing each other.
				assert i == 0 || (last == 0 && entry == 0) || (last != 0 && entry % last == 0) : matrix;
				last = entry;
			}
			assert isDiagonalMatrix(matrix);
			testInvariants();
		}

		// Bring the matrix into a diagonal form
		private void diagonalise() {
			for (int i = 0; i < Math.min(rows, columns); i++) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				boolean reduced;
				do {
					reduced = true;

					// Step 1a: Reduce each row so that all but one entries are zero; non-zero entry
					// on the diagonal (if any)
					reduced &= reduceRow(i);
					testInvariants();

					// Step 1b: Reduce the columns in the same way; non-zero entry on the diagonal
					// (if any)
					reduced &= reduceColumn(i);
					testInvariants();

					debug("After reducing with i=", i, " once, the result is ", matrix);
				} while (!reduced);
			}

			debug("Done with diagonalisation, result is ", matrix);
			assert isDiagonalMatrix(matrix) : matrix;
		}

		// Order the entries on the diagonal so that they are increasingly dividing
		private void orderDiagonalIncreasingly() {
			assert isDiagonalMatrix(matrix) : matrix;

			for (int i = 0; i < Math.min(rows, columns) - 1; i++) {
				orderSuccessiveDiagonalEntries(i);

				// Make sure the diagonal entries are non-negative
				if (matrix.get(i, i) < 0)
					invertRow(i);
				testInvariants();

				if (i > 0) {
					int previousEntry = matrix.get(i - 1, i - 1);
					int thisEntry = matrix.get(i, i);
					if (previousEntry != 0 && thisEntry % previousEntry != 0) {
						// We have to swap this value further up in the diagonal
						debug("Re-examining previous diagonal entry");
						i -= 2;
					}
				}
			}

			// For all but the last diagonal entry, the previous loop guarantees non-negativity
			int lastDiagonalEntry = Math.min(rows, columns) - 1;
			if (matrix.get(lastDiagonalEntry, lastDiagonalEntry) < 0)
				invertRow(lastDiagonalEntry);

			debug("After cleaning up the diagonal: ", matrix);

			assert isDiagonalMatrix(matrix);
			testInvariants();
		}

		// Establish that (i,i) divides (i+1,i+1)
		private void orderSuccessiveDiagonalEntries(int i) {
			while (true) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				int entry = matrix.get(i, i);
				int nextEntry = matrix.get(i + 1, i + 1);

				debugFormat("Entry (%d,%d) = %d and next one is (%d,%d)=%d",
						i, i, entry, i + 1, i + 1, nextEntry);

				// If next one is smaller, swap the diagonal entries
				if (nextEntry != 0 && Math.abs(nextEntry) < Math.abs(entry)) {
					int tmp = entry;
					entry = nextEntry;
					nextEntry = tmp;

					debug("Swapping (absolutely) smaller value up");
					swapRows(i, i + 1);
					swapColumns(i, i + 1);
				}

				// If they are not suitably dividing, do something to improve this
				if (entry == 0 || nextEntry % entry == 0)
					return;

				debug("Trying to construct GCD/LCM of these entries");

				// For this, first bring the value at (i+1,i+1) to (i+1,i).
				addMultipleToRow(i + 1, i, 1);

				// Then re-establish diagonal form (this could be optimised: reduce() will scan the
				// whole row/column, but we already know that there are at most two non-zero entries
				// whose GCD needs to be computed)
				boolean done = false;
				while (!done) {
					done = true;

					done &= reduceRow(i);
					debug("After reducing row ", i, " again: ", matrix);

					done &= reduceColumn(i);
					debug("After also reducing column ", i, " again: ", matrix);
				}

				// It might be that we didn't actually get the result yet (due to interplay between
				// rows and columns).
			}
		}

		private boolean reduceRow(int row) {
			return reduce(row, false);
		}

		private boolean reduceColumn(int row) {
			return reduce(row, true);
		}

		// Reduce the given row/column so that only a single non-zero entry on the diagonal remains, returns
		// true if nothing was done (i.e. the row/column is already reduced)
		private boolean reduce(int row, boolean swapped) {
			debugFormat("Reducing row %d of %s%s", row, matrix, swapped ? " (swapped)" : "");

			boolean didNothing = true;
			boolean didNothingInLastLoop;
			Matrix state = swapped ? transpose(matrix) : matrix;
			Integer pivotColumn;

			do {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				didNothingInLastLoop = true;
				// Pick the entry with the lowest absolute value on the row and reduce all other row
				// entries; repeat as often as possible
				pivotColumn = findPivotInRow(state, row);
				debugFormat("Pivot column is %s", pivotColumn);
				if (pivotColumn == null) {
					// This means the row only has zeros
					assert didNothing;
					return true;
				}

				boolean didSomething = false;
				int pivotEntry = state.get(row, pivotColumn);
				assert pivotEntry != 0;
				for (int column = 0; column < state.getColumns(); column++) {
					if (column == pivotColumn)
						continue;

					int entry = state.get(row, column);
					// Division rounds towards zero; that's just what we want here!
					int factor = -entry / pivotEntry;
					if (factor != 0) {
						didNothingInLastLoop = false;
						didNothing = false;
						if (!swapped)
							addMultipleToColumn(pivotColumn, column, factor);
						else
							addMultipleToRow(pivotColumn, column, factor);
					}
				}
			} while (!didNothingInLastLoop);

			// We are done. Now move the pivot element into the diagonal
			if (pivotColumn != row) {
				didNothing = false;
				if (!swapped)
					swapColumns(pivotColumn, row);
				else
					swapRows(pivotColumn, row);
			}

			debugFormat("Reduction done%s", didNothing ? " (nothing was done)" : "");
			return didNothing;
		}

		// Find the minimum non-zero entry in the given row
		private Integer findPivotInRow(Matrix matrix, int row) {
			Integer result = null;
			int resultEntry = 0;

			for (int column = 0; column < matrix.getColumns(); column++) {
				int entry = matrix.get(row, column);
				if (entry == Integer.MIN_VALUE)
					throw new ArithmeticException("Calculated Integer.MIN_VALUE, integer underflow will occur");
				if (entry == 0)
					continue;
				if (result != null && Math.abs(entry) >= Math.abs(resultEntry)) {
					continue;
				}
				result = column;
				resultEntry = entry;
			}

			return result;
		}

		private void addMultipleToRow(int rowToAdd, int rowToAddTo, int factor) {
			doRowOperation(new MatrixOperationAddMultipleOf(rowToAdd, rowToAddTo, factor));
		}

		private void addMultipleToColumn(int columnToAdd, int columnToAddTo, int factor) {
			doColumnOperation(new MatrixOperationAddMultipleOf(columnToAdd, columnToAddTo, factor));
		}

		private void invertRow(int row) {
			doRowOperation(new MatrixOperationInvert(row));
		}

		private void swapRows(int firstRow, int secondRow) {
			doRowOperation(new MatrixOperationSwap(firstRow, secondRow));
		}

		private void swapColumns(int firstColumn, int secondColumn) {
			doColumnOperation(new MatrixOperationSwap(firstColumn, secondColumn));
		}

		private void doRowOperation(MatrixOperation operation) {
			rowOperations.add(operation);
			operation.applyTo(matrix);
		}

		private void doColumnOperation(MatrixOperation operation) {
			columnOperations.add(operation);
			operation.applyTo(transpose(matrix));
		}

		// Test various invariants of the algorithm
		private void testInvariants() {
			// lhs and its inverse really should be inverse to each other
			assert multiply(getLeftHandMatrix(), getLeftHandMatrixInverse())
				.equals(ArrayMatrix.createIdentityMatrix(rows, rows));
			// Same for rhs
			assert multiply(getRightHandMatrix(), getRightHandMatrixInverse())
				.equals(ArrayMatrix.createIdentityMatrix(columns, columns));
			// It should hold that input = lhs * matrix * rhs
			assert multiply(getLeftHandMatrix(), matrix, getRightHandMatrix())
				.equals(input);
			// ...and the equivalent formula with the inverses
			assert multiply(getLeftHandMatrixInverse(), input,
					getRightHandMatrixInverse()).equals(matrix);
		}

		// Helper function for assertions
		private boolean isDiagonalMatrix(Matrix matrix) {
			for (int row = 0; row < matrix.getRows(); row++) {
				for (int column = 0; column < matrix.getColumns(); column++) {
					if (row != column && matrix.get(row, column) != 0)
						return false;
				}
			}
			return true;
		}
	}

	/**
	 * Get the diagonal entries of the Smith normal form.
	 * @return The diagonal entries.
	 */
	public List<Integer> getDiagonalEntries() {
		return Collections.unmodifiableList(diagonalEntries);
	}

	/**
	 * Get the inverse of the left hand multiplication matrix.
	 * @return The inverse of the matrix L.
	 */
	public Matrix getLeftHandMatrixInverse() {
		Matrix result = ArrayMatrix.createIdentityMatrix(rows, rows);
		for (MatrixOperation op : rowOperations)
			op.applyTo(result);
		return result;
	}

	/**
	 * Get the left hand multiplication matrix.
	 * @return The matrix L.
	 */
	public Matrix getLeftHandMatrix() {
		Matrix result = ArrayMatrix.createIdentityMatrix(rows, rows);
		for (int index = rowOperations.size() - 1; index >= 0; index--)
			rowOperations.get(index).reverseApplyTo(result);
		return result;
	}

	/**
	 * Get the inverse of the right hand multiplication matrix.
	 * @return The inverse of the matrix R.
	 */
	public Matrix getRightHandMatrixInverse() {
		Matrix result = ArrayMatrix.createIdentityMatrix(columns, columns);
		Matrix transposed = transpose(result);
		for (MatrixOperation op : columnOperations)
			op.applyTo(transposed);
		return result;
	}

	/**
	 * Get the right hand multiplication matrix.
	 * @return The matrix R.
	 */
	public Matrix getRightHandMatrix() {
		Matrix result = ArrayMatrix.createIdentityMatrix(columns, columns);
		Matrix transposed = transpose(result);
		for (int index = columnOperations.size() - 1; index >= 0; index--)
			columnOperations.get(index).reverseApplyTo(transposed);
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
