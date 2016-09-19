/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import static uniol.apt.analysis.synthesize.SynthesizeUtils.formatESSPFailure;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.formatSSPFailure;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.makeTS;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.language.Word;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis from a word as a module.
 * @author Uli Schlachter
 */
@AptModule
public class SynthesizeWordModule extends AbstractSynthesizeModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Synthesize a Petri Net from a word";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ getOptionsDescription("cycle, ", " - cycle: Form a cyclic word representing"
					+ " w^* instead of solving the input word w directly\n")
			+ "\n\nThis module tries to synthesize a Petri Net whose prefix language "
			+ "contains only the specified word. Thus, no other words are firable. If this fails, a list "
			+ " of separation failures is printed.\n\nExample calls:\n\n"
			+ " apt " + getName() + " none a,b,a,b\n\n"
			+ "The above prints a Petri net\n\n\n"
			+ " apt " + getName() + " pure,safe a,b,c,a\n\n"
			+ "This also prints a Petri net\n\n\n"
			+ " apt " + getName() + " none a,b,b,a,a\n\n"
			+ "The above produces the following output:\n\n"
			+ " separationFailurePoints: a, b, [a] b, a, a\n\n"
			+ "Here three places where calculated. For example, the first one of them has an initial "
			+ "marking of one, transition 'a' consumes a token on this place and 'b' produces one. "
			+ "However, these three places are not enough for producing the requested word, because after "
			+ "firing 'a' and 'b' once, transition 'a' is enabled, but shouldn't. The module also could "
			+ "not calculate any place which would prevent 'a' from occurring in this state without also "
			+ "restricting 'a' in some state where it must occur.\n\n\n"
			+ " apt " + getName() + " 2-bounded a,a,a\n\n"
			+ "The above produces the following output:\n\n"
			+ " separationFailurePoints: a, a, a [a]\n\n"
			+ "This means that there is no 2-bounded Petri Net where three a's are firable in sequence, "
			+ "but not also a fourth one can occur.";
	}

	@Override
	public String getName() {
		return "word_synthesize";
	}

	@Override
	protected void requireExtra(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("word", Word.class, "The word that should be synthesized");
	}

	@Override
	protected void provideExtra(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("separationFailurePoints", String.class);
		outputSpec.addReturnValue("stateSeparationFailurePoints", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Word word = input.getParameter("word", Word.class);
		SynthesizePN synthesize = runSynthesis(new TSForWord(word), input, output);
		output.setReturnValue("stateSeparationFailurePoints", String.class,
				formatSSPFailure(word, synthesize.getFailedStateSeparationProblems()));
		output.setReturnValue("separationFailurePoints", String.class,
				formatESSPFailure(word, synthesize.getFailedEventStateSeparationProblems(), false));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}

	static private class TSForWord implements TransitionSystemForOptions {
		final private List<String> word;

		public TSForWord(List<String> word) {
			this.word = word;
		}

		@Override
		public Collection<String> supportedExtraOptions() {
			Set<String> options = new HashSet<>();
			options.add("cyclic");
			options.add("cycle");
			return options;
		}

		@Override
		public TransitionSystem getTS(Collection<String> enabledOptions) {
			boolean cycle = enabledOptions.contains("cyclic") || enabledOptions.contains("cycle");
			return makeTS(word, cycle);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
