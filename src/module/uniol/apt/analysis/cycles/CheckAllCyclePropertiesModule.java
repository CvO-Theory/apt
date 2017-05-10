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

package uniol.apt.analysis.cycles;

import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.cycles.lts.CyclePV;
import uniol.apt.analysis.cycles.lts.CycleCounterExample;
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
 * Checks if all smallest cycles have same Parikh vectors, if all smallest
 * cycles have same or mutually disjoint Parikh vectors and it computes Parikh
 * vectors of smallest cycles
 *
 * @author Sören Dierkes
 *
 */
@AptModule
public class CheckAllCyclePropertiesModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "check_all_cycle_prop";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("same_parikh_vectors", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("counter_example_same", CycleCounterExample.class);

		outputSpec.addReturnValue("same_or_mutually_disjoint_pv", Boolean.class,
				ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("counter_example_disjoint", CycleCounterExample.class);

		outputSpec.addReturnValue("parikh_vectors", Set.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		TransitionSystem ts = CoverabilityGraph.get(pn).toReachabilityLTS();

		// Compute Parikh vectors of smallest cycles
		ComputeSmallestCycles small = new ComputeSmallestCycles();
		Set<? extends CyclePV> parikhs = small.computePVsOfSmallestCycles(ts);
		output.setReturnValue("parikh_vectors", Set.class, parikhs);
		// all smallest cycles have same Parikh vectors
		boolean b = small.checkSamePVs(parikhs);
		if (!b) {
			output.setReturnValue("counter_example_same", CycleCounterExample.class,
					small.getCounterExample());
		}
		output.setReturnValue("same_parikh_vectors", Boolean.class, b);

		// all smallest cycles have same or mutually disjoint Parikh vectors
		boolean bo = small.checkSameOrMutallyDisjointPVs(parikhs);
		if (!bo) {
			output.setReturnValue("counter_example_disjoint", CycleCounterExample.class,
					small.getCounterExample());
		}
		output.setReturnValue("same_or_mutually_disjoint_pv", Boolean.class, bo);


	}

	@Override
	public String getShortDescription() {
		return "Check all cycle properties of a Petri net";
	}

	@Override
	public String getLongDescription() {
		return "Check if all smallest cycles have same Parikh vectors, if all smallest cycles have"
				+ " same or mutually disjoint Parikh vectors and it computes Parikh vectors"
				+ " of smallest cycles";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.PN };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
