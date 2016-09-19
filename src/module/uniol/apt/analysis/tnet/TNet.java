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

package uniol.apt.analysis.tnet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This package provides a method to check if a Petri net is a t-net.
 * The Petri net must be plain.
 *
 * @author Daniel, Manuel
 */
public class TNet {

	private final PetriNet petriNet;
	private TNetResult result;

	/**
	 * Class constructor.
	 *
	 * @param petriNet Net which will be checked
	 */
	public TNet(PetriNet petriNet) {
		this.petriNet = petriNet;
		this.result = new TNetResult();
	}

	/**
	 * Checks if a Petri net is a t-net.
	 *
	 * @return true: Net is a TNet - false: Net it NOT a TNet
	 * @throws PreconditionFailedException Precondition failed exception
	 */
	public boolean testPlainTNet() throws PreconditionFailedException {
		if (!new Plain().checkPlain(petriNet)) {
			throw new PreconditionFailedException("the net is not plain.");
		}
		// over all places
		for (Place place : petriNet.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			// is here a merge?
			if (place.getPreset().size() > 1) {
				result.addMergeID(place.getId());
				result.setTNet(false);
			}
			// is here a conflict?
			if (place.getPostset().size() > 1) {
				result.addConflictID(place.getId());
				result.setTNet(false);
			}
		}

		return result.isTNet();

	}

	/**
	 * Get detailed information about test.
	 *
	 * @return result
	 */
	public TNetResult getResult() {
		return this.result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
