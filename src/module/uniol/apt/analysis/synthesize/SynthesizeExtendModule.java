/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AptModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis with some input regions as a module.
 * @author Uli Schlachter
 */
@AptModule
public class SynthesizeExtendModule extends SynthesizeModule implements Module {

	@Override
	public String getShortDescription() {
		return "Synthesize a Petri Net from a transition system, reusing places of a PetriNet";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". This module gets a PetriNet as input and tries to re-use the places"
			+ " of the given PetriNet in the synthesized result.";
	}

	@Override
	public String getName() {
		return "pn_extend_and_synthesize";
	}

	@Override
	protected void requireExtra(ModuleInputSpec inputSpec) {
		super.requireExtra(inputSpec);
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net whose places should be re-used");
	}

	static private BigInteger getArcWeight(Collection<Flow> flows, String event) {
		BigInteger result = BigInteger.ZERO;
		for (Flow flow : flows) {
			if (flow.getTransition().getId().equals(event))
				result = result.add(BigInteger.valueOf(flow.getWeight()));
		}
		return result;
	}

	static private Region addWeightForEvent(Region region, String event) {
		for (Arc arc : region.getTransitionSystem().getEdges()) {
			if (arc.getLabel().equals(event)) {
				try {
					BigInteger source = region.getMarkingForState(arc.getSource());
					BigInteger target = region.getMarkingForState(arc.getTarget());
					Region.Builder builder = new Region.Builder(region);
					builder.addWeightOn(event, target.subtract(source));
					return builder.withInitialMarking(region.getInitialMarking());
				} catch (UnreachableException e) {
					// Try again with another arc
				}
			}
		}
		return region;
	}

	@Override
	public SynthesizePN runSynthesis(TransitionSystemForOptions tsForOpts, ModuleInput input,
				ModuleOutput output) throws ModuleException {
		final PetriNet pn = input.getParameter("pn", PetriNet.class);
		ConfigureSynthesizePNBuilder configure = new ConfigureSynthesizePNBuilder() {
			@Override
			public void configureSynthesizePNBuilder(SynthesizePN.Builder builder) {
				RegionUtility utility = builder.getRegionUtility();
				TransitionSystem ts = utility.getTransitionSystem();
				Set<String> labelsMissingFromTS = new HashSet<>();
				for (Transition transition : pn.getTransitions()) {
					if (!ts.getAlphabet().contains(transition.getId()))
						labelsMissingFromTS.add(transition.getId());
				}
				if (!labelsMissingFromTS.isEmpty())
					System.err.println("Ignoring the following transitions, because no such event"
							+ " is present in the transition system: "
							+ labelsMissingFromTS.toString());

				Set<String> labelsMissingFromPN = new HashSet<>();
				for (String event : ts.getAlphabet()) {
					if (!pn.containsTransition(event))
						labelsMissingFromPN.add(event);
				}
				if (!labelsMissingFromPN.isEmpty())
					System.err.println("The following events are not present in the Petri net: "
							+ labelsMissingFromPN.toString());

				for (Place p : pn.getPlaces()) {
					List<BigInteger> backward = new ArrayList<>();
					List<BigInteger> forward = new ArrayList<>();
					for (String event : utility.getEventList()) {
						backward.add(getArcWeight(p.getPostsetEdges(), event));
						forward.add(getArcWeight(p.getPresetEdges(), event));
					}
					BigInteger marking = BigInteger.valueOf(p.getInitialToken().getValue());
					Region region = new Region.Builder(utility, backward,
							forward).withInitialMarking(marking);
					for (String event : labelsMissingFromPN)
						region = addWeightForEvent(region, event);
					try {
						builder.addRegion(region);
					} catch (InvalidRegionException e) {
						System.err.println(String.format("Ignoring place %s, because it is not"
									+ " valid for the target transition system",
									p.getId()));
					}
				}
			}
		};
		return runSynthesis(tsForOpts, configure, input, output);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
