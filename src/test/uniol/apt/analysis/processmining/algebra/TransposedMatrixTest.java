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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uniol.apt.analysis.processmining.algebra.TransposedMatrix.transpose;

/** @author Uli Schlachter */
public class TransposedMatrixTest {
	@Test
	public void testGetColumnsAndRows() {
		Matrix matrix = mock(Matrix.class);
		when(matrix.getRows()).thenReturn(4);
		when(matrix.getColumns()).thenReturn(9);

		Matrix transposed = transpose(matrix);
		assertThat(transposed.getColumns(), equalTo(4));
		assertThat(transposed.getRows(), equalTo(9));

		verify(matrix).getRows();
		verify(matrix).getColumns();
		verifyNoMoreInteractions(matrix);
	}

	@Test
	public void testGet() {
		Matrix matrix = mock(Matrix.class);
		when(matrix.get(1, 2)).thenReturn(4);
		when(matrix.get(2, 1)).thenReturn(9);

		Matrix transposed = transpose(matrix);
		assertThat(transposed.get(2, 1), equalTo(4));
		assertThat(transposed.get(1, 2), equalTo(9));

		verify(matrix).get(1, 2);
		verify(matrix).get(2, 1);
		verifyNoMoreInteractions(matrix);
	}

	@Test
	public void testSet() {
		Matrix matrix = mock(Matrix.class);

		Matrix transposed = transpose(matrix);
		transposed.set(42, 121, -3);

		verify(matrix).set(121, 42, -3);
		verifyNoMoreInteractions(matrix);
	}

	@Test
	public void testTransposeTranspose() {
		Matrix matrix = mock(Matrix.class);

		assertThat(transpose(transpose(matrix)), sameInstance(matrix));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
