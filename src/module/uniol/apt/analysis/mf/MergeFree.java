/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.analysis.mf;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This module tests if a Petri net is merge-free. That is:
 * <code>\forall s \in S: \mid {}^\bullet s \mid \leq 1</code>
 * @author Manuel Gieseking, Uli Schlachter
 */
public class MergeFree {

	/**
	 * Testing if a Petri net is merge-free. That is:
	 * \forall s \in S: \mid {}^\bullet s \mid \leq 1
	 * @return true if the Petri net is merge-free.
	 */
	public boolean check(PetriNet pn) {
		for (Place place : pn.getPlaces()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (place.getPreset().size() > 1) {
				return false;
			}
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
