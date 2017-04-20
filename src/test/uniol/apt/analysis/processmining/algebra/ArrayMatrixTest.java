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
public class ArrayMatrixTest {
	@Test
	public void testIdentitiyMatrix1x1() {
		Matrix matrix = ArrayMatrix.createIdentityMatrix(1, 1);
		assertThat(matrix.getRows(), equalTo(1));
		assertThat(matrix.getColumns(), equalTo(1));
		assertThat(matrix.get(0, 0), equalTo(1));
	}

	@Test
	public void testIdentitiyMatrix2x3() {
		Matrix matrix = ArrayMatrix.createIdentityMatrix(2, 3);
		assertThat(matrix.getRows(), equalTo(2));
		assertThat(matrix.getColumns(), equalTo(3));
		assertThat(matrix.get(0, 0), equalTo(1));
		assertThat(matrix.get(0, 1), equalTo(0));
		assertThat(matrix.get(0, 2), equalTo(0));
		assertThat(matrix.get(1, 0), equalTo(0));
		assertThat(matrix.get(1, 1), equalTo(1));
		assertThat(matrix.get(1, 2), equalTo(0));
	}

	@Test
	public void setAndGetTest() {
		int rows = 3;
		int columns = 4;
		Matrix matrix = ArrayMatrix.createIdentityMatrix(rows, columns);

		for (int row = 0; row < matrix.getRows(); row++) {
			for (int column = 0; column < matrix.getColumns(); column++) {
				matrix.set(row, column, row * columns + column);
			}
		}
		for (int row = 0; row < matrix.getRows(); row++) {
			for (int column = 0; column < matrix.getColumns(); column++) {
				assertThat(matrix.get(row, column), equalTo(row * columns + column));
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
