/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2016  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.separation.UnsupportedPNPropertiesException;
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
 * Provide the net overapproximation as a module.
 * @author Uli Schlachter
 */
@AptModule
public class SynthesizeOverapproximateModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Synthesize the minimal Petri Net overapproximation from a transition system";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ AbstractSynthesizeModule.getOptionsDescription("", "") + "\n\nExample calls:\n\n"
			+ " apt " + getName() + " none lts.apt\n"
			+ " apt " + getName() + " 3-bounded lts.apt\n"
			+ " apt " + getName() + " pure,safe lts.apt\n";
	}

	@Override
	public String getName() {
		return "overapproximate_synthesize";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("options", String.class, "Comma separated list of options");
		inputSpec.addParameter("lts", TransitionSystem.class,
				"The LTS that should be synthesized to a Petri Net");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("pn", PetriNet.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		String optionsStr = input.getParameter("options", String.class);
		AbstractSynthesizeModule.Options options = AbstractSynthesizeModule.Options.parseProperties(optionsStr);
		try {
			PetriNet pn = OverapproximatePN.overapproximate(ts, options.properties);
			output.setReturnValue("pn", PetriNet.class, pn);
		} catch (UnsupportedPNPropertiesException e) {
			throw new ModuleException(e);
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
