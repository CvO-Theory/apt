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

package uniol.apt.analysis.homogeneous;

import uniol.apt.adt.pn.Flow;
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
import uniol.apt.util.Pair;

/**
 * Checks whether a given plain Petri net is an homogeneous net.
 * @author vsp
 */
@AptModule
public class HomogeneousModule extends AbstractInterruptibleModule implements InterruptibleModule {
	@Override
	public String getName() {
		return "homogeneous";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("net", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("homogeneous", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness1", Flow.class);
		outputSpec.addReturnValue("witness2", Flow.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("net", PetriNet.class);
		Homogeneous homogeneous = new Homogeneous();
		Pair<Flow, Flow> counterexample = homogeneous.check(pn);
		output.setReturnValue("homogeneous", Boolean.class, counterexample == null);
		if (counterexample != null) {
			output.setReturnValue("witness1", Flow.class, counterexample.getFirst());
			output.setReturnValue("witness2", Flow.class, counterexample.getSecond());
		}
	}

	@Override
	public String getTitle() {
		return "Homogeneous";
	}

	@Override
	public String getShortDescription() {
		return "Check if a Petri net is homogeneous";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\nA Petri net is an homogeneous net if " +
			"∀p∈P:∀t₁,t₂∈p°: F(p,t₁)=F(p,t₂)";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
