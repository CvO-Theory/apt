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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * An abstract region of a LTS. This assigns to each event a backward and forward number.
 * @author Uli Schlachter
 */
public class Region {
	private final RegionUtility utility;
	private final Map<String, Integer> backwardMap;
	private final Map<String, Integer> forwardMap;

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardMap The map that describes the backward weights for each event.
	 * @param forwardMap The map that describes the forward weights for each event.
	 */
	public Region(RegionUtility utility, Map<String, Integer> backwardMap, Map<String, Integer> forwardMap) {
		this.utility = utility;
		this.backwardMap = Collections.unmodifiableMap(backwardMap);
		this.forwardMap = Collections.unmodifiableMap(forwardMap);

		assert backwardMap.keySet().equals(forwardMap.keySet());
		assert backwardMap.keySet().equals(utility.getSpanningTree().getGraph().getAlphabet());
	}

	/**
	 * Return the region utility which this region uses.
	 */
	public RegionUtility getRegionUtility() {
		return utility;
	}

	/**
	 * Return the transitions system on which this region is defined.
	 */
	public TransitionSystem getTransitionSystem() {
		return utility.getSpanningTree().getGraph();
	}

	/**
	 * Return the backward weight for the given event.
	 */
	public int getBackwardWeight(String event) {
		return backwardMap.get(event);
	}

	/**
	 * Return the forward weight for the given event.
	 */
	public int getForwardWeight(String event) {
		return forwardMap.get(event);
	}

	/**
	 * Return the total weight for the given event.
	 */
	public int getWeight(String event) {
		return forwardMap.get(event) - backwardMap.get(event);
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @param arcEnter An arc which should only be used for its backward weight (optional)
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector, Arc arcEnter) {
		List<String> events = utility.getEventList();
		assert vector.size() == events.size();

		int result = 0;
		if (arcEnter != null)
			result = -getBackwardWeight(arcEnter.getLabel());

		for (int i = 0; i < events.size(); i++)
			result += vector.get(i) * getWeight(events.get(i));

		return result;
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector) {
		return evaluateParikhVector(vector, null);
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the initial state.
	 * @return The resulting number.
	 */
	public int getNormalRegionMarking() {
		// TODO: This is highly inefficient, would be better to evaluate according to a depth-first search
		// through reachable markings
		int marking = 0;
		for (State state : utility.getSpanningTree().getGraph().getNodes()) {
			// For reaching that 
			Arc predecessor = utility.getSpanningTree().getPredecessorEdge(state);
			if (predecessor != null)
				marking = Math.max(marking, -evaluateParikhVector(utility.getReachingParikhVector(predecessor.getSource()), predecessor));
		}
		return marking;
	}

	@Override
	public String toString() {
		String result = "";
		for (String event : backwardMap.keySet()) {
			if (!result.isEmpty())
				result += ", ";
			result += backwardMap.get(event) + ":" + event + ":" + forwardMap.get(event);
		}
		return "{ " + result + " }";
	}

	/**
	 * Create a pure region with the given weight vector.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param vector A vector which contains the weight for each event in the order described by the RegionUtility.
	 * @return The resulting region.
	 */
	public static Region createPureRegionFromVector(RegionUtility utility, List<Integer> vector) {
		Map<String, Integer> backwardMap = new HashMap<>();
		Map<String, Integer> forwardMap = new HashMap<>();
		List<String> events = utility.getEventList();

		assert vector.size() == events.size();
		for (int i = 0; i < events.size(); i++) {
			int value = vector.get(i);
			if (value > 0) {
				backwardMap.put(events.get(i), 0);
				forwardMap.put(events.get(i), value);
			} else {
				backwardMap.put(events.get(i), -value);
				forwardMap.put(events.get(i), 0);
			}
		}

		return new Region(utility, backwardMap, forwardMap);
	}

	/**
	 * Create an impure region with the given weight vector.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param vector A vector which contains the weight for each event in the order described by the RegionUtility.
	 * This vector first contains all backward weights and afterwards all forward weights.
	 * @return The resulting region.
	 */
	public static Region createImpureRegionFromVector(RegionUtility utility, List<Integer> vector) {
		Map<String, Integer> backwardMap = new HashMap<>();
		Map<String, Integer> forwardMap = new HashMap<>();
		List<String> events = utility.getEventList();

		assert vector.size() == 2 * events.size();
		for (int i = 0; i < events.size(); i++) {
			assert vector.get(i) >= 0;
			assert vector.get(i + events.size()) >= 0;

			backwardMap.put(events.get(i), vector.get(i));
			forwardMap.put(events.get(i), vector.get(i + events.size()));
		}

		return new Region(utility, backwardMap, forwardMap);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
