/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017-2018  Uli Schlachter
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.CycleSearchViaChords;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.util.equations.InequalitySystem;
import uniol.apt.util.equations.InequalitySystemSolver;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.util.MathTools;
import uniol.apt.util.Pair;
import uniol.apt.util.SpanningTree;
import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * This class quickly synthesises TS into output-nonbranching Petri net. This implementation is based on the theory
 * presented in "Bounded Choice-Free Petri Net Synthesis" by Eike Best, Raymond Devillers, and Uli Schlachter,
 * doi: 10.1007/s00236-017-0310-9.
 * @author Uli Schlachter
 */
class OutputNonbranchingSeparation implements Separation, Synthesizer {
	private final RegionUtility utility;
	private final TransitionSystem ts;

	// The Parikh vectors of small cycles (but entry 0 is null)
	private final List<ParikhVector> smallCyclesPVs = new ArrayList<>();

	// The labels of small cycles (but entry 0 contains labels not appearing on cycles)
	private final List<Set<String>> cycleLabels = new ArrayList<>();

	private final Map<State, ParikhVector> distanceCache = new HashMap<>();

	private final List<Region> regions = new ArrayList<>();

	/**
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 * @throws UnsupportedPNPropertiesException If the requested properties are not supported.
	 */
	public OutputNonbranchingSeparation(RegionUtility utility, PNProperties properties,
			String[] locationMap) throws UnsupportedPNPropertiesException {
		this.utility = utility;
		this.ts = utility.getTransitionSystem();

		// Check if only supported properties are requested.
		PNProperties supported = new PNProperties();
		if (!supported.containsAll(properties))
			throw new UnsupportedPNPropertiesException();

		Set<String> locations = new HashSet<>(Arrays.asList(locationMap));
		locations.remove(null);
		if (locations.size() != ts.getAlphabet().size())
			throw new UnsupportedPNPropertiesException();

		// Compute PVs of small cycles and also check total reachability, determinism, persistence, and disjoint
		// small cycles.
		try {
			this.smallCyclesPVs.addAll(new CycleSearchViaChords().searchCycles(ts));
		} catch (PreconditionFailedException e) {
			throw new UnsupportedPNPropertiesException(e);
		}
		// Check the prime cycle property (gcd of each small cycle must be 1).
		// This also sets up cycleLabels (computes the support of small cycles)
		Set<String> remainingLabels = new HashSet<>(ts.getAlphabet());
		for (ParikhVector pv : this.smallCyclesPVs) {
			int[] weights = new int[pv.getLabels().size()];
			int index = 0;
			for (String label : pv.getLabels()) {
				weights[index++] = pv.get(label);
			}

			if (MathTools.gcd(weights) != 1) {
				throw new UnsupportedPNPropertiesException("Non prime small cycle: " + pv.toString());
			}

			remainingLabels.removeAll(pv.getLabels());
			this.cycleLabels.add(pv.getLabels());

			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
		}

		// Check short path property
		if (!checkShortPathProperty())
			throw new UnsupportedPNPropertiesException();

		this.smallCyclesPVs.add(0, null);
		this.cycleLabels.add(0, remainingLabels);

		// Presynthesis is done, now do "proper" synthesis
		synthesis();
	}

	/**
	 * Check if the transition system satisfies the short path property. The spanning tree assigns a reaching path
	 * to each state. No Parikh vector of such a path may be contained in a smallest cycle.
	 */
	private boolean checkShortPathProperty() {
		// We only have to check states which have no successor in the spanning tree
		SpanningTree<TransitionSystem, Arc, State> tree = utility.getSpanningTree();
		Set<State> maximalStates = new HashSet<>(ts.getNodes());
		for (State state : ts.getNodes()) {
			State prev = tree.getPredecessor(state);
			if (prev != null)
				maximalStates.remove(prev);
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
		}

		// Now check each maximal state for the needed property
		for (State state : maximalStates) {
			ParikhVector pv = getDistance(state);
			for (ParikhVector smallCycle : smallCyclesPVs) {
				ParikhVector.Comparison comp = pv.compare(smallCycle);
				if (comp.equals(ParikhVector.Comparison.EQUAL) ||
						comp.equals(ParikhVector.Comparison.GREATER_THAN))
					return false;

				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			}
		}
		return true;
	}

	// Get the distance \Delta_{state} from the initial state to state
	private ParikhVector getDistance(State state) {
		ParikhVector result = distanceCache.get(state);
		if (result != null)
			return result;

		SpanningTree<TransitionSystem, Arc, State> tree = utility.getSpanningTree();
		Deque<Pair<State, String>> pending = new ArrayDeque<>();
		State current = state;
		while (result == null) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Arc predecessor = tree.getPredecessorEdge(current);
			if (predecessor == null) {
				// Reached the initial state
				result = new ParikhVector();
				distanceCache.put(current, result);
				break;
			}

			pending.addLast(new Pair<State, String>(current, predecessor.getLabel()));
			current = predecessor.getSource();
			result = distanceCache.get(state);
		}
		while (!pending.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Pair<State, String> pair = pending.removeLast();
			result = result.add(pair.getSecond());
			distanceCache.put(pair.getFirst(), result);
		}
		distanceCache.put(state, result);
		return result;
	}

	// Do proper synthesis and actually compute regions
	private void synthesis() throws UnsupportedPNPropertiesException {
		Set<String> tZero = this.cycleLabels.get(0);
		for (int l = 0; l < this.cycleLabels.size(); l++) {
			Set<String> labels = this.cycleLabels.get(l);
			Set<String> tZeroAndL = new HashSet<>(labels);
			if (l != 0)
				tZeroAndL.addAll(tZero);
			for (String x : labels) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				new ComputeRegions(l, x, tZeroAndL, smallCyclesPVs.get(l));
			}
		}
	}

	// Member class for actually computing regions. This has a fixed transition x and computes the necessary places
	// in its preset. This is a member class so that some constants can easily be access everywhere without having
	// to be passed around all the time.
	private class ComputeRegions {
		// Index into cycleLabels that we are working on
		private final int l;

		// The event that we are solving for (and entry of cycleLabels.get(l))
		private final String x;

		// Events that can possibly produce tokens on the place that we are computing
		private final Set<String> tZeroAndL;

		// The small cycle that x belongs to (or null if x does not belong to a small cycle)
		private final ParikhVector smallCycle;

		private ComputeRegions(int l, String x, Set<String> tZeroAndL, ParikhVector smallCycle)
				throws UnsupportedPNPropertiesException {
			this.l = l;
			this.x = x;
			this.tZeroAndL = tZeroAndL;
			this.smallCycle = smallCycle;

			// Compute XNX and NX
			Set<State> xnx = new HashSet<>();
			Set<State> nx = new HashSet<>();
			if (l > 0 && cycleLabels.get(l).size() == 1) {
				// Special case: x only loops on states, i.e. does not change the state.
				// nx contains states where x is not enabled, xnx where x is enabled
				for (State state : ts.getNodes()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					if (state.getPostsetEdgesByLabel(x).isEmpty())
						nx.add(state);
					else
						xnx.add(state);
				}
			} else {
				// General case:
				// nx contains states not enabling x, xnx contains states where x becomes disabled
				for (State state : ts.getNodes()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					if (state.getPostsetEdgesByLabel(x).isEmpty()) {
						nx.add(state);
						if (!state.getPresetEdgesByLabel(x).isEmpty())
							xnx.add(state);
					}
				}
			}

			debugFormat("XNX(%s) = %s", x, xnx);
			debugFormat("NX(%s) = %s", x, nx);

			// Compute mXNX and mNX. This is a representative system of the sets above where we use
			// knowledge about the number of tokens for some states being smaller than for others. Details
			// are in the paper.
			Set<State> mXNX = computeRepresentives(xnx, true);
			debugFormat("mXNX(%s) = %s", x, mXNX);
			Set<State> mNX = computeRepresentives(nx, false);
			debugFormat("mNX(%s) = %s", x, mNX);

			for (State state : mNX)
				solve(state, mXNX);
		}

		// Compute the maximal / minimal elements of the given set for the order \leq defined in the paper.
		private Set<State> computeRepresentives(Set<State> states, boolean maximal) {
			Set<State> result = new HashSet<>();
			for (State candidate : states) {
				Iterator<State> iter = result.iterator();
				boolean add = true;
				while (iter.hasNext()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					State other = iter.next();
					if (isLessOrEqual(candidate, other, maximal)) {
						add = false;
						break;
					}
					if (isLessOrEqual(other, candidate, maximal))
						iter.remove();
				}
				if (add)
					result.add(candidate);
			}
			return result;
		}

		public boolean isLessOrEqual(State a, State b, boolean swap) {
			if (swap)
				return isLessOrEqual(b, a);
			return isLessOrEqual(a, b);
		}

		// Implementation of the weak order from definition 15 on page 24 in the paper.
		public boolean isLessOrEqual(State a, State b) {
			ParikhVector pva = getDistance(a);
			ParikhVector pvb = getDistance(b);

			for (String j : cycleLabels.get(0)) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				if (!j.equals(x) && pva.get(j) > pvb.get(j))
					return false;
			}

			if (l == 0) {
				if (pva.get(x) < pvb.get(x))
					return false;
				return true;
			}

			ParikhVector cycle = smallCyclesPVs.get(l);
			for (String j : cycleLabels.get(l)) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				if (j.equals(x))
					continue;
				int lhs = cycle.get(x) * pva.get(j) - cycle.get(j) * pva.get(x);
				int rhs = cycle.get(x) * pvb.get(j) - cycle.get(j) * pvb.get(x);
				if (lhs > rhs)
					return false;
			}
			return true;
		}

		// Compute a region that prevents "x" at state "state" while allowing all "x" that lead to states from
		// "mXNX". This solves system (10) / (11) from the paper.
		private void solve(State state, Set<State> mXNX) throws UnsupportedPNPropertiesException {
			debugFormat("solve(%s, %s) called", state, mXNX);

			// Assign an order to the variables, x implicitly gets index 0
			List<String> variables = new ArrayList<>();
			for (String var : tZeroAndL) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				if (!var.equals(x))
					variables.add(var);
			}

			InequalitySystem system = new InequalitySystem();
			requireNonNegativeSolution(system, 1 + variables.size());

			ParikhVector distanceState = getDistance(state);
			if (l == 0) {
				debugFormat("Variables order will be initial marking, then %s", variables);
				for (State other : mXNX) {
					ParikhVector distanceOther = getDistance(other);
					int[] coefficients = new int[1 + variables.size()];
					coefficients[0] = 1 + distanceState.get(x) - distanceOther.get(x);
					for (int index = 0; index < variables.size(); index++) {
						InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
						String j = variables.get(index);
						coefficients[index + 1] = distanceOther.get(j) - distanceState.get(j);
					}
					system.addInequality(0, "<", coefficients, "Inequality for state " + other);
				}
			} else {
				debugFormat("Variables order will be %s", variables);
				for (State other : mXNX) {
					ParikhVector distanceOther = getDistance(other);
					int[] coefficients = new int[1 + variables.size()];
					int smallCycleX = smallCycle.get(x);
					for (int index = 0; index < variables.size(); index++) {
						InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
						String j = variables.get(index);
						coefficients[index + 1] = 0;
						coefficients[index + 1] += smallCycle.get(j) *
								(1 + distanceState.get(x) - distanceOther.get(x));
						coefficients[index + 1] -= smallCycle.get(x) *
								(distanceState.get(j) - distanceOther.get(j));
					}
					system.addInequality(0, "<", coefficients, "Inequality for state " + other);
				}
			}

			List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system)
				.findSolution();
			debugFormat("Got solution: %s", solution);
			if (solution.isEmpty())
				// TODO: Can we instead come up with an unsolvable separation problem?
				// For example, on state 'state', event x cannot be prevented.
				// Indeed we can, but this does not really help us here (we are not doing quick-fail)
				throw new UnsupportedPNPropertiesException("Failure for x=" + x
						+ " and state=" + state);

			// Find the weight k with which x consumes from the place
			BigInteger k;
			BigInteger factor;
			if (l == 0) {
				factor = BigInteger.ONE;
				k = solution.get(0);
			} else {
				// Solve sum k_j * smallCycle.get(j) = k * smallCycle.get(x),
				// which is equivalent to (1/smallCycle.get(x)) * sum k_j * smallCycle.get(j) = k
				BigInteger lhs = BigInteger.ZERO;
				for (int index = 0; index < variables.size(); index++) {
					// lhs += solution.get(index + 1) * smallCycle.get(j)
					BigInteger f = solution.get(index + 1);
					int f2 = smallCycle.get(variables.get(index));
					lhs = lhs.add(f.multiply(BigInteger.valueOf(f2)));
				}
				BigInteger divide = BigInteger.valueOf(smallCycle.get(x));
				BigInteger gcd = lhs.gcd(divide);
				debugFormat("sum k_j * cycle[j] = %s = k * %s = k * cycle[x]", lhs, divide);
				k = lhs.divide(gcd);
				factor = divide.divide(gcd);
			}
			debugFormat("Using k=%s and factor=%s", k, factor);

			// Compute the initial marking and weight of side condition
			BigInteger mu = null;
			for (State r : mXNX) {
				ParikhVector distance = getDistance(r);
				BigInteger value = k.multiply(BigInteger.valueOf(distance.get(x)));
				for (int index = 0; index < variables.size(); index++) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					BigInteger subtract = factor.multiply(solution.get(index + 1));
					subtract = subtract.multiply(BigInteger.valueOf(
								distance.get(variables.get(index))));
					value = value.subtract(subtract);
				}
				if (mu == null || mu.compareTo(value) < 0)
					mu = value;
			}
			BigInteger h = BigInteger.ZERO;
			if (mu.compareTo(BigInteger.ZERO) < 0) {
				h = mu.negate();
				mu = BigInteger.ZERO;
			}
			debugFormat("Computed h=%s (weight of side condition) and mu=%s (initial marking)", h, mu);

			// Now create the needed region
			Region.Builder builder = new Region.Builder(utility);
			builder.addWeightOn(x, k.negate());
			builder.addLoopAround(x, h);
			for (int index = 0; index < variables.size(); index++) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				builder.addWeightOn(variables.get(index), factor.multiply(solution.get(index + 1)));
			}
			Region r = builder.withInitialMarking(mu);
			debugFormat("Constructed region %s", r);
			regions.add(r);
			debug();
		}
	}

	static private void requireNonNegativeSolution(InequalitySystem system, int numVariables) {
		int[] inequality = new int[numVariables];
		for (int i = 0; i < numVariables; i++) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			inequality[i] = 1;
			system.addInequality(0, "<=", inequality, "Variable should be non-negative");
			inequality[i] = 0;
		}
	}

	/**
	 * Calculate a region solving some state separation problem.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		for (Region region : regions) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (SeparationUtility.isSeparatingRegion(region, state, otherState))
				return region;
		}

		assert state.equals(otherState);
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
		for (Region region : regions) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (SeparationUtility.isSeparatingRegion(region, state, event))
				return region;
		}

		assert SeparationUtility.isEventEnabled(state, event);
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
