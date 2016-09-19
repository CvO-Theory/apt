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

package uniol.apt.analysis.sideconditions;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Checks a net for side conditions and returns a list of the side conditions.
 *
 * @author CS
 *
 */
public class CheckSideConditions {

	private CheckSideConditions() {
	}

	/**
	 * Checks a Petri net for side conditions and returns a list of side
	 * conditions.
	 *
	 * @param pn
	 *            the Petri net
	 * @return the set of side conditions found in the Petri net.
	 */
	public static SideConditions checkSideConditions(PetriNet pn) {
		SideConditions conditions = new SideConditions();

		for (Place p : pn.getPlaces()) {
			// instead of directly checking the post/preset,
			// we are going to check the edges.
			for (Flow a : p.getPostsetEdges()) {
				for (Flow b : p.getPresetEdges()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					if (a.getTarget().getId().equals(b.getSource().getId())) {
						conditions.add(new SideCondition(p, a.getTransition(), a, b));
					}
				}
			}
		}
		return conditions;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
