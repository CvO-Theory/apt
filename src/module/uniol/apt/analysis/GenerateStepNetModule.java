/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis;

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
 * Generate a step net out of a Petri Net
 * @author Uli Schlachter
 */
@AptModule
public class GenerateStepNetModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Calculate the concurrent coverability graph of a Petri net in the step semantics";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "."
			+ " In the step semantics, instead of individual transitions, sets of transitions called"
			+ " 'a step' are fired. When a step fires, first all of its transition consume token and only"
			+ " afterwards produce tokens."
			+ " Put differently, on each place at least as many token have to be present as the sum of the"
			+ " arc weights of the transitions in a step require.";
	}

	@Override
	public String getName() {
		return "concurrent_coverability_graph";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("dot", String.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		String dot = new GenerateStepNet(pn).renderCoverabilityGraphAsDot();
		output.setReturnValue("dot", String.class, dot);
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.PN };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
