/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Schlachter
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

/**
 * A symbol labels transitions in a {@link FiniteAutomaton}. A symbol either represents some possible event from the
 * input or epsilon which is a special-case symbol that is used for labeling transitions that may be followed
 * arbitrarily without consuming any input.
 * @author Uli Schlachter
 */
public class Symbol {
	private final String event;

	/**
	 * Constant representing the epsilon symbol
	 */
	static final public Symbol EPSILON = new Symbol();

	private Symbol() {
		this.event = "";
	}

	/**
	 * Construct a new symbol from some event.
	 * @param event The event that should be represented
	 * @throws IllegalArgumentException When the event is the empty string.
	 */
	public Symbol(String event) {
		if (event.isEmpty())
			throw new IllegalArgumentException("Events must be non-empty strings");
		this.event = event;
	}

	/**
	 * @return true if this symbol represents epsilon
	 */
	public boolean isEpsilon() {
		return event.isEmpty();
	}

	/**
	 * @return the event represented by this symbol or the empty string for epsilon.
	 */
	public String getEvent() {
		return event;
	}

	@Override
	public int hashCode() {
		return event.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Symbol))
			return false;
		Symbol other = (Symbol) o;
		return event.equals(other.event);
	}

	@Override
	public String toString() {
		if (isEpsilon())
			return "[EPSILON]";
		return "[" + event + "]";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
