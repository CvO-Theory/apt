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

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module provides a re-implementation of the synet algorithm.
 * <p/>
 * @author Thomas Strathmann
 */
public class SynthesisModule extends AbstractModule {

	private final static String SHORTDESCRIPTION = "Synthesize a Petri net from an LTS";
	private final static String LONGDESCRIPTION = 
			"Given a (1) totally reachable and (2) event-reduced LTS, this module" + System.lineSeparator() +
			"tries to synthesize a Petri net.  Note that if conditions (1) or (2)" +  System.lineSeparator() +
			"are violated, this implementation will throw an exception.";
	private final static String TITLE = "SynthesizeNet";
	private final static String NAME = "synthesize_net";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS from which a Petri net should be synthesized.");
		inputSpec.addOptionalParameter("verbose", String.class, "", "Enable verbose debugging output");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("net", PetriNet.class,
				ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
		outputSpec.addReturnValue("synthesizable", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);		
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);		
		boolean loggingEnabled = input.getParameter("verbose", String.class).equals("verbose");

		Synthesis synth = new Synthesis(lts, loggingEnabled);

		boolean synthesizable = synth.isSeparated();
		PetriNet net = synth.getPetriNet();
		
		output.setReturnValue("synthesizable", Boolean.class, synthesizable);
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
