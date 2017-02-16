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

package uniol.apt.analysis.bisimulation;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.deterministic.Deterministic;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

/**
 * Check,
 * 1. if there are two labeled Petri nets -&gt; if the reachability graphs of two labeled Petri nets are bisimilar.
 * 2. if there are two transition system -&gt;  if the given transition systems are bisimilar.
 * 3. if tthere is a labeled Petri net and a transition system -&gt; if the reachability graph of the Petri net and
 *      the transition system are bisimilar.
 * @author Raffaela Ferrari
 */
public class Bisimulation {

	private enum Result {

		TRUE, FALSE, UNRELIABLE
	};
	private Set<Pair<State, State>> w;
	private Result result;
	private TransitionSystem lts1;
	private TransitionSystem lts2;
	private LinkedList<Pair<State, State>> errorPath;

	/**
	 * Check, if the given transition systems are bisimilar.
	 * @param ltsOne The first transition system.
	 * @param ltsTwo The second transition system.
	 * @return true, if the graphs are bisimilar. Otherwise return false.
	 */
	public Boolean checkBisimulation(TransitionSystem ltsOne, TransitionSystem ltsTwo) {
		//Step 1: Check if the LTS are bisimilar.
		// if at least one of the LTS is deterministic, we can choose a more simple algorithm
		this.lts1 = ltsOne;
		this.lts2 = ltsTwo;
		Deterministic lts1Deterministic = new Deterministic(this.lts1);
		Deterministic lts2Deterministic = new Deterministic(this.lts2);
		// initialise extensions for the ability to put label on states
		for (State s : ltsOne.getNodes()) {
			s.putExtension("label", "");
		}
		for (State s : ltsTwo.getNodes()) {
			s.putExtension("label", "");
		}
		// choose algorithm
		if (lts1Deterministic.isDeterministic() || lts2Deterministic.isDeterministic()) {
			return checkDeterministicCase();
		} else {
			return checkGeneralCase();
		}
	}

	/**
	 *
	 * @return a List of the non bisimilar path.
	 */
	public NonBisimilarPath getErrorPath() {
		if (errorPath != null) {
			return new NonBisimilarPath(errorPath);
		}
		return null;
	}

	/**
	 * Check if two lts are bisimilar. At least one of them has to be deterministic.
	 * @return A value of a Boolean. true, if the two lts are bisimilar. Otherwise false.
	 */
	private Boolean checkDeterministicCase() {
		errorPath = null;
		//Step 1 : define Variables
		// Contains all the already visited nodes.
		w = new HashSet<>();
		// Stores the execution sequence which is currently analyzed
		Deque<Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>>> stack1 = new LinkedList<>();

		//Step 2 : fill stacks and sets
		Pair<State, State> initialPair = new Pair<>(lts1.getInitialState(), lts2.getInitialState());
		w.add(initialPair);
		stack1.push(new Pair<>(initialPair, getSuccessors(initialPair)));

		//Step 3 : execute algorithm
		while (!stack1.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			List<Pair<Arc, Pair<State, State>>> l = stack1.peek().getSecond();
			if (!l.isEmpty()) {
				Pair<Arc, Pair<State, State>> firstOfL = l.remove(0);
				Pair<State, State> tuple2 = firstOfL.getSecond();
				Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>> newElement =
					new Pair<>(tuple2, getSuccessors(tuple2));
				boolean hasFailSuccessor = false;
				for (Pair<Arc, Pair<State, State>> pair : newElement.getSecond()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

					if (pair.getFirst().getLabel().equals("phi")
						&& pair.getSecond().getFirst().getExtension("label").equals("fail")
						&& pair.getSecond().getSecond() == null) {
						hasFailSuccessor = true;
						break;
					}
				}
				if (!hasFailSuccessor) {
					if (!w.contains(tuple2)) {
						w.add(tuple2);
						stack1.push(newElement);
					}
				} else {
					errorPath = new LinkedList<>();
					constructErrorPathPairs(stack1);
					if (!(firstOfL.getFirst().getLabel().equals("phi")
						&& tuple2.getFirst().getExtension("label").equals("fail")
						&& tuple2.getSecond() == null)) {
						errorPath.add(tuple2);
					}
					return false;
				}
			} else {
				stack1.pop();
			}
		}
		return true;
	}

	/**
	 * Check if two lts are bisimilar. Both lts are nondeterministic.
	 * @return A value of a Boolean. true, if the two lts are bisimilar. Otherwise false.
	 */
	private Boolean checkGeneralCase() {
		//contains all the nodes for which the obtained status is "not bisimilar"
		w = new HashSet<>();
		//contains the result of the Partial DFS
		result = Result.UNRELIABLE;
		while (result == Result.UNRELIABLE) {
			result = getResultOfPartialDFS();
		}
		return (result != Result.FALSE);
	}

	/**
	 * Check for the general case if the two lts are bisimilar in partial DFS.
	 * @return Result of the partial DFS
	 */
	private Result getResultOfPartialDFS() {
		errorPath = null;
		//Step 1: define variables
		//contains all analyzed and visited nodes
		Set<Pair<State, State>> visited = new HashSet<>();
		//stores all the nodes of the current sequence which are visited more than once
		Set<Pair<State, State>> r = new HashSet<>();
		boolean stable = false;
		/*
		 * During the analysis of node(q1_,q2_) in getSuccessors(q1, q2), whenever q1_ nad q2_ found similar
		 * then the value of q1_ and q2_ in m are 1. Thus, when all the successors of(q1, q2) have been
		 * analyzed, q1 and q2 are bisimilar if and only if all the elements of m have been set to 1.
		 */
		Map<State, Integer> m;
		//Stores the execution sequence which is currently analyzed
		Deque<Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>>> stack1 = new LinkedList<>();
		//Contains all the bit-maps
		Deque<Map<State, Integer>> stack2 = new LinkedList<>();

		//Step 2: fill stacks
		Pair<State, State> initialPair = new Pair<>(lts1.getInitialState(), lts2.getInitialState());
		stack1.push(new Pair<>(initialPair, getSuccessors(initialPair)));
		Map<State, Integer> mapForInitialPair = new HashMap<>();
		mapForInitialPair.put(initialPair.getFirst(), 0);
		mapForInitialPair.put(initialPair.getSecond(), 0);
		stack2.push(mapForInitialPair);
		stack2.push(getMapforPostsetNodes(initialPair));

		//Step 3: execute algorithm
		while (!stack1.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			stable = true;
			Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>> topStack1 = stack1.peek();
			Pair<State, State> testPair = topStack1.getFirst();
			List<Pair<Arc, Pair<State, State>>> l = topStack1.getSecond();
			m = stack2.peek();
			if (!l.isEmpty()) {
				Pair<Arc, Pair<State, State>> firstOfL = l.remove(0);
				Pair<State, State> tuple2 = firstOfL.getSecond();
				if (!visited.contains(tuple2) && !w.contains(tuple2)) {
					Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>> newElement =
						new Pair<>(tuple2, getSuccessors(tuple2));
					boolean isInStack1 = false;
					for (Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>> t : stack1) {
						InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

						if (t.getFirst().equals(tuple2)) {
							isInStack1 = true;
						}
					}
					if (!isInStack1) {
						boolean hasFailSuccessor = false;
						for (Pair<Arc, Pair<State, State>> pair : newElement.getSecond()) {
							InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

							if (pair.getFirst().getLabel().equals("phi")
								&& pair.getSecond().getFirst().getExtension("label").
								equals("fail")
								&& pair.getSecond().getSecond() == null) {
								hasFailSuccessor = true;
								break;
							}
						}
						if (!hasFailSuccessor) {
							stack1.push(newElement);
							stack2.push(getMapforPostsetNodes(newElement.getFirst()));
						}
					} else {
						r.add(tuple2);
						m.put(tuple2.getFirst(), 1);
						m.put(tuple2.getSecond(), 1);
					}
				} else {
					if (!w.contains(tuple2)) {
						m.put(tuple2.getFirst(), 1);
						m.put(tuple2.getSecond(), 1);
					}
				}
			} else {
				stack2.pop();
				visited.add(testPair);
				Map<State, Integer> m2 = stack2.peek();
				boolean allOne = true;
				for (Integer i : m.values()) {
					if (i != 1) {
						allOne = false;
					}
				}
				if (allOne) {
					m2.put(testPair.getFirst(), 1);
					m2.put(testPair.getSecond(), 1);
				} else {
					w.add(testPair);
					if (r.contains(testPair)) {
						stable = false;
					}
					if (errorPath == null) {
						errorPath = new LinkedList<>();
						constructErrorPathPairs(stack1);
						List<Pair<Arc, Pair<State, State>>> nonBisimilarElements =
							getSuccessors(testPair);
						for (Pair<Arc, Pair<State, State>> pair : nonBisimilarElements) {
							InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

							if (!(pair.getFirst().getLabel().equals("phi")
									&& pair.getSecond().getFirst().getExtension("label").equals("fail")
									&& pair.getSecond().getSecond() == null) &&
									(m.get(pair.getSecond().getFirst()) == 0
									 || m.get(pair.getSecond().getSecond()) == 0)) {
								boolean hasNonBisimilarPairFind = false;
								List<Pair<Arc, Pair<State, State>>> testElements =
									getSuccessors(pair.getSecond());
								for (Pair<Arc, Pair<State, State>> nodePair : testElements) {
									InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

									if (nodePair.getFirst().getLabel().equals("phi")
										&& nodePair.getSecond().getFirst().getExtension("label").equals("fail")
										&& nodePair.getSecond().getSecond() == null) {
										hasNonBisimilarPairFind = true;
										break;
									}
								}
								if (hasNonBisimilarPairFind) {
									errorPath.add(pair.getSecond());
									break;
								}
							}
						}
					}
				}
				stack1.pop();
			}

		}
		m = stack2.peek();
		if (m.get(initialPair.getFirst()) != 1 && m.get(initialPair.getSecond()) != 1) {
			return Result.FALSE;
		} else if (stable) {
			return Result.TRUE;
		} else {
			return Result.UNRELIABLE;
		}
	}

	/**
	 * This function determines all successors of the given pair of node. Therefore it calculates the direct
	 * successors of the first node of the pair and the second node of the pair applying the transition rules of
	 * the language. Then it calculates the direct successors of (first node, second node) of the pair applying
	 * the rules of the definition of the class.
	 * @param pair Pair for which all successors should be determined.
	 * @return A list which contains all successors of the pair. Therefore it is given the edge and the target pair
	 * of nodes.
	 */
	private List<Pair<Arc, Pair<State, State>>> getSuccessors(Pair<State, State> pair) {
		List<Pair<Arc, Pair<State, State>>> successors = new LinkedList<>();
		// the fail node of this algorithm has a transition to itself
		if (pair.getFirst().getExtension("label").equals("fail")) {
			Arc phi = pair.getFirst().getGraph().createArc(pair.getFirst().getId(),
				pair.getFirst().getId(), "phi");
			successors.add(new Pair<>(phi, pair));
			return successors;
		}
		// Get all direct successors from the first node
		Set<Arc> node1PostsetEdges = pair.getFirst().getPostsetEdges();
		// Get all direct successors from the second node
		Set<Arc> node2PostsetEdges = pair.getSecond().getPostsetEdges();

		/*
		 *  Define the failure nodes if there is an outgoing edge of the first node(second node) an the second
		 *  node(first node) has no outgoing edge with the same label.
		 */
		State fail1 = lts1.createState();
		State fail2 = lts2.createState();
		fail1.putExtension("label", "fail");
		fail2.putExtension("label", "fail");

		// One need only one edge to node "fail" from the Pair
		boolean isConcatenatedWithFail = false;

		/*
		 *  for every edge of the PostsetEdges of node 1 it tests if the is an edge of the PostsetEdges of node
		 *  2, which have the same label. Then it is a successors of the Pair. Otherwise, if a failure node
		 *  isn't already defined, now it will be.
		 */
		for (Arc edge1 : node1PostsetEdges) {
			boolean isConcatenated = false;
			for (Arc edge2 : node2PostsetEdges) {
				if (edge1.getLabel().equals(edge2.getLabel())) {
					successors.add(new Pair<>(edge1, new Pair<>(edge1.getTarget(),
						edge2.getTarget())));
					isConcatenated = true;
				}
			}
			if (!isConcatenated && !isConcatenatedWithFail) {
				State copyOfEdge1PostNode = edge1.getTarget().getGraph().createState();
				copyOfEdge1PostNode.putExtension("label", edge1.getTarget().getExtension("label"));
				Arc phi = lts1.createArc(copyOfEdge1PostNode, fail1, "");
				phi.setLabel("phi");
				successors.add(new Pair<>(phi, new Pair<State, State>(fail1, null)));
				isConcatenatedWithFail = true;
			}
		}
		for (Arc edge2 : node2PostsetEdges) {
			boolean isConcatenated = false;
			for (Arc edgeNode1 : node1PostsetEdges) {
				if (edge2.getLabel().equals(edgeNode1.getLabel())) {
					// The concatenation is already in the list
					isConcatenated = true;
					break;

				}
			}
			if (!isConcatenated && !isConcatenatedWithFail) {
				State copyOfEdge2PostNode = edge2.getTarget().getGraph().createState();
				copyOfEdge2PostNode.putExtension("label", edge2.getTarget().getExtension("label"));
				Arc phi = lts2.createArc(copyOfEdge2PostNode, fail2, "");
				phi.setLabel("phi");
				successors.add(new Pair<>(phi, new Pair<State, State>(fail2, null)));
				isConcatenatedWithFail = true;
			}
		}
		return successors;
	}

	/**
	 * It creates a map with the PostsetNodes of a Pair.
	 * @param pair Pair for which a map with all PostsetNodes should be created.
	 * @return A map in which the PostsetNodes are the keys and in which 0 is the value for every key.
	 */
	private Map<State, Integer> getMapforPostsetNodes(Pair<State, State> pair) {
		Map<State, Integer> hashMap = new HashMap<>();
		for (State node : pair.getFirst().getPostsetNodes()) {
			hashMap.put(node, 0);
		}
		for (State node : pair.getSecond().getPostsetNodes()) {
			hashMap.put(node, 0);
		}
		return hashMap;
	}

	/**
	 * Creates the path of pairs, which is not a bisimilar path.
	 * @param stack for which a List for a path is created.
	 * @return a List of Pairs of two Nodes, which is not a bisimilar path.
	 */
	private List<Pair<State, State>> constructErrorPathPairs(Deque<Pair<Pair<State, State>,
			List<Pair<Arc, Pair<State, State>>>>> stack) {
		for (Pair<Pair<State, State>, List<Pair<Arc, Pair<State, State>>>> element : stack) {
			errorPath.addFirst(element.getFirst());
		}
		return errorPath;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
