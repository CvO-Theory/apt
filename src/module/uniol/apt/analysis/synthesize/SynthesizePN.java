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

import static org.apache.commons.collections4.iterators.PeekingIterator.peekingIterator;
import static uniol.apt.analysis.synthesize.LimitedUnfolding.ORIGINAL_STATE_KEY;
import static uniol.apt.analysis.synthesize.LimitedUnfolding.calculateLimitedUnfolding;
import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.map.LazyMap;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.bcf.BCF;
import uniol.apt.analysis.bicf.BiCF;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.cf.ConflictFree;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.fc.WeightedFreeChoice;
import uniol.apt.analysis.homogeneous.Homogeneous;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.analysis.language.LanguageEquivalence;
import uniol.apt.analysis.mf.MergeFree;
import uniol.apt.analysis.on.OutputNonBranching;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.separation.LargestK;
import uniol.apt.analysis.sideconditions.Pure;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;
import uniol.apt.analysis.synthesize.separation.Synthesizer;
import uniol.apt.util.EquivalenceRelation;
import uniol.apt.util.Pair;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Synthesize a Petri Net from a transition system.
 * @author Uli Schlachter
 */
public class SynthesizePN {
	private final TransitionSystem ts;
	private final RegionUtility utility;
	private final boolean onlyEventSeparation;
	private final Set<Region> regions;
	private final EquivalenceRelation<State> failedStateSeparationRelation = new EquivalenceRelation<>();
	private final Map<String, Set<State>> failedEventStateSeparationProblems = new HashMap<>();
	private final PNProperties properties;

	/**
	 * Builder class for creating instances of SynthesizePN. You create an instance of this class via {@link
	 * createForIsomorphicBehaviour} or {@link createForLanguageEquivalence}, give it all the state that you want
	 * and then use one of the {@link #build()} method to create a SynthesizePN instance.
	 */
	static public class Builder {
		private RegionUtility utility;
		private PNProperties properties = new PNProperties();
		private boolean quickFail = false;
		private final Set<Region> extraRegions = new HashSet<>();
		private final boolean languageEquivalence;

		/**
		 * Create a builder that targets the given RegionUtility and will synthesize up to isomorphic behaviour.
		 * @param utility The region utility whose transition system should be synthesized.
		 * @return A new builder instance
		 */
		static public Builder createForIsomorphicBehaviour(RegionUtility utility) {
			return new Builder(utility, false);
		}

		/**
		 * Create a builder that targets the given TransitionSystem and will synthesize up to isomorphic
		 * behaviour.
		 * @param ts The transition system that should be synthesized.
		 * @return A new builder instance
		 */
		static public Builder createForIsomorphicBehaviour(TransitionSystem ts) {
			return new Builder(ts, false);
		}

		/**
		 * Create a builder that targets the given RegionUtility and will synthesize up to language equivalence.
		 * The transition system that the region utility targets must already be a limited unfolding!
		 * @param utility The region utility whose transition system should be synthesized.
		 * @return A new builder instance
		 */
		static public Builder createForLanguageEquivalence(RegionUtility utility) {
			return new Builder(utility, true);
		}

		/**
		 * Create a builder that targets the given TransitionSystem and will synthesize up to language
		 * equivalence.
		 * @param ts The transition system that should be synthesized.
		 * @return A new builder instance
		 * @throws NonDeterministicException if the transition system is non-deterministic
		 * @see LimitedUnfolding#calculateLimitedUnfolding
		 */
		static public Builder createForLanguageEquivalence(TransitionSystem ts)
				throws NonDeterministicException {
			return new Builder(calculateLimitedUnfolding(ts), true);
		}

		private Builder(TransitionSystem ts, boolean languageEquivalence) {
			this(new RegionUtility(ts), languageEquivalence);
		}

		private Builder(RegionUtility utility, boolean languageEquivalence) {
			this.utility = utility;
			this.languageEquivalence = languageEquivalence;
		}

		/**
		 * Set required properties for the synthesized Petri net.
		 * @param props the properties to satisfy.
		 * @return this
		 */
		public Builder setProperties(PNProperties props) {
			this.properties = props;
			return this;
		}

		/**
		 * Get the required properties for the synthesized Petri net. The instance returned by this method may
		 * be modified directly and the modifications will apply to this builder.
		 * @return The properties that the synthesized net should currently satisfy.
		 */
		public PNProperties getProperties() {
			return this.properties;
		}

		/**
		 * Get the region utility that will be used for Petri net synthesis.
		 * @return The region utility.
		 */
		public RegionUtility getRegionUtility() {
			return this.utility;
		}

		/**
		 * Set the quick fail mode for the SynthesizePN instance. Use quick fail mode if only the result from
		 * {@link SynthesizePN#wasSuccessfullySeparated()} is interesting for you. In this case, SynthesizePN
		 * will stop after the first failure instead of going through all separation problems and trying to
		 * solve them.
		 * The quick fail mode default to false.
		 * @param qf the new value for the quick fail mode.
		 * @return this
		 */
		public Builder setQuickFail(boolean qf) {
			this.quickFail = qf;
			return this;
		}

		/**
		 * Add an already-known region to this builder. If some regions are already known, adding them can speed
		 * up the Petri net synthesis.
		 * @param r The region that should be added.
		 * @throws IllegalArgumentException If the given region belongs to a different RegionUtility
		 * @throws InvalidRegionException If the given region is not valid
		 * @return this
		 */
		public Builder addRegion(Region r) throws InvalidRegionException {
			if (!r.getRegionUtility().equals(utility))
				throw new IllegalArgumentException(
						"The given region belongs to a different region utility");
			r.checkValidRegion();
			extraRegions.add(r);
			return this;
		}

		/**
		 * Create a SynthesizePN instance for the current configuration of this builder.
		 * @return A SynthesizePN instance that synthesize the input as configured in this builder.
		 * @throws MissingLocationException if the transition system for the utility has locations for only some
		 * events
		 */
		public SynthesizePN build() throws MissingLocationException {
			if (languageEquivalence)
				return new SynthesizePN(utility, properties, true, ORIGINAL_STATE_KEY, quickFail,
						extraRegions);
			else
				return new SynthesizePN(utility, properties, false, null, quickFail, extraRegions);
		}
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @param onlyEventSeparation Should state separation be ignored? This means that two different states might get
	 * the same marking.
	 * @param stateMappingExtension An extension key that will be used to map States. All states in the input
	 * transition system must have this extension and it must refer to a State object.
	 * @param quickFail If true, stop the calculation as soon as it is known that it won't be successful. If false,
	 * try to solve all separation problems. Only if true will the list of failed problems be fully filled.
	 * @param extraRegions Some already known regions that should be re-used.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	SynthesizePN(RegionUtility utility, PNProperties properties, boolean onlyEventSeparation,
			String stateMappingExtension, boolean quickFail, Set<Region> extraRegions)
			throws MissingLocationException {
		this.ts = utility.getTransitionSystem();
		this.utility = utility;
		this.onlyEventSeparation = onlyEventSeparation;
		this.properties = properties;
		this.regions = new HashSet<>(extraRegions);

		debug("Input regions: ", regions);

		Synthesizer synthesizer = SeparationUtility.createSynthesizerInstance(utility, properties,
				onlyEventSeparation, quickFail);
		regions.addAll(synthesizer.getSeparatingRegions());

		// Handle unsolvable state separation problems
		for (Set<State> group : synthesizer.getUnsolvableStateSeparationProblems()) {
			Iterator<State> iter = group.iterator();
			if (!iter.hasNext())
				continue;
			State first = mapState(stateMappingExtension, iter.next());
			while (iter.hasNext()) {
				State next = mapState(stateMappingExtension, iter.next());
				failedStateSeparationRelation.joinClasses(first, next);
			}
		}

		// Handle unsolvable event/state separation problems
		for (Map.Entry<String, Set<State>> entry : synthesizer.getUnsolvableEventStateSeparationProblems()
				.entrySet()) {
			Set<State> states = entry.getValue();
			if (states.isEmpty())
				continue;
			Set<State> mappedStates = new HashSet<>();
			for (State state : states) {
				mappedStates.add(mapState(stateMappingExtension, state));
			}
			failedEventStateSeparationProblems.put(entry.getKey(), mappedStates);
		}
	}

	private State mapState(String stateMappingExtension, State state) {
		if (stateMappingExtension == null)
			return state;
		return (State) state.getExtension(stateMappingExtension);
	}

	/**
	 * Calculate the set of states which aren't separated by the given regions.
	 * @param states The states to separate
	 * @param regions The regions that are used for separation
	 * @return All states which have for at least one other state the same marking in all regions.
	 */
	static public Set<State> calculateUnseparatedStates(Set<State> states, Set<Region> regions) {
		Set<State> result = new HashSet<>();
		Set<Set<State>> partition = new HashSet<>();
		partition.add(new HashSet<>(states));

		debug("Calculating unseparated states");
		for (Region region : regions) {
			int discarded = 0;
			Set<Set<State>> newPartition = new HashSet<>();
			for (Set<State> family : partition) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				// Separate this family by the given region: States to which this region assigns
				// different markings are separated.
				Map<Integer, Set<State>> markings = LazyMap.lazyMap(new HashMap<Integer, Set<State>>(),
						FactoryUtils.prototypeFactory(new HashSet<State>()));
				for (State state : family) {
					try {
						markings.get(region.getMarkingForState(state)).add(state);
					} catch (UnreachableException e) {
						// SSP with any unreachable state is unsolvable
						return states;
					}
				}

				// Now collect families of not-yet-separated states
				for (Map.Entry<Integer, Set<State>> entry : markings.entrySet()) {
					if (entry.getValue().size() > 1)
						newPartition.add(entry.getValue());
					else
						discarded++;
				}
			}

			partition = newPartition;
			debugFormat("After region %s, still have %d families "
					+ "(%d resulting singular families discarded)",
					region, partition.size(), discarded);
			if (partition.isEmpty())
				break;
		}

		// All remaining states are not yet separated. Throw away the family information and return them all.
		for (Set<State> family : partition)
			result.addAll(family);

		return result;
	}

	/**
	 * Calculate definitely required regions and for each remaining separation problem all regions which solve this
	 * problem.
	 * @param ts The transition system that is being solved.
	 * @param separationProblems For each still unsolved separation problem, the set of all regions that solve it is
	 * calculated and added to this argument.
	 * @param requiredRegions Regions which are definitely required will be added to this set.
	 * @param remainingRegions Regions to choose from and if they are definitely required to move to
	 * requiredRegions.
	 * @param onlyEventSeparation Should state separation be ignored?
	 */
	static private void calculateRequiredRegionsAndProblems(TransitionSystem ts,
			Set<Set<Region>> separationProblems, Set<Region> requiredRegions, Set<Region> remainingRegions,
			boolean onlyEventSeparation) {
		// Event separation
		problemLoop:
		for (Pair<State, String> problem : new EventStateSeparationProblems(ts)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			State state = problem.getFirst();
			String event = problem.getSecond();
			// Does one of our required regions already solve ESSP? If so, skip
			for (Region r : requiredRegions) {
				if (SeparationUtility.isSeparatingRegion(r, state, event))
					continue problemLoop;
			}
			// Calculate which of the remaining regions solves this ESSP instance
			Set<Region> sep = new HashSet<>();
			for (Region r : remainingRegions) {
				if (SeparationUtility.isSeparatingRegion(r, state, event))
					sep.add(r);
			}
			if (sep.size() == 1) {
				// If only one region solves this problem, that region is required
				Region r = sep.iterator().next();
				requiredRegions.add(r);
				remainingRegions.remove(r);
			} else if (!sep.isEmpty())
				separationProblems.add(sep);
		}

		if (onlyEventSeparation)
			return;

		// State separation
		// All regions which are already separated by our requiredRegions can be skipped, so use
		// calculateUnseparatedStates() to look at the rest.
		Set<State> remainingStates = new HashSet<>(calculateUnseparatedStates(ts.getNodes(), requiredRegions));
		Iterator<State> iterator = remainingStates.iterator();
		while (iterator.hasNext()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			State state = iterator.next();
			iterator.remove();

			innerStatesLoop:
			for (State otherState : remainingStates) {
				// Does one of our required regions already solve SSP? If so, skip
				for (Region r : requiredRegions) {
					if (SeparationUtility.isSeparatingRegion(r, state, otherState))
						continue innerStatesLoop;
				}
				// Calculate which of the remaining regions solves SSP for this instance
				Set<Region> sep = new HashSet<>();
				for (Region r : remainingRegions) {
					if (SeparationUtility.isSeparatingRegion(r, state, otherState))
						sep.add(r);
				}
				if (sep.size() == 1) {
					// If only one region solves this problem, that region is required
					Region r = sep.iterator().next();
					requiredRegions.add(r);
					remainingRegions.remove(r);
				} else if (!sep.isEmpty())
					separationProblems.add(sep);
			}
		}
	}

	/**
	 * Try to eliminate redundant regions.
	 * @param ts The transition system that is being solved.
	 * @param requiredRegions Set of regions to minimize. Redundant regions will be removed.
	 * @param onlyEventSeparation Should state separation be ignored?
	 */
	static public void minimizeRegions(TransitionSystem ts, Set<Region> requiredRegions,
			boolean onlyEventSeparation) {
		int numInputRegions = requiredRegions.size();
		Set<Region> remainingRegions = new HashSet<>(requiredRegions);
		requiredRegions.clear();

		// Build a list where each entry is generated from a separation problem and contains all regions that
		// solve this problem.
		Set<Set<Region>> separationProblems = new HashSet<>();
		calculateRequiredRegionsAndProblems(ts, separationProblems, requiredRegions, remainingRegions,
				onlyEventSeparation);

		debug("Required regions after first pass:");
		debug(requiredRegions);
		debug("List of regions that solve each remaining separation problem:");
		debug(separationProblems);

		// Now go through all remaining problems again
		for (Set<Region> problem : separationProblems) {
			// If none of our required regions solve this problem, we pick one arbitrarily that does
			if (Collections.disjoint(requiredRegions, problem))
				requiredRegions.add(problem.iterator().next());
		}

		debug("List of required regions:");
		debug(requiredRegions);
		debugFormat("Picked %d required regions out of %d input regions",
				requiredRegions.size(), numInputRegions);
	}

	/**
	 * Get all separating regions which were calculated
	 * @return All separating regions found.
	 */
	public Set<Region> getSeparatingRegions() {
		return Collections.unmodifiableSet(regions);
	}

	/**
	 * Check if the transition system was successfully separated.
	 * @return True if the transition was successfully separated.
	 */
	public boolean wasSuccessfullySeparated() {
		return failedStateSeparationRelation.isEmpty() && failedEventStateSeparationProblems.isEmpty();
	}

	/**
	 * Get all the state separation problems which could not be solved.
	 * @return A set containing sets of two states which cannot be differentiated by any region.
	 */
	public Collection<Set<State>> getFailedStateSeparationProblems() {
		return Collections.unmodifiableCollection(failedStateSeparationRelation);
	}

	/**
	 * Get all the event/state separation problems which could not be solved.
	 * @return A set containing instances of the event/state separation problem.
	 */
	public Map<String, Set<State>> getFailedEventStateSeparationProblems() {
		// This would still allow modifying the entries of the map. Whatever...
		return Collections.unmodifiableMap(failedEventStateSeparationProblems);
	}

	/**
	 * Get the RegionUtility instance used for synthesis.
	 * @return The region utility.
	 */
	public RegionUtility getUtility() {
		return utility;
	}

	/**
	 * Get the properties that were requested from synthesis.
	 * @return The properties.
	 */
	public PNProperties getProperties() {
		return properties;
	}

	/**
	 * Was only event/state separation solved or also state separation?
	 * @return true if only event/state separation was solved
	 */
	public boolean onlyEventSeparation() {
		return onlyEventSeparation;
	}

	/**
	 * Check if the PetriNet is a distributed implementation
	 * @param utility The region utility that defines the required distribution
	 * @param properties The properties instance to use; this is needed to handle ON correctly.
	 * @param pn The PetriNet to check
	 * @return true if the pn is suitably distributed.
	 */
	static public boolean isDistributedImplementation(RegionUtility utility, PNProperties properties, PetriNet pn) {
		String[] locationMap;
		try {
			locationMap = SeparationUtility.getLocationMap(utility, properties);
		} catch (MissingLocationException e) {
			debug("Couldn't get location map");
			return false;
		}

		// All transitions that consume tokens from the same place must have the same location.
		for (Place p : pn.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			String location = null;
			debug("Examining preset of place ", p, " for obeying the required distribution");
			for (Transition t : p.getPostset()) {
				int event = utility.getEventIndex(t.getLabel());
				if (locationMap[event] == null)
					continue;
				if (location == null) {
					location = locationMap[event];
					debug("Transition ", t, " sets location to ", location);
				} else if (!location.equals(locationMap[event])) {
					debug("Transition ", t, " would set location to ", locationMap[event],
							", but this conflicts with earlier location");
					debug("PN is not a distributed implementation!");
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Check if the PetriNet is a generalized T-Net. In a T-Net, every place has a preset and postset with at most
	 * one entry. In a generalized T-Net, arc weights are allowed.
	 * @param pn The Petri net to check
	 * @return true if the pn is a generalized T-Net.
	 */
	static public boolean isGeneralizedTNet(PetriNet pn) {
		for (Place place : pn.getPlaces()) {
			// is here a merge?
			if (place.getPreset().size() > 1) {
				debug("T-Net check: There is a merge at ", place.getId());
				return false;
			}
			// is here a conflict?
			if (place.getPostset().size() > 1) {
				debug("T-Net check: There is a conflict at ", place.getId());
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the PetriNet is a generalized marked graph. In a marked graph, every place has a preset and postset
	 * with exacly one entry. In a generalized marked graph, arc weights are allowed.
	 * @param pn The Petri net to check
	 * @return true if the pn is a generalized marked graph.
	 */
	static public boolean isGeneralizedMarkedGraph(PetriNet pn) {
		for (Place place : pn.getPlaces()) {
			if (place.getPreset().size() != 1) {
				debug("marked graph check: Preset of ", place.getId(),
						" doesn't have exactly one entry");
				return false;
			}
			if (place.getPostset().size() != 1) {
				debug("marked graph check: Postset of ", place.getId(),
						" doesn't have exactly one entry");
				return false;
			}
		}
		return true;
	}

	// Assert that all regions are valid; this is done in an extra function so that we do not even iterate over all
	// regions when assertions are disabled.
	static private boolean regionsAreValid(Collection<Region> regions) {
		for (Region region : regions)
			try {
				region.checkValidRegion();
			} catch (InvalidRegionException e) {
				throw new AssertionError(e);
			}
		return true;
	}

	/**
	 * Synthesize a Petri Net from the separating regions that were calculated.
	 * @return The synthesized PetriNet
	 */
	public PetriNet synthesizePetriNet() {
		if (!wasSuccessfullySeparated())
			return null;
		return synthesizePetriNet(regions);
	}

	/**
	 * Synthesize a Petri net from the given regions, checking that it is indeed a valid solution.
	 * @param regions The regions that should be used for synthesis.
	 * @return The synthesized PetriNet
	 */
	public PetriNet synthesizePetriNet(Set<Region> regions) {
		PetriNet pn = synthesizePetriNet(utility, regions);

		// Test if all regions are valid
		assert regionsAreValid(regions);

		// Test if the synthesized PN really satisfies all the properties that it should
		assert !properties.isPure() || Pure.checkPure(pn) : regions;
		assert !properties.isPlain() || new Plain().checkPlain(pn) : regions;
		assert !properties.isTNet() || isGeneralizedTNet(pn) : regions;
		assert !properties.isMarkedGraph() || isGeneralizedMarkedGraph(pn) : regions;
		assert !properties.isKBounded() || Bounded.checkBounded(pn).k <= properties.getKForKBounded() : regions;
		assert !properties.isOutputNonbranching() || new OutputNonBranching(pn).check() : regions;
		assert !properties.isMergeFree() || new MergeFree().check(pn) : regions;
		try {
			assert !properties.isConflictFree() || new ConflictFree(pn).check() : regions;
		} catch (PreconditionFailedException e) {
			assert false : regions;
		}
		assert !properties.isHomogeneous() || new Homogeneous().check(pn) == null : regions;
		assert !properties.isEqualConflict() || new Homogeneous().check(pn) == null : regions;
		assert !properties.isEqualConflict() || new WeightedFreeChoice().check(pn) : regions;

		try {
			assert !properties.isBehaviourallyConflictFree() || new BCF().check(pn) == null : regions;
			assert !properties.isBinaryConflictFree() || new BiCF().check(pn) == null : regions;

			if (!onlyEventSeparation)
				// The resulting PN should always have a reachability graph isomorphic to the ts
				assert new IsomorphismLogic(CoverabilityGraph.get(pn).toReachabilityLTS(), ts, true)
					.isIsomorphic() : regions;
			else
				// The resulting PN should be language-equivalent to what we started with
				assert LanguageEquivalence.checkLanguageEquivalence(
						CoverabilityGraph.get(pn).toReachabilityLTS(), ts) == null
					: regions;
		} catch (UnboundedException e) {
			assert false : regions;
		}

		assert isDistributedImplementation(utility, properties, pn) : regions;
		assert new LargestK(pn).computeLargestK() % properties.getKForKMarking() == 0
			: properties.getKForKMarking() + "-marking: " + regions;

		return pn;
	}

	/**
	 * Synthesize a Petri Net from the given regions.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @param regions The regions that should be used for synthesis.
	 * @return The synthesized PetriNet
	 */
	public static PetriNet synthesizePetriNet(RegionUtility utility, Set<Region> regions) {
		PetriNet pn = new PetriNet();

		debug("Synthesizing PetriNet from these regions:");
		debug(regions);

		// First generate the transitions so that isolated transitions do get created
		for (String event : utility.getEventList())
			pn.createTransition(event);

		for (Region region : regions) {
			Place place = pn.createPlace();
			place.setInitialToken(bigIntToLong(region.getInitialMarking()));
			place.putExtension(Region.class.getName(), region);

			for (String event : region.getRegionUtility().getEventList()) {
				Transition transition = pn.getTransition(event);
				int backward = bigIntToInt(region.getBackwardWeight(event));
				assert backward >= 0;
				if (backward > 0)
					pn.createFlow(place, transition, backward);

				int forward = bigIntToInt(region.getForwardWeight(event));
				assert forward >= 0;
				if (forward > 0)
					pn.createFlow(transition, place, forward);
			}
		}

		return pn;
	}

	private static long bigIntToLong(BigInteger value) {
		if (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
				value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
			throw new ArithmeticException("Cannot represent value as long: " + value);
		return value.longValue();
	}

	private static int bigIntToInt(BigInteger value) {
		if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ||
				value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0)
			throw new ArithmeticException("Cannot represent value as int: " + value);
		return value.intValue();
	}

	/**
	 * Iterable for iterating over all event/state separation problems of a transition system. An event/state
	 * separation problem consists of a state and an event that is not enabled in this state.
	 */
	static public class EventStateSeparationProblems implements Iterable<Pair<State, String>> {
		private final TransitionSystem ts;

		/**
		 * Construct a new instance of this iterable for the given transition system.
		 * @param ts The transition system whose ESSP instances should be returned.
		 */
		public EventStateSeparationProblems(TransitionSystem ts) {
			this.ts = ts;
		}

		@Override
		public Iterator<Pair<State, String>> iterator() {
			return new Iterator<Pair<State, String>>() {
				private Iterator<State> states = ts.getNodes().iterator();
				private State currentState = null;
				private PeekingIterator<String> alphabet = null;

				@Override
				public boolean hasNext() {
					while (true) {
						if (alphabet == null || !alphabet.hasNext()) {
							if (!states.hasNext())
								return false;

							currentState = states.next();
							alphabet = peekingIterator(ts.getAlphabet().iterator());
						} else {
							if (!SeparationUtility.isEventEnabled(currentState,
										alphabet.peek()))
								return true;
							alphabet.next();
						}
					}
				}

				@Override
				public Pair<State, String> next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return new Pair<>(currentState, alphabet.next());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
