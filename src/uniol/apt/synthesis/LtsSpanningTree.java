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
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.impl.APTRenderer;
import uniol.apt.util.BinaryPredicate;
import uniol.apt.util.CollectionUtils;
import uniol.apt.util.Predicate;

/**
 * Compute a (not necessarily minimal) spanning tree for a given LTS.
 * 
 * @author Thomas Strathmann
 */
public class LtsSpanningTree {

	/**
	 * Computes a spanning tree of the LTS.
	 * 
	 * @param lts the LTS whose spanning tree is to be computed
	 * @return a spanning tree of <code>lts</code>
	 */
	public static TransitionSystem spanningTree(TransitionSystem lts) {
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
	 * Compute a cycle basis for the given LTS using a spanning tree.
	 * 
	 * @param lts the LTS whose cycle basis is to be computed
	 * @param span a spanning tree of the LTS
	 * @return a set of cycles forming a cycle basis for the LTS
	 */
	public static Set<Set<Arc>> cycleBasis(TransitionSystem lts, TransitionSystem span) {
		HashSet<Set<Arc>> cycles = new HashSet<Set<Arc>>();		
		
		// the set of arcs that represent the cycles
		Set<Arc> cycleArcs = CollectionUtils.difference(lts.getEdges(), span.getEdges(),
				new BinaryPredicate<Arc, Arc>() {
					public boolean eval(Arc a1, Arc a2) {
						return (a1.getSourceId().equals(a2.getSourceId()) &&
								a1.getTargetId().equals(a2.getTargetId()) &&
								a1.getLabel().equals(a2.getLabel()));
					}
		});
		
		// build an explicit representation of the set of cycles
		// TODO: this is pretty dumb
		for(Arc t : cycleArcs) {
			Set<Arc> ct = new HashSet<Arc>();
			ct.add(t);
			ct.addAll(span.getEdges());
			cycles.add(ct);
		}
		
		return Collections.unmodifiableSet(cycles);
	}
	
	// compute the rows of the matrix for abstract regions of the lts
	public static Set<HashMap<String,Integer>> cycleWeights(TransitionSystem lts, TransitionSystem span) {
		// the set of arcs that represent the cycles
		Set<Arc> cycleArcs = CollectionUtils.difference(lts.getEdges(), span.getEdges(),
				new BinaryPredicate<Arc, Arc>() {
					public boolean eval(Arc a1, Arc a2) {
						return (a1.getSourceId().equals(a2.getSourceId()) &&
								a1.getTargetId().equals(a2.getTargetId()) &&
								a1.getLabel().equals(a2.getLabel()));
					}
				});

		/*
		APTRenderer renderer = new APTRenderer();
		System.out.println(renderer.render(span));
		*/
		
		HashSet<HashMap<String, Integer>> rows = new HashSet<HashMap<String, Integer>>();
		
		for(Arc t : cycleArcs) {
			HashMap<String, Integer> vt = new HashMap<String, Integer>();
			for(String c : lts.getAlphabet()) {
				vt.put(c, 0);				
			}
		
			/*
			// find the path from the source to the target of t in span
			// and close it to a cycle by adding t at the right position
			State source = span.getNode(t.getSource().getId());
			State target = span.getNode(t.getTarget().getId());
		
			System.out.println("+--------------------------\n" +
							   "| working on chord " + t);
			
			// find path from source of t to the initial state
			// stop if the target of t is reached along the way
			boolean targetReached = false;
			Set<State> visitedStates = new HashSet<State>();
			
			System.out.println("|> searching path from source(" + t + ")");
			while(source != span.getInitialState()) {
				if(source == target) { //source.getId().equals(target.getId())) {
					targetReached = true;
					break;
				}
				Arc a = source.getPresetEdges().iterator().next();
				System.out.println("|>> added arc " + a);
				updateWeight(vt, a.getLabel(), 1);
				source = a.getSource();
			}
			
			// if the target node of t is in a different branch of the
			// spanning tree than the source node, find a path
			// from the target node to the their greatest common ancestor
			if(!targetReached) {
				System.out.println("|> searching path from target(" + t + ")");
				while(target != span.getInitialState()) { //!target.getId().equals(span.getInitialState().getId())) {
					Arc a = target.getPresetEdges().iterator().next();
					System.out.println("|>> added arc " + a);
					updateWeight(vt, a.getLabel(), -1);
					target = a.getSource();
				}
			}

			// finally, add the weight for the chord that induces this cycle
			updateWeight(vt, t.getLabel(), 1);
			*/
			
			System.out.println("+--------------------------\n" +
					   "| working on chord " + t);
			
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
			
			// DEBUG
			Vector<Arc> cycle = new Vector<Arc>(ct0);
			Collections.reverse(cycle);
			cycle.add(t);
			cycle.addAll(ct1);
			System.out.println("cycle: " + cycle);
			
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
		System.out.print("path from " + s);
		Vector<Arc> path = new Vector<Arc>();
		while(s != tree.getInitialState()) {
			Arc a = s.getPresetEdges().iterator().next();
			path.add(a);
			s = a.getSource();
		}
		System.out.println(" to root = " + path);
		return path;
	}
	
}
