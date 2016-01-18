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

package uniol.apt.analysis.sideconditions;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Datatype for a side condition.
 *
 * A side condition is a self loop of a place and a transition, including it's two
 * arcs.
 *
 * @author CS
 *
 */
public class SideCondition {

	Place place;
	Transition transition;
	Flow placeToTransitionArc;
	Flow transitionToPlaceArc;

	/**
	 * Constructor for Sideconditon.
	 *
	 * A side condition is a self loop of a place and a transition, including it's two
	 * arcs.
	 *
	 * @param p the place
	 * @param t the transiton
	 * @param placeToTransitionArc the arc that goes from place to transition.
	 * @param transitionToPlaceArc the arc that goes from transition to place.
	 */
	public SideCondition(Place p, Transition t, Flow placeToTransitionArc, Flow transitionToPlaceArc) {
		this.place = p;
		this.transition = t;
		this.placeToTransitionArc = placeToTransitionArc;
		this.transitionToPlaceArc = transitionToPlaceArc;
	}

	/**
	 * Gets the place.
	 *
	 * @return the place
	 */
	public Place getPlace() {
		return this.place;
	}

	/**
	 * Gets the transition.
	 *
	 * @return the transition
	 */
	public Transition getTransition() {
		return this.transition;
	}

	/**
	 * Gets the arc from place to transition.
	 *
	 * @return the arc from place to transition
	 */
	public Flow getPlaceToTransitionArc() {
		return this.placeToTransitionArc;
	}

	/**
	 * Gets the arc from transition to place.
	 *
	 * @return the arc from transition to place.
	 */
	public Flow getTransitionToPlaceArc() {
		return this.transitionToPlaceArc;
	}

	/**
	 * Checks if the side condition is simple. A side condition is simple,
	 * when both arc weights are 1.
	 *
	 * @return true when the side condition is simple.
	 */
	public boolean isSimple() {
		return this.transitionToPlaceArc.getWeight() == 1 && this.placeToTransitionArc.getWeight() == 1;
	}

	/**
	 * Gives out the values of the side condition in form of a string.
	 *
	 * @return Sidecondition as String
	 */
	@Override
	public String toString() {
		return "Sidecondition: Place ["
			+ this.getPlace().getId()
			+ "] Transition ["
			+ this.getTransition().getId() + "]"
			+ " Weights P->T/T->P ["
			+ this.getPlaceToTransitionArc().getWeight()
			+ "]";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
