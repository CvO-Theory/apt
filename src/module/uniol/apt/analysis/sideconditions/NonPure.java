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

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Checks if the Petri net is nonpure and has simple side conditions (and no
 * non-simple side conditions).
 *
 * @author CS
 *
 */

public class NonPure {

	private NonPure() { }
	/**
	 * Checks if a given Petri net is nonpure and checking only simple side
	 * conditions.
	 *
	 * @param pn Petri net
	 * @return true if the net is nonpure.
	 */
	public static boolean checkNonPure(PetriNet pn) {
		SideConditions conditions = CheckSideConditions.checkSideConditions(pn);
		if (conditions.isEmpty()) {
			return false;
		}
		for (SideCondition condition : conditions) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (!condition.isSimple()) {
				return false;
			}
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
