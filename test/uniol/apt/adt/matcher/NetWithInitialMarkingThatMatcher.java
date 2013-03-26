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
import uniol.apt.adt.pn.PetriNet;

/**
 * Matcher to verify that a Petri net has a matching initial marking
 * @author Uli Schlachter
 */
public class NetWithInitialMarkingThatMatcher extends TypeSafeMatcher<PetriNet> {

	private final Matcher<? super Marking> matcher;

	private NetWithInitialMarkingThatMatcher(Matcher<? super Marking> matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean matchesSafely(PetriNet pn) {
		return matcher.matches(pn.getInitialMarkingCopy());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("PetriNet with initial marking that ");
		matcher.describeTo(description);
	}

	@Factory
	public static <T> Matcher<PetriNet> netWithInitialMarkingThat(Matcher<? super Marking> matcher) {
		return new NetWithInitialMarkingThatMatcher(matcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
