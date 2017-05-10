/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.json;

import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ModuleInputImpl;
import uniol.apt.module.impl.ModuleOutputImpl;
import uniol.apt.module.impl.ModuleUtils;
import uniol.apt.module.impl.OptionalParameter;
import uniol.apt.module.impl.Parameter;
import uniol.apt.module.impl.ReturnValue;
import uniol.apt.ui.ParametersTransformer;
import uniol.apt.ui.ReturnValuesTransformer;
import uniol.apt.util.interrupt.ChainedInterrupter;
import uniol.apt.util.interrupt.Interrupter;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.interrupt.TimeoutInterrupter;
import uniol.apt.util.interrupt.UncheckedInterruptedException;

/**
 * All to interface with modules via JSON commands. This class makes it possible
 * to list, describe and call modules via commands that are given as instance of
 * the class JSONObject.
 * @author Uli Schlachter
 */
public class JSONExecutor {
	static private final long MILLISECONDS_PER_NANOSECOND = 1000l * 1000l;

	private final ModuleRegistry moduleRegistry;
	private final ParametersTransformer parametersTransformer;
	private final ReturnValuesTransformer returnValuesTransformer;

	/**
	 * Create a new JSONExecutor.
	 * @param moduleRegistry The module registry describing which modules to use.
	 * @param parametersTransformer The transformer to parse parameters.
	 * @param returnValuesTransformer The transformer to render return values.
	 */
	public JSONExecutor(ModuleRegistry moduleRegistry, ParametersTransformer parametersTransformer,
			ReturnValuesTransformer returnValuesTransformer) {
		this.moduleRegistry = moduleRegistry;
		this.parametersTransformer = parametersTransformer;
		this.returnValuesTransformer = returnValuesTransformer;
	}

	/**
	 * Execute a command given as a JSONObject.
	 * @param arguments The JSONObject describing what should be done.
	 * @return The result of the command.
	 */
	public JSONObject execute(JSONObject arguments) {
		try {
			return doExecute(arguments);
		} catch (JSONException e) {
			return JSONUtilities.toJSONObject(e);
		}
	}

	private JSONObject doExecute(JSONObject arguments) {
		String command = arguments.getString("command");
		JSONObject result;

		switch (command) {
			case "describe_module":
				result = describeModule(arguments.getString("module"));
				break;
			case "list_modules":
				result = listModules();
				break;
			case "run_module":
				result = callModule(arguments);
				break;
			default:
				result = new JSONObject();
				result.put("error", "Unsupported command: " + command);
				break;
		}

		return result;
	}

	private Module findModule(String name) {
		Module module = moduleRegistry.findModule(name);
		if (!isModuleAllowed(module))
			return null;
		return module;
	}

	// The following code handles listing and describing modules

	private JSONObject listModules() {
		JSONArray modules = new JSONArray();
		for (Module module : moduleRegistry.getModules()) {
			if (!isModuleAllowed(module))
				continue;
			modules.put(describeModule(module, false));
		}
		JSONObject result = new JSONObject();
		result.put("modules", modules);
		return result;
	}

	private JSONObject describeModule(String name) {
		JSONObject result = new JSONObject();
		Module module = findModule(name);
		if (module == null) {
			result.put("error", "No such module: " + name);
			return result;
		}
		return describeModule(module, true);
	}

	private JSONObject describeModule(Module module, boolean verbose) {
		JSONObject result = new JSONObject();
		result.put("name", module.getName());
		result.put("description", module.getShortDescription());
		result.put("description_long", module.getLongDescription());
		result.put("categories", transformCategories(module.getCategories()));
		if (verbose) {
			result.put("parameters", transformParameters(ModuleUtils.getAllParameters(module)));
			result.put("return_values", transformReturnValues(ModuleUtils.getReturnValues(module)));
		}
		return result;
	}

	private JSONArray transformCategories(Category[] categories) {
		JSONArray result = new JSONArray();
		for (Category category : categories)
			result.put(category.getName());
		return result;
	}

	private JSONArray transformParameters(Iterable<Parameter> parameters) {
		JSONArray result = new JSONArray();
		for (Parameter parameter : parameters) {
			JSONObject json = new JSONObject();
			json.put("name", parameter.getName());
			json.put("description", parameter.getDescription());
			json.put("type", parameter.getKlass().getName());
			json.put("properties", new JSONArray(parameter.getProperties()));
			if (parameter instanceof OptionalParameter) {
				@SuppressWarnings("unchecked")
				OptionalParameter<Object> opt = (OptionalParameter<Object>) parameter;
				json.put("optional", true);
				json.put("default", opt.getDefaultValueString());
			} else {
				json.put("optional", false);
			}
			result.put(json);
		}
		return result;
	}

	private JSONArray transformReturnValues(Iterable<ReturnValue> returnValues) {
		JSONArray result = new JSONArray();
		for (ReturnValue value : returnValues) {
			JSONObject json = new JSONObject();
			json.put("name", value.getName());
			json.put("type", value.getKlass().getName());
			result.put(json);
		}
		return result;
	}

	// The following code handles the execution of modules

	private JSONObject callModule(JSONObject arguments) {
		JSONObject result = new JSONObject();
		Module module = findModule(arguments.getString("module"));
		if (module == null) {
			result.put("error", "No such module");
			return result;
		}

		try {
			ModuleOutputImpl output = callModule(module, arguments);
			result.put("return_values", transformReturnValues(module, output));
			return result;
		} catch (ModuleException | UncheckedInterruptedException e) {
			return JSONUtilities.toJSONObject(e);
		}
	}

	private ModuleOutputImpl callModule(Module module, JSONObject arguments) throws ModuleException {
		// timeout handling
		long timeout = arguments.optLong("timeout_milliseconds", -1);
		Interrupter originalInterrupter = null;
		if (timeout != -1) {
			if (!(module instanceof InterruptibleModule)) {
				throw new ModuleException("This module does not support timeouts");
			}
			originalInterrupter = InterrupterRegistry.getCurrentThreadInterrupter();
			InterrupterRegistry.setCurrentThreadInterrupter(new ChainedInterrupter(originalInterrupter,
						new TimeoutInterrupter(timeout * MILLISECONDS_PER_NANOSECOND)));
		}
		try {
			ModuleInput input = transformArguments(module, arguments.getJSONObject("arguments"));
			ModuleOutputImpl output = ModuleUtils.getModuleOutput(module);
			module.run(input, output);
			return output;
		} finally {
			if (originalInterrupter != null)
				InterrupterRegistry.setCurrentThreadInterrupter(originalInterrupter);
		}
	}

	private ModuleInput transformArguments(Module module, JSONObject arguments) throws ModuleException {
		ModuleInputImpl input = new ModuleInputImpl();
		for (Parameter parameter : ModuleUtils.getAllParameters(module)) {
			Object arg;
			if (!arguments.has(parameter.getName())) {
				if (parameter instanceof OptionalParameter) {
					@SuppressWarnings("unchecked")
					OptionalParameter<Object> opt = (OptionalParameter<Object>) parameter;
					arg = opt.getDefaultValue();
				} else {
					throw new ModuleException("Missing module argument: " + parameter.getName());
				}
			} else {
				arg = getArgument(arguments, parameter.getName(), parameter.getKlass());
			}
			input.setParameter(parameter.getName(), arg);
		}
		return input;
	}

	private Object getArgument(JSONObject outerArguments, String parameterName, Class<?> klass)
			throws ModuleException {
		JSONObject argument = outerArguments.optJSONObject(parameterName);
		if (argument == null) {
			// Argument is given directly as a string
			String argString = outerArguments.getString(parameterName);
			return parametersTransformer.transformString(argString, klass);
		}

		// Argument is given as a nested module call
		String returnValue = argument.getString("use");
		Module module = findModule(argument.getString("module"));
		if (module == null) {
			throw new ModuleException("No such module: " + argument.getString("module"));
		}
		ModuleOutputImpl output;
		try {
			output = callModule(module, argument);
		} catch (UncheckedInterruptedException e) {
			throw new ModuleException("Module " + module + " timed out", e);
		}
		Object result = output.getValue(returnValue);
		if (result == null)
			throw new ModuleException("Module did not produce a return value with name " + returnValue);
		if (!klass.isInstance(result))
			throw new ModuleException(String.format("Module produced a return value with type %s, "
						+ "but %s was expected", result.getClass().getName(),
						klass.getName()));
		return result;
	}

	private JSONObject transformReturnValues(Module module, ModuleOutputImpl output) throws ModuleException {
		JSONObject result = new JSONObject();
		for (ReturnValue returnValue : ModuleUtils.getReturnValues(module)) {
			Object value = output.getValue(returnValue.getName());
			if (value != null) {
				StringWriter writer = new StringWriter();
				try {
					returnValuesTransformer.transform(writer, value, returnValue.getKlass());
				} catch (IOException e) {
					throw new AssertionError("A StringWriter should not throw IOException", e);
				}
				result.put(returnValue.getName(), writer.toString());
			}
		}
		return result;
	}

	/**
	 * Check if the given module is allowed to be used. This function can be overriden in base classes to limit the
	 * modules that can be used.
	 * @param module The module that is checked.
	 * @return This implementation always returns true.
	 */
	protected boolean isModuleAllowed(Module module) {
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
