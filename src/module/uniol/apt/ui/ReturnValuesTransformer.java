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
import java.util.HashMap;
import java.util.Map;

import uniol.apt.module.exception.ModuleException;

/**
 * This is just the reverse of {@link ParametersTransformer}.
 *
 * @author Renke Grunwald
 *
 */
public interface ReturnValuesTransformer {

	public <T> ReturnValueTransformation<T> getTransformation(Class<T> klass);

	public <T> void transform(Writer output, Object arg, Class<T> klass) throws ModuleException, IOException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
