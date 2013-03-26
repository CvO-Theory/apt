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

package uniol.apt.generator;

import java.util.HashMap;
import java.util.Map;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Some common utility functions that various generators need.
 * @author Uli Schlachter
 */
public class GeneratorUtils {
	// No, you should not create instances of this class

	private GeneratorUtils() {
	}

	/**
	 * Duplicate the given Petri net.
	 * @param pn the Petri net that should be cloned.
	 * @param id the id for the new Petri net.
	 * @return a new Petri net with the given id which is otherwise an exact copy of the given net.
	 */
	public static PetriNet cloneNet(PetriNet pn, String id) {
		PetriNet net = new PetriNet(id);
		Map<Node, Node> nodes = new HashMap<>();

		// Copy all places of the original net
		for (Place place : pn.getPlaces()) {
			Place p = net.createPlace(place);
			p.setInitialToken(place.getInitialToken());
			nodes.put(place, p);
		}
		// Copy all transitions of the original net
		for (Transition trans : pn.getTransitions()) {
			Transition t = net.createTransition(trans);
			t.setLabel(trans.getLabel());
			nodes.put(trans, t);
		}

		// and finally do the arcs (aka edges?)
		for (Flow arc : pn.getEdges()) {
			Node source = nodes.get(arc.getSource());
			Node target = nodes.get(arc.getTarget());
			assert source != null && target != null;

			Flow otherArc = net.createFlow(source, target);
			otherArc.setWeight(arc.getWeight());
		}

		return net;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
