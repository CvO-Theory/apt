/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

package uniol.apt.analysis.homogenous;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;

/**
 * Checks whether a given plain Petri net is an homogenous net.
 * @author vsp
 */
@AptModule
public class HomogenousModule extends AbstractModule implements Module {
	@Override
	public String getName() {
		return "homogenous";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("net", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("homogenous", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness1", Flow.class);
		outputSpec.addReturnValue("witness2", Flow.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("net", PetriNet.class);
		Homogenous homogenous = new Homogenous();
		Pair<Flow, Flow> counterexample = homogenous.check(pn);
		output.setReturnValue("homogenous", Boolean.class, counterexample == null);
		if (counterexample != null) {
			output.setReturnValue("witness1", Flow.class, counterexample.getFirst());
			output.setReturnValue("witness2", Flow.class, counterexample.getSecond());
		}
	}

	@Override
	public String getTitle() {
		return "Asymmetric Choice";
	}

	@Override
	public String getShortDescription() {
		return "Check if a Petri net is homogenous";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\nA Petri net is an homogenous net if " +
			"∀p∈P:∀t₁,t₂∈p°: F(p,t₁)=F(p,t₂)";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
