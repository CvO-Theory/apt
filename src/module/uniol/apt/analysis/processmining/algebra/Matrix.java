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
 * A representation of a matrix.
 * @author Uli Schlachter
 */
public interface Matrix {
	/**
	 * Get the number of columns in the matrix.
	 * @return the number of columns.
	 */
	public int getColumns();

	/**
	 * Get the number of rows in the matrix.
	 * @return the number of rows.
	 */
	public int getRows();

	/**
	 * Get an entry of the matrix.
	 * @param row The row to look at.
	 * @param column The column to look at.
	 * @return The entry at the specified position.
	 */
	public int get(int row, int column);

	/**
	 * Set an entry in the matrix.
	 * @param row The row to look at.
	 * @param column The column to look at.
	 * @param value The value to set the specified position to.
	 */
	public void set(int row, int column, int value);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
