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
import java.util.List;

import uniol.apt.module.Module;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.ModuleInvocationException;

/**
 * A class that calls
 * {@link Module#run(uniol.apt.module.ModuleInput, uniol.apt.module.ModuleOutput)}
 * according to specifications {@link uniol.apt.module.ModuleInputSpec} and
 * {@link uniol.apt.module.ModuleOutputSpec} of a module.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleInvoker {

	/**
	 * Maps the given array of arguments to the modules required and
	 * optional input parameters and then executes the module on those. The
	 * module's return values are returned.
	 *
	 * @param module
	 *                module to execute
	 * @param arguments
	 *                module arguments
	 * @return module return values
	 * @throws ModuleException
	 *                 thrown when the module was not executed successfully
	 */
	public List<Object> invoke(Module module, Object... arguments) throws ModuleException {
		List<Parameter> parameters = ModuleUtils.getParameters(module);
		List<OptionalParameter<?>> optionalParameters = ModuleUtils.getOptionalParameters(module);
		List<Parameter> allParameters = ModuleUtils.getAllParameters(module);

		if (arguments == null) {
			throw new ModuleInvocationException("Arguments can't be null");
		}

		if (arguments.length < parameters.size()) {
			throw new ModuleInvocationException("Too few arguments");
		}

		if (arguments.length > allParameters.size()) {
			throw new ModuleInvocationException("Too many arguments");
		}

		ModuleInputImpl input = new ModuleInputImpl();

		int currentArgumentsIndex = 0;

		for (Parameter param : parameters) {
			if (!param.getKlass().isInstance(arguments[currentArgumentsIndex])) {
				String errorMessage = "Wrong type for parameter " + param.getName();
				throw new ModuleInvocationException(errorMessage);
			}

			input.setParameter(param.getName(), arguments[currentArgumentsIndex++]);
		}

		for (OptionalParameter<?> param : optionalParameters) {
			// Check if a value was provided for this optional parameter
			if (currentArgumentsIndex < arguments.length) { // Yes, it was
				if (!param.getKlass().isInstance(arguments[currentArgumentsIndex])) {
					String errorMessage = "Wrong type for parameter " + param.getName();
					throw new ModuleInvocationException(errorMessage);
				}

				input.setParameter(param.getName(), arguments[currentArgumentsIndex++]);
			} else { // No, let's take the default value
				input.setParameter(param.getName(), param.getDefaultValue());
			}
		}

		ModuleOutputImpl output = ModuleUtils.getModuleOutput(module);

		module.run(input, output);

		List<ReturnValue> returnValues = ModuleUtils.getReturnValues(module);
		List<Object> values = new ArrayList<>();

		for (ReturnValue returnValue : returnValues) {
			Object value = output.getValue(returnValue.getName());

			if (value != null && !returnValue.getKlass().isInstance(value)) {
				String errorMessage = "Wrong type for return value " + returnValue.getName();
				throw new ModuleInvocationException(errorMessage);
			}

			values.add(value);
		}

		return values;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
