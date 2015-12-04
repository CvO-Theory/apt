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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis as a module.
 * @author Uli Schlachter
 */
@AptModule
public class SynthesizeModule extends AbstractModule implements Module {

	@Override
	public String getShortDescription() {
		return "Synthesize a Petri Net from a transition system";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\nExample calls:\n\n"
			+ " apt " + getName() + " none lts.apt\n"
			+ " apt " + getName() + " 3-bounded lts.apt\n"
			+ " apt " + getName() + " pure,safe lts.apt\n"
			+ " apt " + getName() + " upto-language-equivalence lts.apt\n";
	}

	@Override
	public String getName() {
		return "synthesize";
	}

	static public void requireCommon(ModuleInputSpec inputSpec) {
		requireCommon(inputSpec, "", "");
	}

	static public void requireCommon(ModuleInputSpec inputSpec, String extra1, String extra2) {
		inputSpec.addParameter("options", String.class,
				"Comma separated list of options,"
				+ " can be verbose, none, safe, [k]-bounded, pure, plain, tnet, marked-graph,"
				+ " output-nonbranching (on), conflict-free (cf), homogenous, "
				+ " upto-language-equivalence (language, le), minimize" + extra1 + "."
				+ " Special options are verbose (print detail information about the regions),"
				+ " quick-fail (fail quickly when the result 'success: No' is known)" + extra2 + " and"
				+ " minimize (minimize the number of places in the solution).");
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		requireCommon(inputSpec);
		inputSpec.addParameter("lts", TransitionSystem.class,
				"The LTS that should be synthesized to a Petri Net");
	}

	static public void provideCommon(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("solvedEventStateSeparationProblems", String.class);
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		provideCommon(outputSpec);
		outputSpec.addReturnValue("failedStateSeparationProblems", String.class);
		outputSpec.addReturnValue("failedEventStateSeparationProblems", String.class);
	}

	static protected interface TransitionSystemForOptions {
		public Collection<String> supportedExtraOptions();
		public TransitionSystem getTS(Collection<String> enabledOptions);
	}

	static protected class ReturnTS implements TransitionSystemForOptions {
		final private TransitionSystem ts;

		public ReturnTS(TransitionSystem ts) {
			this.ts = ts;
		}

		@Override
		public Collection<String> supportedExtraOptions() {
			return Collections.emptyList();
		}

		@Override
		public TransitionSystem getTS(Collection<String> enabledOptions) {
			return ts;
		}
	}

	static public SynthesizePN runSynthesis(TransitionSystemForOptions tsForOpts, ModuleInput input,
				ModuleOutput output)
			throws ModuleException {
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
		SynthesizePN.Builder builder = new SynthesizePN.Builder(tsForOpts.getTS(options.extraOptions))
			.setProperties(options.properties)
			.setQuickFail(quickFail);
		if (languageEquivalence)
			synthesize = builder.buildForLanguageEquivalence();
		else
			synthesize = builder.buildForIsomorphicBehavior();

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
		if (pn != null)
			for (Place p : pn.getPlaces())
				p.removeExtension(Region.class.getName());

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
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		SynthesizePN synthesize = runSynthesis(new ReturnTS(ts), input, output);

		if (!synthesize.wasSuccessfullySeparated()) {
			Set<Set<String>> failedSSP = new HashSet<>();
			for (Set<State> group : synthesize.getFailedStateSeparationProblems()) {
				Set<String> states = new TreeSet<>();
				for (State state : group)
					states.add(state.getId());
				failedSSP.add(states);
			}
			output.setReturnValue("failedStateSeparationProblems", String.class, failedSSP.toString());

			Map<String, Set<String>> failedESSP = new TreeMap<>();
			for (Map.Entry<String, Set<State>> entry :
					synthesize.getFailedEventStateSeparationProblems().entrySet()) {
				failedESSP.put(entry.getKey(), getStateIDs(entry.getValue()));
			}
			output.setReturnValue("failedEventStateSeparationProblems",
					String.class, failedESSP.toString());
		}
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
					case "marked-graph":
						result = result.setMarkedGraph(true);
						break;
					case "output-nonbranching":
					case "on":
						result = result.setOutputNonbranching(true);
						break;
					case "conflict-free":
					case "cf":
						result = result.setConflictFree(true);
						break;
					case "homogenous":
						result = result.setHomogenous(true);
						break;
					default:
						if (prop.endsWith("-bounded")) {
							String value = prop.substring(0, prop.length() - "-bounded".length());
							int k;
							try {
								k = Integer.parseInt(value);
							} catch (NumberFormatException e) {
								throw new ModuleException("Cannot parse '" + prop + "': "
										+ "Invalid number for property 'k-bounded'");
							}
							if (k < 1)
								throw new ModuleException("Cannot parse '" + prop + "': "
										+ "Bound must be positive");
							result = result.requireKBounded(k);
						} else
							throw new ModuleException("Cannot parse '" + prop
									+ "': Unknown property");
				}
			}
			return new Options(result, extraOptions);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
