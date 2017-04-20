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

package uniol.apt.analysis.cycles.lts;

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
 * This module checks whether all smallest cycles of a lts have same or mutually disjoint Parikh vectors
 * @author Manuel Gieseking
 */
@AptModule
public class CyclesHaveSameOrMutallyDisjointPVModule extends AbstractInterruptibleModule
		implements InterruptibleModule {

	private final static String SHORTDESCRIPTION = "Check if the smallest cycles of a Petri net or LTS"
		+ " have the same or mutually disjoint parikh vectors";
	private final static String NAME = "cycles_same_disjoint_pv";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("graph", PetriNetOrTransitionSystem.class,
			"The Petri net or LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("out", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("counterExamples", CycleCounterExample.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem g = input.getParameter("graph", PetriNetOrTransitionSystem.class);
		ComputeSmallestCycles prog = new ComputeSmallestCycles();
		TransitionSystem ts = g.getReachabilityLTS();
		boolean ret = prog.checkSameOrMutallyDisjointPVs(ts);
		CycleCounterExample ex = prog.getCounterExample();
		if (!ret) {
			output.setReturnValue("counterExamples", CycleCounterExample.class, ex);
		}
		output.setReturnValue("out", Boolean.class, ret);
	}

	@Override
	public String getShortDescription() {
		return SHORTDESCRIPTION;
	}

	@Override
	public String getLongDescription() {
		return SHORTDESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
