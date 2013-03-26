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
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.cycles.lts.CycleCounterExample;

/**
 * Check if all smallest cycles have same parikh vectors for the given Petri net
 * <p/>
 * @author Sören
 * <p/>
 */
public class CyclesHaveSamePV {

	private PetriNet pn_;
	private String errorMsg_;
	private CycleCounterExample counterExample_;

	/**
	 * Constructor.
	 * <p/>
	 * @param pn the net to check.
	 */
	public CyclesHaveSamePV(PetriNet pn) {
		pn_ = pn;
	}

	/**
	 * Check if all smallest cycles have same parikh vectors for the given Petri Net
	 * <p/>
	 * @param algo - which algorithm should be used for computing the smallest cycles.
	 * <p/>
	 * @return boolean
	 * <p/>
	 * @throws UnboundedException thrown if the net is not bounded.
	 */
	public boolean check(ComputeSmallestCycles.Algorithm algo) throws UnboundedException {
		TransitionSystem ts = new CoverabilityGraph(pn_).toReachabilityLTS();

		ComputeSmallestCycles cycle = new ComputeSmallestCycles();
		if (!cycle.checkSamePVs(ts, algo)) {
			errorMsg_ = "Not same parikh vectors";
			counterExample_ = cycle.getCounterExample();
			return false;
		}
		return true;
	}

	public CycleCounterExample getCycleCounterExample() {
		return counterExample_;
	}

	public String getErrorMsg() {
		return errorMsg_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
