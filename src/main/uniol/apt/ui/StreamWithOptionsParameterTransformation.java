/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2018  Uli Schlachter
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
import java.io.InputStream;

import uniol.apt.module.exception.ModuleException;
import org.apache.commons.io.IOUtils;

/**
 * Transforms a parameter that is given as a stream into some target type T.
 *
 * @author Uli Schlachter
 * @param <T> transformation target type
 */
public abstract class StreamWithOptionsParameterTransformation<T> extends StreamParameterTransformation<T> {
	@Override
	final public T transform(InputStream arg) throws ModuleException, IOException {
		return transform(arg, "");
	}

	/**
	 * Transforms the data from the given stream into some target type T.
	 * This method should be equivalent to calling reading the stream into a string assuming UTF-8 encoding and
	 * calling {@link ParameterTransformation#transform(String)} on the result.
	 * @param arg parameter
	 * @param options options that were given for this parameter
	 * @return parameter transformed into type T
	 * @throws ModuleException thrown when the transformation fails
	 * @throws IOException thrown when reading from the stream fails
	 */
	abstract public T transform(InputStream arg, String options) throws ModuleException, IOException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
