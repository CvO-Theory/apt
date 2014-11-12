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

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
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
	public String getName() {
		return "synthesize";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be synthesized to a Petri Net");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("warning", String.class);
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("failedStateSeparationProblems", String.class);
		outputSpec.addReturnValue("failedStateEventSeparationProblems", String.class);
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		output.setReturnValue("warning", String.class, "THIS MODULE IS EXPERIMENTAL AND SHOULD NOT BE TRUSTED");

		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		SynthesizePN synthesize = new SynthesizePN(ts);
		boolean success = synthesize.wasSuccessfullySeparated();

		PetriNet pn = synthesize.synthesizePetriNet();
		if (success)
			// TODO: If these stay in, the file output is screwed up. Can we fix the renderers?
			for (Place p : pn.getPlaces())
				p.removeExtension(Region.class.getName());

		output.setReturnValue("success", Boolean.class, synthesize.wasSuccessfullySeparated());
		output.setReturnValue("pn", PetriNet.class, pn);

		if (!success) {
			output.setReturnValue("failedStateSeparationProblems", String.class, synthesize.getFailedStateSeparationProblems().toString());
			output.setReturnValue("failedStateEventSeparationProblems", String.class, synthesize.getFailedEventStateSeparationProblems().toString());
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
