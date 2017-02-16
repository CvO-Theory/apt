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

package uniol.apt.analysis.isomorphism;

import uniol.apt.adt.PetriNetOrTransitionSystem;
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
 * Provide the isomorphism check for petri net's reachability graph as a module.
 * @author Maike Schwammberger, Uli Schlachter
 */
@AptModule
public class IsomorphismModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Check if two Petri nets have isomorphic reachability graphs";
	}

	@Override
	public String getName() {
		return "isomorphism";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn_or_ts1", PetriNetOrTransitionSystem.class,
				"The first Petri net or transition system that should be examined");
		inputSpec.addParameter("pn_or_ts2", PetriNetOrTransitionSystem.class,
				"The second Petri net or transition system that should be examined");
		inputSpec.addOptionalParameterWithoutDefault("dontCheckLabels", String.class,
				"do not check arc labels (default is to check labels)");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("isomorphic_reachability_graphs", Boolean.class,
				ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("isomorphism", Isomorphism.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem arg1 = input.getParameter("pn_or_ts1", PetriNetOrTransitionSystem.class);
		PetriNetOrTransitionSystem arg2 = input.getParameter("pn_or_ts2", PetriNetOrTransitionSystem.class);
		TransitionSystem lts1 = arg1.getReachabilityLTS();
		TransitionSystem lts2 = arg2.getReachabilityLTS();

		boolean checkLabels = true;
		if (input.getParameter("dontCheckLabels", String.class) != null)
			checkLabels = false;

		IsomorphismLogic logic = new IsomorphismLogic(lts1, lts2, checkLabels);
		boolean result = logic.isIsomorphic();
		output.setReturnValue("isomorphic_reachability_graphs", Boolean.class, result);
		if (result)
			output.setReturnValue("isomorphism", Isomorphism.class,
					new Isomorphism(logic.getIsomorphism()));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
