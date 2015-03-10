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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.language.Word;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis from a word as a module.
 * @author Uli Schlachter
 */
public class SynthesizeWordModule extends AbstractModule {

	@Override
	public String getShortDescription() {
		return "Synthesize a Petri Net from a word";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". This module tries to synthesize a Petri Net whose prefix language "
			+ "contains only the specified word. Thus, no other words are firable. If this fails, a list "
			+ " of separation failures is printed.\n\nExample calls:\n\n"
			+ " apt " + getName() + " none a,b,a,b\n\n"
			+ "The above prints a Petri net\n\n\n"
			+ " apt " + getName() + " pure,safe a,b,c,a\n\n"
			+ "This also prints a Petri net\n\n\n"
			+ " apt " + getName() + " none a,b,b,a,a\n\n"
			+ "The above produces the following output:\n\n"
			+ " separatingRegions: [{ init=1, 1:a:0, 0:b:1 }, { init=2, 0:a:0, 1:b:0 }, "
			+ "{ init=0, 0:a:1, 1:b:1 }]\n"
			+ " separationFailurePoints: a, b, [a] b, a, a\n\n"
			+ "Here three places where calculated. For Example, the first one of them has an initial "
			+ "marking of one, transition 'a' consumes a token on this place and 'b' produces one. "
			+ "However, these three places are not enough for producing the requested word, because after "
			+ "firing 'a' and 'b' once, transition 'a' is enabled, but shouldn't. The module also could "
			+ "not calculate any place which would prevent 'a' from occurring in this state without also "
			+ "restricting 'a' in some state where it must occur.\n\n\n"
			+ " apt " + getName() + " 3-bounded a,a,a,a\n\n"
			+ "The above produces the following output:\n\n"
			+ " separationFailurePoints: a, a, a, a [a]\n\n"
			+ "This means that there is no 3-bounded Petri Net where three a's are firable in sequence, "
			+ "but not also a fourth one can occur.";
	}

	@Override
	public String getName() {
		return "word_synthesize";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		SynthesizeModule.requireCommon(inputSpec);
		inputSpec.addParameter("word", Word.class, "The word that should be synthesized");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		SynthesizeModule.provideCommon(outputSpec);
		outputSpec.addReturnValue("separationFailurePoints", String.class);
		outputSpec.addReturnValue("stateSeparationFailurePoints", String.class);
	}

	static private void appendSeparationFailure(StringBuilder result, Set<String> failures) {
		if (failures.isEmpty())
			return;

		boolean first = true;
		if (result.length() != 0)
			result.append(" ");
		result.append("[");
		for (String event : failures) {
			if (!first)
				result.append(",");
			result.append(event);
			first = false;
		}
		result.append("]");
	}

	static public String formatESSPFailure(Word word, Map<String, Set<State>> separationFailures) {
		if (separationFailures.isEmpty())
			return null;

		// List mapping indices into the word to sets of failed separation problems
		List<Set<String>> failedSeparation = new ArrayList<>(word.size());
		// Add one for the initial state
		failedSeparation.add(new HashSet<String>());
		for (String event : word) {
			failedSeparation.add(new HashSet<String>());
		}

		// Add all failed separation problems into the above list
		for (Map.Entry<String, Set<State>> failures : separationFailures.entrySet()) {
			for (State state : failures.getValue()) {
				int index = Integer.parseInt(state.getExtension("index").toString());
				failedSeparation.get(index).add(failures.getKey());
			}
		}

		// Build the string representation of the separation failures
		StringBuilder result = new StringBuilder();
		for (int index = 0; index < word.size(); index++) {
			if (index != 0)
				result.append(",");
			appendSeparationFailure(result, failedSeparation.get(index));
			if (result.length() != 0)
				result.append(" ");
			result.append(word.get(index));
		}
		appendSeparationFailure(result, failedSeparation.get(word.size()));
		return result.toString();
	}

	static public String formatSSPFailure(Word word, Collection<Set<State>> separationFailures) {
		// State separation can only fail due to boundedness. E.g. a safe Petri net cannot generate a,a.
		if (separationFailures.isEmpty())
			return null;

		int separable[] = new int[word.size() + 1];
		Arrays.fill(separable, 0);

		int numFailure = 0;
		for (Set<State> states : separationFailures) {
			numFailure++;
			for (State state : states) {
				int index = Integer.parseInt(state.getExtension("index").toString());
				separable[index] = numFailure;
			}
		}

		// Build the string representation of the separation failures
		StringBuilder result = new StringBuilder();
		for (int index = 0; index < word.size(); index++) {
			if (index != 0)
				result.append(",");
			if (separable[index] != 0) {
				if (index != 0)
					result.append(" ");
				result.append(separable[index]);
			}
			if (result.length() != 0)
				result.append(" ");
			result.append(word.get(index));
		}
		if (separable[word.size()] != 0)
			result.append(" " + separable[word.size()]);
		return result.toString();
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Word word = input.getParameter("word", Word.class);
		TransitionSystem ts = makeTS(word);
		SynthesizePN synthesize = SynthesizeModule.runSynthesis(ts, input, output);
		output.setReturnValue("stateSeparationFailurePoints", String.class,
				formatSSPFailure(word, synthesize.getFailedStateSeparationProblems()));
		output.setReturnValue("separationFailurePoints", String.class,
				formatESSPFailure(word, synthesize.getFailedEventStateSeparationProblems()));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}

	static public TransitionSystem makeTS(List<String> word) {
		TransitionSystem ts = new TransitionSystem();
		State state = ts.createState();
		state.putExtension("index", 0);
		ts.setInitialState(state);

		int index = 1;
		for (String label : word) {
			State nextState = ts.createState();
			nextState.putExtension("index", index);
			ts.createArc(state, nextState, label);

			state = nextState;
			index++;
		}

		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
