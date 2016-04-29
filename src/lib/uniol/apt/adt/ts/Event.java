/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Uli Schlachter
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

package uniol.apt.adt.ts;

import uniol.apt.adt.extension.Extensible;

/**
 * Events label {@link Arc}s. They contain a string that is the actual label. This class exists mainly so that events
 * can be extended.
 * @author Uli Schlachter
 */
public class Event extends Extensible implements Comparable<Event> {

	private final String label;

	/**
	 * Constructor.
	 * @param label The label for this event.
	 */
	Event(String label) {
		this.label = label;
	}

	/**
	 * Gets the label of the arc.
	 * @return the label.
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public int compareTo(Event other) {
		return label.compareTo(other.label);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Event))
			return false;

		return label.equals(((Event) o).label);
	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	@Override
	public String toString() {
		return label;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
