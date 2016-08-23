/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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

package uniol.apt.ui.impl;

import java.util.ServiceLoader;

import uniol.apt.ui.AptReturnValueTransformation;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * A ReturnValuesTransformer which uses SPI / a ServiceLoader to automatically discover return value transformations.
 * @author Uli Schlachter
 */
public class AptReturnValuesTransformer extends ReturnValuesTransformerImpl {
	public static final AptReturnValuesTransformer INSTANCE = new AptReturnValuesTransformer();

	private AptReturnValuesTransformer() {
		for (ReturnValueTransformation<?> transform : ServiceLoader.load(ReturnValueTransformation.class,
					getClass().getClassLoader())) {
			String transformName = transform.getClass().getCanonicalName();

			AptReturnValueTransformation annotation = transform.getClass()
				.getAnnotation(AptReturnValueTransformation.class);
			if (annotation == null)
				throw new RuntimeException(String.format(
						"Transformation %s is not annotated with %s",
						transformName, AptReturnValuesTransformer.class.getCanonicalName()));
			Class<?>[] values = annotation.value();
			if (values == null || values.length == 0)
				throw new RuntimeException(String.format(
						"Transformation %s has an incorrect %s annotation",
						transformName, AptReturnValuesTransformer.class.getCanonicalName()));

			for (Class<?> klass : values) {
				Object old = addTransformationUnchecked(klass, transform);
				if (old != null)
					throw new RuntimeException(String.format(
							"Multiple transformations for %s: %s and %s",
							klass.getCanonicalName(), transformName,
							old.getClass().getCanonicalName()));
			}
		}
	}

	// Private member so that we can get a type variable T and cast the transformation to a generic for that type.
	// If this cast is wrong, then the class was annotated incorrectly. We cannot really check for that thanks to
	// type erasure. TODO: Check for that, somehow
	@SuppressWarnings("unchecked")
	private <T> ReturnValueTransformation<T> addTransformationUnchecked(Class<T> klass,
			ReturnValueTransformation<?> transformation) {
		return super.addTransformation(klass, (ReturnValueTransformation<T>) transformation);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
