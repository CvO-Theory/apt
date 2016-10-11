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

import java.util.Collection;

import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.AptModuleRegistry;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Renke Grunwald
 *
 */
@AptModule
public class HelpModule extends AbstractModule implements Module {
	@Override
	public String getName() {
		return "help";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addOptionalParameterWithDefault("module_name", String.class, "help", "help",
				"Module name for which you want help");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("information", String.class, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String moduleName = input.getParameter("module_name", String.class);
		ModuleRegistry registry = AptModuleRegistry.INSTANCE;
		Collection<Module> foundModules = registry.findModulesByPrefix(moduleName);

		Module module = null;

		if (foundModules.size() == 1) {
			// Only one possible module, we just found it
			module = foundModules.iterator().next();
		} else {
			// Ambiguous, but maybe the name isn't a partial name after all
			module = registry.findModule(moduleName);
		}

		if (module == null) {
			throw new ModuleException("No such module: " + moduleName);
		}

		String information = UIUtils.getModuleUsage(module, AptParametersTransformer.INSTANCE);
		output.setReturnValue("information", String.class, information);
	}

	@Override
	public String getShortDescription() {
		return "Get information about a module";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.MISC};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
