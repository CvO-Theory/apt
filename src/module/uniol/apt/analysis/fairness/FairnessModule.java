/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017       vsp
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

package uniol.apt.analysis.fairness;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.presynthesis.pps.Path;
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
 * Provide the fairness test as a module.
 * @author vsp
 */
@AptModule
public class FairnessModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Check if a Petri net or transition system is fair";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription()
			+ ". A transition system is fair if for every infinite firing sequence every infintely often "
			+ "k-activated event is fired infinitely often.";
	}

	@Override
	public String getName() {
		return "fairness";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pnTs", PetriNetOrTransitionSystem.class,
				"The Petri net or transition system that should be examined");
		inputSpec.addOptionalParameterWithDefault("k", Integer.class, 0, "0",
				"If given, break if a k-unfair situation is found");
		inputSpec.addOptionalParameterWithoutDefault("t", String.class,
				"If given, fairness regarding this event is checked");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("fair", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_state", State.class);
		outputSpec.addReturnValue("witness_firing_sequence_start", Path.class);
		outputSpec.addReturnValue("witness_firing_sequence_cycle", Path.class);
		outputSpec.addReturnValue("witness_firing_sequence_enable", Path.class);
		outputSpec.addReturnValue("smallest_K", Integer.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("pnTs", PetriNetOrTransitionSystem.class).getReachabilityLTS();
		int k = input.getParameter("k", Integer.class);
		String t = input.getParameter("t", String.class);
		FairnessResult result;
		if (t == null) {
			result = Fairness.checkFairness(ts, k);
		} else {
			result = Fairness.checkFairness(ts, k, ts.getEvent(t));
		}
		boolean fair = result.isFair();
		output.setReturnValue("fair", Boolean.class, fair);

		if (!fair) {
			output.setReturnValue("witness_state", State.class, result.unfairState);
			output.setReturnValue("witness_firing_sequence_start", Path.class, new Path(result.sequence));
			output.setReturnValue("witness_firing_sequence_cycle", Path.class, new Path(result.cycle));
			output.setReturnValue("witness_firing_sequence_enable", Path.class, new Path(result.enabling));
			output.setReturnValue("smallest_K", Integer.class, result.k);
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
