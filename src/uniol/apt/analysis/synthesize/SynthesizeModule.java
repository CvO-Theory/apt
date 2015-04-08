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
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis as a module.
 * @author Uli Schlachter
 */
public class SynthesizeModule extends AbstractModule {

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
		inputSpec.addParameter("options", String.class,
				"Comma separated list of options,"
				+ " can be verbose, none, safe, [k]-bounded, pure, plain, tnet,"
				+ " output-nonbranching (on), conflict-free (cf),"
				+ " upto-language-equivalence (language, le)");
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

	static public SynthesizePN runSynthesis(TransitionSystem ts, ModuleInput input, ModuleOutput output)
			throws ModuleException {
		Options options = Options.parseProperties(input.getParameter("options", String.class));
		SynthesizePN synthesize;
		if (options.upToLanguageEquivalence)
			synthesize = SynthesizePN.createUpToLanguageEquivalence(ts, options.properties);
		else
			synthesize = new SynthesizePN(ts, options.properties);

		PetriNet pn = synthesize.synthesizePetriNet();
		if (pn != null)
			for (Place p : pn.getPlaces())
				p.removeExtension(Region.class.getName());

		output.setReturnValue("success", Boolean.class, synthesize.wasSuccessfullySeparated());
		output.setReturnValue("pn", PetriNet.class, pn);
		if (options.verbose) {
			output.setReturnValue("solvedEventStateSeparationProblems", String.class,
					getSolvedEventStateSeparationProblems(synthesize));
		}

		return synthesize;
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		SynthesizePN synthesize = runSynthesis(ts, input, output);

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
	 * @param synthesize The synthesize instance to print from.
	 * @return A string representation of the solved problems.
	 */
	static public String getSolvedEventStateSeparationProblems(SynthesizePN synthesize) {
		StringBuilder result = new StringBuilder("");
		RegionUtility utility = synthesize.getUtility();
		TransitionSystem ts = utility.getTransitionSystem();

		for (Region region : synthesize.getSeparatingRegions()) {
			result.append("\nRegion ").append(region).append(":");
			for (String event : ts.getAlphabet()) {
				Set<State> states = new HashSet<>();
				for (State state : ts.getNodes()) {
					if (!SeparationUtility.isEventEnabled(state, event)
							&& SeparationUtility.isSeparatingRegion(utility, region,
								state, event))
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
		public final boolean verbose;
		public final boolean upToLanguageEquivalence;

		public Options(PNProperties properties, boolean verbose, boolean upToLanguageEquivalence) {
			this.properties = properties;
			this.verbose = verbose;
			this.upToLanguageEquivalence = upToLanguageEquivalence;
		}

		/**
		 * Parse the given string into an Options instance
		 * @param properties the string to parse
		 * @return A representation of the requested options
		 * @throws ModuleException if the properties string is malformed
		 */
		static public Options parseProperties(String properties) throws ModuleException {
			PNProperties result = new PNProperties();
			boolean verbose = false;
			boolean upToLanguageEquivalence = false;

			// Explicitly allow empty string
			properties = properties.trim();
			if (properties.isEmpty())
				return new Options(result, verbose, upToLanguageEquivalence);

			for (String prop : properties.split(",")) {
				prop = prop.trim().toLowerCase();
				switch (prop) {
					case "none":
						break;
					case "safe":
						result.requireSafe();
						break;
					case "pure":
						result.setPure(true);
						break;
					case "plain":
						result.setPlain(true);
						break;
					case "tnet":
						result.setTNet(true);
						break;
					case "output-nonbranching":
					case "on":
						result.setOutputNonbranching(true);
						break;
					case "conflict-free":
					case "cf":
						result.setConflictFree(true);
						break;
					case "verbose":
						verbose = true;
						break;
					case "upto-language-equivalence":
					case "language":
					case "le":
						upToLanguageEquivalence = true;
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
							result.requireKBounded(k);
						} else
							throw new ModuleException("Cannot parse '" + prop
									+ "': Unknown property");
				}
			}
			return new Options(result, verbose, upToLanguageEquivalence);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
