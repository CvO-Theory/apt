/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C)      2017  Uli Schlachter
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

package uniol.apt.analysis.fc;

import uniol.apt.adt.pn.PetriNet;
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
 * Checks whether a given Petri net is a weighted free choice net. That is:
 * \forall t1,t2 \in T: ^{\bullet}t1 \Cap ^{\bullet}t2 \neq \emptyset \Rightarrow ^{\bullet}t1 \eq ^{\bullet}t2.
 * @author Dennis-Michael Borde, Uli Schlachter
 */
@AptModule
public class WFCModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "wfc";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("net", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("wfc", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("net", PetriNet.class);
		WeightedFreeChoice fc = new WeightedFreeChoice();
		output.setReturnValue("wfc", Boolean.class, fc.check(pn));
	}

	@Override
	public String getTitle() {
		return "Weighted Free Choice";
	}

	@Override
	public String getShortDescription() {
		return "Check if a Petri net is weighted free-choice";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". That is: \\forall t1,t2 \\in T: ^{\\bullet}t1 \\Cap "
			+ "^{\\bullet}t2 \\neq \\emptyset \\Rightarrow ^{\\bullet}t1 \\eq "
			+ "^{\\bullet}t2.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
