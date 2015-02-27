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
		inputSpec.addParameter("properties", String.class,
				"Comma separated list of properties for the synthesized net,"
				+ " can be none, safe, [k]-bounded, pure, plain, t-net, output-nonbranching (on), conflict-free (cf), upto-language-equivalence (language, le)");
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		requireCommon(inputSpec);
		inputSpec.addParameter("lts", TransitionSystem.class,
				"The LTS that should be synthesized to a Petri Net");
	}

	static public void provideCommon(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("warning", String.class);
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("separatingRegions", RegionCollection.class);
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		provideCommon(outputSpec);
		outputSpec.addReturnValue("failedStateSeparationProblems", String.class);
		outputSpec.addReturnValue("failedEventStateSeparationProblems", String.class);
	}

	static public SynthesizePN runSynthesis(TransitionSystem ts, ModuleInput input, ModuleOutput output) throws ModuleException {
		output.setReturnValue("warning", String.class, "THIS MODULE IS EXPERIMENTAL AND SHOULD NOT BE TRUSTED");

		PNProperties properties = new PNProperties();
		boolean uptoLanguageEquivalence = parseProperties(properties, input.getParameter("properties", String.class));
		SynthesizePN synthesize;
		if (uptoLanguageEquivalence)
			synthesize = SynthesizePN.createUpToLanguageEquivalence(ts, properties);
		else
			synthesize = new SynthesizePN(ts, properties);

		PetriNet pn = synthesize.synthesizePetriNet();
		if (pn != null)
			for (Place p : pn.getPlaces())
				p.removeExtension(Region.class.getName());

		output.setReturnValue("success", Boolean.class, synthesize.wasSuccessfullySeparated());
		output.setReturnValue("pn", PetriNet.class, pn);
		output.setReturnValue("separatingRegions", RegionCollection.class,
				new RegionCollection(synthesize.getSeparatingRegions()));

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
			for (Map.Entry<String, Set<State>> entry : synthesize.getFailedEventStateSeparationProblems().entrySet()) {
				Set<String> states = new TreeSet<>();
				for (State state : entry.getValue())
					states.add(state.getId());
				failedESSP.put(entry.getKey(), states);
			}
			output.setReturnValue("failedEventStateSeparationProblems", String.class, failedESSP.toString());
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}

	static public boolean parseProperties(PNProperties result, String properties) throws ModuleException {
		// Explicitly allow empty string
		properties = properties.trim();
		if (properties.isEmpty())
			return false;

		boolean uptoLanguageEquivalence = false;
		for (String prop : properties.split(",")) {
			prop = prop.trim().toLowerCase();
			switch (prop) {
				case "none":
					break;
				case "safe":
					result.add(PNProperties.SAFE);
					break;
				case "pure":
					result.add(PNProperties.PURE);
					break;
				case "plain":
					result.add(PNProperties.PLAIN);
					break;
				case "tnet":
					result.add(PNProperties.TNET);
					break;
				case "output-nonbranching":
				case "on":
					result.add(PNProperties.OUTPUT_NONBRANCHING);
					break;
				case "conflict-free":
				case "cf":
					result.add(PNProperties.CONFLICT_FREE);
					break;
				case "upto-language-equivalence":
				case "language":
				case "le":
					uptoLanguageEquivalence = true;
					break;
				default:
					if (prop.endsWith("-bounded")) {
						String value = prop.substring(0, prop.length() - "-bounded".length());
						int k;
						try {
							k = Integer.parseInt(value);
						}
						catch (NumberFormatException e) {
							throw new ModuleException("Cannot parse '" + prop + "': "
									+ "Invalid number for property 'k-bounded'");
						}
						if (k < 1)
							throw new ModuleException("Cannot parse '" + prop + "': "
									+ "Bound must be positive");
						result.add(PNProperties.kBounded(k));
					} else
						throw new ModuleException("Cannot parse '" + prop
								+ "': Unknown property");
			}
		}
		return uptoLanguageEquivalence;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
