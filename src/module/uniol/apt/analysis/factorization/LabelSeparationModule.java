/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.factorization;

import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.language.Word;
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
 * A module to check if an LTS is T'-separated for some label set T'.
 *
 * @author Jonas Prellberg
 *
 */
@AptModule
public class LabelSeparationModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "label_separation";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS to check");
		inputSpec.addParameter("T'", Word.class, "Set of labels to check");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("T'-separated", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_first_state", State.class);
		outputSpec.addReturnValue("witness_second_state", State.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		Word tPrimeWord = input.getParameter("T'", Word.class);
		Set<String> tPrime = new HashSet<>(tPrimeWord);
		if (!ts.getAlphabet().containsAll(tPrime)) {
			throw new ModuleException(String.format(
					"At least one of the supplied labels is not part of the LTS label set."));
		}
		LabelSeparationResult r = LabelSeparation.checkSeparated(ts, tPrime);
		output.setReturnValue("T'-separated", Boolean.class, r.isSeparated());
		if (!r.isSeparated()) {
			output.setReturnValue("witness_first_state", State.class, r.getWitnessState1());
			output.setReturnValue("witness_second_state", State.class, r.getWitnessState2());
		}
	}

	@Override
	public String getShortDescription() {
		return "Check if a LTS is T'-separated for some label set T'";
	}

	@Override
	public String getLongDescription() {
		return "Check if for all states s != s' it is impossible to go from s to s' "
				+ "using only labels from T' (disregarding arc direction) as well as "
				+ "only labels outside T' (disregarding arc direction)";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
