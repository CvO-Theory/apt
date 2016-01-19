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

package uniol.apt.adt.pn;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The Place class for PetriNets.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Place extends Node {

	/**
	 * Constructor to create a place with the given id in the given Petri net.
	 * @param net the net this place belongs to.
	 * @param id  the id this place should have.
	 */
	Place(PetriNet net, String id) {
		super(net, id);
	}

	/**
	 * Constructor copying a given place to the given Petri net. The constructor also copies the references of the
	 * extensions.
	 * @param net the net this place belongs to.
	 * @param p   the Place which should be copied.
	 */
	Place(PetriNet net, Place p) {
		super(net, p);
	}

	/**
	 * Gets the token of the initialmarking of the net for this place. To maintain consistency it's just a delegate
	 * to the petri net.
	 * @return the initial token.
	 */
	public Token getInitialToken() {
		return graph.getInitialToken(id);
	}

	/**
	 * Sets the intial token for this place. That means it changes the initialmarking of the Petrinet this place
	 * belongs to.
	 * @param t the number of tokens on this place.
	 */
	public void setInitialToken(long t) {
		setInitialToken(Token.valueOf(t));
	}

	/**
	 * Gets the initial token of this place. That means it changes the initialmarking of the Petrinet this place
	 * belongs to. To maintain consistency it's just a delegate to the petri net.
	 * @param t the token object.
	 */
	public void setInitialToken(Token t) {
		this.graph.setInitialToken(getId(), t);
	}

	/**
	 * Gets the preset for this place. It is just casting the nodes of getPresetNodes in Transitions.
	 * @return the preset of this place.
	 */
	public Set<Transition> getPreset() {
		final Set<Transition> result = new HashSet<>();
		final Set<Node> set = this.getPresetNodes();
		for (Node n : set) {
			final Transition t = (Transition) n;
			result.add(t);
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Gets the postset for this place. It is just casting the nodes of getPostsetNodes in Transitions.
	 * @return the postset of this place.
	 */
	public Set<Transition> getPostset() {
		final Set<Transition> result = new HashSet<>();
		final Set<Node> set = this.getPostsetNodes();
		for (Node n : set) {
			final Transition t = (Transition) n;
			result.add(t);
		}
		return Collections.unmodifiableSet(result);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
