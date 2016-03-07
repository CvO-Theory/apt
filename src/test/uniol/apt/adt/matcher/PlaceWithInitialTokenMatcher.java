/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;

public class PlaceWithInitialTokenMatcher extends TypeSafeMatcher<Place> {

	private final Token token;

	private PlaceWithInitialTokenMatcher(Token token) {
		this.token = token;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("a place that has ")
			.appendValue(token.isOmega() ? "omega" : token.getValue())
			.appendText(" tokens");
	}

	@Override
	protected boolean matchesSafely(Place place) {
		return place.getInitialToken().equals(token);
	}

	@Factory
	public static Matcher<Place> placeWithInitialToken(long expected) {
		return new PlaceWithInitialTokenMatcher(Token.valueOf(expected));
	}

	@Factory
	public static Matcher<Place> placeWithInitialOmegaToken() {
		return new PlaceWithInitialTokenMatcher(Token.OMEGA);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
