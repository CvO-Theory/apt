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

package uniol.apt.analysis.cycles.lts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.CycleCallback;
import uniol.apt.analysis.cycles.CycleSearch;
import uniol.apt.util.Pair;

/**
 * Compute smallest cycles or cycles which do not contain any state twice via Johnson's algorithm.
 * @author vsp
 */
class ComputeSmallestCyclesJohnson extends AbstractComputeSmallestCycles {
	/**
	 * Computes the parikh vectors of all smallest cycles of a labeled transition system with a algorithm using
	 * Johnson's algorithm. (Requirement A10)
	 * @param ts       - the transitionsystem to examine.
	 * @param smallest - flag which tells if really all or just the smallest should be saved. (Storage vs. Time)
	 * @return a list of the smallest cycles of a given transitionsystem an their parikh vectors.
	 */
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts,
			final boolean smallest) {
		final Set<Pair<List<String>, ParikhVector>> cycles = new HashSet<>();
		new CycleSearch().searchCycles(ts, new CycleCallback() {
			@Override
			public void cycleFound(List<String> nodes, List<String> edges) {
				ParikhVector pv = new ParikhVector(edges);
				if (smallest) {
					Iterator<Pair<List<String>, ParikhVector>> iter = cycles.iterator();
					while (iter.hasNext()) {
						Pair<List<String>, ParikhVector> pair2 = iter.next();
						int comp = pair2.getSecond().tryCompareTo(pv);
						if (comp < 0) {
							// pairs2 has a smaller Parikh vector
							return;
						}
						if (comp > 0) {
							// This vector is smaller than pair2.
							iter.remove();
						}
					}
				}
				cycles.add(new Pair<>(nodes, pv));
			}
		});
		return cycles;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
