/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

package uniol.apt.synthesis;

import java.util.ArrayList;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.synthesis.Synthesis.Region;

/**
 * This module provides a re-implementation of the synet algorithm.
 * <p/>
 * @author Thomas Strathmann
 */
public class SynthesisModule extends AbstractModule {

	private final static String SHORTDESCRIPTION = "Synthesize a Petri net from an LTS";
	private final static String LONGDESCRIPTION = SHORTDESCRIPTION;
	private final static String TITLE = "SynthesizeNet";
	private final static String NAME = "synthesize_net";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS from which a Petri net should be synthesized.");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("net", PetriNet.class,
				ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);
		
		// handle the special case of the empty LTS
		if(lts.getEdges().isEmpty()) {
			PetriNet net = new PetriNet();
			output.setReturnValue("net", PetriNet.class, net);
			return;
		}
				
		Synthesis synth = new Synthesis(lts);
		
		synth.checkStateSeparation();
		
		synth.checkStateEventSeparation();		
		
		ArrayList<int[]> gens = synth.computeAdmissibleRegions();
		
		PetriNet net = new PetriNet();
		
		for(String e : lts.getAlphabet()) {
			Transition t = net.createTransition(e);
			t.setLabel(e);
		}
		
		ArrayList<Region> admissibleRegions = synth.computeRegions(gens);
		
		for(int i=0; i<admissibleRegions.size(); ++i) {
			Region r = admissibleRegions.get(i);
			final String id = "x_" + i; 
			Place p = net.createPlace(id);
			
			p.setInitialToken(r.sigma.get(lts.getInitialState()));
			
			for(String e : lts.getAlphabet()) {
				net.createFlow(id, e, r.pre.get(e));
				net.createFlow(e, id, r.post.get(e));
			}
			
			System.out.println(r.toString());
		}		
		
		output.setReturnValue("net", PetriNet.class, net);
	}
	

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return SHORTDESCRIPTION;
	}

	@Override
	public String getLongDescription() {
		return LONGDESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
