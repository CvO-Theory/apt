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

package uniol.apt.analysis.homogeneous;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

/**
 * Checks whether a given plain Petri net is an homogeneous net.
 *
 * A Petri net is an homogeneous net if:
 * ∀p∈P:∀t₁,t₂∈p°: F(p,t₁)=F(p,t₂)
 *
 * @author vsp
 */
public class Homogeneous {
	/**
	 * Checks whether a given Petri net is a homogeneous net.
	 * @param net the petri net to check.
	 * @return a pair of arcs which is a counterexample if the Petri net doesn't fulfill the property, null if
	 * it fulfills the property.
	 */
	public Pair<Flow, Flow> check(PetriNet net) {
		for (Place p : net.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Flow firstEdge = null;
			int weight = 0;
			for (Flow edge : p.getPostsetEdges()) {
				if (firstEdge == null) {
					firstEdge = edge;
					weight    = edge.getWeight();
				} else if (weight != edge.getWeight()) {
					return new Pair<>(firstEdge, edge);
				}
			}
		}
		return null;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
