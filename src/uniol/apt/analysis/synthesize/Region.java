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

package uniol.apt.analysis.synthesize;

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
	private final int initialMarking;

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 * @param initialMarking Initial marking, or null if one should be calculated.
	 */
	private Region(RegionUtility utility, List<Integer> backwardWeights, List<Integer> forwardWeights, Integer initialMarking) {
		this.utility = utility;
		this.backwardWeights = Collections.unmodifiableList(new ArrayList<>(backwardWeights));
		this.forwardWeights = Collections.unmodifiableList(new ArrayList<>(forwardWeights));

		assert backwardWeights.size() == utility.getNumberOfEvents();
		assert forwardWeights.size() == utility.getNumberOfEvents();

		for (int i : backwardWeights)
			assert i >= 0;
		for (int i : forwardWeights)
			assert i >= 0;

		if (initialMarking != null)
			this.initialMarking = initialMarking;
		else
			this.initialMarking = getNormalRegionMarking();

		assert this.initialMarking >= 0;
	}

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 */
	public Region(RegionUtility utility, List<Integer> backwardWeights, List<Integer> forwardWeights, int initialMarking) {
		this(utility, backwardWeights, forwardWeights, Integer.valueOf(initialMarking));
	}

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 */
	public Region(RegionUtility utility, List<Integer> backwardWeights, List<Integer> forwardWeights) {
		this(utility, backwardWeights, forwardWeights, (Integer) null);
	}

	/**
	 * Return the region utility which this region uses.
	 * @return The region utility on which this region is defined.
	 */
	public RegionUtility getRegionUtility() {
		return utility;
	}

	/**
	 * Return the transitions system on which this region is defined.
	 * @return The transition system on which this region is defined.
	 */
	public TransitionSystem getTransitionSystem() {
		return utility.getTransitionSystem();
	}

	/**
	 * Return the backward weight for the given event index.
	 * @param index The event index to query.
	 * @return the backwards weight of the given event.
	 */
	public int getBackwardWeight(int index) {
		return backwardWeights.get(index);
	}

	/**
	 * Return the backward weight for the given event.
	 * @param event The event to query.
	 * @return the backwards weight of the given event.
	 */
	public int getBackwardWeight(String event) {
		return backwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the forward weight for the given event index.
	 * @param index The event index to query.
	 * @return the forwards weight of the given event.
	 */
	public int getForwardWeight(int index) {
		return forwardWeights.get(index);
	}

	/**
	 * Return the forward weight for the given event.
	 * @param event The event to query.
	 * @return the forwards weight of the given event.
	 */
	public int getForwardWeight(String event) {
		return forwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the total weight for the given event index.
	 * @param index The event index to query.
	 * @return the total weight of the given event.
	 */
	public int getWeight(int index) {
		return getForwardWeight(index) - getBackwardWeight(index);
	}

	/**
	 * Return the total weight for the given event.
	 * @param event The event to query.
	 * @return the total weight of the given event.
	 */
	public int getWeight(String event) {
		return getForwardWeight(event) - getBackwardWeight(event);
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @return The resulting number that this region assigns to the arguments
	 */
	public int evaluateParikhVector(List<Integer> vector) {
		List<String> events = utility.getEventList();
		assert vector.size() == events.size();

		int result = 0;
		for (int i = 0; i < events.size(); i++)
			result += vector.get(i) * getWeight(events.get(i));

		return result;
	}

	/**
	 * Return the initial marking of this region.
	 * @return The initial marking of this region.
	 */
	public int getInitialMarking() {
		return this.initialMarking;
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the initial state.
	 * @return The resulting number.
	 */
	public int getNormalRegionMarking() {
		int marking = 0;
		for (State state : getTransitionSystem().getNodes()) {
			if (utility.getSpanningTree().isReachable(state))
				marking = Math.max(marking, -evaluateParikhVector(utility.getReachingParikhVector(state)));
		}
		return marking;
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the given state.
	 * @param state The state to evaluate. Must be reachable from the initial state.
	 * @return The resulting number.
	 */
	public int getMarkingForState(State state) {
		return getInitialMarking() + evaluateParikhVector(utility.getReachingParikhVector(state));
	}

	/**
	 * Add a multiple of another region to this region and return the result.
	 * @param otherRegion The region to add to this one.
	 * @param factor A factor with which the other region is multiplied before addition.
	 * @return The resulting region.
	 */
	public Region addRegionWithFactor(Region otherRegion, int factor) {
		assert otherRegion.utility == utility;

		if (factor == 0)
			return this;

		List<Integer> backwardList = new ArrayList<>(utility.getNumberOfEvents());
		List<Integer> forwardList = new ArrayList<>(utility.getNumberOfEvents());

		for (int i = 0; i < utility.getNumberOfEvents(); i++) {
			int backward = this.backwardWeights.get(i);
			int forward = this.forwardWeights.get(i);
			if (factor > 0) {
				backward += factor * otherRegion.getBackwardWeight(i);
				forward += factor * otherRegion.getForwardWeight(i);
			} else {
				forward += -factor * otherRegion.getBackwardWeight(i);
				backward += -factor * otherRegion.getForwardWeight(i);
			}
			backwardList.add(backward);
			forwardList.add(forward);
		}

		return new Region(utility, backwardList, forwardList)
			.withInitialMarking(initialMarking + factor * otherRegion.getInitialMarking());
	}

	/**
	 * Add a multiple of another region to this region and return the result.
	 * @param otherRegion The region to add to this one.
	 * @return The resulting region.
	 */
	public Region addRegion(Region otherRegion) {
		return addRegionWithFactor(otherRegion, 1);
	}

	/**
	 * Turn this region into a pure region by enforcing that for every event, at least one of the forward or
	 * backward weight must be zero.
	 * @return The resulting region.
	 */
	public Region makePure() {
		List<Integer> vector = new ArrayList<>(utility.getNumberOfEvents());

		for (int i = 0; i < utility.getNumberOfEvents(); i++) {
			vector.add(getWeight(i));
		}

		return createPureRegionFromVector(utility, vector).withInitialMarking(initialMarking);
	}

	/**
	 * Create a new region that is a copy of this region, but with the specified initial marking.
	 * @param initialMarking The initial marking for the new region.
	 * @return The new region.
	 */
	public Region withInitialMarking(int initialMarking) {
		return new Region(utility, backwardWeights, forwardWeights, initialMarking);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{ init=");
		result.append(initialMarking);
		for (String event : utility.getEventList()) {
			result.append(", ");
			result.append(getBackwardWeight(event));
			result.append(":");
			result.append(event);
			result.append(":");
			result.append(getForwardWeight(event));
		}
		result.append(" }");
		return result.toString();
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
			reg.backwardWeights.equals(backwardWeights) &&
			reg.initialMarking == initialMarking;
	}

	@Override
	public int hashCode() {
		return 31 * (forwardWeights.hashCode() + 31 * backwardWeights.hashCode()) + initialMarking;
	}

	/**
	 * Create a pure region with the given weight vector.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param vector A vector which contains the weight for each event in the order described by the RegionUtility.
	 * @return The resulting region.
	 */
	public static Region createPureRegionFromVector(RegionUtility utility, List<Integer> vector) {
		List<Integer> backwardList = new ArrayList<>(utility.getNumberOfEvents());
		List<Integer> forwardList = new ArrayList<>(utility.getNumberOfEvents());
		assert vector.size() == utility.getNumberOfEvents();

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
		return new Region(utility, vector.subList(0, vector.size() / 2),
				vector.subList(vector.size() / 2, vector.size()));
	}

	/**
	 * Create the trivial region which assigns weight 0 to everything.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @return The resulting region.
	 */
	public static Region createTrivialRegion(RegionUtility utility) {
		List<Integer> vector = Collections.nCopies(utility.getNumberOfEvents(), 0);
		return new Region(utility, vector, vector);
	}

	/**
	 * Create a region which assigns forward and backward weight one to the given event and zero to all other
	 * events.
	 * @param utility The RegionUtility whose alphabet should be used.
	 * @param event The index in the region utility of the event that should get weight one.
	 * @return The resulting region.
	 */
	public static Region createUnitRegion(RegionUtility utility, int event) {
		List<Integer> nullList = Collections.nCopies(utility.getNumberOfEvents(), 0);
		List<Integer> vector = new ArrayList<>(nullList);
		vector.set(event, 1);
		return new Region(utility, vector, vector);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
