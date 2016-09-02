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

package uniol.apt.pnanalysis;

import uniol.apt.adt.pn.PetriNet;
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
 * Provide Pn Analysis as a Module
 *
 * @author Raffaela Ferrari
 *
 */
@AptModule
public class PnAnalysisModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "pn_analysis";
	}

	@Override
	public String getShortDescription() {
		return "Output a T-system of size g, which has a reachability graph, which is isomorph "
			+ "to the reachability graph of the input Petri net";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addParameter("g", Integer.class, "maximum size of places of the checked T-systems");
		inputSpec.addOptionalParameterWithoutDefault("k", Integer.class,
				"maximum number of token of the checked T-systems");
		inputSpec.addOptionalParameterWithDefault("randomly", String.class, "", "",
				"Parameter, which say, that a randomly "
						+ "selected T-system is checked with g as maximum size of places.");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("T-system", PetriNet.class, ModuleOutputSpec.PROPERTY_FILE,
			ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		boolean randomly = false;

		// get k
		Integer k = input.getParameter("k", Integer.class);
		if (k != null && k <= 0) {
			throw new ModuleException("k must be greater than zero.");
		}

		// interpret verbose
		String stringRandomly = input.getParameter("randomly", String.class);
		if (stringRandomly.equals("randomly")) {
			randomly = true;
		}
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		Integer g = input.getParameter("g", Integer.class);
		PetriNet result = new PnAnalysis().checkAllIsomorphicTSystemsForPetriNet(pn, g, k, randomly);
		output.setReturnValue("T-system", PetriNet.class, result);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
