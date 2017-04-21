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

package uniol.apt.ui.impl;

import java.util.Arrays;

import uniol.apt.ui.ParametersParser;

/**
 * A simple parsers for command-line strings that assumes a single module invocation.
 *
 * It parses command-line string like "some arg_1 ... argn_n" such that
 * {@link #getModuleNames()} returns ["some_module"] and
 * {@link #getModuleArguments(String)} for "some_module" returns ["arg_1", ...,
 * "arg_n"]
 *
 * @author Renke Grunwald
 *
 */
public class SimpleParametersParser implements ParametersParser {
	private String[] args_;

	@Override
	public void parse(String[] args) {
		args_ = Arrays.copyOf(args, args.length);
	}

	@Override
	public String[] getModuleNames() {
		if (args_.length == 0)
			return new String[] {};
		String moduleName = args_[0];
		return new String[] { moduleName };
	}

	@Override
	public String[] getModuleArguments(String moduleName) {
		if (args_.length <= 1)
			return new String[0];
		return Arrays.copyOfRange(args_, 1, args_.length);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
