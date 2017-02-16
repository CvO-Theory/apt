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

package uniol.apt.analysis.bisimulation;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the bisimulation as a module.
 * @author Raffaela Ferrari
 */
@AptModule
public class BisimulationModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "bisimulation";
	}

	@Override
	public String getShortDescription() {
		return "Check if the reachability graphs of two bounded labeled Petri nets" +
				" or of two LTS or a combination of both are bisimilar";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pnOrLts1", PetriNetOrTransitionSystem.class, "The first Petri net that"
			+ " should be examined");
		inputSpec.addParameter("pnOrLts2", PetriNetOrTransitionSystem.class, "The second Petri net that"
			+ " should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("bisimilar_graphs", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("non_bisimilar_path", NonBisimilarPath.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem pnOrLts1 = input.getParameter("pnOrLts1", PetriNetOrTransitionSystem.class);
		PetriNetOrTransitionSystem pnOrLts2 = input.getParameter("pnOrLts2", PetriNetOrTransitionSystem.class);
		Bisimulation bisimulation = new Bisimulation();
		Boolean bisimilar = bisimulation.checkBisimulation(pnOrLts1.getReachabilityLTS(), pnOrLts2.getReachabilityLTS());
		output.setReturnValue("bisimilar_graphs", Boolean.class, bisimilar);
		output.setReturnValue("non_bisimilar_path", NonBisimilarPath.class, bisimulation.getErrorPath());
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
