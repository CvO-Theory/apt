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

package uniol.apt.analysis.language;

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
 * Provide the language equivalence check as a module.
 * @author Uli Schlachter
 */
@AptModule
public class LanguageEquivalenceModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Check if two Petri nets generate the same language";
	}

	@Override
	public String getName() {
		return "language_equivalence";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn_or_ts1", PetriNetOrTransitionSystem.class,
			"The first Petri net or transition system that should be examined");
		inputSpec.addParameter("pn_or_ts2", PetriNetOrTransitionSystem.class,
			"The second Petri net or transition system that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("language_equivalent", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_word", Word.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem arg1 = input.getParameter("pn_or_ts1", PetriNetOrTransitionSystem.class);
		PetriNetOrTransitionSystem arg2 = input.getParameter("pn_or_ts2", PetriNetOrTransitionSystem.class);

		TransitionSystem lts1 = arg1.getReachabilityLTS();
		TransitionSystem lts2 = arg2.getReachabilityLTS();

		Word word = LanguageEquivalence.checkLanguageEquivalence(lts1, lts2);
		output.setReturnValue("witness_word", Word.class, word);
		output.setReturnValue("language_equivalent", Boolean.class, word == null);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
