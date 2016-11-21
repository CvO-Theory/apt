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

import java.util.List;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class SmithNormalFormTest {

	private Matrix createMatrix(int[][] data) {
		Matrix result = ArrayMatrix.createIdentityMatrix(data.length, data[0].length);
		for (int row = 0; row < result.getRows(); row++) {
			for (int column = 0; column < result.getColumns(); column++) {
				result.set(row, column, data[row][column]);
			}
		}
		return result;
	}

	@Test
	public void normalForm1Test() {
		int[][] data = new int[][] {
			new int[] { -1, 1, 2 },
			new int[] {  1, 1, 0 },
			new int[] {  2, 2, 0 },
			new int[] {  0, 1, 1 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 0));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void normalFormWrongOrderDividingTest() {
		int[][] data = new int[][] {
			new int[] { 4, 0 },
			new int[] { 0, 2 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(2, 4));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void normalFormWrongOrderNotDividingTest() {
		int[][] data = new int[][] {
			new int[] { 4, 0 },
			new int[] { 0, 3 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 12));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void normalFormLotsOfZerosTest() {
		int[][] data = new int[][] {
			new int[] { 0, 0, 0 },
			new int[] { 0, 0, 0 },
			new int[] { 0, 0, 0 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(0, 0, 0));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix1Test() {
		// At some point this matrix caused an endless loop
		int[][] data = new int[][] {
			new int[] { -56, 5, 41, 33, 41 },
			new int[] { -14, 60, -65, 94, -68 },
			new int[] { -32, -47, -63, 57, -76 },
			new int[] { -6, -4, 6, -26, 66 },
			new int[] { 2, -16, -74, -51, -46 }
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 1933930262));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix2Test() {
		// At some point this matrix caused an assertion failure (diagonal entries of the computed result were
		// not ascendingly dividing)
		int[][] data = new int[][] {
			new int[] { 57, 42, 13, -46, 57, -93 },
			new int[] { 54, 52, 75, -45, 20, 56 },
			new int[] { 67, 56, -31, 61, 36, 57 },
			new int[] { -54, 24, 13, -30, 85, 46 },
			new int[] { 28, 72, 49, -48, 23, -32 },
			new int[] { -39, -84, 67, 78, 26, 31 },
			new int[] { -83, -92, 80, 73, -61, 61 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 2, 2));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix3Test() {
		// At some point this matrix caused an assertion failure (diagonal entries of the computed result were
		// not ascendingly dividing)
		int[][] data = new int[][] {
			new int[] { -92, -96, -4, -60, -86 },
			new int[] { 43, -96, 26, 31, 29 },
			new int[] { -98, 16, -91, -45, -87 },
			new int[] { 84, 79, -18, 78, -91 },
			new int[] { -33, 28, 34, 90, -3 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 1472467094));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix4Test() {
		// At some point this matrix caused an assertion failure (diagonal entries of the computed result were
		// not ascendingly dividing)
		int[][] data = new int[][] {
			new int[] { 9, -77, 59, 90, -20, -63 },
			new int[] { 34, 4, -85, 36, 49, 45 },
			new int[] { -86, 55, -33, 87, 36, 56 },
			new int[] { 44, 65, 79, 91, -32, -56 },
			new int[] { 86, -78, 52, -40, -10, -62 },
			new int[] { -43, -17, 14, 91, -79, -97 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 2, 2041046482));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix5Test() {
		// At some point this matrix caused an assertion failure (diagonal entries of the computed result were
		// not ascendingly dividing)
		int[][] data = new int[][] {
			new int[] { -100, 49, 53, -34, -79, 7, -67 },
			new int[] { 92, -67, -85, 75, -27, -29, -94 },
			new int[] { 28, -15, -69, 73, -12, 48, 80 },
			new int[] { 4, 21, 41, 97, 87, 22, -25 },
			new int[] { 26, -73, -73, 34, -48, 67, 85 },
			new int[] { -30, -97, 97, -85, -11, -53, -55 },
			new int[] { -57, 28, 92, -3, 10, -77, 41 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 1, 1, 430279946));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix6Test() {
		// At some point this matrix caused an assertion failure (algorithm done, but matrix not diagonal)
		int[][] data = new int[][] {
			new int[] { 16, -40, 92, 79, -84, 19 },
			new int[] { 68, -81, -16, -78, -67, 50 },
			new int[] { 57, 25, 67, 69, -87, 32 },
			new int[] { 26, 57, -60, 84, -15, 42 },
			new int[] { -54, -45, 19, 36, -41, -92 },
			new int[] { -17, -24, -26, -72, 76, 19 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 1, 1, 2, 155917448));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix7Test() {
		// At some point this matrix caused an assertion failure (Step 1 didn't generate a diagonal matrix)
		int[][] data = new int[][] {
			new int[] { 0, 0 },
			new int[] { 77, -78 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 0));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test
	public void someMatrix8Test() {
		int[][] data = new int[][] {
			new int[] { -56, 5, 41 },
			new int[] { -14, 60, -65 },
			new int[] { -32, -47, -63 },
			new int[] { -6, -4, 6 },
		};
		SmithNormalForm nf = new SmithNormalForm(createMatrix(data));
		assertThat(nf.getDiagonalEntries(), contains(1, 1, 2));
		testSmithNormalForm(createMatrix(data), nf);
	}

	@Test(expectedExceptions = ArithmeticException.class)
	public void someMatrix9Test() {
		int[][] data = new int[][] {
			new int[] { 78, 55, 58, -6, 75, -32, 13, 54 },
			new int[] { -76, -12, 2, 84, -47, -85, 88, -35 },
			new int[] { -35, -53, -84, 100, -97, -52, 70, -32 },
			new int[] { 85, 40, -16, 67, -91, -43, 50, -17 },
			new int[] { -86, 73, 38, -85, 38, -50, 48, 65 },
			new int[] { -43, 50, -71, 13, 65, -40, 62, 26 },
			new int[] { -99, -41, -40, -18, -39, -35, -50, -90 },
			new int[] { -99, -27, 47, -90, 59, -28, -20, 49 },
			new int[] { 26, -55, 90, -8, -35, -1, 94, -27 },
		};
		new SmithNormalForm(createMatrix(data));
	}

	private static void testSmithNormalForm(Matrix input, SmithNormalForm nf) {
		int rows = input.getRows();
		int columns = input.getColumns();
		Matrix lhs = nf.getLeftHandMatrix();
		Matrix lhsInv = nf.getLeftHandMatrixInverse();
		Matrix rhs = nf.getRightHandMatrix();
		Matrix rhsInv = nf.getRightHandMatrixInverse();
		List<Integer> diagonalEntries = nf.getDiagonalEntries();
		Matrix diagonalMatrix = ArrayMatrix.createIdentityMatrix(rows, columns);
		for (int i = 0; i < Math.min(rows, columns); i++)
			diagonalMatrix.set(i, i, diagonalEntries.get(i));

		assertThat(MatrixMultiplication.multiply(lhs, lhsInv),
				equalTo(ArrayMatrix.createIdentityMatrix(rows, rows)));
		assertThat(MatrixMultiplication.multiply(lhsInv, lhs),
				equalTo(ArrayMatrix.createIdentityMatrix(rows, rows)));
		assertThat(MatrixMultiplication.multiply(rhs, rhsInv),
				equalTo(ArrayMatrix.createIdentityMatrix(columns, columns)));
		assertThat(MatrixMultiplication.multiply(rhsInv, rhs),
				equalTo(ArrayMatrix.createIdentityMatrix(columns, columns)));
		assertThat(MatrixMultiplication.multiply(lhsInv, input, rhsInv),
				equalTo(diagonalMatrix));
		assertThat(MatrixMultiplication.multiply(lhs, diagonalMatrix, rhs),
				equalTo(input));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
