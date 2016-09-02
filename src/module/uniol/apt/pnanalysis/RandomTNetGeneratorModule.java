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
 * Module for generating a random t-net or t-system.
 * @author Manuel Gieseking
 */
@AptModule
public class RandomTNetGeneratorModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "random_t_net_generator";
	}

	@Override
	public String getShortDescription() {
		return "Construct a T-net or T-system (if k given) of size g.";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("g", Integer.class, "maximum count of places of the returned t-net.");
		inputSpec.addOptionalParameterWithoutDefault("k", Integer.class,
				"maximum number of token of a place in the t-net.");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("t-net", PetriNet.class, ModuleOutputSpec.PROPERTY_FILE,
			ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Integer k = input.getParameter("k", Integer.class);
		if (k != null && k <= 0) {
			throw new ModuleException("k must be greater than zero.");
		}
		Integer g = input.getParameter("g", Integer.class);
		PetriNet result = (k == null) ? RandomTNetGenerator.createRandomTNet(g)
			: RandomTNetGenerator.createRandomTSystem(g, k);
		output.setReturnValue("t-net", PetriNet.class, result);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GENERATOR};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
