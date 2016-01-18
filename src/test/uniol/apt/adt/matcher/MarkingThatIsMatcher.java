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

import uniol.apt.adt.pn.Marking;

import java.util.Map;
import java.util.HashMap;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;

/**
 * Matcher to verify that a marking contains the specified tokens.
 * @author Uli Schlachter
 */
public class MarkingThatIsMatcher extends TypeSafeMatcher<Marking> {

	private final Map<String, Long> marking;

	private MarkingThatIsMatcher(Map<String, Long> marking) {
		this.marking = new HashMap<>(marking);
	}

	@Override
	public boolean matchesSafely(Marking mark) {
		for (Map.Entry<String, Long> entry : marking.entrySet()) {
			Token value = mark.getToken(mark.getNet().getPlace(entry.getKey()));
			if (value.isOmega() || value.getValue() != entry.getValue()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("marking that is ").appendText(marking.toString());
	}

	@Factory
	public static <T> Matcher<Marking> markingThatIs(Map<String, Long> marking) {
		return new MarkingThatIsMatcher(marking);
	}

	@Factory
	public static <T> Matcher<Marking> markingThatIs(Marking marking) {
		assert !marking.hasOmega();
		// Urgh
		Map<String, Long> map = new HashMap<>();
		for (Place place : marking.getNet().getPlaces()) {
			map.put(place.getId(), place.getInitialToken().getValue());
		}
		return new MarkingThatIsMatcher(map);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
