/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.pnanalysis;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogicComplex;
import uniol.apt.analysis.isomorphism.IsomorphismLogicComplex.ExtendedState;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.reversible.ReversibleTS;
import uniol.apt.analysis.separation.LargestK;
import uniol.apt.analysis.snet.SNet;
import uniol.apt.generator.marking.MarkingNetGenerator;
import uniol.apt.generator.tnet.TNetGenerator;
import uniol.apt.util.Pair;

/**
 * Checks for a given Petri net depending on g and k, if there is a T-system,
 * whose reachability graph is isomorph to the reachability graph of the given
 * Petri net. Therefore the Petri net has to fulfill some preconditions:
 * <ul>
 * <li>is plain
 * <li>the initial marking is a k-marking with k &gt;= 2
 * <li>is bounded
 * <li>is reversible
 * <li>is persistent
 * <li>all smallest cycles
 * <li>the same parikh vector
 * </ul>
 *
 * @author Raffaela Ferrari, Björn von der Linde
 */
public class PnAnalysis {

	/**
	 * Checks for a given Petri net depending on g and k, if there is a T-system, whose reachability graph is
	 * isomorph to the reachability graph of the given Petri net.
	 * @param pn       the given Petri net, for which the check has to be performed.
	 * @param g        maximum number of places, for which T-systems should be tested.
	 * @param k        maximum number of tokens, for which T-systems should be tested. If k is null, the standard
	 *                 value is 10 for T-system, which aren't cycles, and for cycles all numbers of tokens will be
	 *                 tested at the standard
	 * @param randomly if true, only one randomly selected T-system is checked, otherwise all T-systems are checked
	 * @return null, if no T-systems are found, otherwise the first found T-system, whose reachability graph is
	 *         isomorph to the reachability graph of the given Petri net
	 * @throws PreconditionFailedException is thrown, if the given Petri net breaks one of the preconditions.
	 */
	public PetriNet checkAllIsomorphicTSystemsForPetriNet(PetriNet pn, Integer g, Integer k, boolean randomly)
		throws PreconditionFailedException {
		// Check preconditions for Petri net
		if (!new Plain().checkPlain(pn)) {
			throw new PreconditionFailedException("The input Petri net is not plain.");
		}
		LargestK lk = new LargestK(pn);
		if (lk.computeLargestK() < 2) {
			throw new PreconditionFailedException("The input Petri net has no k-marking >=2.");
		}

		// Step 1: Generate reachability graph of the given Petri net
		CoverabilityGraph cover = CoverabilityGraph.get(pn);
		TransitionSystem reachabilitylts1 = cover.toReachabilityLTS();

		// Check preconditions for lts
		ComputeSmallestCycles sc = new ComputeSmallestCycles();
		if (!sc.checkSamePVs(reachabilitylts1)) {
			throw new PreconditionFailedException("Not all smallest cycles have the"
				+ " same parikh vectors.");
		}
		ReversibleTS reversibleNet = new ReversibleTS(reachabilitylts1);
		reversibleNet.check();
		if (!reversibleNet.isReversible()) {
			throw new PreconditionFailedException("The input Petri net is not reversible.");
		}
		PersistentTS persistentNet = new PersistentTS(reachabilitylts1);
		if (!persistentNet.isPersistent()) {
			throw new PreconditionFailedException("The input Petri net is not persistent.");
		}

		int maxTokens = (k == null) ? 10 : k;
		// Step 2a: Check reachability graph of a randomly selected T-System for
		// isomorphie
		if (randomly) {
			PetriNet tSystem = RandomTNetGenerator.createRandomTSystem(g, k);
			if (isIsomorphic(reachabilitylts1, tSystem)) {
				return tSystem;
			} else {
				return null;
			}
		}

		// Step 2b: Check reachability graphs of T-Systems of the generator for
		// isomorphie
		// At first check all cycle-T-systems
		Iterator<PetriNet> iteratorCycleTNets = new CycleTNetIterator(reachabilitylts1.getNodes().size(), g, k,
			reachabilitylts1.getInitialState().getPostsetEdges().size());
		while (iteratorCycleTNets.hasNext()) {
			PetriNet tSystem = iteratorCycleTNets.next();
			if (isIsomorphic(reachabilitylts1, tSystem)) {
				return tSystem;
			}
		}
		// Check all T-systems
		for (PetriNet tNet : new TNetGenerator(g, false)) {
			if (!new SNet(tNet).testPlainSNet()) {
				for (PetriNet tSystem : new MarkingNetGenerator(tNet, maxTokens)) {
					if (isIsomorphic(reachabilitylts1, tSystem)) {
						return tSystem;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if the reachability graph of the given Petri net is isomorph to the reachability graph of a T-system.
	 * @param t1      the reachability graph of the given Petri net
	 * @param tSystem the T-system, for which the reachability graph is checked for isomorphie
	 * @return true, if the reachability graphs are isomorph, otherwise false
	 * @throws UnboundedException is thrown, if the T-system is unbounded.
	 */
	private boolean isIsomorphic(TransitionSystem t1, PetriNet tSystem) throws UnboundedException {
		CoverabilityGraph coverTSystem = CoverabilityGraph.get(tSystem);
		TransitionSystem reachabilitylts2;
		try {
			reachabilitylts2 = coverTSystem.toReachabilityLTS();
		} catch (UnboundedException e) {
			return false;
		}
		IsomorphismLogicComplex iso = new IsomorphismLogicComplex(t1, reachabilitylts2, false);
		if (iso.isIsomorphic()) {
			// Adjust Labels of T-System, to check if strong isomorphic solution
			// exists
			return adjustLabels(t1, reachabilitylts2, tSystem, iso.getFinalState());
			// return true;
		}
		return false;
	}

	/**
	 * For a given transitions-system t1 and a T-System tSystem, labels in tSystem will be adjusted, so that a found
	 * weak isomorphism (isomorphism that ignores labels) between both systems may be a strong isomorphism
	 * (isomorphism, that doesn't ignore labels)
	 * @param t1              Reachability-Graph of given pn
	 * @param t2              Reachability-Graph of given T-System
	 * @param tSystem         T-System to be checked if isomorphic
	 * @param isomorphicPairs State with isomorphic pairs of nodes, that were found for weak isomorphism ("weak":
	 *                        labels are ignored)
	 * @return true if t1 and tSystem are strong isomorphic ("strong": labels are not ignored)
	 */
	private boolean adjustLabels(TransitionSystem t1, TransitionSystem t2, PetriNet tSystem,
		Deque<ExtendedState> isomorphicPairs) {
		isomorphicPairs.removeFirst();
		Set<String> labels = new HashSet<>();
		Set<ExtendedState> w = new HashSet<>();
		Deque<Pair<ExtendedState, List<ExtendedState>>> stack1 = new LinkedList<>();
		stack1.push(new Pair<>(isomorphicPairs.getFirst(), getNextIsomorphicPairs(isomorphicPairs,
			isomorphicPairs.getFirst(), t1, t2)));
		w.add(isomorphicPairs.getFirst());
		while (!stack1.isEmpty()) {
			List<ExtendedState> l = stack1.peek().getSecond();
			if (!l.isEmpty()) {
				ExtendedState firstOfL = l.remove(0);
				State n = stack1.peek().getFirst().getN();
				State m = stack1.peek().getFirst().getM();
				String label = null;
				for (Arc edge : t1.getPostsetEdges(n)) {
					if (edge.getTarget().equals(firstOfL.getN())) {
						label = edge.getLabel();
					}
				}
				boolean transitionFound = false;
				Map<Arc, String> labelsToChange = new HashMap<>();
				for (Arc edge : t2.getPostsetEdges(m)) {
					if (edge.getTarget().equals(firstOfL.getM())) {
						if (!edge.getLabel().equals(label)
							&& !labels.contains(edge.getLabel())) {
							for (Arc t2Edge : t2.getEdges()) {
								if ((!edge.equals(t2Edge))
									&& edge.getLabel().equals(t2Edge.getLabel())) {
									labelsToChange.put(t2Edge, label);
								}
							}
							tSystem.getTransition(edge.getLabel()).setLabel(label);
							labelsToChange.put(edge, label);
							labels.add(label);
							transitionFound = true;
						} else if (edge.getLabel().equals(label)) {
							transitionFound = true;
						}
					}
				}
				for (Map.Entry<Arc, String> entry : labelsToChange.entrySet()) {
					entry.getKey().setLabel(entry.getValue());
				}
				if (transitionFound) {
					if (!w.contains(firstOfL)) {
						w.add(firstOfL);
						stack1.push(new Pair<>(firstOfL,
							getNextIsomorphicPairs(isomorphicPairs, firstOfL, t1, t2)));
					}
				} else {
					return false;
				}
			} else {
				stack1.pop();
			}
		}
		return true;
	}

	/**
	 * This function determines all postset isomorphic ExtendedStates of the given ExtendedState. Therefore it
	 * shows, if there is a isomorphic ExtendedState in isomorphicPairs, in which n is a state of the postset states
	 * of the first state and m is a state of the postset states of the second state of the given ExtendedState. If
	 * true, then it will be added to the List.
	 * @param isomorphicPairs State with isomorphic pairs of nodes, that were found for weak isomorphism ("weak":
	 *                        labels are ignored)
	 * @param exState         ExtendedState, for which postset isomorphic ExtendedState should be found
	 * @param t1              Reachability-Graph of given pn
	 * @param t2              Reachability-Graph of given t-system
	 * @return a List of ExtendedStates
	 */
	private List<ExtendedState> getNextIsomorphicPairs(Deque<ExtendedState> isomorphicPairs, ExtendedState exState,
		TransitionSystem t1, TransitionSystem t2) {
		List<ExtendedState> nextIsomorphicPairs = new LinkedList<>();
		for (State t1State : t1.getPostsetNodes(exState.getN())) {
			for (State t2State : t2.getPostsetNodes(exState.getM())) {
				Iterator<ExtendedState> iterator = isomorphicPairs.iterator();
				while (iterator.hasNext()) {
					ExtendedState extendedState = iterator.next();
					if (extendedState.getN().equals(t1State)
						&& extendedState.getM().equals(t2State)) {
						nextIsomorphicPairs.add(extendedState);
					}
				}
			}
		}
		return nextIsomorphicPairs;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
