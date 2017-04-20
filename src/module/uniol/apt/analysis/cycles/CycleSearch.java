/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

package uniol.apt.analysis.cycles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.IEdge;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
import uniol.apt.adt.subgraph.SubEdge;
import uniol.apt.adt.subgraph.SubGraph;
import uniol.apt.adt.subgraph.SubNode;
import uniol.apt.analysis.connectivity.Connectivity;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Compute elementary cycles via Johnson's algorithm.
 * This implementation is based on "Finding All the Elementary Circuits of a Directed Graph" from Donald B. Johnson in
 * SIAM J. Comput., 4(1), 77–84. (8 pages) (DOI: 10.1137/0204007).
 *
 * @author vsp
 */
public class CycleSearch {
	public <G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> void
			searchCycles(G graph, CycleCallback<G, E, N> cycleCb) {
		Deque<SubGraph<G, E, N>> componentSubgraphs = new ArrayDeque<>();
		for (Set<N> component : Connectivity.getStronglyConnectedComponents(graph)) {
			componentSubgraphs.add(SubGraph.getSubGraphByNodes(graph, component));
		}
		while (!componentSubgraphs.isEmpty()) {
			SubGraph<G, E, N> subgraph = componentSubgraphs.removeLast();
			Set<SubNode<G, E, N>> nodes = subgraph.getNodes();
			// misuse an iterator to get and remove a random node
			Iterator<SubNode<G, E, N>> it = nodes.iterator();
			SubNode<G, E, N> start = it.next();
			new DoDfs<G, E, N>(start, subgraph, cycleCb);
			it.remove();
			subgraph = subgraph.getFlatSubGraphByNodes(nodes);
			for (Set<SubNode<G, E, N>> component : Connectivity.getStronglyConnectedComponents(subgraph)) {
				componentSubgraphs.add(subgraph.getFlatSubGraphByNodes(component));
			}
		}
	}

	// Do a DFS for circles going through 'start'. We are currently in 'cur'. This is called CIRCUIT() in the
	// paper.
	static private class DoDfs<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>> {
		private final SubNode<G, E, N> start;
		private final CycleCallback<G, E, N> cycleCb;

		private final Deque<SubNode<G, E, N>> sStack;
		private final Deque<SubEdge<G, E, N>> lStack;
		private final Set<SubNode<G, E, N>> blocked;
		private final Map<SubNode<G, E, N>, Set<SubNode<G, E, N>>> b;

		public DoDfs(SubNode<G, E, N> start, SubGraph<G, E, N> graph, CycleCallback<G, E, N> cycleCb) {
			this.start   = start;
			this.cycleCb = cycleCb;

			this.sStack  = new ArrayDeque<>();
			this.lStack  = new ArrayDeque<>();
			this.blocked = new HashSet<>();
			this.b       = new HashMap<SubNode<G, E, N>, Set<SubNode<G, E, N>>>();

			for (SubNode<G, E, N> node : graph.getNodes()) {
				this.b.put(node, new HashSet<SubNode<G, E, N>>());
			}

			doDfs(start);

			assert sStack.isEmpty();
			assert lStack.isEmpty();
		}

		private boolean doDfs(SubNode<G, E, N> cur) {
			boolean foundCycle = false;


			blocked.add(cur);
			sStack.addLast(cur);
			for (SubEdge<G, E, N> arc : cur.getPostsetEdges()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				lStack.addLast(arc);
				SubNode<G, E, N> next = arc.getTarget();
				if (next.equals(start)) {
					// cycle found
					List<N> sCycle = new ArrayList<>();
					for (SubNode<G, E, N> node : sStack) {
						sCycle.add(node.getOriginalNode());
					}
					List<E> lCycle = new ArrayList<>();
					for (SubEdge<G, E, N> edge : lStack) {
						lCycle.add(edge.getOriginalEdge());
					}
					cycleCb.cycleFound(sCycle, lCycle);
					foundCycle = true;
				} else if (!blocked.contains(next)) {
					foundCycle |= doDfs(next);
				}
				lStack.removeLast();
			}
			sStack.removeLast();

			if (foundCycle) {
				unblock(cur);
			} else {
				for (SubNode<G, E, N> next : cur.getPostsetNodes()) {
					b.get(cur).add(next);
				}
			}

			return foundCycle;
		}

		private void unblock(SubNode<G, E, N> node) {
			blocked.remove(node);
			for (SubNode<G, E, N> prev : b.get(node)) {
				if (blocked.contains(prev))
					unblock(prev);
			}
			b.put(node, new HashSet<SubNode<G, E, N>>());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
