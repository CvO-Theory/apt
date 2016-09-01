/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis.cycles.lts;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static uniol.apt.util.DebugUtil.debugFormat;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.reversible.ReversibleTS;
import uniol.apt.analysis.totallyreachable.TotallyReachable;
import uniol.apt.util.Pair;
import uniol.apt.util.SpanningTree;

/**
 * A special-case implementation for checking if all small cycles of a transition system have Parikh vector all one.
 * Compared to {@link ComputeSmallestCycles} this only checks for all-one Parikh vectors and has several preconditions:
 * Deterministic, totally reachable, reversible, persistent, backwards persistent.
 * @author Uli Schlachter
 */
public class AllSmallCyclesHavePVOne {
	private final List<Arc> counterExample;
	private final boolean cycleWithPV1Found;
	private final boolean foundIncomparableCycle;

	/**
	 * Check if each event occurs exactly once in each small cycle.
	 * @param ts The transition system to examine.
	 * @throws PreconditionFailedException if the given transition system is not reversible, deterministic, totally
	 * reachable, persistent or backward persistent.
	 */
	public AllSmallCyclesHavePVOne(TransitionSystem ts) throws PreconditionFailedException {
		debugFormat("Starting AllSmallCyclesHavePVOne");

		// We require a deterministic TS
		if (!new Deterministic(ts).isDeterministic())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not deterministic");
		debugFormat("input is deterministic");

		// We require a totally reachable TS
		if (!new TotallyReachable(ts).isTotallyReachable())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not totally reachable");
		debugFormat("input is totally reachable");

		if (ts.getAlphabet().isEmpty()) {
			// Special case: We have a totally reachable TS with an empty alphabet. This means that it only
			// consists of the initial state and there are no other state. The code below doesn't handle
			// this correctly (it expects to follow at least one edge), so this is needed.
			this.cycleWithPV1Found = true;
			this.counterExample = null;
			this.foundIncomparableCycle = false;
			return;
		}

		// We also require a reversible TS
		if (!new ReversibleTS(ts).isReversible())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not reversible");
		debugFormat("input is reversible");

		// We require a persistent TS
		if (!new PersistentTS(ts).isPersistent())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not persistent");
		debugFormat("input is persistent");

		// We require a backward persistent TS
		if (!new PersistentTS(ts, true).isPersistent())
			throw new PreconditionFailedException("TS " + ts.getName() + " is not backwards persistent");
		debugFormat("input is backward persistent");

		// By totally reachability and reversibility, every state is a home state, so s0 is a home state. By
		// corollary 4 of [1], deterministicity and persistency imply that for every cycle, there is a
		// Parikh-equivalent cycle around a home state. Thus, we are only looking at small cycles around the
		// initial state and can be sure to "see" every existing cycle. We will do this in two phases.
		// [1]: A decomposition theorem for finite persistent transition systems. Eike Best and Philippe
		// Darondeau. Acta Informatica (2009). DOI 10.1007/s00236-009-0095-6.

		// In phase one we check that (a) we find a cycle around the initial state in which every event occurs
		// exactly once, and (b) no smaller cycle exists around the initial state.
		Pair<Boolean, List<Arc>> result = checkPhase1(ts, ts.getInitialState(), new HashSet<String>(),
				new LinkedList<Arc>());
		this.cycleWithPV1Found = result.getFirst();
		this.counterExample = result.getSecond();
		debugFormat("Phase 1 found cycle with PV1: %b, found counter example: %s",
				cycleWithPV1Found, counterExample);
		if (cycleWithPV1Found && counterExample == null) {
			// Phase one succeeded. In Phase two we check if there are any small cycles with Parikh vectors
			// incomparable to the all-ones PV. An example for such a cycle would be a TS with a cycle (1,1)
			// and another cycle (0,2).
			this.foundIncomparableCycle = checkPhase2(ts);
			debugFormat("Phase 2 found incomparable cycle: %b", foundIncomparableCycle);
		} else {
			this.foundIncomparableCycle = false;
		}
	}

	/**
	 * Recursive implementation of the depth-first search that looks for cycles with a Parikh vector of at most all
	 * ones.
	 * @param ts The transition system to examine
	 * @param state The next state that should be followed.
	 * @param firedEvents Set of events which were already fired on the path from the initial state.
	 * @param arcsFollowed List of arcs that were followed from the initial state to this state.
	 * @return A pair were the first element is true if a cycle with Parikh vector 1 was found and the second
	 * element is either null or a cycle with a smaller Parikh vector.
	 */
	static private Pair<Boolean, List<Arc>> checkPhase1(TransitionSystem ts, State state, Set<String> firedEvents,
			LinkedList<Arc> arcsFollowed) {
		boolean success = false;
		for (Arc arc : state.getPostsetEdges()) {
			if (firedEvents.contains(arc.getLabel()))
				continue;

			firedEvents.add(arc.getLabel());
			arcsFollowed.addLast(arc);

			State target = arc.getTarget();
			if (target.equals(ts.getInitialState())) {
				if (firedEvents.containsAll(ts.getAlphabet())) {
					// Found a suitable cycle!
					success = true;
				} else {
					// Found a counter-example
					return new Pair<Boolean, List<Arc>>(false, arcsFollowed);
				}
			} else {
				// Recurse to this new state
				Pair<Boolean, List<Arc>> result = checkPhase1(ts, target, firedEvents, arcsFollowed);
				if (result.getSecond() != null)
					return result;
				success = success || result.getFirst();
			}

			// Undo the modifications done above
			boolean r = firedEvents.remove(arc.getLabel());
			assert r == true;
			Arc last = arcsFollowed.removeLast();
			assert last == arc;
		}
		return new Pair<>(success, null);
	}

	/**
	 * Check if a cycle exists whose Parikh vector is incomparable to (1, ..., 1).
	 * @param ts The transition system to examine
	 * @return true if an incomparable cycle was found, else false.
	 */
	static private boolean checkPhase2(TransitionSystem ts) {
		/*
		 * Proof that the Parikh vector of generalised cycles is a linear combination of the Parikh vectors of
		 * chords:
		 * (Preconditions: total reachability, but nothing else)
		 *
		 * Assume a fixed spanning tree that assigns to each state s a (directed) path from the initial state to
		 * s and also the Parikh vector P(s) of this path. Define the Parikh vector of a chord to be
		 * P(s[t>s')=P(s)+1_t-P_{s'}.
		 *
		 * We will proof that the Parikh vector of a directed cycles is a linear combination of the Parikh
		 * vectors of the chords. This is shown by proving the more general claim that this also holds for
		 * generalised (undirected; allowing arcs to be followed backwards) cycles. Since every directed cycle
		 * is also a generalised cycle, the claim follows.
		 *
		 * We proof this by induction on the number of chords contained in a cycle.
		 *
		 * If a cycle contains no chords, it can only follow arcs inside the spanning tree and so its Parikh
		 * vector must be zero.
		 *
		 * Assume a cycle pi containing a chord so that we can write pi as pi = s [t> s' [sigma> s where sigma
		 * is the remaining cycle. By total reachability, we can use the paths from the initial state to s and
		 * s' to construct a new cycle that does not use this chord, but instead goes from s' backwards to the
		 * initial state and from there goes forwards to s, both by following the arcs of the spanning tree.
		 * This cycle is pi' = s' [sigma> s [-P_s> i [P_{s'}> s'. Note that this is a generalised cycle,
		 * because the path leading to state s is followed backwards. Since this cycle contains one chord less,
		 * by the induction hypothesis it can be written as a linear combination of chords.
		 *
		 * The Parikh vector of pi' satisfies: P(pi')=P(sigma)-P(s)+P(s'). By using P(s[t>s')=P(s)+1_t-P_{s'},
		 * this can be rewritten to P(pi')=P(sigma)-P(s[t>s')+1_t.
		 *
		 * We can now write the Parikh vector of pi as P(pi)=1_t+P(sigma)=P(pi')+P(s[t>s') which shows that
		 * P(pi) is also a linear combination of the Parikh vectors of chords.
		 */

		/*
		 * Proof that for any chord s[t>s' we have P(s)+1_t >= P(s'):
		 * (Preconditions: determinism, persistence, reversibility, total reachability)
		 *
		 * Assume P1 holds.
		 *
		 * In the paper "Characterisation of the State Spaces of Marked Graph Petri Nets" (which was already
		 * mentioned above), short paths are defined. Because it does a breath-first-search, our spanning tree
		 * computes short paths from the initial state to all other states. Thus, the path sigma(s') to s' with
		 * Parikh vector P(s') is a short path. The path sigma(st) that first goes to s via P(s) and then to s'
		 * via our chord is either also a short path, or is longer than the length of a short path.
		 *
		 * Lemma 25 in this paper says that a path is short iff there is some event x which does not appear in
		 * it.
		 *
		 * Lemma 27 in this paper says that for any path from s to s', its Parikh vector is the Parikh vector of
		 * a short path from s to s' plus a number m added to each component of the Parikh vector uniformly.
		 *
		 * Thus, since sigma(s) is a short path, sigma(st) can either be a short path (if one of its entries is
		 * null) or contains a cycle (if none of its entries is null). In the first case we have P(s)+1_t=P(s')
		 * and in the second case we have P(s)+1_t >= P(s').
		 *
		 * Thus, if we find a chord for which P(s)+1_t >= P(s') does not hold, then the transition system does
		 * not satisfy P1, because we already checked for determinism and persistence.
		 */

		/*
		 * Proof that for any chord, all entries must be the same:
		 * (Preconditions: determinism, persistence, reversibility, total reachability)
		 *
		 * Assume P1 holds.
		 *
		 * Lemma 27 in the paper says that for any path from s to s', its Parikh vector is the Parikh vector of
		 * a short path from s to s' plus a number m added to each component of the Parikh vector uniformly.
		 *
		 * We already know that the path to s' via the spanning tree is a short path. Lemma 27 says that the
		 * path going to s and then following s[t>s' must have this Parikh vector plus a constant number.
		 * Thus, their difference must be the same number in each component.
		 *
		 * Again, because determinism and persistence were already checked, if this does not hold, then P1 must
		 * be violated.
		 */

		SpanningTree<TransitionSystem, Arc, State> tree = SpanningTree.get(ts, ts.getInitialState());
		for (Arc chord : tree.getChords()) {
			State source = chord.getSource();
			State target = chord.getTarget();
			Bag<String> pSource = getParikhVectorReaching(tree, source);
			Bag<String> pTarget = getParikhVectorReaching(tree, target);
			pSource.add(chord.getLabel());

			int expected = -1;
			for (String event : ts.getAlphabet()) {
				int val = pSource.getCount(event) - pTarget.getCount(event);

				if (expected == -1)
					// First arc: We will expect this value for other arcs
					expected = val;

				if (val < 0 || val != expected) {
					debugFormat("Chord %s shows that P1 is not satisfied, because %s+%s-%s has "
							+ "either a negative entry or not all entries have the same "
							+ " number of occurrences",
							chord, tree.getEdgePathFromStart(source),
							chord.getLabel(), tree.getEdgePathFromStart(target));
					return true;
				}
			}
		}

		/*
		 * If we get here, then we know:
		 *
		 * - The Parikh vector of any cycle is a linear combination of the Parikh vectors of chords
		 * - All chords have constant (all entries the same) Parikh vectors
		 * - All chords have Parikh vectors >= 0
		 * => All cycles must have Parikh vectors of the form (m, ..., m)
		 * => There can be no cycle with a Parikh vector incomparable to (1, ..., 1)
		 */

		return false;
	}

	static private Bag<String> getParikhVectorReaching(SpanningTree<TransitionSystem, Arc, State> tree,
			State state) {
		Bag<String> result = new HashBag<>();
		while (!state.equals(tree.getStartNode())) {
			Arc arc = tree.getPredecessorEdge(state);
			result.add(arc.getLabel());
			state = arc.getSource();
		}
		return result;
	}

	/**
	 * Get the counter example that proves that smaller cycles than with Parikh vector 1 exists.
	 * @return Either an empty list or an ordered list of arcs that form a cycle around the initial state.
	 */
	public List<Arc> getCounterExample() {
		if (counterExample == null)
			return emptyList();
		return unmodifiableList(counterExample);
	}

	/**
	 * Check if a cycle with Parikh vector 1 was found.
	 * @return true if no such cycle was found.
	 */
	public boolean noPV1CycleFound() {
		return !cycleWithPV1Found;
	}

	/**
	 * Check if a cycle was found whose Parikh vector is incomparable to the all-ones-vector.
	 * @return true if such a cycle was found.
	 */
	public boolean incomparableCycleFound() {
		return foundIncomparableCycle;
	}

	/**
	 * Check if all small cycles have Parikh vector 1. This is equivalent to !noPV1CycleFound() &amp;&amp;
	 * getCounterExample().isEmpty().
	 * @return True if all small cycles have Parikh vector 1.
	 */
	public boolean smallCyclesHavePVOne() {
		return cycleWithPV1Found && counterExample == null && !foundIncomparableCycle;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
