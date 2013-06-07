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

import java.util.LinkedList;
import java.util.List;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

public class LanguageModule extends AbstractModule {
	@Override
	public String getShortDescription() {
		return "Return the _finite_ prefix language of the Petri net";
	}

	@Override
	public String getName() {
		return "language";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class,
				"The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("language", WordList.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Language lang = new Language(input.getParameter("pn", PetriNet.class));
		List<Word> words = new LinkedList<Word>();
		for(Word w : lang.language()) {
			words.add(w);
		}
		// TODO: maybe sort the word list
		output.setReturnValue("language", WordList.class, new WordList(words));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
