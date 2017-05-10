/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017 Uli Schlachter
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

package uniol.apt.analysis.cycles.lts;

import uniol.apt.adt.ts.ParikhVector;

/**
 * Representation of a cycle as its Parikh vector.
 * @author Uli Schlachter
 */
public class CyclePV {
	private final ParikhVector pv;

	/**
	 * Construct a new cycle for the given Parikh vector.
	 * @param pv The Parikh vector to use.
	 */
	public CyclePV(ParikhVector pv) {
		this.pv = pv;
	}

	/**
	 * Get the Parikh vector underlying this cycle.
	 * @return This cycle's Parikh vector.
	 */
	public ParikhVector getParikhVector() {
		return pv;
	}

	@Override
	public int hashCode() {
		return pv.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CyclePV))
			return false;
		return pv.equals(((CyclePV) o).pv);
	}

	@Override
	public String toString() {
		return pv.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
