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

package uniol.apt.analysis.isolated;

import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Checks if there are any isolated elements in the Petri net.
 *
 * @author CS
 *
 */
public class Isolated {

	private Isolated() {
	}

	/**
	 * Checks a given Petri net for isolated elements. An isolated element is
	 * an element that has no incoming and outgoing edges. (&lt;=&gt; the pre/postset is empty).
	 *
	 * @param pn The Petri net to examine.
	 * @return true when an isolated element has been found.
	 */
	public static boolean checkIsolated(PetriNet pn) {
		for (Node n : pn.getNodes()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			// Checks if the node has no incoming and outgoing edges
			if (n.getPresetEdges().isEmpty() && n.getPostsetEdges().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
