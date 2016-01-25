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

package uniol.apt.analysis.cycles.lts;

import java.util.ArrayList;
import uniol.apt.adt.ts.ParikhVector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * This class offers an adaption of the Floyd-Warshall algorithm for computing smallest cycles and parikh vectors.
 * @author Manuel Gieseking
 */
class ComputeSmallestCyclesFloydWarshall extends AbstractComputeSmallestCycles {
	/**
	 * Calculation the smallest cycles and parikh vectors of the given transitionsystem with an adaption of the
	 * algorithm of Floyd-Warshall.
	 * @param ts - The transitionsystem to compute the cycles and parikh vectors from.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 * @return a list of smallest cycles with their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts, boolean smallest) {
		if (!smallest)
			throw new IllegalArgumentException("Algorithm can only calculate smallest cycles");

		// Complexity all in all
		// time: O(|V|^3*|E|^4)
		// place: O(|V|^4*|E|^3)
		Map<Pair<State, State>, Weight> minDistances = new HashMap<>();
		Set<Arc> arcs = ts.getEdges();
		// Create initial weights for all neighbours
		// time: O(|E|)
		// place: O(|V|^2)
		for (Arc arc : arcs) {
			State source = arc.getSource();
			State target = arc.getTarget();
			Weight w = minDistances.get(new Pair<>(source, target));
			if (w == null) {
				w = new Weight();
			}
			ParikhVector pv = new ParikhVector(arc.getLabel());
			List<String> seq = new ArrayList<>();
			seq.add(source.getId());
			seq.add(target.getId());
			w.add(new PVwithSequence(pv, seq));
			minDistances.put(new Pair<>(source, target), w);
		}

		// time: O(|V|^3*|E|^4)
		// place: O(|V|^4*|E|^3)
		for (State nodeK : ts.getNodes()) {
			for (State nodeI : ts.getNodes()) {
				for (State nodeJ : ts.getNodes()) {
					Pair<State, State> key = new Pair<>(nodeI, nodeJ);
					Weight w1 = minDistances.get(new Pair<>(nodeI, nodeK));
					Weight w2 = minDistances.get(new Pair<>(nodeK, nodeJ));
					Weight w3 = minDistances.get(key);
					if (w1 == null || w2 == null) {
						continue;
					}
					Weight erg = new Weight();
					if (w3 != null) {
						erg.addAll(w3);
					}
					erg.addAll(Weight.add(w1, w2));
					Weight result = new Weight();
					// time: O(|E|^3)
					// place: O(|E|^3*|V|)
					for (PVwithSequence p1 : erg) {
						boolean add = true;
						for (PVwithSequence p2 : erg) {
							// time: O(|E|)
							// place: O(0)
							if (p2.getPv().tryCompareTo(p1.getPv()) < 0) {
								add = false;
								break;
							}
						}
						if (add) {
							result.add(p1);
						}
					}
					if (result.size() > 0) {
						minDistances.put(key, result);
					}
				}
			}
		}

		// time: O(|V|*|E|)
		// place: O(|V|^2*|E|^2)
		Set<Pair<List<String>, ParikhVector>> cycles = new HashSet<>();
		for (State node : ts.getNodes()) {
			Weight cycle = minDistances.get(new Pair<>(node, node));
			if (cycle != null) {
				for (PVwithSequence pvs : cycle) {
					cycles.add(new Pair<>(pvs.getSequence(), pvs.getPv()));
				}
			}
		}

		// Compare smallest cycles. Noch unsch"on, sollte besser gehen. Ist leider nur notwendig, da
		// es nur innerhalb Knoten im Algorithmus sichergestellt ist.
		// time: O(|V|^2*|E|^2)
		// place: O(|V|^3*|E|^3)
		Set<Pair<List<String>, ParikhVector>> out = new HashSet<>();
		for (Pair<List<String>, ParikhVector> pair1 : cycles) {
			boolean lt = true;
			for (Pair<List<String>, ParikhVector> pair2 : cycles) {
				if (pair1 != pair2) {
					if (pair2.getSecond().tryCompareTo(pair1.getSecond()) < 0) {
						lt = false;
						break;
					}
				}
			}
			if (lt) {
				out.add(pair1);
			}
		}
		return out;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
