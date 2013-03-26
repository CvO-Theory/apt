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

package uniol.apt.analysis.coverability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.analysis.exception.UnboundedException;

/**
 * This class represents a coverability graph of a Petri net. Let's first define the reachability graph: The reachable
 * markings of a Petri net form a graph. The arcs between markings each belong to a transition which is fired for
 * getting from one transition to another. This graph may be infinitely large.
 *
 * The coverability graph is based on the reachability graph. However, the markings used may contain OMEGAs which means
 * that a place can have infinitely many tokens. This extension makes the coverability graph always bounded.
 * @author Uli Schlachter, vsp
 */
public class CoverabilityGraph {
	// The Petri net that we are handling

	private final PetriNet pn;
	// Map from visited markings to the corresponding nodes
	private final Map<Marking, CoverabilityGraphNode> states = new HashMap<>();
	// List of nodes which were generated but whose enabled transitions weren't handled yet.
	private final Set<CoverabilityGraphNode> unvisited = new HashSet<>();
	// List of nodes that were already visited, this is a list to implement iterators.
	private final List<CoverabilityGraphNode> nodes = new ArrayList<>();
	// Actual marking
	private Marking marking;

	/**
	 * Construct the coverability graph for a given Petri net.
	 * @param pn The Petri net whose coverability graph is wanted.
	 */
	public CoverabilityGraph(PetriNet pn) {
		this.pn = pn;
		addNode(null, pn.getInitialMarkingCopy(), null);
		marking = pn.getInitialMarkingCopy();
	}

	private boolean visitNode() {
		// Pick a random, unvisited node
		Iterator<CoverabilityGraphNode> it = unvisited.iterator();
		if (!it.hasNext()) {
			return false;
		}
		CoverabilityGraphNode node = it.next();

		// Make the node generate its postset
		node.getPostsetEdges();
		return true;
	}

	/**
	 * Generate the postset of a given node. This may only be called by CoverabilityGraphNode.
	 * @param node Node whose postset should get generated.
	 * @return The node's postset
	 */
	Set<CoverabilityGraphEdge> getPostsetEdges(CoverabilityGraphNode node) {
		// The node must be unvisited.
		boolean wasUnvisited = unvisited.remove(node);
		assert wasUnvisited == true;

		// Now follow all activated transitions of that node
		marking = new Marking(node.getMarking());
		Set<CoverabilityGraphEdge> result = new HashSet<>();
		for (Transition t : pn.getTransitions()) {
			if (!t.isFireable(marking)) {
				continue;
			}

			marking.fire(t);
			// checkCover() will also change the marking of the Petri net if some OMEGAs are created!
			checkCover(marking, node);
			result.add(addArc(t, marking, node));

			// We can't use t.unfire() here because checkCover() might have introduced OMEGAs into
			// the marking that wouldn't be undone, hence we do this.
			marking = node.getMarking();
		}

		return result;
	}

	/**
	 * Check if the given marking covers any markings on the current path.
	 * If the marking covers some other marking, suitable omegas are inserted.
	 * @param cur The marking to check.
	 * @param parent The immediate parent node.
	 * @return true if the marking does cover something.
	 */
	private boolean checkCover(Marking cur, CoverabilityGraphNode parent) {
		assert parent != null;
		while (parent != null) {
			Marking m = parent.getMarking();
			if (cur.covers(m)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	/**
	 * Add a new arc to the LTS.
	 * The arc starts in the marking given at the top of the current path.
	 * @param transition The transition for the arc.
	 * @param cur The marking to which the arc goes to.
	 * @param from The marking from which the arc originates.
	 */
	private CoverabilityGraphEdge addArc(Transition transition, Marking cur, CoverabilityGraphNode from) {
		CoverabilityGraphNode state = states.get(cur);
		if (state == null) {
			state = addNode(transition, cur, from);
		}

		return new CoverabilityGraphEdge(transition, from, state);
	}

	/**
	 * Add a node for the given marking to the LTS.
	 * Precondition: There is no node for that marking yet.
	 * @param mark The marking for which a node should be created.
	 * @param parent The parent of this marking. Used for tracing the path to the root.
	 * @return the new node.
	 */
	private CoverabilityGraphNode addNode(Transition transition, Marking mark, CoverabilityGraphNode parent) {
		assert states.get(mark) == null;

		// Copy the marking to make sure no one else messes with it.
		mark = new Marking(mark);
		CoverabilityGraphNode node = new CoverabilityGraphNode(this, transition, mark, parent);
		states.put(mark, node);
		unvisited.add(node);
		nodes.add(node);
		return node;
	}

	/**
	 * Get the initial node of this coverability graph.
	 * @return the inital node.
	 */
	public CoverabilityGraphNode getInitialNode() {
		return nodes.get(0);
	}

	/**
	 * Return an iterable for all nodes in this coverability graph.
	 * @return an iterable
	 */
	public Iterable<CoverabilityGraphNode> getNodes() {
		return new Iterable<CoverabilityGraphNode>() {

			@Override
			public Iterator<CoverabilityGraphNode> iterator() {
				return new Iterator<CoverabilityGraphNode>() {

					private int position = 0;

					@Override
					public boolean hasNext() {
						do {
							// Are we at the end yet?
							if (position < nodes.size()) {
								return true;
							}

							// Then try generating new nodes and try again
						} while (visitNode());

						return false;
					}

					@Override
					public CoverabilityGraphNode next() {
						// Make sure the next state is generated
						hasNext();
						return nodes.get(position++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Return an iterable for all edges in this coverability graph.
	 * @return an iterable
	 */
	public Iterable<CoverabilityGraphEdge> getEdges() {
		final CoverabilityGraph graph = this;
		return new Iterable<CoverabilityGraphEdge>() {

			@Override
			public Iterator<CoverabilityGraphEdge> iterator() {
				return new CoverabilityGraphEdgesIterator(graph);
			}
		};
	}

	/**
	 * Turn this coverability graph into a labeled transition system.
	 * @throws UnboundedException This exception is thrown when the Petri net is unbounded.
	 * @return The new transition system.
	 * @see #toCoverabilityLTS() For a version of this which does not reject unbounded nets.
	 */
	public TransitionSystem toReachabilityLTS() throws UnboundedException {
		return toLTS(true);
	}

	/**
	 * Turn this coverability graph into a labeled transition system.
	 * @return The new transition system.
	 * @see #toReachabilityLTS() For a version of this which rejects unbounded nets.  */
	public TransitionSystem toCoverabilityLTS() {
		try {
			return toLTS(false);
		} catch (UnboundedException e) {
			// This should never happen, because we used "false" as the parameter!
			throw new RuntimeException(e);
		}
	}

	/**
	 * Turn this coverability graph into a labeled transition system.
	 * @return The new transition system.
	 */
	private TransitionSystem toLTS(boolean onlyReachability) throws UnboundedException {
		Map<Marking, State> ltsStates = new HashMap<>();
		TransitionSystem lts = new TransitionSystem();
		lts.putExtension(PetriNet.class.getName(), this.pn);

		for (CoverabilityGraphNode node : this.getNodes()) {
			Marking mark = node.getMarking();
			assert ltsStates.get(mark) == null;

			State n = lts.createState();
			ltsStates.put(mark, n);
			n.putExtension(Marking.class.getName(), mark);
			n.putExtension(CoverabilityGraphNode.class.getName(), node);

			if (onlyReachability && mark.hasOmega()) {
				throw new UnboundedException(this.pn);
			}
		}

		for (CoverabilityGraphEdge edge : this.getEdges()) {
			State source = ltsStates.get(edge.getSource().getMarking());
			State target = ltsStates.get(edge.getTarget().getMarking());
			Transition transition = edge.getTransition();
			try {
				Arc e = lts.createArc(source.getId(), target.getId(), transition.getLabel());
				e.putExtension(Transition.class.getName(), transition);
				e.putExtension(CoverabilityGraphEdge.class.getName(), edge);
			} catch (ArcExistsException e) {
				// Ignore this. Continue your life. Go away. There is nothing to see here.
				//
				// Per definition, a LTS doesn't have arc weights. For a given source node, label and
				// target node, there can only be a single arc (or no arc at all). However, we just
				// calculated something which says otherwise. Since the definition is always right,
				// let's just ignore this calculation.
				//
				// (For unlabeled Petri-nets, this cannot happen)
				//
				// For everyone out there who uses the extension that we put on Arcs: You get some
				// random Transition/CoverabilityGraphEdge instance. It might be another Transition the
				// next time you run this code.
			}
		}

		// Set up the LTS' initial state
		Marking initialMarking = pn.getInitialMarkingCopy();
		State initialNode = ltsStates.get(initialMarking);
		lts.setInitialState(initialNode);
		assert initialNode != null;

		return lts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
