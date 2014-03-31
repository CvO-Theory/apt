/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

package uniol.apt.synthesis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.BinaryPredicate;
import uniol.apt.util.CollectionUtils;
import uniol.apt.util.Predicate;

/**
 * This class provides all the functionality that is
 * connected with computing a information based on a
 * spanning tree of an LTS that is used in the synet
 * Petri net synthesis algorithm.
 * 
 * @author Thomas Strathmann
 */
public class SpanningTree {

	private final TransitionSystem lts;
	private TransitionSystem span;
	private ArrayList<String> alphabet;
	private ArrayList<State> states;
	
	/**
	 * Construct a spanning tree of this LTS
	 * and store it for further reference.
	 * 
	 * @param lts
	 */
	public SpanningTree(TransitionSystem lts) {
		this.lts = lts;
		
		// compute the spanning tree eagerly to simplify things later on
		this.span = computeSpanningTree(lts);
		
		// fix a linear order on the alphabet of lts
		this.alphabet = new ArrayList<String>(lts.getAlphabet());
		
		// fix a linear order on the states of lts
		this.states = new ArrayList<State>(lts.getNodes());
	}
	
	/**
	 * Returns the alphabet in the same order that is used
	 * for all operations inside this class that depend on 
	 * a linear ordering of the LTS's state set.
	 * 
	 * @return the state set (in a fixed linear order)
	 */
	public List<State> getOrderedStates() {
		return Collections.unmodifiableList(this.states);
	}
	
	/**
	 * Returns the state set in the same order that is used
	 * for all operations inside this class that depend on 
	 * a linear ordering of the LTS's alphabet.
	 * 
	 * @return the alphabet (in a fixed linear order)
	 */
	public List<String> getOrderedAlphabet() {
		return Collections.unmodifiableList(this.alphabet);
	}
	
	/**
	 * Get the spanning tree of the LTS.
	 * 
	 * @return a spanning tree of the LTS 
	 * 		(guaranteed to stay the same for the lifetime of this {@link SpanningTree} object) 
	 */
	public TransitionSystem getSpanningTree() {
		return span;
	}
	
	/**
	 * Compute the Parikh vector of a state being the
	 * Parikh vector of the path from the initial state
	 * to the given state in the spanning tree.
	 * 
	 * @param s the state 
	 * @return its Parikh vector
	 */
	public int[] parikhVector(State s) {
		s = span.getNode(s.getId());
		final Vector<Arc> path = pathToRoot(s, span);
		final ParikhVector pv = new ParikhVector(lts, path);
		return pv.getPVLexicalOrder();
	}
	
	/**
	 * Compute the matrix that is subsequently used to compute
	 * a set of generators for a distribution of tension.
	 * The rows of this matrix are the Parikh vectors of the
	 * fundamental cycles (those induced by the chords of a
	 * spanning tree) of the LTS.
	 * 
	 * @return matrix whose rows are the Parikh vectors of the LTS's fundamental cycles
	 */
	public int[][] matrix() {
		// the set of arcs that represent the cycles (also known as chords of the spanning tree)
		Set<Arc> cycleArcs = CollectionUtils.difference(lts.getEdges(), span.getEdges(),
				new BinaryPredicate<Arc, Arc>() {
					public boolean eval(Arc a1, Arc a2) {
						return (a1.getSourceId().equals(a2.getSourceId()) &&
								a1.getTargetId().equals(a2.getTargetId()) &&
								a1.getLabel().equals(a2.getLabel()));
					}
				});
		
		// build the matrix
		int[][] A = new int[cycleArcs.size()][alphabet.size()];
		
		int i = 0;
		for(Arc t : cycleArcs) {
			State source = span.getNode(t.getSource().getId());
			State target = span.getNode(t.getTarget().getId());
			
			// compute the paths from source/target to initial state
			Vector<Arc> ct0 = pathToRoot(source, span);
			Vector<Arc> ct1 = pathToRoot(target, span);

			while(!ct0.isEmpty() && !ct1.isEmpty() &&
					ct0.lastElement() == ct1.lastElement()) {
				ct0.remove(ct0.size() - 1);
				ct1.remove(ct1.size() - 1);
			}

			// compute the Parikh vector of this cycle
			for(Arc a : ct0) {
				A[i][alphabet.indexOf(a.getLabel())] += 1;
			}
			A[i][alphabet.indexOf(t.getLabel())] += 1;
			for(Arc a : ct1) {
				A[i][alphabet.indexOf(a.getLabel())] += -1;
			}

			++i;
		}
		
		return A;
	}
	
	/**
	 * Compute an undirected elementary path from 
	 * source to target and return a vector that
	 * tallies for each forward arc along the path
	 * the inscription positively and for each backward
	 * arc its inscription negatively, giving a mapping
	 * from the alphabet of the LTS into the integers.
	 * 
	 * @param source a state
	 * @param target a state
	 * @return the vector described above
	 */
	public int[] pathWeights(State source, State target) {
		source = span.getNode(source.getId());
		target = span.getNode(target.getId());
		
		Vector<Arc> ct0 = pathToRoot(source, span);
		Vector<Arc> ct1 = pathToRoot(target, span);

		while(!ct0.isEmpty() && !ct1.isEmpty() &&
				ct0.lastElement() == ct1.lastElement()) {
			ct0.remove(ct0.size() - 1);
			ct1.remove(ct1.size() - 1);
		}

		int[] v = new int[alphabet.size()];

		for(Arc a : ct0)
			v[alphabet.indexOf(a.getLabel())] += -1;
		for(Arc a : ct1)
			v[alphabet.indexOf(a.getLabel())] +=  1;

		return v;
	}

	/**
	 * Computes a spanning tree of the LTS.
	 * 
	 * @param lts the LTS whose spanning tree is to be computed
	 * @return a spanning tree of <code>lts</code>
	 */
	private static TransitionSystem computeSpanningTree(TransitionSystem lts) {
		TransitionSystem span = new TransitionSystem();
		
		// add initial state of LTS to spanning state
		State s = span.createState(lts.getInitialState());
		span.setInitialState(s);
		// push all its outgoing edges onto the working stack
		Stack<Arc> work = new Stack<Arc>();
		work.addAll(lts.getPostsetEdges(lts.getInitialState()));
		
		// build spanning tree
		while(!work.isEmpty()) {
			Arc a = work.pop();
			final State s2 = a.getTarget();
			// unless adding this edge would create a cycle, add it
			if(!CollectionUtils.exists(span.getNodes(), new Predicate<State>() {
				public boolean eval(State s) {
					return s.getId().equals(s2.getId());
				}				
			})) {
				span.createState(s2);
				span.createArc(a);
				work.addAll(s2.getPostsetEdges());
			}
		}
		
		return span;
	}

	/**
	 * Compute the path from the given state to the initial state.
	 * (NB: Only works for the spanning tree of a totally reachable LTS,
	 * but these pre-conditions are _NOT_ checked by this function!)
	 * 
	 * @param s the state from which to start
	 * @param tree the tree in which to find the path (see description for important details)
	 * @return the unique path from the given state to the tree's root node
	 */
	private static Vector<Arc> pathToRoot(State s, TransitionSystem tree) {
		Vector<Arc> path = new Vector<Arc>();
		while(s != tree.getInitialState()) {
			Arc a = s.getPresetEdges().iterator().next();
			path.add(a);
			s = a.getSource();
		}
		return path;
	}

	/**
	 * Check if the given LTS is totally reachable (i.e. all its states
	 * are reachable from the initial state)
	 * 
	 * @return true iff the LTS is totally reachable
	 */
	public boolean isTotallyReachable() {
		return this.states.size() == this.span.getNodes().size();
	}
	
}
