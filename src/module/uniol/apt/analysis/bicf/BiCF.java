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

package uniol.apt.analysis.bicf;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.bcf.BCF;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Tests if a Petri net is binary-conflict-free. A Petri Net is BiCF if
 * for every reachable marking and activated transitions t1, t2, the marking has
 * enough tokens to fire both t1 and t2 (not counting the tokens produced by those transitions).
 * @author Uli Schlachter, vsp
 */
public class BiCF extends BCF {

	/**
	 * Check if the given arguments satisfy the BiCF-condition.
	 * @param mark The marking that should be examined.
	 * @param t1 The first transition.
	 * @param t2 The second transition.
	 * @return true if the arguments satisfy the BiCF-condition
	 */
	@Override
	protected boolean check(Marking mark, Transition t1, Transition t2) {
		// Precondition: t1 and t2 are activated/fireable under mark

		// This checks:
		// \forall s\in S: M(s) >= F(s, t1) + F(s, t2)

		// We only have to check places from which both transitions take tokens, because places which are only
		// used by either transition must already have enough token, thanks to the precondition.
		for (Flow a1 : t1.getPresetEdges()) {
			Place place = a1.getPlace();

			// Find an arc in the other preset that goes to the same place
			for (Flow a2 : t2.getPresetEdges()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				if (!a2.getPlace().equals(place)) {
					continue;
				}

				// Check if the marking has enough token for both arcs
				if (mark.getToken(place).getValue() < a1.getWeight() + a2.getWeight()) {
					return false;
				}
			}
		}

		// No counter-example found, the given marking and transitions are ok.
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
