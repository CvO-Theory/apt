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

package uniol.apt.generator.inverse;

import java.util.HashMap;
import java.util.Map;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Given some Petri nets, this class constructs a Petri Net with all arcs reversed.
 * @author Uli Schlachter
 */
public class InverseNetGenerator {

	/** No, you will not create instances of this */
	private InverseNetGenerator() {
	}

	/**
	 * Construct the inverse of the given petri net. The inverse petri net has the same places and transitions, but
	 * the source and target of all arcs are swapped.
	 * @param orig The petri net to invert
	 * @return The inverse of the given petri net
	 */
	public static PetriNet invert(PetriNet orig) {
		PetriNet pn = new PetriNet("Inverse of " + orig.getName());
		Map<String, Node> nodes = new HashMap<>();

		for (Place p : orig.getPlaces()) {
			Place n = pn.createPlace(p);
			n.setInitialToken(p.getInitialToken());
			nodes.put(p.getId(), n);
		}
		for (Transition t : orig.getTransitions()) {
			Transition n = pn.createTransition(t.getId());
			n.setLabel(t.getLabel());
			nodes.put(t.getId(), n);
		}
		for (Flow arc : orig.getEdges()) {
			Node source = nodes.get(arc.getSource().getId());
			Node target = nodes.get(arc.getTarget().getId());
			assert source != null && target != null;
			Flow n = pn.createFlow(target, source);
			n.setWeight(arc.getWeight());
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
