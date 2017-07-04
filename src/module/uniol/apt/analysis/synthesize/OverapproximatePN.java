/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

import static uniol.apt.analysis.synthesize.separation.SeparationUtility.getLocationMap;
import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.separation.UnsupportedPNPropertiesException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Calculate the minimal Petri Net over-approximation of a transition system.
 * @author Uli Schlachter
 */
public class OverapproximatePN {
	private OverapproximatePN() {
	}

	// For some properties we cannot guarantee that the algorithm always terminates (but if it terminates, its
	// result should still be correct). This is the list of properties which does work.
	static private PNProperties supportedProperties = new PNProperties()
		.requireKBounded(0).setPure(true).setPlain(true).setTNet(true).setMarkedGraph(true);

	static private void checkSupported(TransitionSystem ts, PNProperties properties)
			throws MissingLocationException, UnsupportedPNPropertiesException {
		if (!supportedProperties.containsAll(properties))
			throw new UnsupportedPNPropertiesException("Some of the requested properties "
					+ "are not supported for over-approximation; requested: "
					+ properties + "; supported: " + supportedProperties);
		String[] locationMap = getLocationMap(new RegionUtility(ts), properties);
		if (Collections.frequency(Arrays.asList(locationMap), null) != locationMap.length)
			throw new UnsupportedPNPropertiesException("Overapproximation is not possible with locations");
	}

	/**
	 * Calculate the minimal Petri Net over-approximation of a transition system.
	 * @param ts The transition system to over-approximate.
	 * @param properties The properties that the resulting Petri net should satisfy.
	 * @return A Petri net being a minimal over-approximation of the input.
	 * @throws MissingLocationException If some states of the input have a location and others do not.
	 * @throws UnsupportedPNPropertiesException If overapproximation is not possible for the given input.
	 */
	static public PetriNet overapproximate(TransitionSystem ts, PNProperties properties)
			throws MissingLocationException, UnsupportedPNPropertiesException {
		checkSupported(ts, properties);

		ts = getReachablePart(ts);
		SynthesizePN synthesize = null;
		int iterations = 0;

		do {
			iterations++;
			debug();
			debugFormat("Beginning iteration %d", iterations);

			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			SynthesizePN.Builder builder = SynthesizePN.Builder
				.createForIsomorphicBehaviour(ts)
				.setProperties(properties);

			// Add already-calculated regions so that they do not have to be calculated again
			if (synthesize != null)
				copyExistingRegions(builder, synthesize, properties);

			synthesize = builder.build();
			ts = handleSeparationFailures(ts, synthesize.getFailedStateSeparationProblems(),
					synthesize.getFailedEventStateSeparationProblems());
		} while (!synthesize.wasSuccessfullySeparated());

		return synthesize.synthesizePetriNet();
	}

	static private TransitionSystem getReachablePart(TransitionSystem ts) {
		TransitionSystem result = new TransitionSystem();

		// First, copy all reachable states
		State stateToHandle = ts.getInitialState();
		Set<State> handled = new HashSet<>(Collections.singleton(stateToHandle));
		Queue<State> todo = new ArrayDeque<>();
		while (stateToHandle != null) {
			result.createState(stateToHandle);
			for (State next : stateToHandle.getPostsetNodes())
				if (handled.add(next))
					todo.add(next);

			stateToHandle = todo.poll();
		}

		// Then, copy all reachable arcs
		stateToHandle = ts.getInitialState();
		handled = new HashSet<>(Collections.singleton(stateToHandle));
		todo = new ArrayDeque<>();
		while (stateToHandle != null) {
			for (Arc arc : stateToHandle.getPostsetEdges()) {
				State next = arc.getTarget();
				result.createArc(arc);
				if (handled.add(next))
					todo.add(next);
			}

			stateToHandle = todo.poll();
		}

		result.setInitialState(ts.getInitialState());

		return result;
	}

	static private void copyExistingRegions(SynthesizePN.Builder builder, SynthesizePN synthesize,
			PNProperties properties) {
		RegionUtility utility = builder.getRegionUtility();
		for (Region region : synthesize.getSeparatingRegions()) {
			Region newRegion = Region.Builder.copyRegionToUtility(utility, region);

			// The only difference should be in the used RegionUtility, which should not influence
			// the result of toString().
			assert region.toString().equals(newRegion.toString())
				: region.toString() + " = " + newRegion.toString();

			// For every k-bounded region there is a complementary region. If this region exceeds the bound
			// k, then the complementary region necessarily has a negative token count in the same state.
			// Thus, this cannot happen (tm).
			assert !properties.isKBounded() || isKBounded(newRegion, properties.getKForKBounded()) : region;

			try {
				builder.addRegion(newRegion);
			} catch (InvalidRegionException e) {
				// This cannot happen; it was a region previously and we only changed
				// things in a way that this should still be a region.
				throw new AssertionError(e);
			}
		}
	}

	static private boolean isKBounded(Region region, int k) {
		TransitionSystem ts = region.getRegionUtility().getTransitionSystem();
		BigInteger bigK = BigInteger.valueOf(k);
		for (State state : ts.getNodes()) {
			try {
				BigInteger marking = region.getMarkingForState(state);
				if (marking.compareTo(bigK) > 0)
					return false;
			} catch (UnreachableException e) {
				// Previous steps in the algorithm should already have handled unreachable states!
				throw new AssertionError(e);
			}
		}
		return true;
	}

	/**
	 * Create a new transition system from the given system that will not have the given separation failures.
	 * @param ts The transition system whose synthesis was attempted.
	 * @param failedSSP A collection of sets of unseparable states.
	 * @param failedESSP A description of event/state separation failures.
	 * @return A transition system without the given separation failures.
	 */
	static public TransitionSystem handleSeparationFailures(TransitionSystem ts, Collection<Set<State>> failedSSP,
			Map<String, Set<State>> failedESSP) {
		debugFormat("Creating new TS to handle SSP failures %s and ESSP failures %s", failedSSP, failedESSP);
		TransitionSystem result = ts;
		Map<State, State> oldToNewStateMap = null;

		if (!failedSSP.isEmpty()) {
			result = new TransitionSystem();
			oldToNewStateMap = new HashMap<>();

			// First mark the states that we have to modify as "reserved"
			for (Set<State> equivalenceClass : failedSSP)
				for (State state : equivalenceClass)
					oldToNewStateMap.put(state, null);

			// Then copy over all states which we do not modify
			for (State state : ts.getNodes()) {
				if (oldToNewStateMap.containsKey(state))
					continue;
				State newState = result.createState(state);
				oldToNewStateMap.put(state, newState);
			}

			// Then handle states which have to be merged
			for (Set<State> equivalenceClass : failedSSP) {
				State newState = result.createState();
				for (State state : equivalenceClass)
					oldToNewStateMap.put(state, newState);
			}

			result.setInitialState(oldToNewStateMap.get(ts.getInitialState()));

			// Now that all states are created, handle arcs
			for (Arc arc : ts.getEdges()) {
				State source = oldToNewStateMap.get(arc.getSource());
				State target = oldToNewStateMap.get(arc.getTarget());
				String label = arc.getLabel();
				try {
					result.createArc(source, target, label);
				} catch (ArcExistsException e) {
					// This can happen when the state-mapping merges two states together which both
					// have an outgoing edge with the same label.
				}
			}
		}

		if (!failedESSP.isEmpty()) {
			// For each failed ESSP instance (s,e), add an arc (s,e,s') where s' is a newly created state
			for (Map.Entry<String, Set<State>> entry : failedESSP.entrySet()) {
				String label = entry.getKey();
				for (State state : entry.getValue()) {
					State stateToModify;
					if (oldToNewStateMap == null)
						stateToModify = state;
					else
						stateToModify = oldToNewStateMap.get(state);
					if (stateToModify.getPostsetNodesByLabel(label).isEmpty())
						result.createArc(stateToModify, result.createState(), label);
					else
						// This can happen if the state also had an SSP failure and was merged
						// with some state which already has this event enabled.
						assert !failedSSP.isEmpty();
				}
			}
		}

		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
