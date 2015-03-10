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

package uniol.apt.analysis.live;

import java.util.List;

import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

import uniol.apt.analysis.exception.NoSuchTransitionException;
import uniol.apt.analysis.language.FiringSequence;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;

/**
 * Provide the simply live test as a module.
 * @author Uli Schlachter, vsp
 */
public class SimplyLiveModule extends AbstractModule {

	@Override
	public String getShortDescription() {
		return "Check if a Petri net or a transition (if given) is simply live";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription()
			+ ". A transition is simply live when it can fire at least once. A Petri net is simply live "
			+ "when all of its transitions are simply live. For a simply live transition, this module "
			+ "finds a firing sequence that fires the transition.";
	}

	@Override
	public String getName() {
		return "simply_live";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addOptionalParameter("transition", String.class, null,
			"A transition that should be checked for liveness");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("simply_live", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("sample_dead_transition", Transition.class);
		outputSpec.addReturnValue("sample_witness_firing_sequence", FiringSequence.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		String id = input.getParameter("transition", String.class);
		if (id == null) {
			Transition dead = Live.findDeadTransition(pn);
			output.setReturnValue("simply_live", Boolean.class, dead == null);
			output.setReturnValue("sample_dead_transition", Transition.class, dead);
		} else {
			Transition transition = pn.getTransition(id);
			if (transition == null) {
				throw new NoSuchTransitionException(pn, id);
			}

			List<Transition> live = Live.checkSimplyLive(pn, transition);
			output.setReturnValue("simply_live", Boolean.class, live != null);
			if (live != null)
				output.setReturnValue("sample_witness_firing_sequence",
						FiringSequence.class, new FiringSequence(live));
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
