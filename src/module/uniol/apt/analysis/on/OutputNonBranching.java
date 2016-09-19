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

package uniol.apt.analysis.on;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This module tests if a Petri net is output-nonbranching. That is:
 * <code>\forall s \in S: \mid s^\bullet \mid \leq 1</code>
 * @author Manuel Gieseking
 */
public class OutputNonBranching {

	private final PetriNet pn;

	/**
	 * Constructor.
	 * @param pn - the net which should be checked.
	 */
	public OutputNonBranching(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Testing if a Petri net is output-nonbranching. That is:
	 * \forall s \in S: \mid s^\bullet \mid \leq 1
	 * @return true if the Petri net is output-nonbranching.
	 */
	public boolean check() {
		for (Place place : pn.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (place.getPostset().size() > 1) {
				return false;
			}
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
