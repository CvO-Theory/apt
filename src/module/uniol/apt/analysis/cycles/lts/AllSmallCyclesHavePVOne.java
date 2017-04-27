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
import static uniol.apt.util.DebugUtil.debug;
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
	private final boolean onlyMultipleOfPV1Found;
	private final boolean cycleWithPV1Found;

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
			this.onlyMultipleOfPV1Found = true;
			this.cycleWithPV1Found = true;
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

		/*
		 * Combining the above (Preconditions: determinism, persistence, reversibility, total reachability, P1):
		 *
		 * (1) We know that the Parikh vector of any cycle is a combination of the Parikh vector of chords.
		 * (2) For any chord s[t>s' we have P(s)+1_t >= P(s').
		 * (3) For any chord, all entries must be the same.
		 *
		 * By (1) and P1, some chord must have a Parikh vector containing only ones.
		 *
		 * (Thanks to Harro Wimmel for this insight)
		 */

		SpanningTree<TransitionSystem, Arc, State> tree = SpanningTree.get(ts, ts.getInitialState());
		boolean pv1Seen = false;
		for (Arc chord : tree.getChords()) {
			State source = chord.getSource();
			State target = chord.getTarget();
			Bag<String> pSource = getParikhVectorReaching(tree, source);
			Bag<String> pTarget = getParikhVectorReaching(tree, target);
			pSource.add(chord.getLabel());

			int expected = -1;

			for (String event : ts.getAlphabet()) {
				int val = pSource.getCount(event) - pTarget.getCount(event);
				pv1Seen |= val == 1;

				if (expected == -1)
					// First arc: We will expect this value for other arcs
					expected = val;

				if (val < 0 || val != expected) {
					debugFormat("Chord %s shows that P1 is not satisfied, because %s+%s-%s has "
							+ "entry %d for event %s, but should have %d",
							chord, tree.getEdgePathFromStart(source), chord.getLabel(),
							tree.getEdgePathFromStart(target), val, event, expected);
					this.cycleWithPV1Found = pv1Seen;
					this.onlyMultipleOfPV1Found = false;
					return;
				}
			}
		}

		this.onlyMultipleOfPV1Found = true;
		this.cycleWithPV1Found = pv1Seen;
		if (!pv1Seen) {
			debug("No cycle with Parikh vector all-ones seen");
			return;
		}

		/*
		 * If we get here, then we know:
		 *
		 * - There is a chord with Parikh vector all-ones
		 * - All other chords have a multiple of this Parikh vector.
		 *
		 * We can now construct a small cycle with Parikh vector 1 by taking the corresponding chord s[t>s' and
		 * applying Keller's theorem on the path i-to-s + s[t>s' and the path i-to-s'. This will yield an empty
		 * path and a cycle around s' with Parikh vector all ones. Since all chords are a multiple of this, any
		 * cycle (which is a linear combination of chords) must have a larger or equal Parikh vector.
		 *
		 * (Thanks to Harro Wimmel for this insight)
		 */
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
	 * Check if a cycle with Parikh vector 1 was found.
	 * @return true if no such cycle was found.
	 */
	public boolean noPV1CycleFound() {
		return !cycleWithPV1Found;
	}

	/**
	 * Check if a cycle with Parikh vector not a multiple of 1 was found.
	 * @return true if such a cycle was found.
	 */
	public boolean nonMultipleOfPV1CycleFound() {
		return !onlyMultipleOfPV1Found;
	}

	/**
	 * Check if all small cycles have Parikh vector 1.
	 * @return True if all small cycles have Parikh vector 1.
	 */
	public boolean smallCyclesHavePVOne() {
		return cycleWithPV1Found && onlyMultipleOfPV1Found;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
