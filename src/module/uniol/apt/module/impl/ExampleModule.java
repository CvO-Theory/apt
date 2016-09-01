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

package uniol.apt.module.impl;

import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;


/**
 * An example module that lower-cases a string.
 *
 * @author Renke Grunwald
 *
 */
@AptModule
public class ExampleModule extends AbstractModule implements Module {
	@Override
	public String getName() {
		return "example_module";
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("lower_case_string", String.class, "lower_case");
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("string", String.class, "Some string");
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String string = input.getParameter("string", String.class);
		String lowerCaseString = string.toLowerCase();
		output.setReturnValue("lower_case_string", String.class, lowerCaseString);
	}

	@Override
	public String getTitle() {
		return "Example Module";
	}

	@Override
	public String getShortDescription() {
		return "Lowercase a string";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
