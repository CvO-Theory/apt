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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.bisimulation.NonBisimilarPath;
import uniol.apt.analysis.connectivity.Component;
import uniol.apt.analysis.connectivity.Components;
import uniol.apt.analysis.cycles.lts.CycleCounterExample;
import uniol.apt.analysis.invariants.Vector;
import uniol.apt.analysis.isomorphism.Isomorphism;
import uniol.apt.analysis.language.FiringSequence;
import uniol.apt.analysis.language.Word;
import uniol.apt.analysis.language.WordList;
import uniol.apt.analysis.sideconditions.SideConditions;
import uniol.apt.analysis.snet.SNetResult;
import uniol.apt.analysis.tnet.TNetResult;
import uniol.apt.analysis.trapsAndSiphons.TrapsSiphonsList;
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
import uniol.apt.ui.ParametersParser;
import uniol.apt.ui.ParametersTransformer;
import uniol.apt.ui.impl.AptParametersTransformer;
import uniol.apt.ui.impl.ReturnValuesTransformerImpl;
import uniol.apt.ui.impl.SimpleParametersParser;
import uniol.apt.ui.impl.returns.BooleanReturnValueTransformation;
import uniol.apt.ui.impl.returns.ComponentsReturnValueTransformation;
import uniol.apt.ui.impl.returns.INodeCollectionReturnValueTransformation;
import uniol.apt.ui.impl.returns.INodeReturnValueTransformation;
import uniol.apt.ui.impl.returns.IsomorphismReturnValueTransformation;
import uniol.apt.ui.impl.returns.MarkingReturnValueTransformation;
import uniol.apt.ui.impl.returns.NetReturnValueTransformation;
import uniol.apt.ui.impl.returns.NonBisimilarPathReturnValueTransformation;
import uniol.apt.ui.impl.returns.SNetResultReturnValueTransformation;
import uniol.apt.ui.impl.returns.StringArrayArrayReturnValueTransformation;
import uniol.apt.ui.impl.returns.TNetResultReturnValueTransformation;
import uniol.apt.ui.impl.returns.TSReturnValueTransformation;
import uniol.apt.ui.impl.returns.ToStringReturnValueTransformation;
import uniol.apt.ui.impl.returns.TrapsSiphonsListReturnValueTransformation;

/**
 * @author Renke Grunwald
 */
public class APT {
	/**
	 * Symbol that signals that a file should be read from the standard input.
	 */
	public static final String STANDARD_INPUT_SYMBOL = "-";

	private static final ParametersParser parametersParser = new SimpleParametersParser();
	private static final ParametersTransformer parametersTransformer = AptParametersTransformer.INSTANCE;
	private static final ReturnValuesTransformerImpl returnValuesTransformer = new ReturnValuesTransformerImpl();
	private static final ModuleRegistry registry = AptModuleRegistry.INSTANCE;
	private static final Trie<String, String> removedModules = new PatriciaTrie<>();

	private static final PrintStream outPrinter = System.out;
	private static final PrintStream errPrinter = System.err;

	/**
	 * Hidden Constructor.
	 */
	private APT() {
	}

	/**
	 * Add return value transformations.
	 */
	@SuppressWarnings("unchecked")
	public static void addReturnValuesTransformations() {
		returnValuesTransformer.addTransformation(Boolean.class, new BooleanReturnValueTransformation());
		returnValuesTransformer.addTransformation(Component.class,
				new INodeCollectionReturnValueTransformation<Component>());
		returnValuesTransformer.addTransformation(Components.class,
				new ComponentsReturnValueTransformation());
		returnValuesTransformer.addTransformation(CycleCounterExample.class,
				new ToStringReturnValueTransformation<CycleCounterExample>());
		returnValuesTransformer.addTransformation(FiringSequence.class,
				new INodeCollectionReturnValueTransformation<FiringSequence>());
		returnValuesTransformer.addTransformation((Class<INode<?, ?, ?>>) (Class<?>) INode.class,
				new INodeReturnValueTransformation<>());
		returnValuesTransformer.addTransformation(Isomorphism.class,
				new IsomorphismReturnValueTransformation());
		returnValuesTransformer.addTransformation(Integer.class,
				new ToStringReturnValueTransformation<Integer>());
		returnValuesTransformer.addTransformation(Long.class,
				new ToStringReturnValueTransformation<Long>());
		returnValuesTransformer.addTransformation(Marking.class, new MarkingReturnValueTransformation());
		returnValuesTransformer.addTransformation(State.class, new INodeReturnValueTransformation<State>());
		returnValuesTransformer.addTransformation(NonBisimilarPath.class,
				new NonBisimilarPathReturnValueTransformation());
		returnValuesTransformer.addTransformation(ParikhVector.class,
				new ToStringReturnValueTransformation<ParikhVector>());
		returnValuesTransformer.addTransformation(PetriNet.class, new NetReturnValueTransformation());
		returnValuesTransformer.addTransformation(Place.class, new INodeReturnValueTransformation<Place>());
		returnValuesTransformer.addTransformation(SideConditions.class,
				new ToStringReturnValueTransformation<SideConditions>());
		returnValuesTransformer.addTransformation(SNetResult.class, new SNetResultReturnValueTransformation());
		returnValuesTransformer.addTransformation(String.class,
				new ToStringReturnValueTransformation<String>());
		returnValuesTransformer.addTransformation(String[][].class,
				new StringArrayArrayReturnValueTransformation());
		returnValuesTransformer.addTransformation(TNetResult.class, new TNetResultReturnValueTransformation());
		returnValuesTransformer.addTransformation(Transition.class,
				new INodeReturnValueTransformation<Transition>());
		returnValuesTransformer.addTransformation(TransitionSystem.class, new TSReturnValueTransformation());
		returnValuesTransformer.addTransformation(TrapsSiphonsList.class,
				new TrapsSiphonsListReturnValueTransformation());
		returnValuesTransformer.addTransformation(Vector.class,
				new ToStringReturnValueTransformation<Vector>());
		returnValuesTransformer.addTransformation(Word.class, new ToStringReturnValueTransformation<Word>());
		returnValuesTransformer.addTransformation(WordList.class,
				new ToStringReturnValueTransformation<WordList>());
	}

	private static void addRemovedModules() {
		removedModules.put("apt2baggins", "Use pn_convert / lts_convert apt baggins instead.");
		removedModules.put("apt2lola",    "Use pn_convert / lts_convert apt lola instead.");
		removedModules.put("apt2petrify", "Use pn_convert / lts_convert apt petrify instead.");
		removedModules.put("apt2pnml",    "Use pn_convert / lts_convert apt pnml instead.");
		removedModules.put("apt2synet",   "Use pn_convert / lts_convert apt synet instead.");
		removedModules.put("petrify2apt", "Use pn_convert / lts_convert petrify apt instead.");
		removedModules.put("pnml2apt",    "Use pn_convert / lts_convert pnml apt instead.");
		removedModules.put("synet2apt",   "Use pn_convert / lts_convert synet apt instead.");
	}

	public static void main(String[] args) {
		addReturnValuesTransformations();
		addRemovedModules();

		parametersParser.parse(args);

		String[] moduleNames = parametersParser.getModuleNames();

		if (moduleNames.length == 0) {
			printUsageAndExit();
		}

		String moduleName = moduleNames[0]; // Only use a single module for now
		Collection<Module> foundModules = registry.findModulesByPrefix(moduleName);

		Module module = null;

		if (foundModules.isEmpty()) {
			printNoSuchModuleAndExit(moduleName);
		} else if (foundModules.size() == 1) {
			// These is only one right module and we just found it
			module = foundModules.iterator().next();
		} else {
			// Ambiguous, but maybe the name isn't a partial name after all
			module = registry.findModule(moduleName);

			if (module == null) {
				printAmbiguousModuleNameAndExit(moduleName, foundModules);
			}
		}

		List<Parameter> parameters = ModuleUtils.getParameters(module);
		List<Parameter> allParameters = ModuleUtils.getAllParameters(module);

		List<ReturnValue> returnValues = ModuleUtils.getReturnValues(module);
		List<ReturnValue> fileReturnValues = ModuleUtils.getFileReturnValues(module);

		String[] moduleArgs = parametersParser.getModuleArguments(moduleName);

		if (moduleArgs.length < parameters.size()) {
			printTooFewArgumentsAndExit(module);
		}

		if (moduleArgs.length > allParameters.size() + fileReturnValues.size()) {
			printTooManyArgumentsAndExit(module);
		}

		try {
			// Number of parameters for which values are provided
			int numberOfUsedParameters;

			if (moduleArgs.length > allParameters.size()) {
				numberOfUsedParameters = allParameters.size();
			} else {
				numberOfUsedParameters = moduleArgs.length;
			}

			Object[] transformedArgs = new Object[numberOfUsedParameters];


			boolean hasStdInParameter = false;


			// First check if multiple parameters are signaled to be read from the standard input
			for (int i = 0; i < numberOfUsedParameters; i++) {
				// FIXME: This is hard-coded and could be way more flexible
				if (moduleArgs[i].equals(STANDARD_INPUT_SYMBOL)
						&& (IGraph.class.isAssignableFrom(allParameters.get(i).getKlass())
							|| PetriNetOrTransitionSystem.class.isAssignableFrom(
								allParameters.get(i).getKlass()))) {

					if (hasStdInParameter) {
						printCanOnlyReadOneParameterFromStdInAndExit();
					}

					hasStdInParameter = true;
				}
			}

			for (int i = 0; i < numberOfUsedParameters; i++) {
				transformedArgs[i] = parametersTransformer.transform(moduleArgs[i],
						allParameters.get(i).getKlass());
			}

			ModulePreconditionsChecker checker = new SimpleModulePreconditionsChecker();

			List<Parameter> unmetParameters = checker.check(registry, module, transformedArgs);

			if (!unmetParameters.isEmpty()) {
				printPreconditionsUnmetAndExit(unmetParameters);
			}

			ModuleInvoker invoker = new ModuleInvoker();
			List<Object> values = invoker.invoke(module, transformedArgs);

			String[] fileArgs = new String[moduleArgs.length - numberOfUsedParameters];


			boolean hasStandardOutputFileReturnValue = false;

			for (int i = 0; i < fileArgs.length; i++) {
				String filename = moduleArgs[i + numberOfUsedParameters];
				fileArgs[i] = filename;

				if (filename.equals(STANDARD_INPUT_SYMBOL)) {
					hasStandardOutputFileReturnValue = true;
				}
			}

			// Handle module output
			try (CloseableCollection<PrintStream> outputs = new CloseableCollection<>()) {
				boolean outputWithName[] = new boolean[values.size()];

				// Figure out where the values which the module produced should be printed to
				for (int i = 0; i < values.size(); i++) {
					int usedFileArgsCount = 0;

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
								outputs.add(outPrinter, false);
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
					outputs.add(outPrinter, false);
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
					returnValuesTransformer.transform(writer,
							values.get(i), returnValues.get(i).getKlass());
					writer.flush();
					out.println();
				}
			}
			catch (IOException e) {
				errPrinter.println("Error writing to file: " + e.getMessage());
				errPrinter.flush();
				System.exit(ExitStatus.ERROR.getValue());
			}

			ModuleExitStatusChecker statusChecker = new PropertyModuleExitStatusChecker();
			ExitStatus status = statusChecker.check(module, values);

			outPrinter.flush();
			System.exit(status.getValue());
		} catch (ModuleException e) {
			errPrinter.println(String.format("Error while invoking module '%s':%n  %s",
						module.getName(), e.getMessage()));
			errPrinter.flush();
			System.exit(ExitStatus.ERROR.getValue());
		}
	}

	private static void printPreconditionsUnmetAndExit(List<Parameter> unmetParameters) {
		errPrinter.println("Some preconditions are unmet:");

		for (Parameter parameter : unmetParameters) {
			errPrinter.print("Parameter \"" + parameter.getName() + "\" is not");

			for (String property : parameter.getProperties()) {
				errPrinter.print(" " + property);
			}

			errPrinter.println();
		}

		errPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	public static PrintStream openOutput(String fileName) throws IOException {
		File file = new File(fileName);

		if (file.exists())
			throw new IOException("File '" + file + "' already exists");
		return new PrintStream(FileUtils.openOutputStream(file), false, "UTF-8");
	}

	private static void printTooManyArgumentsAndExit(Module module) {
		errPrinter.println("Too many arguments");
		errPrinter.println();
		errPrinter.println(ModuleUtils.getModuleUsage(module));
		errPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printTooFewArgumentsAndExit(Module module) {
		errPrinter.println("Too few arguments");
		errPrinter.println();
		errPrinter.println(ModuleUtils.getModuleUsage(module));
		errPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printAmbiguousModuleNameAndExit(String moduleName,
			Collection<Module> foundModules) {
		errPrinter.println("Ambiguous module name: " + moduleName);

		errPrinter.println();

		errPrinter.println("Available modules (starting with " + moduleName + "):");
		printModuleList(foundModules, errPrinter);

		errPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printNoSuchModuleAndExit(String moduleName) {
		errPrinter.println("No such module: " + moduleName);

		Map<String, String> messages = removedModules.prefixMap(moduleName);
		if (messages.size() == 1) {
			Map.Entry<String, String> entry = messages.entrySet().iterator().next();
			errPrinter.println(String.format("The module '%s' was removed. %s",
						entry.getKey(), entry.getValue()));
		}

		errPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printUsageAndExit() {
		outPrinter.println("Usage: apt <module> <arguments>");
		outPrinter.println();

		outPrinter.println("Available modules:");
		printModuleList(registry.getModules(), outPrinter);

		outPrinter.flush();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printCanOnlyReadOneParameterFromStdInAndExit() {
		errPrinter.println("Only one parameter can be read from standard input at the same time");
		errPrinter.flush();
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
