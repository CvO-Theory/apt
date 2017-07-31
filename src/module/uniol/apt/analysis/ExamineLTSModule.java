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

package uniol.apt.analysis;

import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.cycles.lts.CyclePV;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.reversible.ReversibleTS;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.Pair;

/**
 * Provide various checks in a single module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class ExamineLTSModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Perform various tests on a transition system at once";
	}

	@Override
	public String getName() {
		return "examine_lts";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class,
			"The LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("num_states", Integer.class);
		outputSpec.addReturnValue("num_arcs", Integer.class);
		outputSpec.addReturnValue("num_labels", Integer.class);
		outputSpec.addReturnValue("deterministic", Boolean.class);
		outputSpec.addReturnValue("backwards_deterministic", Boolean.class);
		outputSpec.addReturnValue("persistent", Boolean.class);
		outputSpec.addReturnValue("backwards_persistent", Boolean.class);
		outputSpec.addReturnValue("totally_reachable", Boolean.class);
		outputSpec.addReturnValue("reversible", Boolean.class);
		outputSpec.addReturnValue("isolated_elements", Boolean.class);
		outputSpec.addReturnValue("strongly_connected", Boolean.class);
		outputSpec.addReturnValue("weakly_connected", Boolean.class);
		outputSpec.addReturnValue("same_parikh_vectors", Boolean.class);
		outputSpec.addReturnValue("same_or_mutually_disjoint_pv", Boolean.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);
		Deterministic det = new Deterministic(lts);
		Deterministic bDet = new Deterministic(lts, false);
		PersistentTS per = new PersistentTS(lts);
		PersistentTS backwardsPer = new PersistentTS(lts, true);
		TotallyReachable tot = new TotallyReachable(lts);
		ReversibleTS rev = new ReversibleTS(lts);
		output.setReturnValue("num_states", Integer.class, lts.getNodes().size());
		output.setReturnValue("num_arcs", Integer.class, lts.getEdges().size());
		output.setReturnValue("num_labels", Integer.class, lts.getAlphabet().size());
		output.setReturnValue("deterministic", Boolean.class, det.isDeterministic());
		output.setReturnValue("backwards_deterministic", Boolean.class, bDet.isDeterministic());
		output.setReturnValue("persistent", Boolean.class, per.isPersistent());
		output.setReturnValue("backwards_persistent", Boolean.class, backwardsPer.isPersistent());
		output.setReturnValue("totally_reachable", Boolean.class, tot.isTotallyReachable());
		output.setReturnValue("reversible", Boolean.class, rev.isReversible());
		output.setReturnValue("isolated_elements", Boolean.class,
			!Connectivity.findIsolatedElements(lts).isEmpty());
		output.setReturnValue("strongly_connected", Boolean.class, Connectivity.isStronglyConnected(lts));
		output.setReturnValue("weakly_connected", Boolean.class, Connectivity.isWeaklyConnected(lts));
		ComputeSmallestCycles csc = new ComputeSmallestCycles();
		Set<? extends CyclePV> vecs = csc.computePVsOfSmallestCycles(lts);
		output.setReturnValue("same_parikh_vectors", Boolean.class, csc.checkSamePVs(vecs));
		output.setReturnValue("same_or_mutually_disjoint_pv", Boolean.class,
			csc.checkSameOrMutallyDisjointPVs(vecs));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
