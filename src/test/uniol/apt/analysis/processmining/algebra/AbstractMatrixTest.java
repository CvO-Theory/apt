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

/** @author Uli Schlachter */
public class AbstractMatrixTest {
	// A simple, read-only matrix used for tests
	private static class TestMatrix extends AbstractMatrix implements Matrix {
		private final int rows;
		private final int columns;
		private final int offset;

		public TestMatrix(int rows, int columns, int offset) {
			this.rows = rows;
			this.columns = columns;
			this.offset = offset;
		}

		@Override
		public int getColumns() {
			return columns;
		}

		@Override
		public int getRows() {
			return rows;
		}

		@Override
		public int get(int row, int column) {
			if (row < 0 || row >= rows)
				throw new IllegalArgumentException();
			if (column < 0 || column >= columns)
				throw new IllegalArgumentException();

			return offset + row * columns + column;
		}

		@Override
		public void set(int row, int column, int value) {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	public void testToString() {
		assertThat(new TestMatrix(1, 1, 0), hasToString("[[0]]"));
		assertThat(new TestMatrix(1, 1, 42), hasToString("[[42]]"));
		assertThat(new TestMatrix(2, 2, 0), hasToString("[[0, 1][2, 3]]"));
		assertThat(new TestMatrix(2, 3, 0), hasToString("[[0, 1, 2][3, 4, 5]]"));
	}

	@Test
	public void testEquals() {
		assertThat(new TestMatrix(10, 10, 0), equalTo(new TestMatrix(10, 10, 0)));
		assertThat(new TestMatrix(10, 10, 0), not(equalTo(new TestMatrix(10, 10, 1))));
		assertThat(new TestMatrix(10, 10, 0), not(equalTo(new TestMatrix(10, 9, 0))));
		assertThat(new TestMatrix(10, 10, 0), not(equalTo(new TestMatrix(9, 10, 0))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
