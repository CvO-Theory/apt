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

package uniol.apt.analysis.synthesize.separation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.CycleSearchViaChords;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.reversible.ReversibleTS;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class quickly finds solutions to separation problems for TS satisfying some properties. A marked graph is a
 * plain t-net Petri net. This implementation is based on the theory presented in "Characterisation of the State Spaces
 * of Marked Graph Petri Nets" by Eike Best and Raymond Devillers.
 * @author Uli Schlachter
 */
class MarkedGraphSeparation implements Separation, Synthesizer {
	private final RegionUtility utility;
	private final List<Region> regions;
	private final Map<Integer, List<BigInteger>> reachedOnlyByPVs;

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public MarkedGraphSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		TransitionSystem ts = utility.getTransitionSystem();

		this.utility = utility;

		// Check if only supported properties are requested. Ignore the locationMap since t-nets satisfy any
		// location map.
		PNProperties supported = new PNProperties()
			.setPure(true)
			.setPlain(true)
			.setTNet(true)
			.setMarkedGraph(true)
			.setOutputNonbranching(true);
		if (!supported.containsAll(properties))
			throw new UnsupportedPNPropertiesException();

		// Check all of our preconditions:
		// reversible, deterministic, totally reachable, persistent, backward persistent.

		// The following also checks: Deterministic, totally reachable, persistent.
		// Check if all small cycles have a Parikh vector of all ones.
		try {
			for (ParikhVector pv : new CycleSearchViaChords().searchCycles(ts)) {
				for (String label : ts.getAlphabet())
					if (pv.get(label) != 1)
						throw new UnsupportedPNPropertiesException(
								"Not all small cycles satisfy P1, e.g. " + pv);
			}
		} catch (PreconditionFailedException e) {
			throw new UnsupportedPNPropertiesException(e);
		}
		// Check remaining preconditions
		if (!new ReversibleTS(ts).isReversible())
			throw new UnsupportedPNPropertiesException("TS " + ts.getName() + " is not reversible");
		if (!new PersistentTS(ts, true).isPersistent())
			throw new UnsupportedPNPropertiesException("TS " + ts.getName() + " is not backwards persistent");

		reachedOnlyByPVs = calculateParikhVectorOfUniqueReachedState(utility);
		regions = calculateRegions();
	}

	/**
	 * Calculate the parikh vectors of the unique states that are only reached by a single event.
	 * @param utility region utility to use
	 * @return A map from event index to the parikh vector of the state for that event
	 */
	static public Map<Integer, List<BigInteger>> calculateParikhVectorOfUniqueReachedState(RegionUtility utility) {
		Map<Integer, List<BigInteger>> result = new HashMap<>();
		stateLoop:
		for (State state : utility.getTransitionSystem().getNodes()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			Set<Arc> arcs = state.getPresetEdges();
			if (arcs.size() != 1)
				continue;

			Arc arc = arcs.iterator().next();
			int event = utility.getEventIndex(arc.getLabel());
			List<BigInteger> pv;
			try {
				pv = utility.getReachingParikhVector(state);
			} catch (UnreachableException e) {
				// Just silently skip unreachable states (yes, we have 'totally reachable' as a
				// precondition...)
				continue stateLoop;
			}
			List<BigInteger> old = result.put(event, pv);
			assert old == null;
		}
		return result;
	}

	// Calculate the required regions.
	private List<Region> calculateRegions() {
		List<Region> result = new LinkedList<>();
		// For each state with only a single following node... (aka: sequentializing state)
		for (State state : utility.getTransitionSystem().getNodes()) {
			Set<Arc> following = state.getPostsetEdges();
			if (following.size() != 1 || !utility.getSpanningTree().isReachable(state))
				continue;

			Arc arc = following.iterator().next();
			int event = utility.getEventIndex(arc.getLabel());
			// ...look at the postset of that following node (this is s_a)...
			for (Arc followingArc : arc.getTarget().getPostsetEdges()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				int otherEvent = utility.getEventIndex(followingArc.getLabel());
				// We are looking for sequentializing states: 'state' doesn't enable an event and all
				// following states do. So skip if this doesn't actually hold.
				if (event == otherEvent)
					continue;

				// Create a region where event produces and otherEvent consumes one token
				BigInteger[] array = new BigInteger[utility.getNumberOfEvents()];
				Arrays.fill(array, BigInteger.ZERO);
				array[event] = BigInteger.ONE;
				array[otherEvent] = BigInteger.ONE.negate();
				Region.Builder builder = Region.Builder.createPure(utility, Arrays.asList(array));

				// Its initial marking is the number of times that 'event' appears on a shortest path
				// from reachedOnlyByPVs(otherEvent) to the initial state. Due to the special net
				// structure, we can calculate this from the opposite path, because path + opposite path
				// = (k, ..., k) (The cycle property gives us this). A path is short if it contains a
				// zero entry somewhere. So k is the maximum entry in the parikh vector.
				List<BigInteger> pv = reachedOnlyByPVs.get(otherEvent);
				// This code assumes that the spanning tree computes shortest paths. Check with an
				// assert (Lemma 25: a path is short iff some event never occurs in it).
				assert pv.contains(BigInteger.ZERO) : pv;
				BigInteger k = Collections.max(pv);
				result.add(builder.withInitialMarking(k.subtract(pv.get(event))));
			}
		}
		return result;
	}

	/**
	 * Calculate a region solving some state separation problem.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		for (Region region : regions)
			if (SeparationUtility.isSeparatingRegion(region, state, otherState))
				return region;

		assert !utility.getSpanningTree().isReachable(state)
			|| !utility.getSpanningTree().isReachable(otherState);
		return null;
	}

	/**
	 * Get a region solving some event/state separation problem.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		for (Region region : regions)
			if (SeparationUtility.isSeparatingRegion(region, state, event))
				return region;

		assert !utility.getSpanningTree().isReachable(state) || SeparationUtility.isEventEnabled(state, event);
		return null;
	}

	@Override
	public List<Region> getSeparatingRegions() {
		return Collections.unmodifiableList(regions);
	}

	@Override
	public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
		return Collections.emptySet();
	}

	@Override
	public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
		return Collections.emptyMap();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
