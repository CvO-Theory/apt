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

import java.util.List;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the language equivalence check as a module.
 * @author Uli Schlachter
 */
public class LanguageEquivalenceModule extends AbstractModule {

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
		inputSpec.addOptionalParameter("verbose", String.class, "", "Optionally more output");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("language_equivalent", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("witness_words", WordList.class);
		outputSpec.addReturnValue("witness_word", Word.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem arg1 = input.getParameter("pn_or_ts1", PetriNetOrTransitionSystem.class);
		PetriNetOrTransitionSystem arg2 = input.getParameter("pn_or_ts2", PetriNetOrTransitionSystem.class);

		// interpret verbose
		boolean all = false;
		String stringVerbose = input.getParameter("verbose", String.class);
		if (stringVerbose.equals("verbose")) {
			all = true;
		}

		TransitionSystem lts1 = arg1.getTs();
		TransitionSystem lts2 = arg2.getTs();
		if (lts1 == null) {
			lts1 = CoverabilityGraph.get(arg1.getNet()).toReachabilityLTS();
		}
		if (lts2 == null) {
			lts2 = CoverabilityGraph.get(arg2.getNet()).toReachabilityLTS();
		}

		List<Word> words = LanguageEquivalence.checkLanguageEquivalence(lts1, lts2, all);
		if (all) {
			output.setReturnValue("witness_words", WordList.class, new WordList(words));
		} else if (!words.isEmpty()) {
			output.setReturnValue("witness_word", Word.class, words.iterator().next());
		}
		output.setReturnValue("language_equivalent", Boolean.class, words.isEmpty());
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
