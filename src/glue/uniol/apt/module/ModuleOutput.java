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

package uniol.apt.module;

import uniol.apt.module.exception.ModuleException;

/**
 * Storage of return values.
 *
 * @author Renke Grunwald
 *
 */
public interface ModuleOutput {
	/**
	 * Sets the value of the parameter for the given name. The value has to be
	 * an instance of the given class.
	 *
	 * @param name name of the parameter
	 * @param klass class of the parameter
	 * @param value the value of the parameter
	 * @param <T> the type of the return value
	 * @throws ModuleException When the wrong type for the return value is used compared to {@link Module#provide}.
	 */
	public <T> void setReturnValue(String name, Class<T> klass, T value) throws ModuleException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
