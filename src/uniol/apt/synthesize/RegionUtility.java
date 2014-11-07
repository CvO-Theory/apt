/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.synthesize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.SpanningTree;

/**
 * Utility class for generating regions. This class assigns indices to the variable and produces parikh vectors that
 * reach a given state from the initial state.
 * @author Uli Schlachter
 */
public class RegionUtility {
	private final TransitionSystem ts;
	private final SpanningTree<TransitionSystem, Arc, State> tree;
	private final List<String> alphabet;
	private final Map<State, List<Integer>> parikhVectorMap = new HashMap<>();

	/**
	 * Construct a new RegionUtility.
	 * @param ts The TransitionSystem on which regions should be examined.
	 * @param tree A spanning tree for the given transition system.
	 */
	public RegionUtility(TransitionSystem ts, SpanningTree<TransitionSystem, Arc, State> tree) {
		assert tree.getGraph() == ts;
		this.ts = ts;
		this.tree = tree;
		this.alphabet = new ArrayList<>(ts.getAlphabet());
	}

	/**
	 * Construct a new RegionUtility.
	 * @param ts The TransitionSystem on which regions should be examined.
	 */
	public RegionUtility(TransitionSystem ts) {
		this(ts, new SpanningTree<TransitionSystem, Arc, State>(ts, ts.getInitialState()));
	}

	/**
	 * Get the index of the given letter.
	 * @param letter The letter whose index should be returned.
	 * @return The letter's index or null.
	 */
	public Integer getLetterIndex(String letter) {
		return alphabet.indexOf(letter);
	}

	/**
	 * @return The spanning tree on which this RegionUtility operates.
	 */
	public SpanningTree<TransitionSystem, Arc, State> getSpanningTree() {
		return tree;
	}

	/**
	 * @param node The node whose Parikh vector should be returned.
	 * @return The Parikh vector that reaches the node from the initial state.
	 */
	public List<Integer> getReachingParikhVector(State node) {
		List<Integer> result = parikhVectorMap.get(node);
		if (result == null) {
			if (node.equals(tree.getStartNode())) {
				Integer[] array = new Integer[alphabet.size()];
				Arrays.fill(array, 0);
				result = Arrays.asList(array);
			} else {
				Arc predecessor = tree.getPredecessorEdge(node);
				if (predecessor == null)
					return Collections.emptyList();

				List<Integer> predVector = getReachingParikhVector(predecessor.getSource());
				result = new ArrayList<>();
				result.addAll(predVector);

				int idx = getLetterIndex(predecessor.getLabel());
				result.set(idx, result.get(idx) + 1);
			}
			result = Collections.unmodifiableList(result);
			parikhVectorMap.put(node, result);
		}
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
