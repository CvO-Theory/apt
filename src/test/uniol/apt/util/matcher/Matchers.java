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

package uniol.apt.util.matcher;

import java.util.List;

import uniol.apt.util.Pair;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.is;

/**
 * Matchers to verify that utility classes match given conditions.
 *
 * @author vsp, Uli Schlachter
 */
public class Matchers {
	public static <T, U> Matcher<? super Pair<? extends T, ? extends U>> pairWith(Matcher<T> t, Matcher<U> u) {
		return PairWithMatcher.<T, U>pairWith(t, u);
	}

	public static <T, U> Matcher<? super Pair<? extends T, ? extends U>> pairWith(T t, U u) {
		return pairWith(is(t), is(u));
	}

	public static <T> Matcher<Iterable<? extends T>> containsRotated(List<Matcher<? super T>> expected) {
		return ContainsRotatedMatcher.<T>containsRotated(expected);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Matcher<Iterable<? extends T>> containsRotated(Matcher<? super T>... expected) {
		return ContainsRotatedMatcher.<T>containsRotated(expected);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Matcher<Iterable<? extends T>> containsRotated(T... expected) {
		return ContainsRotatedMatcher.<T>containsRotated(expected);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
