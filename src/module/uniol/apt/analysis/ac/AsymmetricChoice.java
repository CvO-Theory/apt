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

package uniol.apt.analysis.ac;

import static java.util.Collections.disjoint;

import java.util.Collection;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.Pair;

/**
 * Checks whether a given plain Petri net is an asymmetric choice net.
 *
 * A Petri net is an asymmetric choice net if:
 * ∀p₁,p₂∈P: p₁°∩p₂°≠∅ ⇒ p₁°⊆p₂° ∨ p₁°⊇p₂°
 *
 * @author vsp
 */
public class AsymmetricChoice {

	/**
	 * Checks whether a given Petri net is a asymmetric choice net.
	 * @param net the petri net to check.
	 * @return a pair of places which are a counterexample if the Petri net doesn't fulfills the propertey, null if
	 * it fulfills the property.
	 */
	public Pair<Place, Place> check(PetriNet net) {
		for (Pair<Place, Place> placePair : new DifferentPairsIterable<>(net.getPlaces())) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			Collection<?> p1Postset = placePair.getFirst().getPostset();
			Collection<?> p2Postset = placePair.getSecond().getPostset();
			if (!disjoint(p1Postset, p2Postset) && !p1Postset.containsAll(p2Postset)
						&& !p2Postset.containsAll(p1Postset)) {
				return placePair;
			}
		}
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
