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

import java.io.IOException;
import java.io.Writer;

import uniol.apt.module.exception.ModuleException;

/**
 * This is just the reverse of {@link ParametersTransformer}.
 *
 * @author Renke Grunwald
 *
 */
public interface ReturnValuesTransformer {

	/**
	 * Returns an object that transforms objects of type T to strings.
	 *
	 * @param <T>
	 *                source type that will be transformed to a string
	 * @param klass
	 *                class for source type T
	 * @return the transformer
	 */
	public <T> ReturnValueTransformation<T> getTransformation(Class<T> klass);

	/**
	 * Transforms the given argument to a string and appends it to the given
	 * writer.
	 *
	 * @param <T>
	 *                source type that will be transformed to a string
	 * @param output
	 *                writer
	 * @param arg
	 *                argument of class {@code klass}
	 * @param klass
	 *                type of given argument
	 * @throws ModuleException
	 *                 thrown when the transformation fails
	 * @throws IOException
	 *                 thrown when the append to the writer fails
	 */
	public <T> void transform(Writer output, Object arg, Class<T> klass) throws ModuleException, IOException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
