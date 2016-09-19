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

package uniol.apt.analysis.bounded;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.language.FiringSequence;
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
 * Provide the k-boundedness test as a module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class KBoundedModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Find the smallest k for which a Petri net is k-bounded";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription()
			+ ". A Petri net is bounded if there is an upper limit for the number of token on each place. "
			+ "It is k-bounded if this limit isn't bigger than k.";
	}

	@Override
	public String getName() {
		return "k_bounded";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("k", Long.class);
		outputSpec.addReturnValue("bounded", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_place", Place.class);
		outputSpec.addReturnValue("witness_firing_sequence", FiringSequence.class);
		outputSpec.addReturnValue("witness_firing_sequence_cycle", FiringSequence.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		BoundedResult result = Bounded.checkBounded(pn);
		output.setReturnValue("k", Long.class, result.k);
		output.setReturnValue("bounded", Boolean.class, result.isBounded());
		output.setReturnValue("witness_place", Place.class, result.unboundedPlace);
		output.setReturnValue("witness_firing_sequence", FiringSequence.class,
			new FiringSequence(result.sequence));
		if (!result.cycle.isEmpty())
			output.setReturnValue("witness_firing_sequence_cycle", FiringSequence.class,
				new FiringSequence(result.cycle));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
