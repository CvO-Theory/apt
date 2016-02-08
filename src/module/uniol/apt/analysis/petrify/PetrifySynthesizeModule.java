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

package uniol.apt.analysis.petrify;

import java.io.IOException;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
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
 * Generates with Petrify a petri net for a given transitionsystem.
 *
 * @author Sören Dierkes
 *
 */
@AptModule
public class PetrifySynthesizeModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "use_petrify";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
		inputSpec.addOptionalParameterWithoutDefault("dead", String.class, "If the given LTS is dead");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("synthesizable", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("error", String.class);
		outputSpec.addReturnValue("pn", PetriNet.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);

		PetrifyLTSSynthesize checkLTS = new PetrifyLTSSynthesize(ts);
		checkLTS.setSndParameter(input.getParameter("dead", String.class));

		boolean b;
		try {
			b = checkLTS.check();
			output.setReturnValue("synthesizable", Boolean.class, b);
			if (b) {
				output.setReturnValue("error", String.class, null);
				output.setReturnValue("pn", PetriNet.class, checkLTS.getPN());
			} else {
				output.setReturnValue("error", String.class, checkLTS.getError());
			}
		} catch (IOException ex) {
			throw new ModuleException(ex);
		}
	}

	@Override
	public String getShortDescription() {
		return "Check if Petrify can generate a Petri net from a LTS";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "\n\n" + "For this module to function"
			+ " properly you must ensure that the Petrify executable can be"
			+ " found on your system. On most systems adding the directory"
			+ " where the executable is located to the PATH environment"
			+ " variable suffices to make it available to the APT system.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
