/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       Uli Schlachter
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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.Event;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.util.MathTools;
import uniol.apt.util.Pair;
import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * This class finds k-bounded solutions to separation problems.
 *
 * It is based on the theory of "New Region-Based Algorithms for Deriving
 * Bounded Petri Nets" by Carmona, Cortadella and Kishinevsky, IEEE Transactions
 * on Computers, Vol. 59, No. 3 from March 2010.
 * @author Uli Schlachter
 */
class KBoundedSeparation implements Separation {
	private final RegionUtility utility;
	private final Set<Region> regions = new HashSet<>();
	private final boolean pure;

	/**
	 * package-visible constructor used by tests
	 */
	KBoundedSeparation(TransitionSystem ts, PNProperties properties) throws UnsupportedPNPropertiesException {
		this(new RegionUtility(ts), properties, new String[0]);
	}

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public KBoundedSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this.utility = utility;
		this.pure = properties.isPure();

		// Are we the right Separation implementation for the task?
		if (!properties.isKBounded())
			throw new UnsupportedPNPropertiesException();

		PNProperties supported = new PNProperties()
			.requireKBounded(properties.getKForKBounded())
			.setPure(true);
		if (!supported.containsAll(properties))
			throw new UnsupportedPNPropertiesException();

		// We do not support locations, so no locations may be specified
		if (Collections.frequency(Arrays.asList(locationMap), null) != locationMap.length)
			throw new UnsupportedPNPropertiesException();

		// Check our preconditions
		TransitionSystem ts = utility.getTransitionSystem();
		if (!new TotallyReachable(ts).isTotallyReachable())
			throw new UnsupportedPNPropertiesException();
		Set<Event> events = new HashSet<>();
		for (Arc arc : ts.getEdges())
			events.add(arc.getEvent());
		if (!events.equals(ts.getAlphabetEvents()))
			throw new UnsupportedPNPropertiesException();

		// Ok, we can do it. Now do it.
		if (properties.getKForKBounded() == 0)
			// There are no 0-bounded regions that solve any kind of separation problem.
			// (Except if the alphabet has events that do not occur on any arc, which is not supported)
			return;
		generateAllRegions(properties.getKForKBounded());
	}

	/* package-visible getter used by tests */
	Set<Region> getRegions() {
		return Collections.unmodifiableSet(regions);
	}

	// Generate the all k-bounded Regions of the input.
	private void generateAllRegions(int k) {
		assert k >= 1;

		Set<Bag<State>> known = new HashSet<>();
		Deque<Bag<State>> todo = new LinkedList<>();
		addExcitationAndSwitchingRegions(known);
		todo.addAll(known);

		while (!todo.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Bag<State> r = todo.removeFirst();

			debug();
			debugFormat("Examining %s", r);

			Pair<Event, Integer> event = findEventWithNonConstantGradient(r);
			if (event == null) {
				debug("It is a region!");
				regions.add(convertToRegion(r));
				continue;
			}

			// Expand (= add entries) to the multiset so that it becomes "more region-like".
			// To do this, we either want to go towards a region with gradient(event) <= g or
			// gradient(event) > g. These two cases follow.

			Bag<State> r1 = expandBelowOrEqual(r, event.getFirst(), event.getSecond());
			debugFormat("for gradient(%s) <= %d, new result is %s",
					event.getFirst(), event.getSecond(), r1);
			if (shouldExplore(r1, k) && known.add(r1))
				todo.add(r1);
			else
				debug("...which should not be explored");

			Bag<State> r2 = expandAboveOrEqual(r, event.getFirst(), event.getSecond() + 1);
			debugFormat("for gradient(%s) >= %d, new result is %s",
					event.getFirst(), event.getSecond() + 1, r2);
			if (shouldExplore(r2, k) && known.add(r2))
				todo.add(r2);
			else
				debug("...which should not be explored");
		}

		debugFormat("Found the following regions: %s", regions);
	}

	// See expandBelowOrEqual() and expandAboveOrEqual()
	private Bag<State> expand(Bag<State> input, Event event, int g, boolean forward) {
		TransitionSystem ts = utility.getTransitionSystem();
		Bag<State> result = new HashBag<State>();

		for (State state : ts.getNodes()) {
			int increment = 0;
			for (Arc arc : forward ? state.getPostsetEdges() : state.getPresetEdges()) {
				if (arc.getEvent().equals(event)) {
					int value = getGradient(input, arc) - g;
					if (!forward)
						value = -value;
					if (value > increment)
						increment = value;
				}
			}
			result.add(state, input.getCount(state) + increment);
		}

		return result;
	}

	// Expand the given multiset so that the gradient of event is "more region-like" and will be <= g
	private Bag<State> expandBelowOrEqual(Bag<State> input, Event event, int g) {
		return expand(input, event, g, true);
	}

	// Expand the given multiset so that the gradient of event is "more region-like" and will be >= g
	private Bag<State> expandAboveOrEqual(Bag<State> input, Event event, int g) {
		return expand(input, event, g, false);
	}

	/**
	 * Add the excitation and switching regions of each event to the given collection.
	 * The excitation region of an event e is the (multi)set of states in which it is enabled. Analogously, the
	 * switching region is the (multi)set of states reached by some arc with label e.
	 */
	private void addExcitationAndSwitchingRegions(Collection<Bag<State>> result) {
		TransitionSystem ts = utility.getTransitionSystem();
		for (Event event : ts.getAlphabetEvents()) {
			Set<State> excitation = new HashSet<>();
			Set<State> switching = new HashSet<>();

			for (Arc arc : ts.getEdges()) {
				if (arc.getEvent().equals(event)) {
					excitation.add(arc.getSource());
					switching.add(arc.getTarget());
				}
			}

			debugFormat("For event %s, excitation=%s and switching=%s", event, excitation, switching);

			if (!excitation.isEmpty())
				result.add(new HashBag<>(excitation));
			if (!switching.isEmpty())
				result.add(new HashBag<>(switching));
		}
	}

	// Check if r is a region and if not return information on why not
	private Pair<Event, Integer> findEventWithNonConstantGradient(Bag<State> r) {
		TransitionSystem ts = utility.getTransitionSystem();
		Map<Event, Integer> gradients = new HashMap<>();
		Event nonConstantGradientEvent = null;
		int minGradient = Integer.MAX_VALUE;
		int maxGradient = Integer.MIN_VALUE;

		for (Arc arc : ts.getEdges()) {
			if (nonConstantGradientEvent == null) {
				int gradient = getGradient(r, arc);
				Integer old = gradients.put(arc.getEvent(), gradient);
				if (old != null && old != gradient) {
					nonConstantGradientEvent = arc.getEvent();
					minGradient = Math.min(gradient, old);
					maxGradient = Math.max(gradient, old);
				}
			} else if (nonConstantGradientEvent.equals(arc.getEvent())) {
				int gradient = getGradient(r, arc);
				minGradient = Math.min(minGradient, gradient);
				maxGradient = Math.max(maxGradient, gradient);
			}
		}

		if (nonConstantGradientEvent == null)
			return null;

		// Yup, this should round down
		int average = MathTools.meanTowardsMinusInfinity(minGradient, maxGradient);
		debugFormat("For %s: average %d, max gradient is %d and min gradient is %d for multiset %s",
				nonConstantGradientEvent, average, maxGradient, minGradient, r);
		return new Pair<Event, Integer>(nonConstantGradientEvent, average);
	}

	private int getGradient(Bag<State> r, Arc arc) {
		return r.getCount(arc.getTarget()) - r.getCount(arc.getSource());
	}

	private boolean shouldExplore(Bag<State> r, int k) {
		// Don't continue if no state has cardinality zero, because the result won't be a minimal region
		if (r.containsAll(utility.getTransitionSystem().getNodes()))
			return false;

		// Don't continue if some state has cardinality higher than k
		for (State state : r.uniqueSet())
			if (r.getCount(state) > k)
				return false;

		return true;
	}

	private Region convertToRegion(Bag<State> r) {
		TransitionSystem ts = utility.getTransitionSystem();
		Region.Builder builder = new Region.Builder(utility);

		for (Event event : ts.getAlphabetEvents()) {
			Arc representativeArc = null;
			int minEnabledValue = Integer.MAX_VALUE;
			for (Arc arc : ts.getEdges()) {
				if (arc.getEvent().equals(event)) {
					representativeArc = arc;
					minEnabledValue = Math.min(minEnabledValue, r.getCount(arc.getSource()));
				}
			}

			// TS should not have an event which is not the label of any arc
			assert representativeArc != null;

			int gradient = getGradient(r, representativeArc);
			int forward = 0;
			int backward = 0;
			if (!pure) {
				backward = minEnabledValue;
				forward = minEnabledValue + gradient;
			} else {
				if (gradient > 0)
					forward = gradient;
				else
					backward = -gradient;
			}

			builder.addWeightOn(event.getLabel(), BigInteger.valueOf(-backward));
			builder.addWeightOn(event.getLabel(), BigInteger.valueOf(forward));
		}

		int initial = r.getCount(utility.getTransitionSystem().getInitialState());
		Region region = builder.withInitialMarking(BigInteger.valueOf(initial));
		debugFormat("Region %s corresponds to %s", region, r);
		return region;
	}

	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		for (Region region : regions)
			if (SeparationUtility.isSeparatingRegion(region, state, otherState))
				return region;
		return null;
	}

	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		for (Region region : regions)
			if (SeparationUtility.isSeparatingRegion(region, state, event))
				return region;
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
