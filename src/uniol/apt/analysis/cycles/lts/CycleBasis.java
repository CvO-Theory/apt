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

package uniol.apt.analysis.cycles.lts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.invariants.InvariantCalculator;

/**
 * Calculate a minimal basis of the given LTS cycle space.
 * @author Thomas Strathmann
 */
public class CycleBasis {
	
	/**
	 * Compute a minimal cycle basis of the LTS
	 * @param lts the LTS whose minimal cycle basis is to be computed
	 * @return a set of minimal basis cycles of the LTS
	 */
	public static Set<Vector<Arc>> cycleBasis(TransitionSystem lts) {
		// compute incidence matrix
		ArrayList<State> states = Collections.list(Collections.enumeration(lts.getNodes()));
		ArrayList<Arc> arcs = Collections.list(Collections.enumeration(lts.getEdges()));
		int[][] incidence = new int[arcs.size()][states.size()];
		for(int i=0; i<arcs.size(); ++i) {
			Arc e = arcs.get(i);
			int source = states.indexOf(e.getSource());
			int target = states.indexOf(e.getTarget());
			incidence[i][source] =  1;
			incidence[i][target] = -1;
		}
		
		// solve homogeneous system
		Set<List<Integer>> basisVectors = InvariantCalculator.calcInvariantsFarkas(incidence);
		
		// translate solution to cycle data structure
		Set<Vector<Arc>> cycles = new HashSet<Vector<Arc>>();
		for(List<Integer> v : basisVectors) {
			Vector<Arc> cycle = new Vector<Arc>();
			for(int k=0; k<v.size(); ++k) {
				if(v.get(k) == 1) {
					cycle.add(arcs.get(k));
				}
			}
			cycles.add(cycle);
		}
		
		return cycles;
	}
	
	/**
	 * Compute the set of Parikh vectors for the minimal cycle
	 * basis of the given LTS
	 * @param lts the LTS whose minimal cycles' Parikh vectors are to be computed
	 * @return a set of Parikh vectors
	 */
	public static Set<ParikhVector> cycleBasisParikhVectors(TransitionSystem lts) {
		Set<Vector<Arc>> cycles = cycleBasis(lts);
		Set<ParikhVector> pvs = new HashSet<ParikhVector>();
		for(Vector<Arc> c : cycles) {
			pvs.add(new ParikhVector(lts, c));
		}
		return pvs;
	}
	
}
