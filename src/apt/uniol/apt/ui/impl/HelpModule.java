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

import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleUtils;

/**
 * @author Renke Grunwald
 *
 */
public class HelpModule extends AbstractModule implements Module {
	private ModuleRegistry registry;

	/**
	 * Constructor
	 */
	public HelpModule(ModuleRegistry registry) {
		this.registry = registry;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addOptionalParameter("module_name", String.class, "help",
				"Module name for which you want help");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("information", String.class, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String moduleName = input.getParameter("module_name", String.class);
		Module module = registry.findModule(moduleName);

		if (module == null) {
			throw new ModuleException("No such module: " + moduleName);
		}

		String information = ModuleUtils.getModuleUsage(module);
		output.setReturnValue("information", String.class, information);
	}

	@Override
	public String getShortDescription() {
		return "Get information about a module";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
