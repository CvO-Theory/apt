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
 * A module to check if an LTS is a T'-gdiam with a label-set T'.
 *
 * @author Jonas Prellberg
 *
 */
@AptModule
public class GeneralDiamondModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "gdiam";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS to check");
		inputSpec.addParameter("T'", Word.class, "Set of labels to check");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("T'-gdiam", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_state", State.class);
		outputSpec.addReturnValue("witness_first_label", String.class);
		outputSpec.addReturnValue("witness_second_label", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		Word tPrimeWord = input.getParameter("T'", Word.class);
		Set<String> tPrime = new HashSet<>(tPrimeWord);
		if (!ts.getAlphabet().containsAll(tPrime)) {
			throw new ModuleException(String.format(
					"At least one of the supplied labels is not part of the LTS label-set."));
		}
		GeneralDiamondResult r = GeneralDiamond.checkGdiam(ts, tPrime);
		output.setReturnValue("T'-gdiam", Boolean.class, r.isGdiam());
		if (!r.isGdiam()) {
			output.setReturnValue("witness_state", State.class, r.getWitnessState());
			output.setReturnValue("witness_first_label", String.class, r.getWitnessLabel1String());
			output.setReturnValue("witness_second_label", String.class, r.getWitnessLabel2String());
		}
	}

	@Override
	public String getShortDescription() {
		return "Check if a LTS is a T'-gdiam";
	}

	@Override
	public String getLongDescription() {
		return "Check if for the given LTS = (S, ->, T, s0) for each pair of labels "
				+ "a in T', b in T\\T' the general diamond property holds.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
