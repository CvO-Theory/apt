/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.processmining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.analysis.language.WordList;
import uniol.apt.analysis.language.Word;
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
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Provide the LTS creation as a module.
 * @author Uli Schlachter
 */
@AptModule
public class CreateLTSModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Create an LTS from a list of words";
	}

	@Override
	public String getName() {
		return "create_lts";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("words", WordList.class, "The list of words that should be possible");
		inputSpec.addOptionalParameterWithoutDefault("invariants", WordList.class,
				"The list of invariants that should be honored");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("lts", TransitionSystem.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}


	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		WordList words = input.getParameter("words", WordList.class);
		WordList invariants = input.getParameter("invariants", WordList.class);

		CreateLTS create;
		if (invariants == null) {
			create = new CreateLTS();
		} else {
			Set<String> alphabet = new HashSet<>();
			for (Word word : words) {
				alphabet.addAll(word);
			}
			Collection<List<String>> invariantCollection = new ArrayList<>();
			for (Word word : invariants) {
				invariantCollection.add(new ArrayList<>(word));
				alphabet.addAll(word);
			}
			InvariantsMapper mapper = new InvariantsMapper(alphabet, invariantCollection);
			create = new CreateLTS(mapper);
		}

		for (Word word : words) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			create.addWord(word);
		}
		TransitionSystem result = create.getTransitionSystem();

		output.setReturnValue("lts", TransitionSystem.class, result);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
