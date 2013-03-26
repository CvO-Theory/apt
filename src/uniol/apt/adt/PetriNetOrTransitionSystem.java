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

package uniol.apt.adt;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * A class that stores either a Petri net or a labeled transition system. The purpose of this class is to allow modules
 * to take either a Petri net or labeled transition system; in particular, modules that check properties (e.g.
 * reversibility) that apply to both Petri nets and labeled transition benefit from this class.
 * <p/>
 * @author Renke Grunwald
 */
public class PetriNetOrTransitionSystem {

	private PetriNet net = null;
	private TransitionSystem ts = null;

	/**
	 * Store a Petri net.
	 * <p/>
	 * @param net the Petri net
	 */
	public PetriNetOrTransitionSystem(PetriNet net) {
		this.net = net;
	}

	/**
	 * Store a labeled transition system.
	 * <p/>
	 * @param ts the labeled transition system
	 */
	public PetriNetOrTransitionSystem(TransitionSystem ts) {
		this.ts = ts;
	}

	/**
	 * Gets the stored Petri net or null.
	 * <p/>
	 * @return Petri net or null
	 */
	public PetriNet getNet() {
		return net;
	}

	/**
	 * Gets the stored labeled transition system or null.
	 * <p/>
	 * @return Labeled transition or null
	 */
	public TransitionSystem getTs() {
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
