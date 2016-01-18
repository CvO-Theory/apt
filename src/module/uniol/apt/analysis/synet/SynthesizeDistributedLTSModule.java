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

package uniol.apt.analysis.synet;

import java.io.IOException;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
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
 * Creates with Synet to a given labeled transition system a petrinet with locations.
 *
 * @author Sören Dierkes
 *
 */
@AptModule
public class SynthesizeDistributedLTSModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "use_synet";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("synthesize_distributed_lts", Boolean.class,
				ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("error", String.class);
		outputSpec.addReturnValue("separationError", String.class);
		outputSpec.addReturnValue("pn", PetriNet.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);

		SynetSynthesizeDistributedLTS checkLTS = new SynetSynthesizeDistributedLTS(ts);

		boolean b;
		try {
			b = checkLTS.check();

			if (checkLTS.getSeparationError() == null) {
				output.setReturnValue("synthesize_distributed_lts", Boolean.class, b);
				output.setReturnValue("separationError", String.class, null);
			} else {
				output.setReturnValue("separationError", String.class, checkLTS.getSeparationError());
			}

			if (b) {
				output.setReturnValue("error", String.class, null);
			} else {
				output.setReturnValue("error", String.class, checkLTS.getError());
			}
		} catch (IOException | ParseException | RenderException ex) {
			throw new ModuleException(ex);
		}

		output.setReturnValue("pn", PetriNet.class, checkLTS.getPN());
	}

	@Override
	public String getShortDescription() {
		return "Check if Synet can generate a Petri net from a LTS";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "\n\n" + "For this module to function"
			+ " properly you must ensure that the Synet executable can be"
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
