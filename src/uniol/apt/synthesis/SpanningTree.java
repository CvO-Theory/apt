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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	public ParikhVector parikhVector(State s) {
		Vector<Arc> path = pathToRoot(s, lts);
		return new ParikhVector(lts, path);
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
	 * Compute the rows of the matrix that is subsequently used to
	 * compute a set of generators for a distribution of tension.
	 * 
	 * @return the set of Parikh vectors of the LTS's fundamental cycles
	 */
	public Set<HashMap<String, Integer>> cycleWeights() {
		// the set of arcs that represent the cycles (also known as chords of the spanning tree)
		Set<Arc> cycleArcs = CollectionUtils.difference(lts.getEdges(), span.getEdges(),
				new BinaryPredicate<Arc, Arc>() {
					public boolean eval(Arc a1, Arc a2) {
						return (a1.getSourceId().equals(a2.getSourceId()) &&
								a1.getTargetId().equals(a2.getTargetId()) &&
								a1.getLabel().equals(a2.getLabel()));
					}
				});

		HashSet<HashMap<String, Integer>> rows = new HashSet<HashMap<String, Integer>>();
		
		for(Arc t : cycleArcs) {
			HashMap<String, Integer> vt = new HashMap<String, Integer>();
			for(String c : lts.getAlphabet()) {
				vt.put(c, 0);				
			}
		
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
				updateWeight(vt, a.getLabel(), 1);
			}
			updateWeight(vt, t.getLabel(), 1);
			for(Arc a : ct1) {
				updateWeight(vt, a.getLabel(), -1);
			}
			
			// add this row iff it contains at least one non-zero entry
			for(String label : vt.keySet()) {
				if(vt.get(label) != 0) {
					rows.add(vt);
					break;
				}
			}
		}
		
		return Collections.unmodifiableSet(rows);
	}
	
	private static void updateWeight(Map<String, Integer> weights, String label, int offset) {
		weights.put(label, weights.get(label) + offset);
	}
	
	private static Vector<Arc> pathToRoot(State s, TransitionSystem tree) {
		Vector<Arc> path = new Vector<Arc>();
		while(s != tree.getInitialState()) {
			Arc a = s.getPresetEdges().iterator().next();
			path.add(a);
			s = a.getSource();
		}
		return path;
	}
		
}
