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

/**
 * Specification of parametres of a module.
 *
 * @author Renke Grunwald
 *
 */
public interface ModuleInputSpec {
	/**
	 * Specifies a parameter of a module. The order of calls of this method
	 * matters.
	 *
	 * @param name name of the paramter
	 * @param klass class of the parameter
	 * @param documentation user-readable documentation for this parameter
	 * @param properties required properties
	 */
	public void addParameter(String name, Class<?> klass, String documentation, String... properties);

	/**
	 * Specifies a optional parameter of a module. The order of calls of this
	 * method matters; however, optional parameters always appear last. A
	 * default value can be set for optional parameters. If the default value is
	 * dependent on other parameters it can be set to null and handled in the
	 * module itself.
	 *
	 * @param name name of the parameter
	 * @param klass class of the parameter
	 * @param defaultValue the default value (may be null)
	 * @param documentation user-readable documentation for this parameter
	 * @param properties required properties
	 * @param <T> the type of the parameter
	 */
	public <T> void addOptionalParameter(String name, Class<T> klass,
			T defaultValue, String documentation, String... properties);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
