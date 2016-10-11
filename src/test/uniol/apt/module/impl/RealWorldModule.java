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

package uniol.apt.module.impl;

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
 * @author Renke Grunwald
 *
 */
public class RealWorldModule extends AbstractModule {

	@Override
	public String getName() {
		return "real_world_module";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("input_net", PetriNet.class, "");
		inputSpec.addParameter("input_aut", TransitionSystem.class, "");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("output_net", PetriNet.class);
		outputSpec.addReturnValue("output_aut", TransitionSystem.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet net = input.getParameter("input_net", PetriNet.class);
		TransitionSystem aut = input.getParameter("input_aut", TransitionSystem.class);

		output.setReturnValue("output_net", PetriNet.class, net);
		output.setReturnValue("output_aut", TransitionSystem.class, aut);
	}

	@Override
	public String getTitle() {
		return "Real world module";
	}

	@Override
	public String getShortDescription() {
		return "A module that uses parameters and return values just like in the real world."
				+ " It doesn't do anything useful though.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.MISC};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
