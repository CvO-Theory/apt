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

package uniol.apt.analysis.cycles;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.util.Pair;

/**
 * Calculates the parikh vectors of the smallest cycles of an Petri Net.
 * <p/>
 * @author Manuel Gieseking
 */
public class CyclesPVs {

	private PetriNet pn_;
	private Set<Pair<List<String>, ParikhVector>> cycles_;

	/**
	 * Constructor.
	 * <p/>
	 * @param pn - PetriNet to analysise.
	 */
	public CyclesPVs(PetriNet pn) {
		pn_ = pn;
	}

	/**
	 * Calculates the parikh vectors of the smallest cycles of an Petri Net.
	 * <p/>
	 * @param algo - which algorithm should be used for computing the smallest cycles.
	 * <p/>
	 * @throws UnboundedException if net is not bounded.
	 */
	public void calc(ComputeSmallestCycles.Algorithm algo) throws UnboundedException {
		TransitionSystem ts = new CoverabilityGraph(pn_).toReachabilityLTS();

		ComputeSmallestCycles cycle = new ComputeSmallestCycles();
		cycles_ = cycle.computePVsOfSmallestCycles(ts, algo);
	}

	public Set<Pair<List<String>, ParikhVector>> getCycles() {
		return Collections.unmodifiableSet(cycles_);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
