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

import java.util.HashMap;
import java.util.Map;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.NoSuchTransformationException;

/**
 * This class manages a bunch of parameter transformations and uses them to
 * transform a given parameter string to an object of a specific type.
 *
 * @author Renke Grunwald
 *
 */
public class ParametersTransformer {
	Map<Class<?>, ParameterTransformation<?>> transformations = new HashMap<>();

	/**
	 * Adds a transformation.
	 *
	 * @param klass
	 *            the type of the resulting object
	 * @param transformation
	 *            the actual transformation
	 */
	public <T> void addTransformation(Class<T> klass, ParameterTransformation<T> transformation) {
		transformations.put(klass, transformation);
	}

	/**
	 * Gets the transformation that can transform objects of the given type.
	 *
	 * @param klass the type of the object the transformation transforms
	 * @return the transformation
	 */
	@SuppressWarnings("unchecked")
	public <T> ParameterTransformation<T> getTransformation(Class<T> klass) {
		return (ParameterTransformation<T>) transformations.get(klass);
	}

	/**
	 * Transforms a parameter string to an object of a specific type.
	 *
	 * @param arg
	 *            the parameter string
	 * @param klass
	 *            the type of the object the parameter string should be
	 *            transformed to
	 * @return the transformed object
	 * @throws ModuleException
	 */
	public Object transform(String arg, Class<?> klass) throws ModuleException {
		ParameterTransformation<?> transformation = transformations.get(klass);
		if (transformation == null)
			throw new NoSuchTransformationException(klass);
		Object obj = transformation.transform(arg);
		if (obj == null)
			throw new NullPointerException("Parameter transformation for class " + klass + " returned "
					+ "null when given the argument '" + arg + "'");
		return obj;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
