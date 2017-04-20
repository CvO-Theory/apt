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

package uniol.apt.analysis.synthesize.separation;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * This class solves separation problems producing elementary Petri nets (plain, pure, safe). The algorithm is based on
 * the book "Petri Net Synthesis" by Badouel, Bernardinello and Darondeau.
 * @author Uli Schlachter
 */
class ElementarySeparation implements Separation {
	static private final BigInteger ONE = BigInteger.valueOf(1);
	static private final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

	private final RegionUtility utility;
	private final Map<String, Set<Arc>> arcsWithlabel = new HashMap<>();
	private final boolean pure;
	private final String[] locationMap;

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public ElementarySeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this.utility = utility;
		this.pure = properties.isPure();
		this.locationMap = locationMap;

		PNProperties required = new PNProperties().requireSafe();
		// When "safe" is already required, plain is no longer limiting
		// (arc weights > 1 belong to transitions which can never fire)
		PNProperties supported = required
			.setPlain(true)
			.setPure(true);

		if (!properties.containsAll(required))
			throw new UnsupportedPNPropertiesException();
		if (!supported.containsAll(properties))
			throw new UnsupportedPNPropertiesException();

		if (!utility.getSpanningTree().isTotallyReachable()) {
			debug("Only totally reachable transition systems are supported!");
			throw new UnsupportedPNPropertiesException();
		}

		// Compute arcsWithlabel for quickly accessing all arcs with some label
		for (Arc arc : utility.getTransitionSystem().getEdges()) {
			Set<Arc> set = arcsWithlabel.get(arc.getLabel());
			if (set == null) {
				set = new HashSet<>();
				arcsWithlabel.put(arc.getLabel(), set);
			}
			set.add(arc);
		}
	}

	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		if (!utility.getSpanningTree().isReachable(state)
			|| !utility.getSpanningTree().isReachable(otherState))
			return null;

		// Look for a region which contains just one of the two states. Since the complement of a region is also
		// a region, it does not matter how we assign the states here.
		RoughRegion region = new RoughRegion();
		region.setState(state, true);
		region.setState(otherState, false);

		return extractRegion(region);
	}

	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		if (!utility.getSpanningTree().isReachable(state) || SeparationUtility.isEventEnabled(state, event))
			return null;

		// Calculate a region which does not contain the given state, but which is left by the event
		RoughRegion region = new RoughRegion();
		region.setState(state, false);
		region.setLabelOperation(event, Operation.EXIT);

		Region result = extractRegion(region);
		if (result == null && !pure) {
			// Now try a region not containing the state and having a side condition to the event
			region = new RoughRegion();
			region.setState(state, false);
			region.setLabelOperation(event, Operation.INSIDE);
			result = extractRegion(region);
		}

		return result;
	}

	/**
	 * Given a rough region, extract a concrete region from it. All assignments already done to the given region
	 * will also be valid in the result.
	 * @param region The rough region from which a concrete region should be extracted.
	 * @return The concrete region, or null if none exists.
	 */
	private Region extractRegion(RoughRegion region) {
		Deque<RoughRegion> unhandled = new ArrayDeque<>();
		unhandled.add(region);
		while (!unhandled.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			region = unhandled.remove();

			// Refine the region as long as this is uniquely possible
			while (!region.isInconsistent()) {
				if (!region.refine())
					break;
			}

			if (region.isInconsistent()) {
				debug("rough region became inconsistent, skipping it");
				continue;
			}

			// The next step is no longer unique, find some label and try all possibilities for it
			String label = region.getUnassignedLabel();
			if (label == null) {
				// No unassigned label found, do we have a solution?
				Region result = region.extractValidRegion();
				if (result != null) {
					debugFormat("Extracted region %s from %s", result, region);
					return result;
				}
				debug("No unassigned label found, skipping this rough region");
			} else {
				debugFormat("Splitting the rough region on label %s", label);
				RoughRegion enter = new RoughRegion(region);
				RoughRegion exit = new RoughRegion(region);
				RoughRegion inside = null;
				if (!pure)
					inside = new RoughRegion(region);

				if (!pure)
					inside.setLabelOperation(label, Operation.INSIDE);
				region.setLabelOperation(label, Operation.DONT_CROSS);
				enter.setLabelOperation(label, Operation.ENTER);
				exit.setLabelOperation(label, Operation.EXIT);

				if (!pure)
					unhandled.addFirst(inside);
				unhandled.addFirst(enter);
				unhandled.addFirst(exit);
				unhandled.addFirst(region);
			}
		}
		return null;
	}

	// The different kind of operations that a label can do to a region
	private static enum Operation {
		// Arcs for our label enter the region
		ENTER {
			@Override
			public void refineStates(RoughRegion region, Arc arc, Map<State, Boolean> statesInRegion) {
				region.setState(arc.getSource(), false);
				region.setState(arc.getTarget(), true);
			}

			@Override
			public void setupRegion(Region.Builder builder, int eventIndex) {
				builder.addWeightOn(eventIndex, ONE);
			}

			@Override
			public void toStringHelper(String entry, Set<String> enter, Set<String> exit,
					Set<String> dontCross, Set<String> inside) {
				enter.add(entry);
			}
		},
		// Arcs for our label leave the region
		EXIT {
			@Override
			public void refineStates(RoughRegion region, Arc arc, Map<State, Boolean> statesInRegion) {
				region.setState(arc.getSource(), true);
				region.setState(arc.getTarget(), false);
			}

			@Override
			public void setupRegion(Region.Builder builder, int eventIndex) {
				builder.addWeightOn(eventIndex, MINUS_ONE);
			}

			@Override
			public void toStringHelper(String entry, Set<String> enter, Set<String> exit,
					Set<String> dontCross, Set<String> inside) {
				exit.add(entry);
			}
		},
		// Arcs for our label do not cross the border of the region
		DONT_CROSS {
			@Override
			public void refineStates(RoughRegion region, Arc arc, Map<State, Boolean> statesInRegion) {
				// If one of the two states is assigned, assign the other one
				Boolean bool = statesInRegion.get(arc.getSource());
				if (bool != null) {
					region.setState(arc.getTarget(), bool);
				} else {
					bool = statesInRegion.get(arc.getTarget());
					if (bool != null)
						region.setState(arc.getSource(), bool);
				}
			}

			@Override
			public void setupRegion(Region.Builder builder, int eventIndex) {
				// Nothing to do
			}

			@Override
			public void toStringHelper(String entry, Set<String> enter, Set<String> exit,
					Set<String> dontCross, Set<String> inside) {
				dontCross.add(entry);
			}
		},
		// Arcs for our label are inside of the region
		INSIDE {
			@Override
			public void refineStates(RoughRegion region, Arc arc, Map<State, Boolean> statesInRegion) {
				region.setState(arc.getSource(), true);
				region.setState(arc.getTarget(), true);
			}

			@Override
			public void setupRegion(Region.Builder builder, int eventIndex) {
				builder.addWeightOn(eventIndex, ONE);
				builder.addWeightOn(eventIndex, MINUS_ONE);
			}

			@Override
			public void toStringHelper(String entry, Set<String> enter, Set<String> exit,
					Set<String> dontCross, Set<String> inside) {
				inside.add(entry);
			}
		};

		public abstract void refineStates(RoughRegion region, Arc arc, Map<State, Boolean> statesInRegion);
		public abstract void setupRegion(Region.Builder builder, int eventIndex);
		public abstract void toStringHelper(String entry, Set<String> enter, Set<String> exit,
				Set<String> dontCross, Set<String> inside);
	}

	/**
	 * A rough region is a region in which some parts are still undetermined.
	 */
	private class RoughRegion {
		// Maps states to whether they are inside or outside of this region
		private final Map<State, Boolean> statesInRegion;

		// Maps labels to the operation that they do on this region
		private final Map<String, Operation> labelOperations;

		// States which were assigned in statesInRegion and which might cause further changes
		private final Deque<State> statesToHandle = new ArrayDeque<>();

		// Labels which were assigned in labelOperations and which might cause further changes
		private final Deque<String> labelsToHandle = new ArrayDeque<>();

		// If this is not null, at least one event exits from this region, this is the location of this event
		// If another event with another location wants to leave this region, the region becomes inconsistent
		private String location = null;

		// If this value is true, then there is no concrete region "inside" of this rough region
		private boolean inconsistent = false;

		/**
		 * Constructor
		 */
		public RoughRegion() {
			statesInRegion = new HashMap<>();
			labelOperations = new HashMap<>();
		}

		/**
		 * Copy constructor
		 * @other The RoughRegion to copy
		 */
		public RoughRegion(RoughRegion other) {
			statesInRegion = new HashMap<>(other.statesInRegion);
			labelOperations = new HashMap<>(other.labelOperations);
			statesToHandle.addAll(other.statesToHandle);
			labelsToHandle.addAll(other.labelsToHandle);
			location = other.location;
			inconsistent = other.inconsistent;
		}

		/**
		 * Is this RoughRegion inconsistent? If yes, then no further refinement can ever produce a concrete
		 * region.
		 * @return True if this RoughRegion is inconsistent
		 */
		public boolean isInconsistent() {
			return inconsistent;
		}

		/**
		 * Get a label without an operation assigned.
		 * @return An unassigned label
		 */
		public String getUnassignedLabel() {
			for (String label : utility.getTransitionSystem().getAlphabet())
				if (!labelOperations.containsKey(label))
					return label;
			return null;
		}

		/**
		 * Set the "is in the region"-status of some state. The region can become inconsistent if this
		 * new state contradicts the previous one.
		 * @param s The state to set
		 * @param inRegion Wether the given state should be inside or outside of the region
		 */
		public void setState(State s, boolean inRegion) {
			Boolean old = statesInRegion.put(s, inRegion);
			if (old == null)
				statesToHandle.add(s);
			else if (old != inRegion)
				inconsistent = true;
		}

		/**
		 * Set the operation of some label. The region can become inconsistent if this new operation contradicts
		 * the previous one or if the operation is EXIT or INSIDE and other label, which also have this
		 * operation, have another location.
		 * @param label The label to set
		 * @param op The operation to assign to the label
		 */
		public void setLabelOperation(String label, Operation op) {
			Operation oldOp = labelOperations.put(label, op);
			if (oldOp == null)
				labelsToHandle.add(label);
			else if (!oldOp.equals(op))
				inconsistent = true;
			if (op.equals(Operation.EXIT) || op.equals(Operation.INSIDE)) {
				String newLocation = locationMap[utility.getEventIndex(label)];
				if (location == null)
					location = newLocation;
				else if (!location.equals(newLocation))
					inconsistent = true;
			}
		}

		/**
		 * Try to make this rough region more concrete if this is possible in a unique way.
		 * @return True if something was changed
		 */
		public boolean refine() {
			return refineOnLabel() || refineOnState();
		}

		private boolean refineOnLabel() {
			String label = labelsToHandle.poll();
			if (label == null)
				return false;

			Operation op = labelOperations.get(label);
			debugFormat("Refining %s on label %s with operation %s", this, label, op);

			assert op != null;
			for (Arc arc : arcsWithlabel.get(label)) {
				op.refineStates(this, arc, statesInRegion);
			}
			return true;
		}

		private boolean refineOnState() {
			State state = statesToHandle.poll();
			if (state == null)
				return false;

			debugFormat("Refining %s on state %s which is in=%s", this, state, statesInRegion.get(state));
			for (Arc arc : state.getNeighboringEdges()) {
				Boolean sourceInRegion = statesInRegion.get(arc.getSource());
				Boolean targetInRegion = statesInRegion.get(arc.getTarget());
				if (targetInRegion == null || sourceInRegion == null) {
					// Perhaps we can now assign based on DONT_CROSS?
					assert targetInRegion != null || sourceInRegion != null;
					if (Operation.DONT_CROSS.equals(labelOperations.get(arc.getLabel()))) {
						boolean val = targetInRegion != null ? targetInRegion : sourceInRegion;
						setState(arc.getSource(), val);
						setState(arc.getTarget(), val);
					}
					continue;
				}
				if (!sourceInRegion && targetInRegion)
					setLabelOperation(arc.getLabel(), Operation.ENTER);
				if (sourceInRegion && !targetInRegion)
					setLabelOperation(arc.getLabel(), Operation.EXIT);
				if (pure && sourceInRegion.equals(targetInRegion))
					// With !pure, INSIDE would also be a viable possibility
					setLabelOperation(arc.getLabel(), Operation.DONT_CROSS);
				if (!pure && !sourceInRegion && !targetInRegion)
					setLabelOperation(arc.getLabel(), Operation.DONT_CROSS);
			}
			return true;
		}

		/**
		 * Check if this rough region contains a valid, concrete region.
		 * @return The concrete region described by this rough region, or null.
		 */
		public Region extractValidRegion() {
			if (inconsistent)
				return null;

			// All pending operations must be done
			if (!statesToHandle.isEmpty() || !labelsToHandle.isEmpty())
				return null;

			// All states must be mapped
			if (!statesInRegion.keySet().equals(utility.getTransitionSystem().getNodes()))
				return null;

			// All labels must have an assigned operation
			if (!labelOperations.keySet().equals(utility.getTransitionSystem().getAlphabet()))
				return null;

			// Everything is ok, now build a region
			Region.Builder builder = new Region.Builder(utility);
			for (int i = 0; i < utility.getNumberOfEvents(); i++) {
				labelOperations.get(utility.getEventList().get(i)).setupRegion(builder, i);
			}
			BigInteger initialMarking = statesInRegion.get(utility.getTransitionSystem().getInitialState())
				? BigInteger.ONE : BigInteger.ZERO;
			return builder.withInitialMarking(initialMarking);
		}

		@Override
		public String toString() {
			Set<String> in = new HashSet<>();
			Set<String> out = new HashSet<>();
			for (Map.Entry<State, Boolean> entry : statesInRegion.entrySet()) {
				if (entry.getValue())
					in.add(entry.getKey().getId());
				else
					out.add(entry.getKey().getId());
			}
			Set<String> enter = new HashSet<>();
			Set<String> exit = new HashSet<>();
			Set<String> dontCross = new HashSet<>();
			Set<String> inside = new HashSet<>();
			for (Map.Entry<String, Operation> entry : labelOperations.entrySet()) {
				entry.getValue().toStringHelper(entry.getKey(), enter, exit, dontCross, inside);
			}

			return String.format("RoughRegion[%sin=%s, out=%s, enter=%s, exit=%s, dontCross=%s, inside=%s]",
					inconsistent ? "INCONSISTENT! " : "", in, out, enter, exit, dontCross, inside);
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
