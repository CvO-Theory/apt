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

import java.util.HashSet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.ac.AsymmetricChoice;
import uniol.apt.analysis.bcf.BCF;
import uniol.apt.analysis.bicf.BiCF;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.analysis.cf.ConflictFree;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.analysis.fc.FreeChoice;
import uniol.apt.analysis.fc.WeightedFreeChoice;
import uniol.apt.analysis.fcnet.FCNet;
import uniol.apt.analysis.homogeneous.Homogeneous;
import uniol.apt.analysis.live.Live;
import uniol.apt.analysis.mf.MergeFree;
import uniol.apt.analysis.on.OutputNonBranching;
import uniol.apt.analysis.persistent.PersistentNet;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.reversible.ReversibleNet;
import uniol.apt.analysis.separation.LargestK;
import uniol.apt.analysis.sideconditions.NonPure;
import uniol.apt.analysis.sideconditions.Pure;
import uniol.apt.analysis.snet.SNet;
import uniol.apt.analysis.tnet.TNet;
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
 * Provide various checks in a single module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class ExaminePNModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Perform various tests on a Petri net at once";
	}

	@Override
	public String getName() {
		return "examine_pn";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("num_places", Integer.class);
		outputSpec.addReturnValue("num_transitions", Integer.class);
		outputSpec.addReturnValue("num_labels", Integer.class);
		outputSpec.addReturnValue("num_arcs", Integer.class);
		outputSpec.addReturnValue("num_tokens", Integer.class);
		outputSpec.addReturnValue("plain", Boolean.class);
		outputSpec.addReturnValue("pure", Boolean.class);
		outputSpec.addReturnValue("nonpure_only_simple_side_conditions", Boolean.class);
		outputSpec.addReturnValue("free_choice", Boolean.class);
		outputSpec.addReturnValue("weighted_free_choice", Boolean.class);
		outputSpec.addReturnValue("restricted_free_choice", Boolean.class);
		outputSpec.addReturnValue("t_net", Boolean.class);
		outputSpec.addReturnValue("s_net", Boolean.class);
		outputSpec.addReturnValue("output_nonbranching", Boolean.class);
		outputSpec.addReturnValue("merge_free", Boolean.class);
		outputSpec.addReturnValue("conflict_free", Boolean.class);
		outputSpec.addReturnValue("k-marking", Long.class);
		outputSpec.addReturnValue("safe", Boolean.class);
		outputSpec.addReturnValue("bounded", Boolean.class);
		outputSpec.addReturnValue("k-bounded", Long.class);
		outputSpec.addReturnValue("isolated_elements", Boolean.class);
		outputSpec.addReturnValue("strongly_connected", Boolean.class);
		outputSpec.addReturnValue("weakly_connected", Boolean.class);
		outputSpec.addReturnValue("bcf", Boolean.class);
		outputSpec.addReturnValue("bicf", Boolean.class);
		outputSpec.addReturnValue("strongly_live", Boolean.class);
		outputSpec.addReturnValue("weakly_live", Boolean.class);
		outputSpec.addReturnValue("simply_live", Boolean.class);
		outputSpec.addReturnValue("persistent", Boolean.class);
		outputSpec.addReturnValue("backwards_persistent", Boolean.class);
		outputSpec.addReturnValue("reversible", Boolean.class);
		outputSpec.addReturnValue("homogeneous", Boolean.class);
		outputSpec.addReturnValue("asymmetric_choice", Boolean.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		output.setReturnValue("num_places", Integer.class, pn.getPlaces().size());
		output.setReturnValue("num_transitions", Integer.class, pn.getTransitions().size());
		output.setReturnValue("num_arcs", Integer.class, pn.getEdges().size());

		int tokens = 0;
		for (Place p : pn.getPlaces()) {
			tokens += p.getInitialToken().getValue();
		}
		output.setReturnValue("num_tokens", Integer.class, tokens);

		HashSet<String> labels = new HashSet<>();
		for (Transition t : pn.getTransitions()) {
			labels.add(t.getLabel());
		}
		output.setReturnValue("num_labels", Integer.class, labels.size());
		BoundedResult result = Bounded.checkBounded(pn);
		boolean plain = new Plain().checkPlain(pn);
		output.setReturnValue("plain", Boolean.class, plain);
		output.setReturnValue("pure", Boolean.class, Pure.checkPure(pn));
		output.setReturnValue("nonpure_only_simple_side_conditions", Boolean.class, NonPure.checkNonPure(pn));
		output.setReturnValue("weighted_free_choice", Boolean.class, new WeightedFreeChoice().check(pn));
		if (plain) {
			output.setReturnValue("free_choice", Boolean.class, new FreeChoice().check(pn));
			output.setReturnValue("restricted_free_choice", Boolean.class, new FCNet(pn).check());
			output.setReturnValue("t_net", Boolean.class, new TNet(pn).testPlainTNet());
			output.setReturnValue("s_net", Boolean.class, new SNet(pn).testPlainSNet());
			output.setReturnValue("conflict_free", Boolean.class, new ConflictFree(pn).check());
		}
		output.setReturnValue("k-marking", Long.class, new LargestK(pn).computeLargestK());
		output.setReturnValue("output_nonbranching", Boolean.class, new OutputNonBranching(pn).check());
		output.setReturnValue("merge_free", Boolean.class, new MergeFree().check(pn));
		output.setReturnValue("safe", Boolean.class, result.isSafe());
		output.setReturnValue("bounded", Boolean.class, result.isBounded());
		if (result.isBounded()) {
			output.setReturnValue("k-bounded", Long.class, result.k);
		}
		output.setReturnValue("isolated_elements", Boolean.class,
				!Connectivity.findIsolatedElements(pn).isEmpty());
		output.setReturnValue("strongly_connected", Boolean.class, Connectivity.isStronglyConnected(pn));
		output.setReturnValue("weakly_connected", Boolean.class, Connectivity.isWeaklyConnected(pn));
		if (result.isBounded()) {
			PersistentNet persistent = new PersistentNet(pn);
			PersistentNet backwardsPersistent = new PersistentNet(pn, true);
			ReversibleNet reversible = new ReversibleNet(pn);
			persistent.check();
			backwardsPersistent.check();
			reversible.check();
			output.setReturnValue("bcf", Boolean.class, new BCF().check(pn) == null);
			output.setReturnValue("bicf", Boolean.class, new BiCF().check(pn) == null);
			output.setReturnValue("strongly_live", Boolean.class,
				Live.findNonStronglyLiveTransition(pn) == null);
			output.setReturnValue("weakly_live", Boolean.class,
				Live.findNonWeaklyLiveTransition(pn) == null);
			output.setReturnValue("persistent", Boolean.class, persistent.isPersistent());
			output.setReturnValue("backwards_persistent", Boolean.class,
					backwardsPersistent.isPersistent());
			output.setReturnValue("reversible", Boolean.class, reversible.isReversible());
		}
		output.setReturnValue("simply_live", Boolean.class, Live.findDeadTransition(pn) == null);
		output.setReturnValue("homogeneous", Boolean.class, new Homogeneous().check(pn) == null);
		output.setReturnValue("asymmetric_choice", Boolean.class, new AsymmetricChoice().check(pn) == null);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
