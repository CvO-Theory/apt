/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.ui;

/**
 * Transforms a parameter that is given as a string into some target type T. In
 * addition there is a method that returns a string that describes the expected
 * string format necessary for the transformation to work.
 *
 * @author Uli Schlachter
 * @param <T>
 *                transformation target type
 */
public interface DescribedParameterTransformation<T> extends ParameterTransformation<T> {

	/**
	 * Returns a description of the string format that the parameter is
	 * expected to follow.
	 *
	 * @return expected parameter string format for successful
	 *         transformation
	 */
	public String getFormatDescription();

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
