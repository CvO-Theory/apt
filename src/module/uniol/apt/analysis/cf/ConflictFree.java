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

package uniol.apt.analysis.cf;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This module tests if a plain Petri net is conflict-free. That is:
 * <code>\forall s \in S: \mid s^\bullet \mid \leq 1 \vee s^\bullet \subset ^\bullet s</code>
 * @author Manuel Gieseking
 */
public class ConflictFree {

	private final PetriNet pn;

	/**
	 * Creates a new instance for checking the conflict free property of a given petri net.
	 * @param pn - the petri net to check.
	 */
	public ConflictFree(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Precondition: plain Petri net
	 * Testing if a plain Petri net is conflict-free. That is:
	 * \forall s \in S: \mid s^\bullet \mid \leq 1 \vee s^\bullet \subset ^\bullet s
	 * @return true if the Petri net is conflict free
	 * @throws PreconditionFailedException thrown if the given net is not plain.
	 */
	public boolean check() throws PreconditionFailedException {
		if (!new Plain().checkPlain(pn)) {
			throw new PreconditionFailedException("the net is not plain.");
		}
		for (Place place : pn.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (!(place.getPostset().size() <= 1 || place.getPreset().containsAll(place.getPostset()))) {
				return false;
			}
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
