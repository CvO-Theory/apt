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

import java.util.Stack;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
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
	
}
