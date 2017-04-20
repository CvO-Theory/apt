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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Uli Schlachter */
public class MatrixMultiplicationTest {
	static private class MatrixFromArray extends AbstractMatrix implements Matrix {
		private final int[][] data;

		public MatrixFromArray(int[][] data) {
			this.data = data;
		}

		@Override
		public int getColumns() {
			return data[0].length;
		}

		@Override
		public int getRows() {
			return data.length;
		}

		@Override
		public int get(int row, int column) {
			return data[row][column];
		}

		@Override
		public void set(int row, int column, int value) {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	public void testExampleFromWikipedia() {
		int[][] a = new int[][] {
			new int[] { 3, 2, 1 },
			new int[] { 1, 0, 2 }
		};
		int[][] b = new int[][] {
			new int[] { 1, 2 },
			new int[] { 0, 1 },
			new int[] { 4, 0 }
		};
		int[][] result = new int[][] {
			new int[] { 7, 8 },
			new int[] { 9, 2 },
		};
		Matrix mA = new MatrixFromArray(a);
		Matrix mB = new MatrixFromArray(b);
		Matrix mResult = new MatrixFromArray(result);

		assertThat(MatrixMultiplication.multiply(mA, mB), equalTo(mResult));
	}

	@Test
	public void testLinearExample() {
		Matrix mA = new MatrixFromArray(new int[][] { new int[] { 1, 3, 5 } });
		Matrix mB = new MatrixFromArray(new int[][] { new int[] { 3 }, new int[] { 2 }, new int[] { 1 } });
		Matrix mResult = new MatrixFromArray(new int[][] { new int[] { 1 * 3 + 3 * 2 + 5 * 1 } });

		assertThat(MatrixMultiplication.multiply(mA, mB), equalTo(mResult));
	}

	@Test
	public void testNumberExample() {
		Matrix mA = new MatrixFromArray(new int[][] { new int[] { 42 } });
		Matrix mB = new MatrixFromArray(new int[][] { new int[] { 21 } });
		Matrix mResult = new MatrixFromArray(new int[][] { new int[] { 42 * 21 } });

		assertThat(MatrixMultiplication.multiply(mA, mB), equalTo(mResult));
	}

	@Test
	public void testDimensionZeroOne() {
		Matrix mA = mock(Matrix.class);
		when(mA.getRows()).thenReturn(0);
		when(mA.getColumns()).thenReturn(1);
		Matrix mB = new MatrixFromArray(new int[][] { new int[] { 21, 42 } });

		Matrix result = MatrixMultiplication.multiply(mA, mB);
		assertThat(result.getRows(), equalTo(0));
		assertThat(result.getColumns(), equalTo(2));
	}

	@Test
	public void testDimensionOneZero() {
		Matrix mA = new MatrixFromArray(new int[][] { new int[] { 21 }, new int[] { 42 } });
		Matrix mB = mock(Matrix.class);
		when(mB.getRows()).thenReturn(1);
		when(mB.getColumns()).thenReturn(0);

		Matrix result = MatrixMultiplication.multiply(mA, mB);
		assertThat(result.getRows(), equalTo(2));
		assertThat(result.getColumns(), equalTo(0));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNonMatchingDimensions() {
		Matrix mA = mock(Matrix.class);
		when(mA.getRows()).thenReturn(1);
		when(mA.getColumns()).thenReturn(2);
		Matrix mB = mock(Matrix.class);
		when(mB.getRows()).thenReturn(3);
		when(mB.getColumns()).thenReturn(4);

		MatrixMultiplication.multiply(mA, mB);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
