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

package uniol.apt.analysis.live;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class implements various liveness tests for Petri nets.
 * @author Uli Schlachter, vsp
 */
public class Live {

	private Live() { }

	/**
	 * Find a dead transition in the Petri net.
	 * @param pn The Petri net that should be examined.
	 * @return A transition which is dead live, else null.
	 * @see #checkSimplyLive(PetriNet, Transition)
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public Transition findDeadTransition(PetriNet pn) throws UnboundedException {
		for (Transition t : pn.getTransitions())
			if (checkSimplyLive(pn, t) == null)
				return t;
		return null;
	}

	/**
	 * Check if the given transition is simply live. This means that the transition can fire in at least one
	 * reachable state and thus is not dead.
	 * @param pn The Petri net that should be examined.
	 * @param transition The transition that is checked.
	 * @return A firable firing sequence that ends with the given transition, or null.
	 */
	static public List<Transition> checkSimplyLive(PetriNet pn, Transition transition) {
		for (CoverabilityGraphEdge arc : CoverabilityGraph.get(pn).getEdges()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Transition trans = arc.getTransition();
			if (trans.equals(transition)) {
				// We found an edge which actually fires this transition!
				CoverabilityGraphNode source = arc.getSource();
				List<Transition> result = new ArrayList<>(source.getFiringSequence());
				result.add(transition);
				return result;
			}
		}
		// We checked the coverability graph and the wanted transition didn't show up. Thus it must be dead.
		return null;
	}

	/**
	 * Find a transition which is not weakly live.
	 * @param pn The Petri net that should be examined.
	 * @return A transition which is not weakly live, else null.
	 * @see #checkWeaklyLive(PetriNet, Transition)
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public Transition findNonWeaklyLiveTransition(PetriNet pn) throws UnboundedException {
		TransitionSystem lts = CoverabilityGraph.get(pn).toReachabilityLTS();
		Set<? extends Set<State>> components = Connectivity.getStronglyConnectedComponents(lts);
		for (Transition t : pn.getTransitions())
			if (!checkWeaklyLive(lts, components, t))
				return t;
		return null;
	}

	/**
	 * Check if the given transition is weakly live. This means that there exists an infinite fire sequence which
	 * fires the transition infinitely often.
	 * @param pn The Petri net that should be examined.
	 * @param transition The transition that is checked.
	 * @return True if the transition is weakly live, else false.
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public boolean checkWeaklyLive(PetriNet pn, Transition transition) throws UnboundedException {
		TransitionSystem lts = CoverabilityGraph.get(pn).toReachabilityLTS();
		Set<? extends Set<State>> components = Connectivity.getStronglyConnectedComponents(lts);
		return checkWeaklyLive(lts, components, transition);
	}

	/**
	 * Check if a transition is weakly live.
	 * @param lts The reachability graph of a PetriNet, generated via {@link CoverabilityGraph#toReachabilityLTS}.
	 * @param components The strongly connected components of the lts.
	 * @param transition The transition that should be checked.
	 * @return true If the transition is weakly live, else false.
	 */
	static private boolean checkWeaklyLive(TransitionSystem lts, Set<? extends Set<State>> components,
			Transition transition) {
		/* We are working with bounded Petri nets. Thus, an infinite fire sequence creates a circle in the
		 * reachability graph. This means that there exists an edge for our transition in the graph which is
		 * taken infinitely often. This means that both nodes of the transition belong to the same strongly
		 * connected component.
		 *
		 * For the other direction:
		 * If there is no edge for our transition which has both of its nodes in the same strongly connected
		 * component, there obviously can't be an infinite fire sequence which contains the transition
		 * infinitely often.
		 */
		for (Arc edge : lts.getEdges()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			// Look for edges labeled with our transition...
			Transition trans = (Transition) edge.getExtension(Transition.class.getName());
			if (!trans.equals(transition))
				continue;

			// ...where both endpoints are in the same component
			State source = edge.getSource();
			State target = edge.getTarget();
			for (Set<State> component : components) {
				if (component.contains(source)) {
					if (component.contains(target))
						// Both nodes in the same component
						return true;

					// We now know for sure that they can't be in the same component, because source
					// can only be in a single component
					break;
				}
			}
		}

		// We didn't find a suitable component
		return false;
	}

	/**
	 * Find a transition which is not strongly live.
	 * @param pn The Petri net that should be examined.
	 * @return A transition which is not strongly live, else null.
	 * @see #checkStronglyLive(PetriNet, Transition)
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public Transition findNonStronglyLiveTransition(PetriNet pn) throws UnboundedException {
		for (Transition t : pn.getTransitions())
			if (!checkStronglyLive(pn, t))
				return t;
		return null;
	}

	/**
	 * Check if the given transition is strongly live. This means that for every reachable state there exists a fire
	 * sequence after which this transition is activated.
	 * @param pn The Petri net that should be examined.
	 * @param transition The transition that is checked.
	 * @return True if the transition is strongly live, else false.
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public boolean checkStronglyLive(PetriNet pn, Transition transition) throws UnboundedException {
		return findKillingFireSequence(pn, transition) == null;
	}

	/**
	 * Find a firing sequence after which the given transition can never fire again. Such a firing sequence exists
	 * if and only if the given transition is not strongly live.
	 * @param pn The Petri net that should be examined.
	 * @param transition The transition that is checked.
	 * @return null if the transition is strongly live, else a firing sequence after which it can no longer fire.
	 * @throws UnboundedException If the reachability graph is unbounded.
	 */
	static public List<Transition> findKillingFireSequence(PetriNet pn, Transition transition)
			throws UnboundedException {
		// We are looking for a node from which no edge for our transition is reachable
		TransitionSystem lts = CoverabilityGraph.get(pn).toReachabilityLTS();
		Collection<State> nodes = new HashSet<>(lts.getNodes());

		// Look for edges labeled with our transition...
		for (Arc edge : lts.getEdges()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Transition trans = (Transition) edge.getExtension(Transition.class.getName());
			if (!trans.equals(transition))
				continue;
			// All of the nodes in this edge's preset can reach a state where the transition is activated
			handleStronglyLiveNode(nodes, edge.getSource());
		}

		// everything which can eventually enable transition was removed, so the transition is strongly live if
		// this collection is empty.
		if (nodes.isEmpty())
			return null;
		State node = nodes.iterator().next();
		return ((CoverabilityGraphNode) node.getExtension(CoverabilityGraphNode.class.getName()))
			.getFiringSequence();
	}

	// Recursively remove the preset from the set of nodes
	static private void handleStronglyLiveNode(Collection<State> nodes, State node) {
		Deque<State> toRemove = new ArrayDeque<>();
		toRemove.add(node);
		while (!toRemove.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			node = toRemove.remove();
			if (nodes.remove(node))
				// Node was not yet handled
				toRemove.addAll(node.getPresetNodes());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
