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
 * Abstract base class for Matrix implementations. This provides implementations of hashCode(), equals() and toString().
 * @author Uli Schlachter
 */
public abstract class AbstractMatrix implements Matrix {
	@Override
	public int hashCode() {
		int result = (getRows() << 16) | getColumns();
		for (int row = 0; row < getRows(); row++) {
			for (int column = 0; column < getColumns(); column++) {
				int value = get(row, column);
				result = (result << 7) + 11 * (result >> 25) + value;
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Matrix))
			return false;
		Matrix other = (Matrix) o;
		if (getRows() != other.getRows())
			return false;
		if (getColumns() != other.getColumns())
			return false;
		for (int row = 0; row < getRows(); row++) {
			for (int column = 0; column < getColumns(); column++) {
				if (get(row, column) != other.get(row, column))
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		int rows = getRows();
		int columns = getColumns();
		StringBuilder builder = new StringBuilder("[");

		for (int row = 0; row < rows; row++) {
			builder.append("[");
			for (int column = 0; column < columns; column++) {
				if (column != 0)
					builder.append(", ");
				builder.append(get(row, column));
			}
			builder.append("]");
		}

		builder.append("]");
		return builder.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
