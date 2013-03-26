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

import java.util.HashSet;
import java.util.Set;

import uniol.apt.module.AbstractModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.impl.ModuleInputSpecImpl;
import uniol.apt.module.impl.ModuleOutputSpecImpl;
import uniol.apt.module.impl.Parameter;
import uniol.apt.module.impl.ReturnValue;
import uniol.apt.ui.ParameterTransformation;
import uniol.apt.ui.ParametersTransformer;
import uniol.apt.ui.ReturnValueTransformation;
import uniol.apt.ui.ReturnValuesTransformer;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Renke Grunwald
 *
 */
public class InternalsModule extends AbstractModule {
	private ModuleRegistry moduleRegistry;
	private ParametersTransformer paramTransformer;
	private ReturnValuesTransformer returnValueTransformer;

	@Override
	public String getName() {
		return "internal";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("action", String.class, "The action that should be executed");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("information", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String action = input.getParameter("action", String.class);

		switch (action) {
		case "modules":
			StringBuilder moduleBuilder = new StringBuilder("Modules");

			for (Module module : moduleRegistry.getModules()) {
				moduleBuilder.append("\n");
				moduleBuilder.append(module.getName());
				moduleBuilder.append(" ");
				moduleBuilder.append("(");
				moduleBuilder.append(module.getClass());
				moduleBuilder.append(")");
			}

			output.setReturnValue("information", String.class, moduleBuilder.toString());
			break;
		case "parameters":
			Set<Class<?>> paramClasses = new HashSet<>();

			for (Module module : moduleRegistry.getModules()) {
				ModuleInputSpecImpl inputSpec = new ModuleInputSpecImpl();
				module.require(inputSpec);

				for (Parameter param : inputSpec.getParameters()) {
					paramClasses.add(param.getKlass());
				}
			}

			StringBuilder paramBuilder = new StringBuilder("Parameters");

			for (Class<?> klass : paramClasses) {
				paramBuilder.append("\n");
				paramBuilder.append(klass.toString());
				paramBuilder.append(" ");
				paramBuilder.append("(");

				paramBuilder.append("transformation: ");

				ParameterTransformation<?> paramTransformation =
						paramTransformer.getTransformation(klass);

				if (paramTransformation != null) {
					paramBuilder.append(paramTransformation.getClass());
				} else {
					paramBuilder.append("none");
				}

				paramBuilder.append(")");
			}

			output.setReturnValue("information", String.class, paramBuilder.toString());
			break;
		case "return_values":
			Set<Class<?>> returnValueClasses = new HashSet<>();

			for (Module module : moduleRegistry.getModules()) {
				ModuleOutputSpecImpl outputSpec = new ModuleOutputSpecImpl();
				module.provide(outputSpec);

				for (ReturnValue returnValue : outputSpec.getReturnValues()) {
					returnValueClasses.add(returnValue.getKlass());
				}
			}

			StringBuilder returnValueBuilder = new StringBuilder("Return values");

			for (Class<?> klass : returnValueClasses) {
				returnValueBuilder.append("\n");
				returnValueBuilder.append(klass.toString());
				returnValueBuilder.append(" ");
				returnValueBuilder.append("(");

				returnValueBuilder.append("transformation: ");

				ReturnValueTransformation<?> returnValueTransformation =
						returnValueTransformer.getTransformation(klass);

				if (returnValueTransformation != null) {
					returnValueBuilder.append(returnValueTransformation.getClass());
				} else {
					returnValueBuilder.append("none");
				}

				returnValueBuilder.append(")");
			}

			output.setReturnValue("information", String.class, returnValueBuilder.toString());
			break;
		default:
			output.setReturnValue("information", String.class, "Possible actions:\n  modules\n  parameters\n  return_values");
			break;
		}
	}


	@Override
	public String getTitle() {
		return "Internal Module";
	}

	@Override
	public String getShortDescription() {
		return "Get internal information from the APT system";
	}

	public void setModuleRegistry(ModuleRegistry registry) {
		this.moduleRegistry = registry;
	}

	public void setParamTransformer(ParametersTransformer paramTransformer) {
		this.paramTransformer = paramTransformer;
	}

	public void setReturnValueTransformer(ReturnValuesTransformer returnValueTransformer) {
		this.returnValueTransformer = returnValueTransformer;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
