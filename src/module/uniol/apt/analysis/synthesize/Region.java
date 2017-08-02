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
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * An abstract region of a LTS. This assigns to each event a backward and forward number.
 * @author Uli Schlachter
 */
public class Region {
	private final RegionUtility utility;
	private final List<BigInteger> backwardWeights;
	private final List<BigInteger> forwardWeights;
	private final BigInteger initialMarking;
	private final Map<State, BigInteger> stateMarkingCache = new HashMap<>();

	/**
	 * Create a new region.
	 * @param utility The RegionUtility instance that supports this region.
	 * @param backwardWeights List of weights for the backward weight of each event.
	 * @param forwardWeights List of weights for the forward weights of each event.
	 * @param initialMarking Initial marking, or null if one should be calculated.
	 */
	private Region(RegionUtility utility, List<BigInteger> backwardWeights, List<BigInteger> forwardWeights,
			BigInteger initialMarking) {
		this.utility = utility;
		this.backwardWeights = Collections.unmodifiableList(new ArrayList<>(backwardWeights));
		this.forwardWeights = Collections.unmodifiableList(new ArrayList<>(forwardWeights));
		this.initialMarking = initialMarking;

		int numberEvents = utility.getNumberOfEvents();
		if (backwardWeights.size() != numberEvents)
			throw new IllegalArgumentException("There must be as many backward weights as events");
		if (forwardWeights.size() != numberEvents)
			throw new IllegalArgumentException("There must be as many forward weights as events");
		for (BigInteger i : backwardWeights)
			if (i.compareTo(BigInteger.ZERO) < 0)
				throw new IllegalArgumentException("Backward weight i=" + i + " must not be negative");
		for (BigInteger i : forwardWeights)
			if (i.compareTo(BigInteger.ZERO) < 0)
				throw new IllegalArgumentException("Forward weight i=" + i + " must not be negative");
		if (initialMarking.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException("Initial marking " + initialMarking +
					" must not be negative");
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
	public BigInteger getBackwardWeight(int index) {
		return backwardWeights.get(index);
	}

	/**
	 * Return the backward weight for the given event.
	 * @param event The event to query.
	 * @return the backwards weight of the given event.
	 */
	public BigInteger getBackwardWeight(String event) {
		return backwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the forward weight for the given event index.
	 * @param index The event index to query.
	 * @return the forwards weight of the given event.
	 */
	public BigInteger getForwardWeight(int index) {
		return forwardWeights.get(index);
	}

	/**
	 * Return the forward weight for the given event.
	 * @param event The event to query.
	 * @return the forwards weight of the given event.
	 */
	public BigInteger getForwardWeight(String event) {
		return forwardWeights.get(utility.getEventIndex(event));
	}

	/**
	 * Return the total weight for the given event index.
	 * @param index The event index to query.
	 * @return the total weight of the given event.
	 */
	public BigInteger getWeight(int index) {
		return getForwardWeight(index).subtract(getBackwardWeight(index));
	}

	/**
	 * Return the total weight for the given event.
	 * @param event The event to query.
	 * @return the total weight of the given event.
	 */
	public BigInteger getWeight(String event) {
		return getWeight(utility.getEventIndex(event));
	}

	/**
	 * Evaluate the given Parikh vector with respect to this region.
	 * @param vector The vector to evaluate.
	 * @return The resulting number that this region assigns to the arguments
	 */
	public BigInteger evaluateParikhVector(List<BigInteger> vector) {
		assert vector.size() == utility.getEventList().size();

		BigInteger result = BigInteger.ZERO;
		for (int i = 0; i < vector.size(); i++)
			result = result.add(vector.get(i).multiply(getWeight(i)));

		return result;
	}

	/**
	 * Return the initial marking of this region.
	 * @return The initial marking of this region.
	 */
	public BigInteger getInitialMarking() {
		return this.initialMarking;
	}

	/**
	 * Get the marking that a normal region based on this abstract would assign to the given state.
	 * @param state The state to evaluate. Must be reachable from the initial state.
	 * @return The resulting number.
	 * @throws UnreachableException if the given state is unreachable from the initial state
	 */
	public BigInteger getMarkingForState(State state) throws UnreachableException {
		BigInteger i = stateMarkingCache.get(state);
		if (i == null) {
			i = getInitialMarking().add(evaluateParikhVector(utility.getReachingParikhVector(state)));
			stateMarkingCache.put(state, i);
		}
		return i;
	}

	/**
	 * Check if this region prevents any arcs that it should not prevent.
	 * @return A pair of a state in which an enabled event is prevented by this region
	 * @see checkValidRegion
	 */
	public Pair<State, String> findPreventedArc() {
		for (State state : getTransitionSystem().getNodes()) {
			BigInteger marking;
			try {
				marking = getMarkingForState(state);
			} catch (UnreachableException e) {
				continue;
			}
			for (Arc arc : state.getPostsetEdges()) {
				BigInteger targetMarking = marking.subtract(getBackwardWeight(arc.getLabel()));
				if (targetMarking.compareTo(BigInteger.ZERO) < 0)
					return new Pair<State, String>(state, arc.getLabel());
			}
		}
		return null;
	}

	/**
	 * Check if this region allows the cycles that it must allow. This function checks for all arcs if the marking
	 * of the source state plus the effect of the transition is the marking of the target state.
	 * @return An arc which does not lead to the expected state.
	 * @see checkValidRegion
	 */
	public Arc findArcWithWrongEffect() {
		for (Arc arc : getTransitionSystem().getEdges()) {
			try {
				BigInteger source = getMarkingForState(arc.getSource());
				BigInteger target = getMarkingForState(arc.getTarget());
				BigInteger effect = getWeight(arc.getLabel());
				if (!source.add(effect).equals(target))
					return arc;
			} catch (UnreachableException e) {
				continue;
			}
		}
		return null;
	}

	/**
	 * Check if this region is indeed a valid region and throw an exception if not.
	 * @throws InvalidRegionException If this region is not valid.
	 * @see findPreventedArc
	 * @see findArcWithWrongEffect
	 */
	public void checkValidRegion() throws InvalidRegionException {
		Pair<State, String> counterexample = findPreventedArc();
		if (counterexample != null)
			throw new InvalidRegionException(counterexample.getFirst(), counterexample.getSecond());
		Arc counterexample2 = findArcWithWrongEffect();
		if (counterexample2 != null)
			throw new InvalidRegionException(counterexample2);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{ init=");
		result.append(getInitialMarking());
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
			reg.getInitialMarking().equals(getInitialMarking());
	}

	@Override
	public int hashCode() {
		return 31 * (forwardWeights.hashCode() + 31 * backwardWeights.hashCode())
			+ getInitialMarking().hashCode();
	}

	/**
	 * Helper class for creating Region instances.
	 */
	static public class Builder {
		private final RegionUtility utility;
		private final List<BigInteger> backwardList;
		private final List<BigInteger> forwardList;

		/**
		 * Create a builder for the region with the given weights.
		 * @param utility The region utility that should be used.
		 * @param backward The backward weights.
		 * @param forward The forward weights.
		 */
		public Builder(RegionUtility utility, List<BigInteger> backward, List<BigInteger> forward) {
			if (backward.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The backward list must contain an entry per event");
			if (forward.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The forward list must contain an entry per event");
			this.utility = utility;
			this.backwardList = new ArrayList<>(backward);
			this.forwardList = new ArrayList<>(forward);
		}

		/**
		 * Create a builder for the region which, for now, assigns weight 0 to everything.
		 * @param utility The region utility that should be used.
		 */
		public Builder(RegionUtility utility) {
			this(utility, Collections.nCopies(utility.getNumberOfEvents(), BigInteger.ZERO),
					Collections.nCopies(utility.getNumberOfEvents(), BigInteger.ZERO));
		}

		/**
		 * Create a builder and initialize the weights from the given region.
		 * @param region The region to copy.
		 */
		public Builder(Region region) {
			this(region.utility, region.backwardWeights, region.forwardWeights);
		}

		/**
		 * Add some weight around the given event. If the argument is positive, the forward weight is increased
		 * accordingly, otherwise the backward weight is increased.
		 * @param event The event whose weight should be modified.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addWeightOn(String event, BigInteger weight) {
			return addWeightOn(utility.getEventIndex(event), weight);
		}

		/**
		 * Add some weight around the given event. If the argument is positive, the forward weight is increased
		 * accordingly, otherwise the backward weight is increased.
		 * @param index The event index whose weight should be modified.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addWeightOn(int index, BigInteger weight) {
			int cmp = weight.compareTo(BigInteger.ZERO);
			if (cmp == 0)
				return this;
			if (cmp > 0)
				forwardList.set(index, forwardList.get(index).add(weight));
			else
				backwardList.set(index, backwardList.get(index).subtract(weight));
			return this;
		}

		/**
		 * Add a loop with the given weight around the given event. This means that the backward weight and the
		 * forward weight are both increased by the given weight.
		 * @param event The event on which a loop should be added.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addLoopAround(String event, BigInteger weight) {
			return addLoopAround(utility.getEventIndex(event), weight);
		}

		/**
		 * Add a loop with the given weight around the given event. This means that the backward weight and the
		 * forward weight are both increased by the given weight.
		 * @param index The event index on which a loop should be added.
		 * @param weight The weight that should be added.
		 * @return This builder instance.
		 */
		public Builder addLoopAround(int index, BigInteger weight) {
			backwardList.set(index, backwardList.get(index).add(weight));
			forwardList.set(index, forwardList.get(index).add(weight));
			return this;
		}

		/**
		 * Add the weights of a region with some factor applied to our current state. Adding a region with a
		 * factor of one means that its backward and forward weights get added to our backward and forward
		 * weights. Adding a region with a factor of minus one means that its backward weights get added to our
		 * forward weight, and vice versa.
		 * @param region The region to add.
		 * @param factor The factor that should be used.
		 * @return This builder instance.
		 */
		public Builder addRegionWithFactor(Region region, BigInteger factor) {
			if (factor.equals(BigInteger.ZERO))
				return this;

			List<BigInteger> theirBackwardWeights = region.backwardWeights;
			List<BigInteger> theirForwardWeights = region.forwardWeights;

			// If the factor is negative, swap the weights and negate the factor
			if (factor.compareTo(BigInteger.ZERO) < 0) {
				factor = factor.negate();
				List<BigInteger> tmp = theirBackwardWeights;
				theirBackwardWeights = theirForwardWeights;
				theirForwardWeights = tmp;
			}

			// Do the addition
			for (int i = 0; i < utility.getNumberOfEvents(); i++) {
				BigInteger weight = backwardList.get(i);
				backwardList.set(i, weight.add(factor.multiply(theirBackwardWeights.get(i))));

				weight = forwardList.get(i);
				forwardList.set(i, weight.add(factor.multiply(theirForwardWeights.get(i))));
			}
			return this;
		}

		/**
		 * Turn this builder's weight into the weight for a normal region. This modifies the weight so that the
		 * effect of each event is still the same, but at least one of the forward or the backward weights are
		 * zero.
		 * @return This builder instance.
		 */
		public Builder makePure() {
			for (int i = 0; i < utility.getNumberOfEvents(); i++) {
				BigInteger weight = forwardList.get(i).subtract(backwardList.get(i));
				if (weight.compareTo(BigInteger.ZERO) >= 0) {
					forwardList.set(i, weight);
					backwardList.set(i, BigInteger.ZERO);
				} else {
					forwardList.set(i, BigInteger.ZERO);
					backwardList.set(i, weight.negate());
				}
			}
			return this;
		}

		/**
		 * Create a region from the current state of the builder and the given initial marking.
		 * @param initial The initial marking of the region.
		 * @return A new region corresponding to the weights that are currently in this builder.
		 */
		public Region withInitialMarking(BigInteger initial) {
			return new Region(utility, backwardList, forwardList, initial);
		}

		/**
		 * Create a region from the current state of this builder and the initial marking that a normal region
		 * would have. Please note that the normal region marking is only valid for pure region. Also, of course
		 * the region has to be cycle-consistent (going through a cycle reaches the same value again).
		 * @return a new region corresponding to the weights that are currently in this builder.
		 */
		public Region withNormalRegionInitialMarking() {
			int numEvents = utility.getNumberOfEvents();
			BigInteger initial = BigInteger.ZERO;
			for (State state : utility.getTransitionSystem().getNodes()) {
				try {
					BigInteger value = BigInteger.ZERO;
					List<BigInteger> pv = utility.getReachingParikhVector(state);
					for (int i = 0; i < numEvents; i++)
						value = value.add(pv.get(i).multiply(forwardList.get(i)
									.subtract(backwardList.get(i))));
					initial = initial.max(value.negate());
				} catch (UnreachableException e) {
					continue;
				}
			}
			return withInitialMarking(initial);
		}

		/**
		 * Create a new region builder for a pure region with the given weights.
		 * @param utility The region utility that should be used.
		 * @param vector A vector which contains the weight for each event in the order described by utility.
		 * @return A region builder containing the given weights.
		 */
		static public Builder createPure(RegionUtility utility, List<BigInteger> vector) {
			if (vector.size() != utility.getNumberOfEvents())
				throw new IllegalArgumentException("The vector must contain one entry per event");

			Builder result = new Builder(utility);
			for (int i = 0; i < vector.size(); i++) {
				BigInteger value = vector.get(i);
				if (value.compareTo(BigInteger.ZERO) > 0)
					result.forwardList.set(i, value);
				else
					result.backwardList.set(i, value.negate());
			}
			return result;
		}

		/**
		 * Create a copy of a region for a different RegionUtility. Every event used by the given region's
		 * utility must be available in the given utility!
		 * @param utility The utility to use.
		 * @param region The region to copy to the given utility.
		 * @return A new region with the same weights and initial token count as the given region, but referring
		 * to the given region utility.
		 * @throws IllegalArgumentException When the event lists are incompatible.
		 */
		static public Region copyRegionToUtility(RegionUtility utility, Region region) {
			if (utility.getEventList().equals(region.utility.getEventList()))
				return new Region(utility, region.backwardWeights, region.forwardWeights,
						region.initialMarking);

			Builder builder = new Builder(utility);
			List<String> regionEventList = region.utility.getEventList();
			for (int index = 0; index < regionEventList.size(); index++) {
				int newIndex = utility.getEventIndex(regionEventList.get(index));
				if (newIndex < 0)
					throw new IllegalArgumentException("The given region utility does not have "
							+ "event '" + regionEventList.get(index) + "'");
				builder.forwardList.set(newIndex, region.forwardWeights.get(index));
				builder.backwardList.set(newIndex, region.backwardWeights.get(index));
			}
			return builder.withInitialMarking(region.initialMarking);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
