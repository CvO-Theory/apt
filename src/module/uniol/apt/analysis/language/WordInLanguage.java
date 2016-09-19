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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Check if a given word is in the prefix language of a given Petri net. Each transition has a label. The set of labels
 * is the alphabet with can be used for forming words. The prefix language of a Petri net is the language that contains
 * a word iff there is a fire sequence in the Petri net that is activated in the initial state where the label of each
 * fired transition is the next part of the word.
 * @author Uli Schlachter
 */
public class WordInLanguage {

	private final PetriNet pn;

	/**
	 * Constructor.
	 * @param pn The Petri net whose prefix language should be analyzed.
	 */
	public WordInLanguage(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Get all of the Petri nets labels. Please note that the result of this is only valid as long as no one
	 * modifies the Petri net.
	 * @return A map that maps a label to all transitions with that label.
	 */
	Map<String, Set<Transition>> getLabels() {
		Map<String, Set<Transition>> result = new HashMap<>();
		for (Transition trans : this.pn.getTransitions()) {
			Set<Transition> set = result.get(trans.getLabel());
			if (set == null) {
				set = new HashSet<>();
				result.put(trans.getLabel(), set);
			}
			set.add(trans);
		}
		return result;
	}

	/**
	 * Check if the given word is in the Petri net's prefix language.
	 * @param word The word that should be checked.
	 * @return A fire sequence which is activated in the Petri net's initial state and which produces the given
	 * word.
	 */
	public FiringSequence checkWord(List<String> word) {
		return checkWord(pn.getInitialMarking(), word, 0, getLabels());
	}

	/**
	 * Check if the given sub-word is in the language. The Petri net's current marking is used as the starting point
	 * for the search.
	 * @param curMarking Current marking.
	 * @param word The complete word that is being checked.
	 * @param idx The first index in #word that was not yet checked.
	 * @param labels The Petri net's labels and the set of transitions for each label.
	 * @return A fire sequence that produces the wanted word, or null.
	 */
	FiringSequence checkWord(Marking curMarking, List<String> word, int idx, Map<String, Set<Transition>> labels) {
		/* Are we checking for the empty word? That one is always in the language */
		if (idx == word.size()) {
			return new FiringSequence();
		}

		Set<Transition> transitions = labels.get(word.get(idx));
		if (transitions == null) { // Invalid label found in the word.
			return null;
		}

		// Find a transition with which we can continue
		for (Transition trans : transitions) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (!trans.isFireable(curMarking)) {
				continue;
			}

			// Yay, we can continue down this path, find the next part of the word
			Marking newMarking = trans.fire(curMarking);
			FiringSequence result = checkWord(newMarking, word, idx + 1, labels);

			if (result == null) {
				// That didn't lead to a correct firing sequence, let's continue searching.
				continue;
			}

			// Success!
			result.add(0, trans);
			return result;
		}

		// We didn't find a good firing sequence :-(
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
