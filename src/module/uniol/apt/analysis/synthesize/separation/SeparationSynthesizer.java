/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.SynthesizePN;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.EquivalenceRelation;
import uniol.apt.util.Pair;
import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * Interface for something that synthesizes a Petri net.
 * @author Uli Schlachter
 */
public class SeparationSynthesizer implements Synthesizer {
	private final Collection<Region> separatingRegions;
	private final Map<String, Set<State>> unsolvableESSP;
	private final Collection<Set<State>> unsolvableSSP;

	/**
	 * Synthesize the given transition system via the given separation implementation.
	 * @param ts The transition system to synthesize.
	 * @param separation A separation implementation that solves separation problems on the given transition system.
	 * @param onlyEventSeparation A flag indicating that state separation should be ignored.
	 * @param quickFail If true, stop the calculation as soon as it is known that it won't be successful. If false,
	 * try to solve all separation problems. Only if true will the list of failed problems be fully filled.
	 */
	public SeparationSynthesizer(TransitionSystem ts, Separation separation,
			boolean onlyEventSeparation, boolean quickFail) {
		Set<Region> regions = new HashSet<>();
		Map<String, Set<State>> essp = new HashMap<>();
		EquivalenceRelation<State> ssp = new EquivalenceRelation<>();

		solveEventStateSeparation(ts, separation, quickFail, regions, essp);
		if (!onlyEventSeparation && (!quickFail || essp.isEmpty()))
			solveStateSeparation(ts, separation, quickFail, regions, ssp);
		if (!quickFail || (essp.isEmpty() && ssp.isEmpty()))
			minimizeRegions(ts, regions, onlyEventSeparation);

		this.separatingRegions = Collections.unmodifiableSet(regions);
		this.unsolvableESSP = Collections.unmodifiableMap(essp);
		this.unsolvableSSP = Collections.unmodifiableCollection(ssp);
		debug();
	}

	private void solveEventStateSeparation(TransitionSystem ts, Separation separation, boolean quickFail,
			Set<Region> regions, Map<String, Set<State>> failedProblems) {
		debug();
		debug("Solving event-state separation");
		for (State state : ts.getNodes()) {
			for (String event : ts.getAlphabet()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				if (SeparationUtility.isEventEnabled(state, event))
					continue;

				debugFormat("Trying to separate %s from event '%s'", state, event);
				Region r = null;
				for (Region region : regions)
					if (SeparationUtility.isSeparatingRegion(region, state, event)) {
						r = region;
						break;
					}
				if (r != null) {
					debug("Found region ", r);
					continue;
				}

				r = separation.calculateSeparatingRegion(state, event);
				if (r == null) {
					Set<State> set = failedProblems.get(event);
					if (set == null) {
						set = new HashSet<>();
						failedProblems.put(event, set);
					}
					set.add(state);
					debug("Failure!");
					if (quickFail)
						return;
				} else {
					debug("Calculated region ", r);
					regions.add(r);
				}
			}
		}
	}

	private void solveStateSeparation(TransitionSystem ts, Separation separation, boolean quickFail,
			Set<Region> regions, EquivalenceRelation<State> failedStateSeparationRelation) {
		debug();
		debug("Solving state separation");
		for (Pair<State, State> problem : new DifferentPairsIterable<State>(
					SynthesizePN.calculateUnseparatedStates(ts.getNodes(), regions))) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			State state = problem.getFirst();
			State otherState = problem.getSecond();
			debugFormat("Trying to separate %s from %s", state, otherState);
			Region r = null;
			for (Region region : regions)
				if (SeparationUtility.isSeparatingRegion(region, state, otherState)) {
					r = region;
					break;
				}
			if (r != null) {
				debug("Found region ", r);
				continue;
			}

			r = separation.calculateSeparatingRegion(state, otherState);
			if (r == null) {
				failedStateSeparationRelation.joinClasses(state, otherState);
				debug("Failure!");
				if (quickFail)
					return;
			} else {
				debug("Calculated region ", r);
				regions.add(r);
			}
		}
	}

	private void minimizeRegions(TransitionSystem ts, Set<Region> regions, boolean onlyEventSeparation) {
		debug();
		debug("Minimizing regions");
		SynthesizePN.minimizeRegions(ts, regions, onlyEventSeparation);
	}

	@Override
	public Collection<Region> getSeparatingRegions() {
		return separatingRegions;
	}

	@Override
	public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems() {
		return unsolvableESSP;
	}

	@Override
	public Collection<Set<State>> getUnsolvableStateSeparationProblems() {
		return unsolvableSSP;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
