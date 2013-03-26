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

package uniol.apt.analysis.isomorphism;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.bisimulation.Pair;

/**
 * Check if two nets are isomorphic by checking if the two reachability graphs are equal.
 * This code is a (more or less direct) port of the code of a diploma
 * thesis by Florian Hinz. The original code is written in C++.
 *
 * @author Florian Hinz, Maike Schwammberger, Uli Schlachter
 */
public class IsomorphismLogic {
	private Deque<ExtendedState> finalState = new LinkedList<>();

	private final int numNodes;
	private final TransitionSystem graph1;
	private final TransitionSystem graph2;
	private final List<State> nodes1List = new ArrayList<>();
	private final List<State> nodes2List = new ArrayList<>();
	private final Map<State, State> core1 = new HashMap<>();
	private final Map<State, State> core2 = new HashMap<>();
	private final Map<State, Integer> in1 = new HashMap<>();
	private final Map<State, Integer> in2 = new HashMap<>();
	private final Map<State, Integer> out1 = new HashMap<>();
	private final Map<State, Integer> out2 = new HashMap<>();
	private final boolean result;
	private final boolean checkLabels;

	/**
	 * Constructor
	 *
	 * @param pn1
	 * @param pn2
	 * @throws UnboundedException
	 */
	public IsomorphismLogic(PetriNet pn1, PetriNet pn2) throws UnboundedException {
		this(pn1, pn2, false);
	}

	/**
	 * Constructor for testing if two Petri nets have isomorphic reachability graphs.
	 *
	 * @param pn1 The first PN to test.
	 * @param pn2 The second PN to test.
	 * @param checkLabels If true, "strong isomorphism" is tested and labels have to be identical between the PN.
	 *                    Otherwise labels are ignored.
	 * @throws UnboundedException If one of the input Petri nets is unbounded.
	 */
	public IsomorphismLogic(PetriNet pn1, PetriNet pn2, boolean checkLabels) throws UnboundedException {
		this(new CoverabilityGraph(pn1).toReachabilityLTS(), new CoverabilityGraph(pn2).toReachabilityLTS(),
				checkLabels);
	}

	/**
	 *
	 * @param lts1
	 * @param lts2
	 */
	public IsomorphismLogic(TransitionSystem lts1, TransitionSystem lts2) {
		this(lts1, lts2, false);
	}

	/**
	 * Constructor for testing if two labelled transition systems are isomorphic.
	 *
	 * @param lts1 The first LTS to test.
	 * @param lts2 The second LTS to test.
	 * @param checkLabels If true, "strong isomorphism" is tested and labels have to be identical between the PN.
	 *                    Otherwise labels are ignored.
	 */
	public IsomorphismLogic(TransitionSystem lts1, TransitionSystem lts2, boolean checkLabels) {
		this.checkLabels = checkLabels;
		this.graph1 = lts1;
		this.graph2 = lts2;
		numNodes = lts1.getNodes().size();

		// Check trivial case
		if (lts1.getNodes().size() != lts2.getNodes().size()) {
			result = false;
			return;
		}

		// Add all nodes into the list so that the initial state gets index 0
		nodes1List.add(lts1.getInitialState());
		for (State n : lts1.getNodes()) {
			if (!n.equals(lts1.getInitialState())) {
				nodes1List.add(n);
			}
		}
		nodes2List.add(lts2.getInitialState());
		for (State n : lts2.getNodes()) {
			if (!n.equals(lts2.getInitialState())) {
				nodes2List.add(n);
			}
		}

		/*
		 * Create zeroth state
		 * (this one doesn't yet include any pair of isomorphic nodes, of course).
		 * Then start isomorphism-check for zeroth state.
		 */
		result = doMatch(new ExtendedState());
	}

	/**
	 * Returns result of algorithm.
	 *
	 * @return
	 */
	public boolean isIsomorphic() {
		return result;
	}

	/**
	 * Isomorphism-check for current state
	 *
	 * @param state current state
	 * @return true if node-pair m, n in state s is isomorphic.
	 */
	private boolean doMatch(ExtendedState state) {
		// Statal-tree, that stores all states
		Deque<ExtendedState> states = new LinkedList<>();

		// Add current state to list of states
		states.addLast(state);
		int depth = 0;

		while (!states.isEmpty()) {
			ExtendedState s = states.getLast();
			depth = s.depth;

			if (depth > 0) {
				core1.put(s.n, s.m);
				core2.put(s.m, s.n);
			}
			if (depth == numNodes) {
				break;
			}
			if (!s.active) {
				computeTerminalSets(depth, s);
			}

			Pair<State, State> node = computeP(depth, s);

			State n, m;
			//boolean that is true, if (m, n) is an isomorphic pair
			boolean goodState = false;
			s.active = true;
			for (; node != null; node = computeP(depth, s)) {
				n = node.getFirst();
				m = node.getSecond();

				//if (m,n) is an isomorphic pair
				if (isFeasible(n, m)) {
					goodState = true;

					//increment depth because of new isomorphic pair and
					//set new pair (n,m) as current pair in new state
					ExtendedState news = new ExtendedState(s, depth + 1, n, m);

					if (!in1.containsKey(n)) {
						in1.put(n, depth + 1);
						news.numin1++;
					}
					if (!out1.containsKey(n)) {
						out1.put(n, depth + 1);
						news.numout1++;
					}
					if (!in2.containsKey(m)) {
						in2.put(m, depth + 1);
						news.numin2++;
					}
					if (!out2.containsKey(m)) {
						out2.put(m, depth + 1);
						news.numout2++;
					}

					//Add new state to state-tree
					states.addLast(news);

					break;
				}
			}

			//Discard current state if no isomorphic pair has been found
			if (!goodState) {
				rollback(depth, s.n, s.m);
				ExtendedState last = states.removeLast();
				assert last == s;
			}

		}

		//Set final state, to get pairs of isomorphic nodes.
		setFinalState(states);

		return (depth == numNodes);
	}

	/**
	 * Call this method if a state should get discarded.
	 *
	 * @param depth depth of state
	 * @param nodeN current node of pn1
	 * @param nodeM current node of pn2
	 */
	private void rollback(int depth, State nodeN, State nodeM) {
		State old;
		old = core1.remove(nodeN);
		assert depth == 0 || old != null;
		old = core2.remove(nodeM);
		assert depth == 0 || old != null;
		for (State n : nodes1List) {
			if (in1.containsKey(n) && in1.get(n) <= depth) {
				in1.remove(n);
			}
			if (out1.containsKey(n) && out1.get(n) <= depth) {
				out1.remove(n);
			}
		}
		for (State m : nodes2List) {
			if (in2.containsKey(m) && in2.get(m) <= depth) {
				in2.remove(m);
			}
			if (out2.containsKey(m) && out2.get(m) <= depth) {
				out2.remove(m);
			}
		}
	}

	/**
	 * Check if a given pair of nodes m, n is isomorphic.
	 *
	 * @param nodeN A node in net pn1
	 * @param nodeM A node in net pn2
	 * @return true if pair (m, n) is part of isomorphism
	 */
	private boolean isFeasible(State nodeN, State nodeM) {
		int tin1 = 0, tin2 = 0, tout1 = 0, tout2 = 0, new1 = 0, new2 = 0;

		// Only an initial state can map to an initial state, so if just one of these is an initial state, the
		// combination is not feasible. (Yes, this really wants to compare references and not equals())
		if ((graph1.getInitialState() == nodeN) != (graph2.getInitialState() == nodeM)) {
			return false;
		}

		for (Arc inN : nodeN.getPresetEdges()) {
			if (core1.containsKey(inN.getSource())) {
				if (!checkMatchingArc(core1.get(inN.getSource()), nodeM, inN.getLabel())) {
					return false;
				}
			} else {
				if (in1.containsKey(inN.getSource())) {
					tin1++;
				}
				if (out1.containsKey(inN.getSource())) {
					tout1++;
				}
				if (!in1.containsKey(inN.getSource()) && !out1.containsKey(inN.getSource())) {
					new1++;
				}
			}
		}

		for (Arc outN : nodeN.getPostsetEdges()) {
			if (core1.containsKey(outN.getTarget())) {
				if (!checkMatchingArc(nodeM, core1.get(outN.getTarget()), outN.getLabel())) {
					return false;
				}
			} else {
				if (in1.containsKey(outN.getTarget())) {
					tin1++;
				}
				if (out1.containsKey(outN.getTarget())) {
					tout1++;
				}
				if (!in1.containsKey(outN.getTarget()) && !out1.containsKey(outN.getTarget())) {
					new1++;
				}
			}
		}

		for (Arc inM : nodeM.getPresetEdges()) {
			if (core2.containsKey(inM.getSource())) {
				if (!checkMatchingArc(core2.get(inM.getSource()), nodeN, inM.getLabel())) {
					return false;
				}
			} else {
				if (in2.containsKey(inM.getSource())) {
					tin2++;
				}
				if (out2.containsKey(inM.getSource())) {
					tout2++;
				}
				if (!in2.containsKey(inM.getSource()) && !out2.containsKey(inM.getSource())) {
					new2++;
				}
			}
		}

		for (Arc outM : nodeM.getPostsetEdges()) {
			if (core2.containsKey(outM.getTarget())) {
				if (!checkMatchingArc(nodeN, core2.get(outM.getTarget()), outM.getLabel())) {
					return false;
				}
			} else {
				if (in2.containsKey(outM.getTarget())) {
					tin2++;
				}
				if (out2.containsKey(outM.getTarget())) {
					tout2++;
				}
				if (!in2.containsKey(outM.getTarget()) && !out2.containsKey(outM.getTarget())) {
					new2++;
				}
			}
		}

		return (tin1 == tin2) && (tout1 == tout2) && (new1 == new2);
	}

	/**
	 * Compute all pairs of nodes, that might be isomorphic pairs in state s
	 * (Check if they really are isomorphic follows in feasible(m, n))
	 *
	 * @param depth depth of current state in state-tree
	 * @param s current state
	 * @return all pairs of potential isomorphic nodes
	 */
	private Pair<State, State> computeP(int depth, ExtendedState s) {
		State nodeId = null;

		if (s.numout1 > depth && s.numout2 > depth) {
			if (s.mint2out == null) {
				for (State i : nodes2List) {
					if (out2.containsKey(i) && !core2.containsKey(i)) {
						nodeId = i;
						break;
					}
				}

				if (nodeId == null) {
					return null;
				}
				s.mint2out = nodeId;
			}
			while (s.curnode < numNodes) {
				s.curnode++;
				if (out1.containsKey(nodes1List.get(s.curnode - 1)) && !core1.containsKey(nodes1List.get(s.curnode - 1))) {
					return new Pair<>(nodes1List.get(s.curnode - 1), s.mint2out);
				}
			}
		} else if ((s.numout1 <= depth && s.numout2 <= depth) && (s.numin1 > depth && s.numin2 > depth)) {
			if (s.mint2in == null) {
				for (State i : nodes2List) {
					if (!in2.containsKey(i) && !core2.containsKey(i)) {
						nodeId = i;
						break;
					}
				}

				if (nodeId == null) {
					return null;
				}
				s.mint2in = nodeId;
			}

			while (s.curnode < numNodes) {
				s.curnode++;
				if (in1.containsKey(nodes1List.get(s.curnode - 1)) && !core1.containsKey(nodes1List.get(s.curnode - 1))) {
					return new Pair<>(nodes1List.get(s.curnode - 1), s.mint2in);
				}
			}
		} else if (s.numout1 == depth && s.numout2 == depth && s.numin1 == depth && s.numin2 == depth) {
			if (s.minn2m2 == null) {
				for (State n2 : nodes2List) {
					if (!core2.containsKey(n2)) {
						nodeId = n2;
						break;
					}
				}
				if (nodeId == null) {
					return null;
				}
				s.minn2m2 = nodeId;
			}
			while (s.curnode < numNodes) {
				s.curnode++;
				if (!core1.containsKey(nodes1List.get(s.curnode - 1))) {
					return new Pair<>(nodes1List.get(s.curnode - 1), s.minn2m2);
				}
			}
		}

		return null;
	}

	private void computeTerminalSets(int depth, ExtendedState s) {
		for (State src : nodes1List) {
			if (core1.containsKey(src)) {
				for (Arc outArc : src.getPostsetEdges()) {
					if (!core1.containsKey(outArc.getTarget())) {
						if (!out1.containsKey(outArc.getTarget())) {
							out1.put(outArc.getTarget(), depth);
							s.numout1++;
						}
					}
				}
				for (Arc inArc : src.getPresetEdges()) {
					if (!core1.containsKey(inArc.getSource())) {
						if (!in1.containsKey(inArc.getSource())) {
							in1.put(inArc.getSource(), depth);
							s.numin1++;
						}
					}
				}
			}
		}

		for (State src : nodes2List) {
			if (core2.containsKey(src)) {
				for (Arc outArc : src.getPostsetEdges()) {
					if (!core2.containsKey(outArc.getTarget())) {
						if (!out2.containsKey(outArc.getTarget())) {
							out2.put(outArc.getTarget(), depth);
							s.numout2++;
						}
					}
				}
				for (Arc inArc : src.getPresetEdges()) {
					if (!core2.containsKey(inArc.getSource())) {
						if (!in2.containsKey(inArc.getSource())) {
							in2.put(inArc.getSource(), depth);
							s.numin2++;
						}
					}
				}
			}
		}
	}

	private boolean checkMatchingArc(State source, State target, String label) {
		for (Arc arc : source.getPostsetEdges()) {
			if (arc.getTarget() != target) {
				continue;
			}
			if (this.checkLabels && !arc.getLabel().equals(label)) {
				continue;
			}
			return true;
		}
		return false;
	}

	/**
	 * Store all pairs of yet found isomorphic nodes and some
	 * additional information in states.
	 * A list of these states represent a subset of the
	 * potential found isomorphism.
	 *
	 * @author Florian Hinz, Maike Schwammberger, Uli Schlachter
	 */
	public static class ExtendedState {

		int numin1, numin2, numout1, numout2, curnode, depth;
		private State n;
		State m;
		State mint2out;
		State mint2in;
		State minn2m2;
		boolean active;

		public ExtendedState() {
			active = false;
		}

		public ExtendedState(ExtendedState s, int d, State newN, State newM) {
			numin1 = s.numin1;
			numin2 = s.numin2;
			numout1 = s.numout1;
			numout2 = s.numout2;
			curnode = 0;
			//Depth of s in statal-tree
			depth = d;
			n = newN;
			m = newM;
			//Only current state is active.
			active = false;
		}

		public State getN() {
			return n;
		}

		public State getM() {
			return m;
		}
	}


	/**
	 * Get final state (includes pairs of isomorphic nodes)
	 *
	 * @return returns
	 */
	public Deque<ExtendedState> getFinalState() {
		return finalState;
	}

	/**
	 * Set final state (includes pairs of isomorphic nodes)
	 *
	 * @param finalState
	 */
	public void setFinalState(Deque<ExtendedState> finalState) {
		this.finalState = finalState;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
