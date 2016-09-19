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

package uniol.apt.analysis.reversible;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
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
 * @author Renke Grunwald
 *
 */
@AptModule
public class ReversibleModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "reversible";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn_or_ts", PetriNetOrTransitionSystem.class,
			"The Petri net or LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("reversible", Boolean.class, "reversible", ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("marking", Marking.class); // For Petri nets
		outputSpec.addReturnValue("state", State.class); // For LTSs
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem pnOrTs = input.getParameter("pn_or_ts", PetriNetOrTransitionSystem.class);

		PetriNet pn = pnOrTs.getNet();
		TransitionSystem ts = pnOrTs.getTs();

		output.setReturnValue("marking", Marking.class, null);
		output.setReturnValue("state", State.class, null);

		if (pn != null) {
			ReversibleNet reversible = new ReversibleNet(pn);
			reversible.check();

			output.setReturnValue("reversible", Boolean.class, reversible.isReversible());
			output.setReturnValue("marking", Marking.class, reversible.getMarking());
		} else {
			ReversibleTS reversible = new ReversibleTS(ts);
			reversible.check();

			output.setReturnValue("reversible", Boolean.class, reversible.isReversible());
			output.setReturnValue("state", State.class, reversible.getNode());
		}
	}

	@Override
	public String getTitle() {
		return "Reversible";
	}

	@Override
	public String getShortDescription() {
		return "Check if a Petri net or LTS is reversible";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "."
			+ " A LTS is reversible if every reachable state can reach the initial state again."
			+ " A Petri net is reversible if its reachability graph is reversible.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
