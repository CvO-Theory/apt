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

/**
 * All the things a command-line parser must provide us with re modules and
 * their arguments.
 *
 * @author Renke Grunwald
 *
 */
public interface ParametersParser {
	/**
	 * Parses a string consisting of arguments.
	 *
	 * @param args
	 *            command-line string
	 */
	public void parse(String[] args);

	/**
	 * Gets all the modules used in the command-line string.
	 *
	 * @return the module names
	 */
	public String[] getModuleNames();

	/**
	 * Gets all parameter names of the module in the command-line string.
	 *
	 * @param moduleName The name of the module whose arguments should be returned
	 * @return the parameter names
	 */
	public String[] getModuleArguments(String moduleName);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
