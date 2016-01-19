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

package uniol.apt.analysis.reversible;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;

/**
 * Check if a given Petri net is reversible.
 *
 * @author Vincent Göbel
 */
public class ReversibleNet {

	private PetriNet pn_;
	private boolean reversible_ = false;
	private Marking marking_ = null;

	public ReversibleNet(PetriNet pn) {
		pn_ = pn;
	}

	/**
	 * This method
	 * 1) generates the coverability LTS and
	 * 2) checks whether it is reversible.
	 *
	 * If the coverability LTS is reversible, so is the original PN.
	 * @throws UnboundedException If the examined Petri net is unbounded
	 */
	public void check() throws UnboundedException {

		TransitionSystem ts;
		ts = CoverabilityGraph.get(pn_).toReachabilityLTS();

		ReversibleTS ltsPersistent = new ReversibleTS(ts);
		ltsPersistent.check();
		reversible_ = ltsPersistent.isReversible();
		if (ltsPersistent.getNode() != null)
			marking_ = (Marking) ltsPersistent.getNode().getExtension(Marking.class.getName());

		return;
	}

	/**
	 * @return true, if the PN is reversible, false otherwise
	 */
	public boolean isReversible() {
		return reversible_;
	}

	/**
	 * @return A non-reversible marking. If no such marking exists, null is returned instead.
	 */
	public Marking getMarking() {
		return marking_;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
