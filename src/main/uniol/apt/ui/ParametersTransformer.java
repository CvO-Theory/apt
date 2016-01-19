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
import uniol.apt.module.exception.NoSuchTransformationException;

/**
 * This class manages a bunch of parameter transformations and uses them to
 * transform a given parameter string to an object of a specific type.
 *
 * @author Renke Grunwald
 */
public interface ParametersTransformer {
	/**
	 * Gets the transformation that can transform objects of the given type.
	 *
	 * @param klass the class of the object the transformation transforms
	 * @param <T> the type of the object the transformation transforms
	 * @return the transformation
	 */
	public <T> ParameterTransformation<T> getTransformation(Class<T> klass);

	/**
	 * Gets the description of the transformation for the given klass.
	 *
	 * @param klass the type of the object the transformation transforms
	 * @return the description or an empty string
	 * @throws NoSuchTransformationException When no suitable transformation is available
	 */
	public String getTransformationDescription(Class<?> klass) throws NoSuchTransformationException;

	/**
	 * Transforms a parameter string to an object of a specific type.
	 *
	 * @param arg
	 *            the parameter string
	 * @param klass
	 *            the type of the object the parameter string should be
	 *            transformed to
	 * @return the transformed object
	 * @throws NoSuchTransformationException When no suitable transformation is available
	 */
	public Object transform(String arg, Class<?> klass) throws ModuleException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
