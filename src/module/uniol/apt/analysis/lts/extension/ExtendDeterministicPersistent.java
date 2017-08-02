/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

package uniol.apt.analysis.lts.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.analysis.exception.MustLeadToSameStateException;
import uniol.apt.analysis.exception.NoFiniteExtensionPossibleException;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.EquivalenceRelation;
import uniol.apt.util.Pair;
import uniol.apt.util.SpanningTree;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * Extend a transition system to a deterministic persistent transition system.
 *
 * @author vsp, Uli Schlachter
 */
public class ExtendDeterministicPersistent {
	private static class NeededArc extends Pair<State, String> {
		private NeededArc(State state, String label) {
			super(state, label);
		}

		public String toString() {
			return String.format("%s--%s->", getFirst().getId(), getSecond());
		}
	}

	private static List<String> arcListToEventList(List<Arc> arcs) {
		List<String> events = new ArrayList<>();
		for (Arc arc : arcs) {
			events.add(arc.getLabel());
		}
		return events;
	}

	private static List<String> residual(List<String> p, List<String> q) {
		ParikhVector qPV = new ParikhVector(q);
		List<String> res = new ArrayList<>();
		for (String event : p) {
			ParikhVector newPV = qPV.tryRemove(event, 1);
			if (newPV == null) {
				res.add(event);
			} else {
				qPV = newPV;
			}
		}
		return res;
	}

	/**
	 * Extend a transition system to a deterministic persistent transition system.
	 * @param ts The transition system to extend
	 *
	 * @param maxPhase2Rounds How many rounds which add new states should maximally be done?
	 * @return Collection of equivalence classes of needed but not added arcs (arcs are equivalent if they must lead
	 * to the same state). This is empty if maxPhase2Rounds is sufficently high.
	 * @throws NoFiniteExtensionPossibleException Thrown if the algorithm would lead to the addition of infinitely
	 * many states.
	 * @throws NonDeterministicException Thrown the given transition system is not deterministic.
	 * @throws MustLeadToSameStateException Thrown if a persistence diamond can't get constructed because of already
	 * existing arcs (i.e. the transition system or a partially extended version of it isn't fully deterministic)
	 */
	public Collection<Set<Pair<State, String>>> extendTs(TransitionSystem ts, int maxPhase2Rounds) throws
			NoFiniteExtensionPossibleException, NonDeterministicException, MustLeadToSameStateException {
		new Deterministic(ts).throwIfNonDeterministic();

		int rounds    = 0;
		int phase2Ctr = 0;
		Collection<Set<Pair<State, String>>> eqRel;
		while (true) { // we need to exit the loop in the middle of its body :-(
			debugFormat("Starting round %d", rounds++);
			eqRel = findNeededArcs(ts.getNodes());
			if (eqRel.isEmpty()) {
				break;
			}
			Pair<Collection<Set<Pair<State, String>>>, Boolean> phase1Pair = completeDiamonds(ts, eqRel);
			if (phase1Pair.getSecond()) {
				// arcs got added, reanalyse the transition system
				continue;
			}
			eqRel = phase1Pair.getFirst();
			phase2Ctr++;
			if (eqRel.isEmpty() || phase2Ctr > maxPhase2Rounds) {
				break;
			}
			Map<State, Set<String>> uncompletableStates = checkFiniteCompletionPossible(ts);
			for (Set<Pair<State, String>> arcSet : eqRel) {
				for (Pair<State, String> neededArc : arcSet) {
					if (uncompletableStates.containsKey(neededArc.getFirst())
							&& uncompletableStates.get(neededArc.getFirst())
							.contains(neededArc.getSecond()))
						throw new NoFiniteExtensionPossibleException(neededArc.getFirst());
				}
			}
			constructDiamonds(ts, eqRel);
		}

		return eqRel;
	}

	private Map<State, Set<String>> checkFiniteCompletionPossible(TransitionSystem ts) {
		Map<State, Set<String>> uncompletableStates = new HashMap<>();
		SpanningTree<TransitionSystem, Arc, State> st = SpanningTree.get(ts);
		for (Arc chord : st.getChords()) {
			List<Arc> sourcePath = new ArrayList<>(st.getEdgePathFromStart(chord.getSource()));
			sourcePath.add(chord);
			List<Arc> targetPath = st.getEdgePathFromStart(chord.getTarget());
			List<String> sourceSeq = arcListToEventList(sourcePath);
			List<String> targetSeq = arcListToEventList(targetPath);
			List<String> sourceResidual = residual(sourceSeq, targetSeq);
			List<String> targetResidual = residual(targetSeq, sourceSeq);
			if (!sourceResidual.isEmpty() && !targetResidual.isEmpty()) {
				Set<String> uncompletableEvents = uncompletableStates.get(chord.getTarget());
				if (uncompletableEvents == null) {
					uncompletableEvents = new HashSet<>();
					uncompletableStates.put(chord.getTarget(), uncompletableEvents);
				}
				uncompletableEvents.add(sourceResidual.get(0));
				uncompletableEvents.add(targetResidual.get(0));
			}
		}
		return uncompletableStates;
	}

	/**
	 * Phase 0: Create an equivalence relation of (possible) arcs which must lead to the same state.
	 *
	 * @param states The collection of states to use as base of a persistence diamonds.
	 * @return The equivalence relation
	 */
	private Collection<Set<Pair<State, String>>> findNeededArcs(Iterable<State> states) {
		EquivalenceRelation<Pair<State, String>> ret = new EquivalenceRelation<>();
		// Go through all states
		for (State state : states) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Map<String, State> postset = getStatePostset(state);
			Set<String> labels = postset.keySet();
			// Go through all pairs of enabled labels
			for (Pair<String, String> labelPair : new DifferentPairsIterable<>(labels)) {
				String label1 = labelPair.getFirst();
				String label2 = labelPair.getSecond();
				State state1 = postset.get(label1);
				State state2 = postset.get(label2);
				ret.joinClasses(new NeededArc(state2, label1), new NeededArc(state1, label2));
			}
		}
		return ret;
	}

	/**
	 * Phase 1: complete persistence diamonds, where only one arc is missing (and also add other arcs which must
	 * lead to the same state
	 *
	 * @param ts the transition system
	 * @param eqClasses Equivalence relation of all (possible) arcs calculated in phase 0.
	 * @return A new equivalence relation only containing unhandled arcs.
	 * @throws MustLeadToSameStateException Thrown if a persistence diamond can't get constructed because of already
	 * existing arcs (i.e. the transition system or a partially extended version of it isn't fully deterministic)
	 */
	private Pair<Collection<Set<Pair<State, String>>>, Boolean> completeDiamonds(TransitionSystem ts,
			Collection<Set<Pair<State, String>>> eqClasses) throws MustLeadToSameStateException {
		Collection<Set<Pair<State, String>>> unsolvedClasses = new ArrayList<>();
		boolean modified = false;
		for (Set<Pair<State, String>> eqClass : eqClasses) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Arc succ = null;
			Pair<State, String> succArc = null;
			for (Pair<State, String> arcPair : eqClass) {
				Arc curSucc = getArc(arcPair.getFirst(), arcPair.getSecond());
				if (curSucc != null) {
					if (succ == null) {
						succ    = curSucc;
						succArc = arcPair;
					} else if (!succ.getTarget().equals(curSucc.getTarget())) {
						throw new MustLeadToSameStateException(succ, curSucc);
					}
				}
			}
			if (succ != null) {
				modified |= mapEqClass(ts, eqClass, succ.getTarget());
			} else {
				unsolvedClasses.add(eqClass);
			}
		}
		return new Pair<>(unsolvedClasses, modified);
	}

	/**
	 * Phase 2: construct new states for persistence diamonds with two missing arcs
	 *
	 * @param ts the transition system
	 * @param unsolvedClasses Equivalence relation of all (possible) arcs calculated in phase 0.
	 */
	private void constructDiamonds(TransitionSystem ts, Collection<Set<Pair<State, String>>> unsolvedClasses) {
		for (Set<Pair<State, String>> eqClass : unsolvedClasses) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			State succ = ts.createState();
			mapEqClass(ts, eqClass, succ);
			debugFormat("Added new State %s as target of %s.", eqClass, succ);
		}
	}

	private boolean mapEqClass(TransitionSystem ts, Set<Pair<State, String>> eqClass, State state) {
		boolean modified = false;
		for (Pair<State, String> arcPair : eqClass) {
			try {
				ts.createArc(arcPair.getFirst(), state, arcPair.getSecond());
				modified = true;
			} catch (ArcExistsException ex) {
				/* ignore it:
				 * This method was called from phase 1 and this arc was one of the arcs leading to the
				 * selection of this state as successor */
			}
		}
		return modified;
	}

	// Get the postset of a state as a Map which maps a label to a state
	private Map<String, State> getStatePostset(State node) {
		Map<String, State> result = new HashMap<>();
		for (Arc arc : node.getPostsetEdges()) {
			State old = result.put(arc.getLabel(), arc.getTarget());
			assert old == null;
		}
		result = Collections.unmodifiableMap(result);
		return result;
	}

	// Get the state that is reached via "label" from "state"
	private Arc getArc(State node, String label) {
		for (Arc arc : node.getPostsetEdges())
			if (label.equals(arc.getLabel()))
				return arc;
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
