/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Base class for different modules provide net synthesis.
 * @author Uli Schlachter, vsp
 */
public abstract class AbstractSynthesizeModule extends AbstractInterruptibleModule implements InterruptibleModule {
	static protected interface TransitionSystemForOptions {
		public Collection<String> supportedExtraOptions();
		public TransitionSystem getTS(Collection<String> enabledOptions);
	}

	static protected interface ConfigureSynthesizePNBuilder {
		public void configureSynthesizePNBuilder(SynthesizePN.Builder builder);
	}

	static String getOptionsDescription(String extraOptions, String extraOptionsDescriptions) {
		return "Supported options are: none, [k]-bounded, safe, [k]-marking, pure, plain, tnet,"
			+ " generalized-marked-graph (gmg), marked-graph (mg), generalized-output-nonbranching (gon)"
			+ " output-nonbranching (on), merge-free (mf), conflict-free (cf), homogeneous,"
			+ " behaviourally-conflict-free (bcf), binary-conflict-free (bicf), equal-conflict (ec),"
			+ " upto-language-equivalence (language, le), " + extraOptions + "minimize (minimal), verbose "
			+ "and quick-fail.\n\nThe meaning of these options is as follows:\n"
			+ " - none: No further requirements are made.\n"
			+ " - [k]-bounded: In every reachable marking, every place contains at most [k] tokens.\n"
			+ " - safe: Equivalent to 1-bounded.\n"
			+ " - [k]-marking: The initial marking of each place is a multiple of k.\n"
			+ " - pure: Every transition either consumes or produces tokens on a place, but not both"
			+ " (=no side-conditions).\n"
			+ " - plain: Every flow has a weight of at most one.\n"
			+ " - tnet: Every place's preset and postset contains at most one entry.\n"
			+ " - generalized-marked-graph: Every place's preset and postset contains exactly one entry.\n"
			+ " - marked-graph: generalized-marked-graph + plain.\n"
			+ " - generalized-output-nonbranching: Every place's postset contains at most one entry.\n"
			+ " - output-nonbranching: generalized-output-nonbranching + plain.\n"
			+ " - merge-free: Every place's pretset contains at most one entry.\n"
			+ " - conflict-free: The Petri net is plain and every place either has at most one entry in"
			+ " its postset or its preset is contained in its postset.\n"
			+ " - homogeneous: All outgoing flows from a place have the same weight.\n"
			+ " - behaviourally-conflict-free: In every reachable marking, the preset of activated"
			+ " transitions is disjoint.\n"
			+ " - binary-conflict-free: For every reachable marking and pair of activated transitions,"
			+ " enough tokens for both transitions are present.\n"
			+ " - equal-conflict: The Petri net is homogeneous and two transitions with non-disjoint"
			+ " presets have equal presets.\n"
			+ " - minimize: The Petri net has as few places as possible.\n"
			+ extraOptionsDescriptions
			+ "The following options only affect the output, but not the produced Petri net:\n"
			+ " - verbose: Print details about each calculated region/place.\n"
			+ " - quick-fail: Stop the algorithm when the result 'success: No' is clear.";
	}

	/**
	 * Override point for specification of extra input parameters.
	 *
	 * @param inputSpec object which collects the input parameter specifications.
	 */
	protected abstract void requireExtra(ModuleInputSpec inputSpec);

	@Override
	public final void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("options", String.class, "Comma separated list of options");
		requireExtra(inputSpec);
	}

	/**
	 * Override point for specification of extra return values.
	 *
	 * @param outputSpec object which collects the return value specifications.
	 */
	protected abstract void provideExtra(ModuleOutputSpec outputSpec);

	@Override
	public final void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("solvedEventStateSeparationProblems", String.class);
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
		provideExtra(outputSpec);
	}

	public SynthesizePN runSynthesis(TransitionSystemForOptions tsForOpts, ModuleInput input,
				ModuleOutput output) throws ModuleException {
		return runSynthesis(tsForOpts, null, input, output);
	}

	public SynthesizePN runSynthesis(TransitionSystemForOptions tsForOpts, ConfigureSynthesizePNBuilder configure,
				ModuleInput input, ModuleOutput output) throws ModuleException {
		String quickFailStr = "quick-fail", verboseStr = "verbose";
		Collection<String> languageEquivalenceStr = Arrays.asList("upto-language-equivalence", "language",
				"le");
		Collection<String> minimizeStr = Arrays.asList("minimize", "minimise", "minimal");
		Set<String> supportedExtraOptions = new HashSet<>(Arrays.asList(quickFailStr, verboseStr));
		supportedExtraOptions.addAll(languageEquivalenceStr);
		supportedExtraOptions.addAll(minimizeStr);
		supportedExtraOptions.addAll(tsForOpts.supportedExtraOptions());

		Options options = Options.parseProperties(input.getParameter("options", String.class),
				supportedExtraOptions);
		boolean quickFail = options.extraOptions.contains(quickFailStr);
		boolean verbose = options.extraOptions.contains(verboseStr);
		boolean languageEquivalence = !Collections.disjoint(options.extraOptions, languageEquivalenceStr);
		boolean minimize = !Collections.disjoint(options.extraOptions, minimizeStr);

		SynthesizePN synthesize;
		SynthesizePN.Builder builder;
		TransitionSystem ts = tsForOpts.getTS(options.extraOptions);
		if (languageEquivalence)
			builder = SynthesizePN.Builder.createForLanguageEquivalence(ts);
		else
			builder = SynthesizePN.Builder.createForIsomorphicBehaviour(ts);
		builder .setProperties(options.properties)
			.setQuickFail(quickFail);
		if (configure != null)
			configure.configureSynthesizePNBuilder(builder);
		synthesize = builder.build();

		PetriNet pn;
		Collection<Region> regions;
		if (synthesize.wasSuccessfullySeparated() && minimize) {
			MinimizePN min = new MinimizePN(synthesize);
			regions = min.getSeparatingRegions();
			pn = min.synthesizePetriNet();
		} else {
			regions = synthesize.getSeparatingRegions();
			pn = synthesize.synthesizePetriNet();
		}

		output.setReturnValue("success", Boolean.class, synthesize.wasSuccessfullySeparated());
		output.setReturnValue("pn", PetriNet.class, pn);
		if (verbose) {
			output.setReturnValue("solvedEventStateSeparationProblems", String.class,
					getSolvedEventStateSeparationProblems(
						synthesize.getUtility().getTransitionSystem(),
						regions));
		}

		return synthesize;
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}

	/**
	 * Get the IDs of a set of states.
	 * @param states The states to look at
	 * @return The set of state IDs.
	 */
	static public Set<String> getStateIDs(Set<State> states) {
		Set<String> result = new TreeSet<>();
		for (State state : states)
			result.add(state.getId());
		return result;
	}

	/**
	 * Get a string representation that describes for each region which Event/State Separation Problems it solves.
	 * @param ts The transition system that should be solved.
	 * @param regions The regions that solve (part of) the transition system.
	 * @return A string representation of the solved problems.
	 */
	static public String getSolvedEventStateSeparationProblems(TransitionSystem ts, Collection<Region> regions) {
		StringBuilder result = new StringBuilder("");

		for (Region region : regions) {
			result.append("\nRegion ").append(region).append(":");
			for (String event : ts.getAlphabet()) {
				Set<State> states = new HashSet<>();
				for (State state : ts.getNodes()) {
					if (!SeparationUtility.isEventEnabled(state, event)
							&& SeparationUtility.isSeparatingRegion(region, state, event))
						states.add(state);
				}
				if (!states.isEmpty()) {
					result.append("\n\tseparates event ");
					result.append(event);
					result.append(" at states ");
					result.append(getStateIDs(states));
				}
			}
		}

		String ret = result.toString();
		if (ret.isEmpty())
			return "none";
		return ret;
	}

	/**
	 * Instances of this class hold options that can be specified for synthesis.
	 */
	static public class Options {
		public final PNProperties properties;
		public final Set<String> extraOptions;

		public Options(PNProperties properties, Collection<String> extraOptions) {
			this.properties = properties;
			this.extraOptions = Collections.unmodifiableSet(new HashSet<>(extraOptions));
		}

		/**
		 * Parse the given string into an Options instance
		 * @param properties the string to parse
		 * @return A representation of the requested options
		 * @throws ModuleException if the properties string is malformed
		 */
		static public Options parseProperties(String properties) throws ModuleException {
			return parseProperties(properties, Collections.<String>emptySet());
		}

		/**
		 * Parse the given string into an Options instance
		 * @param properties the string to parse
		 * @param supportedExtraOptions extra options which should also be supported
		 * @return A representation of the requested options
		 * @throws ModuleException if the properties string is malformed
		 */
		static public Options parseProperties(String properties, Collection<String> supportedExtraOptions)
				throws ModuleException {
			PNProperties result = new PNProperties();

			// Explicitly allow empty string
			properties = properties.trim();
			if (properties.isEmpty())
				return new Options(result, Collections.<String>emptySet());

			Set<String> extraOptions = new HashSet<>();
			for (String prop : properties.split(",")) {
				prop = prop.trim().toLowerCase();

				if (supportedExtraOptions.contains(prop)) {
					extraOptions.add(prop);
					continue;
				}

				switch (prop) {
					case "none":
						break;
					case "safe":
						result = result.requireSafe();
						break;
					case "pure":
						result = result.setPure(true);
						break;
					case "plain":
						result = result.setPlain(true);
						break;
					case "tnet":
						result = result.setTNet(true);
						break;
					case "generalized-marked-graph":
					case "generalised-marked-graph":
					case "gmg":
						result = result.setMarkedGraph(true);
						break;
					case "marked-graph":
					case "mg":
						result = result.setMarkedGraph(true).setPlain(true);
						break;
					case "generalized-output-nonbranching":
					case "generalised-output-nonbranching":
					case "gon":
						result = result.setOutputNonbranching(true);
						break;
					case "output-nonbranching":
					case "on":
						result = result.setOutputNonbranching(true).setPlain(true);
						break;
					case "merge-free":
					case "mf":
						result = result.setMergeFree(true);
						break;
					case "conflict-free":
					case "cf":
						result = result.setConflictFree(true);
						break;
					case "homogeneous":
						result = result.setHomogeneous(true);
						break;
					case "behaviourally-conflict-free":
					case "bcf":
						result = result.setBehaviourallyConflictFree(true);
						break;
					case "binary-conflict-free":
					case "bicf":
						result = result.setBinaryConflictFree(true);
						break;
					case "equal-conflict":
					case "ec":
						result = result.setEqualConflict(true);
						break;
					default:
						if (prop.endsWith("-bounded")) {
							result = result.requireKBounded(parseInt(prop, "-bounded"));
						} else if (prop.endsWith("-marking")) {
							result = result.requireKMarking(parseInt(prop, "-marking"));
						} else
							throw new ModuleException("Cannot parse '" + prop
									+ "': Unknown property");
				}
			}
			return new Options(result, extraOptions);
		}

		static private int parseInt(String prop, String suffix) throws ModuleException {
			assert prop.endsWith(suffix);
			String value = prop.substring(0, prop.length() - suffix.length());
			int k;
			try {
				k = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new ModuleException("Cannot parse '" + prop + "': "
						+ "Invalid number for property 'k" + suffix + "'");
			}
			if (k < 1)
				throw new ModuleException("Cannot parse '" + prop + "': "
						+ "Bound must be positive");
			return k;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
