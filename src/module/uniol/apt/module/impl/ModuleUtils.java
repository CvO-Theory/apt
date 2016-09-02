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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutputSpec;

/**
 * Some utility methods to simplify work with the module system.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleUtils {

	private ModuleUtils() {
	}

	/**
	 * Returns the module's required input parameters.
	 *
	 * @param module
	 *                module in question
	 * @return list of required input parameters
	 */
	public static List<Parameter> getParameters(Module module) {
		ModuleInputSpecImpl inputSpec = new ModuleInputSpecImpl();
		module.require(inputSpec);
		return inputSpec.getParameters();
	}

	/**
	 * Returns the module's optional input parameters.
	 *
	 * @param module
	 *                module in question
	 * @return list of optional input parameters
	 */
	public static List<OptionalParameter<?>> getOptionalParameters(Module module) {
		ModuleInputSpecImpl inputSpec = new ModuleInputSpecImpl();
		module.require(inputSpec);
		return inputSpec.getOptionalParameters();
	}

	/**
	 * Returns all (required and optional) of the module's input parameters.
	 *
	 * @param module
	 *                module in question
	 * @return list of input parameters
	 */
	public static List<Parameter> getAllParameters(Module module) {
		ModuleInputSpecImpl inputSpec = new ModuleInputSpecImpl();
		module.require(inputSpec);
		return inputSpec.getAllParameters();
	}

	/**
	 * Returns a list of return values that the module will provide after
	 * execution.
	 *
	 * @param module
	 *                module in question
	 * @return list of return values
	 */
	public static List<ReturnValue> getReturnValues(Module module) {
		ModuleOutputSpecImpl outputSpec = new ModuleOutputSpecImpl();
		module.provide(outputSpec);
		return outputSpec.getReturnValues();
	}

	/**
	 * Returns a list of return values with the file property that the
	 * module will provide after execution.
	 *
	 * @param module
	 *                module in question
	 * @return list of return values
	 */
	public static List<ReturnValue> getFileReturnValues(Module module) {
		List<ReturnValue> returnValues = getReturnValues(module);
		List<ReturnValue> fileReturnValues = new ArrayList<>();

		for (ReturnValue returnValue : returnValues) {
			if (returnValue.hasProperty(ModuleOutputSpec.PROPERTY_FILE)) {
				fileReturnValues.add(returnValue);
			}
		}

		return fileReturnValues;
	}

	public static ModuleInputImpl getModuleInput(Module module) {
		ModuleInputImpl input = new ModuleInputImpl();
		return input;
	}

	public static ModuleOutputImpl getModuleOutput(Module module) {
		ModuleOutputSpecImpl outputSpec = new ModuleOutputSpecImpl();
		module.provide(outputSpec);
		ModuleOutputImpl output = new ModuleOutputImpl(outputSpec);
		return output;
	}

	public static ModuleInputSpec getFilledInputSpec(Module module) {
		ModuleInputSpecImpl inputSpec = new ModuleInputSpecImpl();
		module.require(inputSpec);
		return inputSpec;
	}

	public static ModuleOutputSpec getFilledOutputSpec(Module module) {
		ModuleOutputSpecImpl outputSpec = new ModuleOutputSpecImpl();
		module.provide(outputSpec);
		return outputSpec;
	}

	/**
	 * Returns the subset of the given module collection that is part of the
	 * given category.
	 *
	 * @param modules
	 *                collection of modules that is being filtered
	 * @param category
	 *                category to search for
	 * @return new list of modules in the given category
	 */
	public static List<Module> getModulesByCategory(Collection<Module> modules, Category category) {
		List<Module> modulesByCategory = new LinkedList<>();

		for (Module module : modules) {
			if (Arrays.asList(module.getCategories()).contains(category)) {
				modulesByCategory.add(module);
			}
		}

		return modulesByCategory;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
