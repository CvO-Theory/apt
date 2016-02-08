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

import uniol.apt.adt.ts.ParikhVector;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.CyclesPVs;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;

/**
 * This module computes Parikh vectors of smallest cycles of a lts.
 * @author Manuel Gieseking
 */
@AptModule
public class PVsOfSmallestCyclesModule extends AbstractModule implements Module {

	private final static String DESCRIPTION = "Compute parikh vectors of smallest cycles of a Petri net or"
		+ " LTS";
	private final static String NAME = "compute_pvs";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("graph", PetriNetOrTransitionSystem.class,
			"The Petri net or LTS that should be examined");
		inputSpec.addOptionalParameterWithDefault("algo", Character.class,
				ComputeSmallestCyclesAlgorithms.getDefaultAlgorithmChar(),
				String.valueOf(ComputeSmallestCyclesAlgorithms.getDefaultAlgorithmChar()),
				ComputeSmallestCyclesAlgorithms.getAlgorithmCharDescription());
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("output_format", String.class);
		outputSpec.addReturnValue("output", Set.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem g = input.getParameter("graph", PetriNetOrTransitionSystem.class);
		Character algo = input.getParameter("algo", Character.class);
		ComputeSmallestCycles prog = ComputeSmallestCyclesAlgorithms.getAlgorithm(algo);
		TransitionSystem ts = g.getTs();
		PetriNet pn = g.getNet();
		Set<Pair<List<String>, ParikhVector>> parikhs = null;
		if (ts != null) {
			parikhs = prog.computePVsOfSmallestCycles(ts);
		} else if (pn != null) {
			CyclesPVs pnProg = new CyclesPVs(prog);
			parikhs = pnProg.calcCycles(pn);
		}
		output.setReturnValue("output", Set.class, parikhs);
		output.setReturnValue("output_format", String.class, "[(cycle, parikh vector), ... ]");
	}

	@Override
	public String getShortDescription() {
		return DESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
