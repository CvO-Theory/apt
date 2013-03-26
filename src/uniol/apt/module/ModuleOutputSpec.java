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
 * Specification of return value of a module.
 *
 * @author Renke Grunwald
 *
 */
public interface ModuleOutputSpec {
	/**
	 * The return value is indented to be written to a file.
	 *
	 * The UI will show a special file input parameter. E.g. `apt
	 * coverability_graph pn [ts]` where the squared brackets denote the
	 * optional file input parameter.
	 */
	public static final String PROPERTY_FILE = "file";

	/**
	 * The return value should be presented to the user verbatim without showing
	 * its name.
	 *
	 * E.g instead of "some_name: some_value: only "some_value" is shown in the
	 * UI.
	 */
	public static final String PROPERTY_RAW = "raw";

	/**
	 * The return value determines if the module invocation was successful. This
	 * is property should only be used on Boolean type return values; it will be
	 * ignored for other types.
	 */
	public static final String PROPERTY_SUCCESS = "success";

	/**
	 * Specifies a return value of a module. The order of calls of this method
	 * matters.
	 *
	 * @param name
	 *            name of the return value
	 * @param klass
	 *            class of the return value
	 * @param properties
	 *            provided properties
	 */
	public void addReturnValue(String name, Class<?> klass,
			String... properties);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
