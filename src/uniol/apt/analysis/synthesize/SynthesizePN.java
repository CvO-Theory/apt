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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.FactoryUtils;
import org.apache.commons.collections4.map.LazyMap;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.cf.ConflictFree;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.analysis.language.LanguageEquivalence;
import uniol.apt.analysis.on.OutputNonBranching;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.sideconditions.Pure;
import uniol.apt.analysis.synthesize.separation.Separation;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;
import uniol.apt.analysis.tnet.TNet;
import uniol.apt.util.EquivalenceRelation;

import static uniol.apt.analysis.synthesize.LimitedUnfolding.ORIGINAL_STATE_KEY;
import static uniol.apt.analysis.synthesize.LimitedUnfolding.calculateLimitedUnfolding;
import static uniol.apt.util.DebugUtil.debug;

/**
 * Synthesize a Petri Net from a transition system.
 * @author Uli Schlachter
 */
public class SynthesizePN {
	private final TransitionSystem ts;
	private final RegionUtility utility;
	private final boolean onlyEventSeparation;
	private final Set<Region> regions = new HashSet<>();
	private final EquivalenceRelation<State> failedStateSeparationRelation = new EquivalenceRelation<>();
	private final Map<String, Set<State>> failedEventStateSeparationProblems = new HashMap<>();
	private final PNProperties properties;
	private final Separation separation;
	private final String stateMappingExtension;

	/**
	 * Create a SynthesizePN instance that synthesizes a given transition system up to language equivalence.
	 * Internally, this function creates a limited unfolding. Please note that this means that any references to
	 * states in the resulting SynthesizePN instance refers to states in the unfolding instead of the original ts.
	 * @param ts The transition system to synthesize.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @return A synthesizePN instance that synthesizes the input of to language equivalence.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 * @throws NonDeterministicException if the transition system is non-deterministic
	 * @see LimitedUnfolding#calculateLimitedUnfolding
	 */
	static public SynthesizePN createUpToLanguageEquivalence(TransitionSystem ts, PNProperties properties)
			throws MissingLocationException, NonDeterministicException {
		return new SynthesizePN(new RegionUtility(calculateLimitedUnfolding(ts)), properties, true,
				ORIGINAL_STATE_KEY);
	}

	/**
	 * Create a SynthesizePN instance that synthesizes a given transition system up to language equivalence.
	 * Internally, this function creates a limited unfolding.
	 * @param ts The transition system to synthesize.
	 * @return A synthesizePN instance that synthesizes the input of to language equivalence.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 * @throws NonDeterministicException if the transition system is non-deterministic
	 * @see LimitedUnfolding#calculateLimitedUnfolding
	 */
	static public SynthesizePN createUpToLanguageEquivalence(TransitionSystem ts)
			throws MissingLocationException, NonDeterministicException {
		return createUpToLanguageEquivalence(ts, new PNProperties());
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @param onlyEventSeparation Should state separation be ignored? This means that two different states might get
	 * the same marking.
	 * @param stateMappingExtension An extension key that will be used to map States. All states in the input
	 * transition system must have this extension and it must refer to a State object.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	private SynthesizePN(RegionUtility utility, PNProperties properties, boolean onlyEventSeparation,
			String stateMappingExtension) throws MissingLocationException {
		this.ts = utility.getTransitionSystem();
		this.utility = utility;
		this.onlyEventSeparation = onlyEventSeparation;
		this.properties = properties;
		this.separation = SeparationUtility.createSeparationInstance(utility, properties);
		this.stateMappingExtension = stateMappingExtension;

		debug("Region basis: ", utility.getRegionBasis());

		// ESSP calculates new regions while SSP only choses regions from the basis. Solve ESSP first since the
		// calculated regions may also solve SSP and thus we get less places in the resulting net.
		debug();
		debug("Solving event-state separation");
		solveEventStateSeparation();

		debug();
		debug("Solving state separation");
		solveStateSeparation();

		debug();
		debug("Minimizing regions");
		minimizeRegions(utility, regions, onlyEventSeparation);

		debug();
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @param onlyEventSeparation Should state separation be ignored? This means that two different states might get
	 * the same marking.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	SynthesizePN(RegionUtility utility, PNProperties properties, boolean onlyEventSeparation)
			throws MissingLocationException {
		this(utility, properties, onlyEventSeparation, null);
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	public SynthesizePN(RegionUtility utility, PNProperties properties) throws MissingLocationException {
		this(utility, properties, false);
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param utility An instance of RegionUtility for the requested transition system.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	public SynthesizePN(RegionUtility utility) throws MissingLocationException {
		this(utility, new PNProperties());
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param ts The transition system to synthesize.
	 * @param properties Properties that the synthesized Petri net should satisfy.
	 * @throws MissingLocationException if the transition system has locations for only some events
	 */
	public SynthesizePN(TransitionSystem ts, PNProperties properties) throws MissingLocationException {
		this(new RegionUtility(ts), properties);
	}

	/**
	 * Synthesize a Petri Net which generates the given transition system.
	 * @param ts The transition system to synthesize.
	 * @throws MissingLocationException if the transition system has locations for only some events
	 */
	public SynthesizePN(TransitionSystem ts) throws MissingLocationException {
		this(ts, new PNProperties());
	}

	private State mapState(State state) {
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
				// Separate this family by the given region: States to which this region assigns
				// different markings are separated.
				Map<Integer, Set<State>> markings = LazyMap.lazyMap(new HashMap<Integer, Set<State>>(),
						FactoryUtils.prototypeFactory(new HashSet<State>()));
				for (State state : family) {
					try {
						markings.get(region.getMarkingForState(state)).add(state);
					} catch (UnreachableException e) {
						// Unreachable states cannot be separated, so add this to result
						result.add(state);
						continue;
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
			debug("After region ", region, ", still have ", partition.size(), " families (",
					discarded, " resulting singular families discarded)");
		}

		// All remaining states are not yet separated. Throw away the family information and return them all.
		for (Set<State> family : partition)
			result.addAll(family);

		return result;
	}

	/**
	 * Solve all instances of the state separation problem (SSP).
	 */
	private void solveStateSeparation() {
		if (onlyEventSeparation)
			return;

		Set<State> remainingStates = new HashSet<>(calculateUnseparatedStates(
					utility.getTransitionSystem().getNodes(), regions));
		Iterator<State> iterator = remainingStates.iterator();
		while (iterator.hasNext()) {
			State state = iterator.next();
			iterator.remove();

			for (State otherState : remainingStates) {
				debug("Trying to separate ", state,  " from ", otherState);
				Region r = null;
				for (Region region : regions)
					if (SeparationUtility.isSeparatingRegion(utility, region, state, otherState)) {
						r = region;
						break;
					}
				if (r != null) {
					debug("Found region ", r);
					continue;
				}

				r = separation.calculateSeparatingRegion(state, otherState);
				if (r == null) {
					failedStateSeparationRelation.joinClasses(mapState(state),
							mapState(otherState));
					debug("Failure!");
				} else {
					debug("Calculated region ", r);
					regions.add(r);
				}
			}
		}
	}

	/**
	 * Solve all instances of the event/state separation problem (ESSP).
	 */
	private void solveEventStateSeparation() {
		Map<String, Set<State>> failedProblems = LazyMap.lazyMap(failedEventStateSeparationProblems,
				FactoryUtils.prototypeFactory(new HashSet<State>()));
		for (State state : ts.getNodes())
			for (String event : ts.getAlphabet()) {
				if (!SeparationUtility.isEventEnabled(state, event)) {
					debug("Trying to separate ", state, " from event '", event, "'");
					Region r = null;
					for (Region region : regions)
						if (SeparationUtility.isSeparatingRegion(utility, region,
									state, event)) {
							r = region;
							break;
						}
					if (r != null) {
						debug("Found region ", r);
						continue;
					}

					r = separation.calculateSeparatingRegion(state, event);
					if (r == null) {
						failedProblems.get(event).add(mapState(state));
						debug("Failure!");
					} else {
						debug("Calculated region ", r);
						regions.add(r);
					}
				}
			}
	}

	/**
	 * Calculate definitely required regions and for each remaining separation problem all regions which solve this
	 * problem.
	 * @param utility The region utility on which this function should work.
	 * @param separationProblems For each still unsolved separation problem, the set of all regions that solve it is
	 * calculated and added to this argument.
	 * @param requiredRegions Regions which are definitely required will be added to this set.
	 * @param remainingRegions Regions to choose from and if they are definitely required to move to
	 * requiredRegions.
	 * @param onlyEventSeparation Should state separation be ignored?
	 */
	static private void calculateRequiredRegionsAndProblems(RegionUtility utility,
			Set<Set<Region>> separationProblems, Set<Region> requiredRegions, Set<Region> remainingRegions,
			boolean onlyEventSeparation) {
		TransitionSystem ts = utility.getTransitionSystem();
		// Event separation
		for (State state : ts.getNodes()) {
			innerStatesLoop:
			for (String event : ts.getAlphabet()) {
				if (!SeparationUtility.isEventEnabled(state, event)) {
					// Does one of our required regions already solve ESSP? If so, skip
					for (Region r : requiredRegions) {
						if (SeparationUtility.isSeparatingRegion(utility, r, state, event))
							continue innerStatesLoop;
					}
					// Calculate which of the remaining regions solves this ESSP instance
					Set<Region> sep = new HashSet<>();
					for (Region r : remainingRegions) {
						if (SeparationUtility.isSeparatingRegion(utility, r, state, event))
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

		if (onlyEventSeparation)
			return;

		// State separation
		// All regions which are already separated by our requiredRegions can be skipped, so use
		// calculateUnseparatedStates() to look at the rest.
		Set<State> remainingStates = new HashSet<>(calculateUnseparatedStates(
					utility.getTransitionSystem().getNodes(), requiredRegions));
		Iterator<State> iterator = remainingStates.iterator();
		while (iterator.hasNext()) {
			State state = iterator.next();
			iterator.remove();

			innerStatesLoop:
			for (State otherState : remainingStates) {
				// Does one of our required regions already solve SSP? If so, skip
				for (Region r : requiredRegions) {
					if (SeparationUtility.isSeparatingRegion(utility, r, state, otherState))
						continue innerStatesLoop;
				}
				// Calculate which of the remaining regions solves SSP for this instance
				Set<Region> sep = new HashSet<>();
				for (Region r : remainingRegions) {
					if (SeparationUtility.isSeparatingRegion(utility, r, state, otherState))
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
	 * @param utility The region utility on which this function should work.
	 * @param requiredRegions Set of regions to minimize. Redundant regions will be removed.
	 * @param onlyEventSeparation Should state separation be ignored?
	 */
	static public void minimizeRegions(RegionUtility utility, Set<Region> requiredRegions,
			boolean onlyEventSeparation) {
		Set<Region> allRegions = Collections.unmodifiableSet(new HashSet<>(requiredRegions));
		Set<Region> remainingRegions = new HashSet<>(requiredRegions);
		requiredRegions.clear();

		// Build a list where each entry is generated from a separation problem and contains all regions that
		// solve this problem.
		Set<Set<Region>> separationProblems = new HashSet<>();
		calculateRequiredRegionsAndProblems(utility, separationProblems, requiredRegions, remainingRegions,
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
		debug("Picked ", requiredRegions.size(), " required regions out of ",
				allRegions.size(), " input regions");
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

	public RegionUtility getUtility() {
		return utility;
	}

	/**
	 * Check if the PetriNet is a distributed implementation
	 * @param utility The region utility that defines the required distribution
	 * @param pn The PetriNet to check
	 * @return true if the pn is suitably distributed.
	 */
	static public boolean isDistributedImplementation(RegionUtility utility, PetriNet pn) {
		String[] locationMap;
		try {
			locationMap = SeparationUtility.getLocationMap(utility);
		} catch (MissingLocationException e) {
			debug("Couldn't get location map");
			return false;
		}

		// All transitions that consume tokens from the same place must have the same location.
		for (Place p : pn.getPlaces()) {
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
	 * Synthesize a Petri Net from the separating regions that were calculated.
	 * @return The synthesized PetriNet
	 */
	public PetriNet synthesizePetriNet() {
		if (!wasSuccessfullySeparated())
			return null;
		PetriNet pn = synthesizePetriNet(utility, regions);

		// Test if the synthesized PN really satisfies all the properties that it should
		if (properties.isPure())
			assert Pure.checkPure(pn) : regions;
		if (properties.isPlain())
			assert new Plain().checkPlain(pn) : regions;
		if (properties.isTNet())
			try {
				assert new TNet(pn).testPlainTNet() : regions;
			} catch (PreconditionFailedException e) {
				assert false : regions;
			}
		if (properties.isKBounded())
			assert new Bounded().checkBounded(pn).k <= properties.getKForKBoundedness() : regions;
		if (properties.isOutputNonbranching())
			assert new OutputNonBranching(pn).check() : regions;
		if (properties.isConflictFree())
			try {
				assert new ConflictFree(pn).check() : regions;
			} catch (PreconditionFailedException e) {
				assert false : regions;
			}

		try {
			if (!onlyEventSeparation)
				// The resulting PN should always have a reachability graph isomorphic to the ts
				assert new IsomorphismLogic(CoverabilityGraph.get(pn).toReachabilityLTS(), ts, true)
					.isIsomorphic() : regions;
			else
				// The resulting PN should be language-equivalent to what we started with
				assert LanguageEquivalence.checkLanguageEquivalence(
						CoverabilityGraph.get(pn).toReachabilityLTS(), ts, false).isEmpty()
					: regions;
		} catch (UnboundedException e) {
			assert false : regions;
		}

		assert isDistributedImplementation(utility, pn) : regions;

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
			place.setInitialToken(region.getInitialMarking());
			place.putExtension(Region.class.getName(), region);

			for (String event : region.getRegionUtility().getEventList()) {
				Transition transition = pn.getTransition(event);
				int backward = region.getBackwardWeight(event);
				assert backward >= 0;
				if (backward > 0)
					pn.createFlow(place, transition, backward);

				int forward = region.getForwardWeight(event);
				assert forward >= 0;
				if (forward > 0)
					pn.createFlow(transition, place, forward);
			}
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
