/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.adt.automaton;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Uli Schlachter
 */
public class SymbolTest {
	@Test
	public void testEpsilon() {
		assertThat(Symbol.EPSILON, equalTo(Symbol.EPSILON));
		assertThat(Symbol.EPSILON, hasToString("[EPSILON]"));
		assertThat(Symbol.EPSILON.isEpsilon(), equalTo(true));
		assertThat(Symbol.EPSILON.getEvent(), is(""));
	}

	@Test
	public void testSymbolA() {
		Symbol a = new Symbol("a");
		assertThat(a, equalTo(new Symbol("a")));
		assertThat(a, not(equalTo(new Symbol("b"))));
		assertThat(a, not(equalTo(Symbol.EPSILON)));
		assertThat(a.equals("a"), is(false));
		assertThat(a, hasToString("[a]"));
		assertThat(a.isEpsilon(), is(false));
		assertThat(a.getEvent(), is("a"));
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testEmptyEvent() {
		new Symbol("");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
