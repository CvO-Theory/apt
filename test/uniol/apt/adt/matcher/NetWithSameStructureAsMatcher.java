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

package uniol.apt.adt.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Matcher to verify that a Petri net has the same structure as another net. This means that there are places and
 * transitions with the same IDs and that the arcs go between nodes with same IDs.
 * @author Uli Schlachter
 */
public class NetWithSameStructureAsMatcher extends TypeSafeMatcher<PetriNet> {
	private final Matcher<? extends Iterable<? extends Place>> placesMatcher;
	private final Matcher<? extends Iterable<? extends Transition>> transitionsMatcher;
	private final Matcher<? extends Iterable<? extends Flow>> arcsMatcher;

	private NetWithSameStructureAsMatcher(PetriNet pn) {
		Collection<Matcher<? super Place>> expectedPlaces = new ArrayList<>();
		Collection<Matcher<? super Transition>> expectedTransitions = new ArrayList<>();
		Collection<Matcher<? super Flow>> expectedArcs = new ArrayList<>();

		for (Place place : pn.getPlaces())
			expectedPlaces.add(Matchers.nodeWithID(place.getId()));
		for (Transition transition : pn.getTransitions())
			expectedTransitions.add(Matchers.nodeWithID(transition.getId()));
		for (Flow arc : pn.getEdges())
			expectedArcs.add(Matchers.arcThatConnects(arc.getSource().getId(), arc.getTarget().getId()));

		placesMatcher = containsInAnyOrder(expectedPlaces);
		transitionsMatcher = containsInAnyOrder(expectedTransitions);
		arcsMatcher = containsInAnyOrder(expectedArcs);
	}

	@Override
	public boolean matchesSafely(PetriNet pn) {
		if (!placesMatcher.matches(pn.getPlaces()))
			return false;
		if (!transitionsMatcher.matches(pn.getTransitions()))
			return false;
		if (!arcsMatcher.matches(pn.getEdges()))
			return false;
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("PetriNet with places being ").appendDescriptionOf(placesMatcher);
		description.appendText(" and transitions being ").appendDescriptionOf(transitionsMatcher);
		description.appendText(" and arcs being ").appendDescriptionOf(arcsMatcher);
	}

	@Factory
	public static <T> Matcher<PetriNet> netWithSameStructureAs(PetriNet pn) {
		return new NetWithSameStructureAsMatcher(pn);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
