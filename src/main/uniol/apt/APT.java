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

package uniol.apt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;

import uniol.apt.module.AptModuleRegistry;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleExitStatusChecker;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.ModulePreconditionsChecker;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.PropertyModuleExitStatusChecker;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ExitStatus;
import uniol.apt.module.impl.ModuleInvoker;
import uniol.apt.module.impl.ModuleUtils;
import uniol.apt.module.impl.Parameter;
import uniol.apt.module.impl.ReturnValue;
import uniol.apt.module.impl.SimpleModulePreconditionsChecker;
import uniol.apt.ui.AptParameterTransformation;
import uniol.apt.ui.ParameterTransformation;
import uniol.apt.ui.ParametersParser;
import uniol.apt.ui.ParametersTransformer;
import uniol.apt.ui.ReturnValuesTransformer;
import uniol.apt.ui.impl.AptParametersTransformer;
import uniol.apt.ui.impl.AptReturnValuesTransformer;
import uniol.apt.ui.impl.SimpleParametersParser;
import uniol.apt.ui.impl.UIUtils;

/**
 * @author Renke Grunwald
 */
public class APT {
	/**
	 * Symbol that signals that a file should be read from the standard input.
	 */
	public static final String STANDARD_INPUT_SYMBOL = "-";

	/**
	 * The exact APT version that is running as seen by "git describe".
	 */
	public static final String GIT_VERSION;

	/**
	 * A timestamp for when this version of APT was built.
	 */
	public static final String TIMESTAMP;

	/**
	 * A human-readable string generated from {@link GIT_VERSION} and {@link TIMESTAMP}.
	 */
	public static final String VERSION_STRING;

	static {
		Properties props = new Properties();
		try (InputStream in = APT.class.getResourceAsStream("APT.properties")) {
			if (in != null)
				props.load(in);
		} catch (IOException e) {
			// Ignore the exception and just make sure that the version will be UNKNOWN
			props = new Properties();
		}
		GIT_VERSION = props.getProperty("git-version", "UNKNOWN");
		TIMESTAMP = props.getProperty("timestamp", "UNKNOWN");
		VERSION_STRING = "APT version " + GIT_VERSION + " built on " + TIMESTAMP;
	}

	private static final ParametersParser PARAMETERS_PARSER = new SimpleParametersParser();
	private static final ParametersTransformer PARAMETERS_TRANSFORMER = AptParametersTransformer.INSTANCE;
	private static final ReturnValuesTransformer RETURN_VALUES_TRANSFORMER = AptReturnValuesTransformer.INSTANCE;
	private static final ModuleRegistry REGISTRY = AptModuleRegistry.INSTANCE;
	private static final Trie<String, String> REMOVED_MODULES = new PatriciaTrie<>();

	private static final PrintStream OUT_PRINTER = System.out;
	private static final PrintStream ERR_PRINTER = System.err;

	/**
	 * Hidden Constructor.
	 */
	private APT() {
	}

	private static void addRemovedModules() {
		REMOVED_MODULES.put("apt2baggins", "Use pn_convert / lts_convert apt baggins instead.");
		REMOVED_MODULES.put("apt2lola",    "Use pn_convert / lts_convert apt lola instead.");
		REMOVED_MODULES.put("apt2petrify", "Use pn_convert / lts_convert apt petrify instead.");
		REMOVED_MODULES.put("apt2pnml",    "Use pn_convert / lts_convert apt pnml instead.");
		REMOVED_MODULES.put("apt2synet",   "Use pn_convert / lts_convert apt synet instead.");
		REMOVED_MODULES.put("petrify2apt", "Use pn_convert / lts_convert petrify apt instead.");
		REMOVED_MODULES.put("pnml2apt",    "Use pn_convert / lts_convert pnml apt instead.");
		REMOVED_MODULES.put("synet2apt",   "Use pn_convert / lts_convert synet apt instead.");
		REMOVED_MODULES.put("info",        "Use examine_pn instead.");
		REMOVED_MODULES.put("backwards_persistent", "Use backward_persistent instead.");
	}

	/**
	 * Program entry point.
	 *
	 * @param args
	 *                command line arguments
	 */
	public static void main(String[] args) {
		addRemovedModules();

		PARAMETERS_PARSER.parse(args);

		String[] moduleNames = PARAMETERS_PARSER.getModuleNames();

		if (moduleNames.length == 0) {
			printUsageAndExit();
		}

		String moduleName = moduleNames[0]; // Only use a single module for now
		Collection<Module> foundModules = REGISTRY.findModulesByPrefix(moduleName);

		Module module = null;

		if (foundModules.isEmpty()) {
			printNoSuchModuleAndExit(moduleName);
		} else if (foundModules.size() == 1) {
			// These is only one right module and we just found it
			module = foundModules.iterator().next();
		} else {
			// Ambiguous, but maybe the name isn't a partial name after all
			module = REGISTRY.findModule(moduleName);

			if (module == null) {
				printAmbiguousModuleNameAndExit(moduleName, foundModules);
			}
		}

		List<Parameter> parameters = ModuleUtils.getParameters(module);
		List<Parameter> allParameters = ModuleUtils.getAllParameters(module);

		List<ReturnValue> returnValues = ModuleUtils.getReturnValues(module);
		List<ReturnValue> fileReturnValues = ModuleUtils.getFileReturnValues(module);

		String[] moduleArgs = PARAMETERS_PARSER.getModuleArguments(moduleName);

		try {
			if (moduleArgs.length < parameters.size()) {
				printTooFewArgumentsAndExit(module);
			}

			if (moduleArgs.length > allParameters.size() + fileReturnValues.size()) {
				printTooManyArgumentsAndExit(module);
			}

			// Number of parameters for which values are provided
			int numberOfUsedParameters;

			if (moduleArgs.length > allParameters.size()) {
				numberOfUsedParameters = allParameters.size();
			} else {
				numberOfUsedParameters = moduleArgs.length;
			}

			// First check if multiple parameters are signaled to be read from the standard input
			checkNoTwoStdinParameters(moduleArgs, numberOfUsedParameters, allParameters);

			Object[] transformedArgs = new Object[numberOfUsedParameters];
			for (int i = 0; i < numberOfUsedParameters; i++) {
				transformedArgs[i] = PARAMETERS_TRANSFORMER.transform(moduleArgs[i],
						allParameters.get(i).getKlass());
			}

			ModulePreconditionsChecker checker = new SimpleModulePreconditionsChecker();

			List<Parameter> unmetParameters = checker.check(REGISTRY, module, transformedArgs);

			if (!unmetParameters.isEmpty()) {
				printPreconditionsUnmetAndExit(unmetParameters);
			}

			ModuleInvoker invoker = new ModuleInvoker();
			List<Object> values = invoker.invoke(module, transformedArgs);

			String[] fileArgs = Arrays.copyOfRange(moduleArgs, numberOfUsedParameters, moduleArgs.length);
			printModuleOutput(fileArgs, returnValues, values);

			ModuleExitStatusChecker statusChecker = new PropertyModuleExitStatusChecker();
			ExitStatus status = statusChecker.check(module, values);

			OUT_PRINTER.flush();
			System.exit(status.getValue());
		} catch (ModuleException e) {
			ERR_PRINTER.println(String.format("Error while invoking module '%s':%n  %s",
						module.getName(), e.getMessage()));
			ERR_PRINTER.flush();
			System.exit(ExitStatus.ERROR.getValue());
		}
	}

	private static void checkNoTwoStdinParameters(String[] moduleArgs, int numberOfUsedParameters,
			List<Parameter> allParameters) {
		boolean hasStdInParameter = false;

		// First check if multiple parameters are signaled to be read from the standard input
		for (int i = 0; i < numberOfUsedParameters; i++) {
			if (moduleArgs[i].equals(STANDARD_INPUT_SYMBOL)
					&& isFileSourceParameter(allParameters.get(i))) {
				if (hasStdInParameter) {
					printCanOnlyReadOneParameterFromStdInAndExit();
				}
				hasStdInParameter = true;
			}
		}
	}

	private static boolean isFileSourceParameter(Parameter parameter) {
		Class<?> klass = parameter.getKlass();
		ParameterTransformation<?> transformation = PARAMETERS_TRANSFORMER.getTransformation(klass);
		if (transformation == null)
			return false;
		AptParameterTransformation annotation = transformation.getClass()
				.getAnnotation(AptParameterTransformation.class);
		if (annotation == null)
			return false;
		return annotation.fileSource();
	}

	private static void printModuleOutput(String[] fileArgs, List<ReturnValue> returnValues, List<Object> values)
			throws ModuleException {
		boolean hasStandardOutputFileReturnValue = false;

		for (int i = 0; i < fileArgs.length; i++) {
			if (fileArgs[i].equals(STANDARD_INPUT_SYMBOL)) {
				hasStandardOutputFileReturnValue = true;
			}
		}

		// Handle module output
		try (CloseableCollection<PrintStream> outputs = new CloseableCollection<>()) {
			boolean outputWithName[] = new boolean[values.size()];

			// Figure out where the values which the module produced should be printed to
			int usedFileArgsCount = 0;
			for (int i = 0; i < values.size(); i++) {

				if (values.get(i) == null) {
					outputWithName[i] = false;
					outputs.add(null, false);
					continue;
				}

				String returnValueName = returnValues.get(i).getName();

				boolean isRawReturnValue =
					returnValues.get(i).hasProperty(ModuleOutputSpec.PROPERTY_RAW);

				boolean isFileReturnValue =
					returnValues.get(i).hasProperty(ModuleOutputSpec.PROPERTY_FILE);

				// Print the return value to file without its name
				if (isFileReturnValue) {
					// Check if the user supplied a file name for this return value
					if (fileArgs.length > usedFileArgsCount) {
						String filename = fileArgs[usedFileArgsCount];

						if (filename.equals(STANDARD_INPUT_SYMBOL)) {
							outputs.add(OUT_PRINTER, false);
						} else {
							outputs.add(openOutput(filename), true);
						}
						outputWithName[i] = false;

						usedFileArgsCount++;
						continue;
					}
				}

				// Only print the file return value when requested; skip the other return values
				if (hasStandardOutputFileReturnValue) {
					outputWithName[i] = false;
					outputs.add(null, false);
					continue;
				}

				// Print this ordinary return value, possibly with its name
				outputs.add(OUT_PRINTER, false);
				outputWithName[i] = !isRawReturnValue;
			}

			// Print all return values for which the module produced values
			for (int i = 0; i < values.size(); i++) {
				PrintStream out = outputs.get(i);
				if (out == null)
					continue;

				if (outputWithName[i])
					out.print(returnValues.get(i).getName() + ": ");

				OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
				RETURN_VALUES_TRANSFORMER.transform(writer,
						values.get(i), returnValues.get(i).getKlass());
				writer.flush();
				out.println();
			}
		}
		catch (IOException e) {
			ERR_PRINTER.println("Error writing to file: " + e.getMessage());
			ERR_PRINTER.flush();
			System.exit(ExitStatus.ERROR.getValue());
		}
	}

	private static void printPreconditionsUnmetAndExit(List<Parameter> unmetParameters) {
		ERR_PRINTER.println("Some preconditions are unmet:");

		for (Parameter parameter : unmetParameters) {
			ERR_PRINTER.print("Parameter \"" + parameter.getName() + "\" is not");

			for (String property : parameter.getProperties()) {
				ERR_PRINTER.print(" " + property);
			}

			ERR_PRINTER.println();
		}

		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static PrintStream openOutput(String fileName) throws IOException {
		File file = new File(fileName);

		if (file.exists())
			throw new IOException("File '" + file + "' already exists");
		return new PrintStream(FileUtils.openOutputStream(file), false, "UTF-8");
	}

	private static void printVersion() {
		OUT_PRINTER.println(VERSION_STRING + ".");
		OUT_PRINTER.println();
	}

	private static void printTooManyArgumentsAndExit(Module module) throws ModuleException {
		ERR_PRINTER.println("Too many arguments");
		ERR_PRINTER.println();
		ERR_PRINTER.println(UIUtils.getModuleUsage(module, PARAMETERS_TRANSFORMER));
		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printTooFewArgumentsAndExit(Module module) throws ModuleException {
		ERR_PRINTER.println("Too few arguments");
		ERR_PRINTER.println();
		ERR_PRINTER.println(UIUtils.getModuleUsage(module, PARAMETERS_TRANSFORMER));
		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printAmbiguousModuleNameAndExit(String moduleName,
			Collection<Module> foundModules) {
		ERR_PRINTER.println("Ambiguous module name: " + moduleName);

		ERR_PRINTER.println();

		ERR_PRINTER.println("Available modules (starting with " + moduleName + "):");
		printModuleList(foundModules, ERR_PRINTER);

		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printNoSuchModuleAndExit(String moduleName) {
		ERR_PRINTER.println("No such module: " + moduleName);

		Map<String, String> messages = REMOVED_MODULES.prefixMap(moduleName);
		if (messages.size() == 1) {
			Map.Entry<String, String> entry = messages.entrySet().iterator().next();
			ERR_PRINTER.println(String.format("The module '%s' was removed. %s",
						entry.getKey(), entry.getValue()));
		}

		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printUsageAndExit() {
		printVersion();
		OUT_PRINTER.println("Usage: apt <module> <arguments>");
		OUT_PRINTER.println();

		OUT_PRINTER.println("Available modules:");
		printModuleList(REGISTRY.getModules(), OUT_PRINTER);

		OUT_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printCanOnlyReadOneParameterFromStdInAndExit() {
		ERR_PRINTER.println("Only one parameter can be read from standard input at the same time");
		ERR_PRINTER.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printModuleList(Collection<Module> modules, PrintStream printer) {
		for (Category category : Category.values()) {
			List<Module> modulesByCategory = ModuleUtils.getModulesByCategory(modules, category);

			if (modulesByCategory.isEmpty()) {
				continue;
			}

			printer.println();
			printer.println(category.getName());

			for (int i = 0; i < category.getName().length(); i++) {
				printer.print("=");
			}

			printer.println();

			Set<Module> sortedModules = new TreeSet<>(new Comparator<Module>() {

				@Override
				public int compare(Module o1, Module o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			sortedModules.addAll(modulesByCategory);

			// Figure out the length of the longest module name
			int longestModuleName = 0;
			for (Module module : modules) {
				longestModuleName = Math.max(longestModuleName, module.getName().length());
			}

			String format = "  %-" + Integer.toString(longestModuleName) + "s  %s";
			for (Module module : sortedModules) {
				printer.println(String.format(format, module.getName(), module.getShortDescription()));
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
