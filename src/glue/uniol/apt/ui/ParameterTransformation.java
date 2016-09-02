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

import uniol.apt.module.exception.ModuleException;

/**
 * Transforms a parameter that is given as a string into some target type T.
 *
 * @author Renke Grunwald
 * @param <T>
 *                transformation target type
 */
public interface ParameterTransformation<T> {

	/**
	 * Transforms the given string-typed parameter into some target type T.
	 *
	 * @param arg
	 *                parameter
	 * @return parameter transformed into type T
	 * @throws ModuleException
	 *                 thrown when the transformation fails
	 */
	public T transform(String arg) throws ModuleException;

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
