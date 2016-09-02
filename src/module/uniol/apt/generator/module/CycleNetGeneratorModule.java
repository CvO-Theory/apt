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

package uniol.apt.generator.module;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.cycle.CycleGenerator;
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
 * Module for generating cycle nets.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class CycleNetGeneratorModule extends AbstractModule implements Module {

	@Override
	public String getShortDescription() {
		return "Construct a Petri net for a cycle of a given size with a given number of initial token.";
	}

	@Override
	public String getName() {
		return "cycle_generator";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("n", Integer.class, "The argument for the Petri net generator");
		inputSpec.addOptionalParameterWithDefault("init", Integer.class, 1, "1",
				"The number of token in the initial marking");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Integer n = input.getParameter("n", Integer.class);
		Integer init = input.getParameter("init", Integer.class);
		PetriNet pn = new CycleGenerator().generateNet(n, init);
		output.setReturnValue("pn", PetriNet.class, pn);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GENERATOR};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
