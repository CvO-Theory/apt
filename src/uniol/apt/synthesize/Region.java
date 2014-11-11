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
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * An abstract region of a LTS. This assigns to each event a backward and forward number.
 * @author Uli Schlachter
 */
public class Region {
	private final RegionUtility utility;
	private final List<Integer> backwardWeights;
	private final List<Integer> forwardWeights;

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 */
	public Region(RegionUtility utility, List<Integer> backwardWeights, List<Integer> forwardWeights) {
		this.utility = utility;
		this.backwardWeights = Collections.unmodifiableList(new ArrayList<>(backwardWeights));
		this.forwardWeights = Collections.unmodifiableList(new ArrayList<>(forwardWeights));

		assert backwardWeights.size() == utility.getEventList().size();
		assert forwardWeights.size() == utility.getEventList().size();
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
		return utility.getTransitionSystem();
	}

	/**
	 * Return the backward weight for the given event index.
	 */
	public int getBackwardWeight(int index) {
		return backwardWeights.get(index);
	}

	/**
	 * Return the backward weight for the given event.
	 */
	public int getBackwardWeight(String event) {
		return backwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the forward weight for the given event index.
	 */
	public int getForwardWeight(int index) {
		return forwardWeights.get(index);
	}

	/**
	 * Return the forward weight for the given event.
	 */
	public int getForwardWeight(String event) {
		return forwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the total weight for the given event index.
	 */
	public int getWeight(int index) {
		return getForwardWeight(index) - getBackwardWeight(index);
	}

	/**
	 * Return the total weight for the given event.
	 */
	public int getWeight(String event) {
		return getForwardWeight(event) - getBackwardWeight(event);
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @param eventIndex An event which should additionally be included for its backward weight (optional)
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector, Integer eventIndex) {
		List<String> events = utility.getEventList();
		assert vector.size() == events.size();

		int result = 0;
		if (eventIndex != null)
			result = -getBackwardWeight(eventIndex);

		for (int i = 0; i < events.size(); i++)
			result += vector.get(i) * getWeight(events.get(i));

		return result;
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @param arcEnter An arc which should only be used for its backward weight (optional)
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector, Arc arcEnter) {
		if (arcEnter == null)
			return evaluateParikhVector(vector, (Integer) null);
		return evaluateParikhVector(vector, utility.getEventIndex(arcEnter.getLabel()));
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector) {
		return evaluateParikhVector(vector, (Integer) null);
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the initial state.
	 * @return The resulting number.
	 */
	public int getNormalRegionMarking() {
		// TODO: This is highly inefficient, would be better to evaluate according to a depth-first search
		// through reachable markings
		int marking = 0;
		for (State state : getTransitionSystem().getNodes()) {
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
		for (String event : utility.getEventList()) {
			if (!result.isEmpty())
				result += ", ";
			result += getBackwardWeight(event) + ":" + event + ":" + getForwardWeight(event);
		}
		return "{ " + result + " }";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Region))
			return false;
		if (obj == this)
			return true;
		Region reg = (Region) obj;
		return reg.utility.equals(utility) &&
			reg.forwardWeights.equals(forwardWeights) &&
			reg.backwardWeights.equals(backwardWeights);
	}

	@Override
	public int hashCode() {
		return forwardWeights.hashCode() + backwardWeights.hashCode();
	}

	/**
	 * Create a pure region with the given weight vector.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param vector A vector which contains the weight for each event in the order described by the RegionUtility.
	 * @return The resulting region.
	 */
	public static Region createPureRegionFromVector(RegionUtility utility, List<Integer> vector) {
		List<Integer> backwardList = new ArrayList<>(utility.getEventList().size());
		List<Integer> forwardList = new ArrayList<>(utility.getEventList().size());

		for (int i = 0; i < vector.size(); i++) {
			int value = vector.get(i);
			if (value > 0) {
				backwardList.add(0);
				forwardList.add(value);
			} else {
				backwardList.add(-value);
				forwardList.add(0);
			}
		}

		return new Region(utility, backwardList, forwardList);
	}

	/**
	 * Create an impure region with the given weight vector.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param vector A vector which contains the weight for each event in the order described by the RegionUtility.
	 * This vector first contains all backward weights and afterwards all forward weights.
	 * @return The resulting region.
	 */
	public static Region createImpureRegionFromVector(RegionUtility utility, List<Integer> vector) {
		return new Region(utility, vector.subList(0, vector.size() / 2), vector.subList(vector.size() / 2, vector.size()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
