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
 * The Transition class for PetriNets.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Transition extends Node {

	String label = "";

	/**
	 * Constructor to create a transition with the given id in the given Petri net.
	 * <p/>
	 * @param net the PetriNet this Transition belongs to.
	 * @param id the id this Transition should have.
	 */
	Transition(PetriNet net, String id) {
		super(net, id);
	}

	/**
	 * Constructor copying a given transition to the given Petri net. The constructor also copies the references of
	 * the extensions.
	 * <p/>
	 * @param net the net this Transtion belongs to.
	 * @param t the Transition which should be copied.
	 */
	Transition(PetriNet net, Transition t) {
		super(net, t);
		this.label = t.label;
	}

	/**
	 * Gets the label of this Transition.
	 * <p/>
	 * @return the label of this Transition.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of the Transition. To maintain consistency it's just a delegate to the petri net. So it also
	 * changes if it's necessary the alphabet of the net this Transition belongs to.
	 * <p/>
	 * @param l the label to set.
	 */
	public void setLabel(String l) {
		this.graph.setTransitionLabel(this.id, l);
	}

	/**
	 * Checks if the transition is fireable under a given marking. To maintain consistency it's just a delegate to
	 * the petri net.
	 * <p/>
	 * @param m the marking of the petrinet.
	 * <p/>
	 * @return true when the transition is fireable.
	 */
	public boolean isFireable(Marking m) {
		return this.graph.getTransitionIsFireable(this.id, m);
	}

	/**
	 * Fires this transition under a given marking. To maintain consistency it's just a delegate to the petri net.
	 * It is not changing the given marking.
	 * <p/>
	 * @param m the marking.
	 * <p/>
	 * @return the resulting marking upon firing this transition.
	 */
	public Marking fire(Marking m) {
		return this.graph.fireTransition(this.id, new Marking(m));
	}

	/**
	 * Gets the preset for this transition. It is just casting the nodes of getPresetNodes in Places.
	 * <p/>
	 * @return the preset of this transition.
	 */
	public Set<Place> getPreset() {
		final Set<Place> result = new HashSet<>();
		final Set<Node> set = this.getPresetNodes();
		for (Node n : set) {
			if (n instanceof Place) {
				final Place p = (Place) n;
				result.add(p);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Gets the postset for this transition. It is just casting the nodes of getPostsetNodes in Places.
	 * <p/>
	 * @return the postset of this transition.
	 */
	public Set<Place> getPostset() {
		final Set<Place> result = new HashSet<>();
		final Set<Node> set = this.getPostsetNodes();
		for (Node n : set) {
			if (n instanceof Place) {
				final Place p = (Place) n;
				result.add(p);
			}
		}
		return Collections.unmodifiableSet(result);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
