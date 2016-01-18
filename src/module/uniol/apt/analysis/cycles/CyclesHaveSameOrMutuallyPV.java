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

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.cycles.lts.CycleCounterExample;
import uniol.apt.analysis.exception.UnboundedException;

/**
 * Check if cycles same or mutually disjoint parikh vectors for the given Petri Net
 * <p/>
 * @author Sören
 * <p/>
 */
public class CyclesHaveSameOrMutuallyPV {

	private PetriNet pn_;
	private String errorMsg_;
	private CycleCounterExample counterExample_;

	/**
	 * Constructor.
	 * <p/>
	 * @param pn - the net to examine.
	 */
	public CyclesHaveSameOrMutuallyPV(PetriNet pn) {
		pn_ = pn;
	}

	/**
	 * Check if cycles same or mutually disjoint parikh vectors for the given Petri net.
	 * <p/>
	 * @param algo - which algorithm should be used for computing the smallest cycles.
	 * <p/>
	 * @return boolean
	 * <p/>
	 * @throws UnboundedException thrown if the net is not bounded.
	 */
	public boolean check(ComputeSmallestCycles algo) throws UnboundedException {
		TransitionSystem ts = CoverabilityGraph.get(pn_).toReachabilityLTS();

		if (!algo.checkSameOrMutallyDisjointPVs(ts)) {
			counterExample_ = algo.getCounterExample();
			errorMsg_ = "Not mutally disjoint";
			return false;
		}
		return true;
	}

	public CycleCounterExample getCounterExample() {
		return counterExample_;
	}

	public String getErrorMsg() {
		return errorMsg_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
