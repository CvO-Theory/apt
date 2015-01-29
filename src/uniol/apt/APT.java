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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import uniol.apt.analysis.ExamineLTSModule;
import uniol.apt.analysis.ExaminePNModule;
import uniol.apt.analysis.InfoModule;
import uniol.apt.analysis.algebra.MatrixFileFormat;
import uniol.apt.analysis.algebra.MatrixModule;
import uniol.apt.analysis.bcf.BCFModule;
import uniol.apt.analysis.bicf.BiCFModule;
import uniol.apt.analysis.bisimulation.BisimulationModule;
import uniol.apt.analysis.bisimulation.NonBisimilarPath;
import uniol.apt.analysis.bounded.BoundedModule;
import uniol.apt.analysis.bounded.KBoundedModule;
import uniol.apt.analysis.bounded.SafeModule;
import uniol.apt.analysis.cf.ConflictFreeModule;
import uniol.apt.analysis.connectivity.Component;
import uniol.apt.analysis.connectivity.Components;
import uniol.apt.analysis.connectivity.IsolatedElementsModule;
import uniol.apt.analysis.connectivity.StrongComponentsModule;
import uniol.apt.analysis.connectivity.StrongConnectivityModule;
import uniol.apt.analysis.connectivity.WeakComponentsModule;
import uniol.apt.analysis.connectivity.WeakConnectivityModule;
import uniol.apt.analysis.conpres.ConcurrencyPreservingModule;
import uniol.apt.analysis.coverability.CoverabilityModule;
import uniol.apt.analysis.cycles.CheckAllCyclePropertiesModule;
import uniol.apt.analysis.cycles.lts.CycleCounterExample;
import uniol.apt.analysis.cycles.lts.CyclesHaveSameOrMutallyDisjointPVModule;
import uniol.apt.analysis.cycles.lts.CyclesHaveSamePVModule;
import uniol.apt.analysis.cycles.lts.PVsOfSmallestCyclesModule;
import uniol.apt.analysis.deterministic.DeterministicModule;
import uniol.apt.analysis.fc.FCModule;
import uniol.apt.analysis.fcnet.FCNetModule;
import uniol.apt.analysis.invariants.ComputeMinSemiPosInvariantsModule;
import uniol.apt.analysis.invariants.CoveredByInvariantModule;
import uniol.apt.analysis.invariants.Vector;
import uniol.apt.analysis.isolated.IsolatedModule;
import uniol.apt.analysis.isomorphism.IsomorphismModule;
import uniol.apt.analysis.language.FiringSequence;
import uniol.apt.analysis.language.LanguageEquivalenceModule;
import uniol.apt.analysis.language.Word;
import uniol.apt.analysis.language.WordInLanguageModule;
import uniol.apt.analysis.language.WordList;
import uniol.apt.analysis.live.SimplyLiveModule;
import uniol.apt.analysis.live.StronglyLiveModule;
import uniol.apt.analysis.live.WeaklyLiveModule;
import uniol.apt.analysis.on.OutputNonBranchingModule;
import uniol.apt.analysis.persistent.PersistentModule;
import uniol.apt.analysis.persistent.PersistentNetModule;
import uniol.apt.analysis.persistent.PersistentTSModule;
import uniol.apt.analysis.petrify.PetrifySynthesizeModule;
import uniol.apt.analysis.plain.PlainModule;
import uniol.apt.analysis.reversible.ReversibleModule;
import uniol.apt.analysis.reversible.ReversibleNetModule;
import uniol.apt.analysis.reversible.ReversibleTSModule;
import uniol.apt.analysis.separation.LargestKModule;
import uniol.apt.analysis.separation.StrongSeparationLengthModule;
import uniol.apt.analysis.separation.StrongSeparationModule;
import uniol.apt.analysis.separation.WeakSeparationLengthModule;
import uniol.apt.analysis.separation.WeakSeparationModule;
import uniol.apt.analysis.sideconditions.CheckSideConditionsModule;
import uniol.apt.analysis.sideconditions.NonPureModule;
import uniol.apt.analysis.sideconditions.PureModule;
import uniol.apt.analysis.sideconditions.SideConditions;
import uniol.apt.analysis.snet.SNetModule;
import uniol.apt.analysis.snet.SNetResult;
import uniol.apt.analysis.synet.SynthesizeDistributedLTSModule;
import uniol.apt.analysis.synthesize.RegionCollection;
import uniol.apt.analysis.synthesize.SynthesizeModule;
import uniol.apt.analysis.synthesize.SynthesizeWordModule;
import uniol.apt.analysis.tnet.TNetModule;
import uniol.apt.analysis.tnet.TNetResult;
import uniol.apt.analysis.totallyreachable.TotallyReachableModule;
import uniol.apt.analysis.trapsAndSiphons.SiphonModule;
import uniol.apt.analysis.trapsAndSiphons.TrapsModule;
import uniol.apt.analysis.trapsAndSiphons.TrapsSiphonsList;
import uniol.apt.check.CheckModule;
import uniol.apt.extension.ExtendMode;
import uniol.apt.extension.ExtendTSExplicitPVModule;
import uniol.apt.extension.ExtendTSModule;
import uniol.apt.generator.module.BistatePhilNetGeneratorModule;
import uniol.apt.generator.module.BitNetGeneratorModule;
import uniol.apt.generator.module.CycleNetGeneratorModule;
import uniol.apt.generator.module.InverseNetGeneratorModule;
import uniol.apt.generator.module.QuadstatePhilNetGeneratorModule;
import uniol.apt.generator.module.TNetGeneratorModule;
import uniol.apt.generator.module.TristatePhilNetGeneratorModule;
import uniol.apt.io.converter.Apt2PetrifyModule;
import uniol.apt.io.converter.Apt2SynetModule;
import uniol.apt.io.converter.Petrify2AptModule;
import uniol.apt.io.converter.Synet2AptModule;
import uniol.apt.io.renderer.impl.BagginsRendererModule;
import uniol.apt.io.renderer.impl.LoLARendererModule;
import uniol.apt.io.renderer.impl.PNMLRendererModule;
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
import uniol.apt.module.impl.ModuleVisibility;
import uniol.apt.module.impl.Parameter;
import uniol.apt.module.impl.ReturnValue;
import uniol.apt.module.impl.SimpleModulePreconditionsChecker;
import uniol.apt.pnanalysis.PnAnalysisModule;
import uniol.apt.pnanalysis.RandomTNetGeneratorModule;
import uniol.apt.ui.ParametersParser;
import uniol.apt.ui.ParametersTransformer;
import uniol.apt.ui.Printer;
import uniol.apt.ui.ReturnValuesTransformer;
import uniol.apt.ui.impl.DrawModule;
import uniol.apt.ui.impl.DrawNetModule;
import uniol.apt.ui.impl.DrawTSModule;
import uniol.apt.ui.impl.HelpModule;
import uniol.apt.ui.impl.InternalsModule;
import uniol.apt.ui.impl.SimpleParametersParser;
import uniol.apt.ui.impl.TrimmedOutputStreamPrinter;
import uniol.apt.ui.impl.parameter.CharacterParameterTransformation;
import uniol.apt.ui.impl.parameter.ExtendModeParameterTransformation;
import uniol.apt.ui.impl.parameter.GraphParameterTransformation;
import uniol.apt.ui.impl.parameter.IntegerParameterTransformation;
import uniol.apt.ui.impl.parameter.MatrixFileFormatParameterTransformation;
import uniol.apt.ui.impl.parameter.NetOrTSParameterTransformation;
import uniol.apt.ui.impl.parameter.NetParameterTransformation;
import uniol.apt.ui.impl.parameter.ParikhVectorParameterTransformation;
import uniol.apt.ui.impl.parameter.StringParameterTransformation;
import uniol.apt.ui.impl.parameter.TSParameterTransformation;
import uniol.apt.ui.impl.parameter.WordParameterTransformation;
import uniol.apt.ui.impl.returns.BooleanReturnValueTransformation;
import uniol.apt.ui.impl.returns.ComponentsReturnValueTransformation;
import uniol.apt.ui.impl.returns.INodeCollectionReturnValueTransformation;
import uniol.apt.ui.impl.returns.INodeReturnValueTransformation;
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
	 * All the modules that are used in APT.
	 */
	private static final Module[] modules = {
		new Apt2SynetModule(),
		new Apt2PetrifyModule(),
		new BagginsRendererModule(),
		new BCFModule(),
		new BiCFModule(),
		new BisimulationModule(),
		new BistatePhilNetGeneratorModule(),
		new BitNetGeneratorModule(),
		new BoundedModule(),
		new CheckAllCyclePropertiesModule(),
		new CheckModule(),
		new CheckSideConditionsModule(),
		new ComputeMinSemiPosInvariantsModule(),
                new ConcurrencyPreservingModule(),
		new ConflictFreeModule(),
		new CoverabilityModule(),
		new CoveredByInvariantModule(),
		new CycleNetGeneratorModule(),
		new CyclesHaveSameOrMutallyDisjointPVModule(),
		new CyclesHaveSamePVModule(),
		new DeterministicModule(),
		new DrawModule(),
		new ExamineLTSModule(),
		new ExaminePNModule(),
		new ExtendTSModule(),
		new ExtendTSExplicitPVModule(),
		new FCModule(),
		new FCNetModule(),
		new InfoModule(),
		new InverseNetGeneratorModule(),
		new IsolatedElementsModule(),
		new IsolatedModule(),
		new IsomorphismModule(),
		new KBoundedModule(),
		new LanguageEquivalenceModule(),
		new LargestKModule(),
		new LoLARendererModule(),
		new MatrixModule(),
		new NonPureModule(),
		new OutputNonBranchingModule(),
		new PVsOfSmallestCyclesModule(),
		new PersistentModule(),
		new Petrify2AptModule(),
		new PetrifySynthesizeModule(),
		new PlainModule(),
		new PnAnalysisModule(),
		new PNMLRendererModule(),
		new PureModule(),
		new QuadstatePhilNetGeneratorModule(),
		new RandomTNetGeneratorModule(),
		new ReversibleModule(),
		new SNetModule(),
		new SafeModule(),
		new SimplyLiveModule(),
		new SiphonModule(),
		new StrongComponentsModule(),
		new StrongConnectivityModule(),
		new StrongSeparationLengthModule(),
		new StrongSeparationModule(),
		new StronglyLiveModule(),
		new Synet2AptModule(),
		new SynthesizeDistributedLTSModule(),
		new SynthesizeModule(),
		new SynthesizeWordModule(),
		new TNetGeneratorModule(),
		new TNetModule(),
		new TotallyReachableModule(),
		new TrapsModule(),
		new TristatePhilNetGeneratorModule(),
		new WeakComponentsModule(),
		new WeakConnectivityModule(),
		new WeakSeparationLengthModule(),
		new WeakSeparationModule(),
		new WeaklyLiveModule(),
		new WordInLanguageModule()
	};

	/**
	 * All the internal modules that are used in APT. These modules can't be invoked by the user directly.
	 */
	private static final Module[] internalModules = {
		new DrawNetModule(),
		new DrawTSModule(),
		new PersistentTSModule(),
		new PersistentNetModule(),
		new ReversibleTSModule(),
		new ReversibleNetModule()
	};

	private static final ParametersParser parametersParser = new SimpleParametersParser();
	private static final ParametersTransformer parametersTransformer = new ParametersTransformer();
	private static final ReturnValuesTransformer returnValuesTransformer = new ReturnValuesTransformer();
	private static final ModuleRegistry registry = new ModuleRegistry();

	private static final Printer outPrinter = new TrimmedOutputStreamPrinter(System.out);
	private static final Printer errPrinter = new TrimmedOutputStreamPrinter(System.err);

	/**
	 * Hidden Constructor.
	 */
	private APT() {
	}

	/**
	 * Register all modules that are used in APT
	 */
	public static void registerModules() {
		registry.registerModules(modules);
		registry.registerModules(ModuleVisibility.INTERNAL, internalModules);

		InternalsModule internalsModule = new InternalsModule();
		HelpModule helpModule = new HelpModule(registry);

		internalsModule.setModuleRegistry(registry);
		internalsModule.setParamTransformer(parametersTransformer);
		internalsModule.setReturnValueTransformer(returnValuesTransformer);

		registry.registerModule(internalsModule, ModuleVisibility.HIDDEN);
		registry.registerModule(helpModule);
	}

	/**
	 * Add parameter transformations.
	 * <p/>
	 */
	@SuppressWarnings("unchecked")
	public static void addParametersTransformations() {
		parametersTransformer.addTransformation(String.class, new StringParameterTransformation());
		parametersTransformer.addTransformation(Character.class, new CharacterParameterTransformation());
		parametersTransformer.addTransformation(Integer.class, new IntegerParameterTransformation());
		parametersTransformer.addTransformation(Word.class, new WordParameterTransformation());
		parametersTransformer.addTransformation(PetriNet.class, new NetParameterTransformation());
		parametersTransformer.addTransformation(TransitionSystem.class, new TSParameterTransformation());
		parametersTransformer.addTransformation(ExtendMode.class, new ExtendModeParameterTransformation());
		parametersTransformer.addTransformation(ParikhVector.class, new ParikhVectorParameterTransformation());
		parametersTransformer.addTransformation(PetriNetOrTransitionSystem.class,
			new NetOrTSParameterTransformation());
		parametersTransformer.addTransformation((Class<IGraph<?, ?, ?>>) (Class<?>) IGraph.class,
			new GraphParameterTransformation());
		parametersTransformer.addTransformation(MatrixFileFormat.class, new MatrixFileFormatParameterTransformation());
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
		returnValuesTransformer.addTransformation(Integer.class,
			new ToStringReturnValueTransformation<Integer>());
		returnValuesTransformer.addTransformation(Marking.class, new MarkingReturnValueTransformation());
		returnValuesTransformer.addTransformation(State.class, new INodeReturnValueTransformation<State>());
		returnValuesTransformer.addTransformation(NonBisimilarPath.class,
			new NonBisimilarPathReturnValueTransformation());
		returnValuesTransformer.addTransformation(ParikhVector.class,
			new ToStringReturnValueTransformation<ParikhVector>());
		returnValuesTransformer.addTransformation(PetriNet.class, new NetReturnValueTransformation());
		returnValuesTransformer.addTransformation(Place.class, new INodeReturnValueTransformation<Place>());
		returnValuesTransformer.addTransformation(RegionCollection.class,
				new ToStringReturnValueTransformation<RegionCollection>());
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

	public static void main(String[] args) {
		registerModules();

		addParametersTransformations();
		addReturnValuesTransformations();

		parametersParser.parse(args);

		String[] moduleNames = parametersParser.getModuleNames();

		if (moduleNames.length == 0) {
			printUsageAndExit();
		}

		String moduleName = moduleNames[0]; // Only use a single module for now
		Collection<Module> foundModules = registry.findModulesByPrefix(moduleName, ModuleVisibility.SHOWN);

		Module module;

		if (foundModules.isEmpty()) {
			// Okay, maybe it's a hidden module
			module = registry.findModule(moduleName, ModuleVisibility.HIDDEN);

			if (module == null) {
				printNoSuchModuleAndExit(moduleName);
			}
		} else if (foundModules.size() == 1) {
			// These is only one right module and we just found it
			module = foundModules.iterator().next();
		} else {
			// Ambiguous, but maybe the name isn't a partial name after all
			module = registry.findModule(moduleName, ModuleVisibility.SHOWN);

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
				if (moduleArgs[i].equals(NetOrTSParameterTransformation.STANDARD_INPUT_SYMBOL)
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

				if (filename.equals(NetOrTSParameterTransformation.STANDARD_INPUT_SYMBOL)) {
					hasStandardOutputFileReturnValue = true;
				}
			}

			// Print all return values for which the module produced values
			for (int i = 0; i < values.size(); i++) {
				int usedFileArgsCount = 0;

				if (values.get(i) == null) {
					continue;
				}

				String transformedValue =
						returnValuesTransformer.transform(values.get(i), returnValues.get(i).getKlass());

				String returnValueName = returnValues.get(i).getName();

				boolean isRawReturnValue =
						Arrays.asList(returnValues.get(i).getProperties()).contains(ModuleOutputSpec.PROPERTY_RAW);

				boolean isFileReturnValue =
						Arrays.asList(returnValues.get(i).getProperties()).contains(ModuleOutputSpec.PROPERTY_FILE);

				// Print the return value to file without its name
				if (isFileReturnValue) {
					// Check if the user supplied a file name for this return value
					if (fileArgs.length > usedFileArgsCount) {
						String filename = fileArgs[usedFileArgsCount];

						if (filename.equals(NetOrTSParameterTransformation.STANDARD_INPUT_SYMBOL)) {
							outPrinter.println(transformedValue);
						} else {
							writeTransformedValueToString(transformedValue, filename);
						}

						usedFileArgsCount++;
						continue;
					}
				}

				// Only print the file return value when requested; skip the other return values
				if (hasStandardOutputFileReturnValue) continue;

				// Print the return value without its name
				if (isRawReturnValue) {
					outPrinter.println(transformedValue);
					continue;
				}

				// Print this ordinary return value
				outPrinter.println(returnValueName + ": " + transformedValue);

			}

			ModuleExitStatusChecker statusChecker = new PropertyModuleExitStatusChecker();
			ExitStatus status = statusChecker.check(module, values);

			outPrinter.show();
			System.exit(status.getValue());
		} catch (ModuleException e) {
			errPrinter.println("Error while invoking module '" + module.getName() + "':\n" + "  " + e.getMessage());
			errPrinter.show();
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

		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	public static void writeTransformedValueToString(String value, String fileName) {
		File file = new File(fileName);

		if (!file.exists()) {
			try {
				FileUtils.write(new File(fileName), value);
			} catch (IOException e) {
				errPrinter.println("Error writing to file " + e.getMessage());
				errPrinter.show();
				System.exit(ExitStatus.ERROR.getValue());
			}
		} else {
			errPrinter.println("File already exists: " + file.getAbsolutePath());
			errPrinter.show();
			System.exit(ExitStatus.ERROR.getValue());
		}
	}

	private static void printTooManyArgumentsAndExit(Module module) {
		errPrinter.println("Too many arguments");
		errPrinter.println();
		errPrinter.println(ModuleUtils.getModuleUsage(module));
		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printTooFewArgumentsAndExit(Module module) {
		errPrinter.println("Too few arguments");
		errPrinter.println();
		errPrinter.println(ModuleUtils.getModuleUsage(module));
		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printAmbiguousModuleNameAndExit(String moduleName,
		Collection<Module> foundModules) {
		errPrinter.println("Ambiguous module name: " + moduleName);

		errPrinter.println();

		errPrinter.println("Available modules (starting with " + moduleName + "):");
		printModuleList(foundModules, errPrinter);

		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printNoSuchModuleAndExit(String moduleName) {
		errPrinter.println("No such module: " + moduleName);
		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printUsageAndExit() {
		outPrinter.println("Usage: apt <module> <arguments>");
		outPrinter.println();

		outPrinter.println("Available modules:");
		printModuleList(registry.getModules(ModuleVisibility.SHOWN), outPrinter);

		outPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printCanOnlyReadOneParameterFromStdInAndExit() {
		errPrinter.println("Only one parameter can be read from standard input at the same time");
		errPrinter.show();
		System.exit(ExitStatus.ERROR.getValue());
	}

	private static void printModuleList(Collection<Module> modules, Printer printer) {
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
