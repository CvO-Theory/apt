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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Utility functions used by the modules.
 *
 * @author Uli Schlachter
 */
public class SynthesizeUtils {
	private SynthesizeUtils() { /* hide constructor */ }

	private static void appendSeparationFailure(StringBuilder result, Set<String> failures,
			boolean compressedFormat) {
		if (failures.isEmpty())
			return;

		boolean first = true;
		if (!compressedFormat && result.length() != 0)
			result.append(" ");
		result.append("[");
		for (String event : failures) {
			if (!compressedFormat && !first)
				result.append(",");
			result.append(event);
			first = false;
		}
		result.append("]");
	}

	public static String formatESSPFailure(List<String> word, Map<String, Set<State>> separationFailures,
			boolean compressedFormat) {
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
			if (!compressedFormat && index != 0)
				result.append(",");
			appendSeparationFailure(result, failedSeparation.get(index), compressedFormat);
			if (!compressedFormat && result.length() != 0)
				result.append(" ");
			result.append(word.get(index));
		}
		appendSeparationFailure(result, failedSeparation.get(word.size()), compressedFormat);
		return result.toString();
	}

	public static String formatSSPFailure(List<String> word, Collection<Set<State>> separationFailures) {
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

	public static TransitionSystem makeTS(List<String> word) {
		return makeTS(word, false);
	}

	public static TransitionSystem makeTS(List<String> word, boolean cycle) {
		TransitionSystem ts = new TransitionSystem();
		State state = ts.createState();
		state.putExtension("index", 0);
		ts.setInitialState(state);

		int index = 1;
		for (String label : word) {
			State nextState;
			if (cycle && index == word.size())
				nextState = ts.getInitialState();
			else {
				nextState = ts.createState();
				nextState.putExtension("index", index);
			}
			ts.createArc(state, nextState, label);

			state = nextState;
			index++;
		}

		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

