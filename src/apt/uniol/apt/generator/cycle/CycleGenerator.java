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

package uniol.apt.generator.cycle;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Generate a Petri net consisting of a single cycle
 *
 * @author vsp
 */
public class CycleGenerator {

	/**
	 * Generate the Petri net with initial marking 1.
	 *
	 * @param n size of the cycle.
	 * @return The generated Petri net.
	 */
	public PetriNet generateNet(int n) {
		return generateNet(n, 1);
	}

	/**
	 * Generate the Petri net
	 *
	 * @param n size of the cycle.
	 * @param init number of token initially on some place.
	 * @return The generated Petri net.
	 */
	public PetriNet generateNet(int n, int init) {
		if (n < 1) {
			throw new IllegalArgumentException("Cannot construct cycles of a non-positive size");
		}

		PetriNet pn = new PetriNet("Cycle of size " + String.valueOf(n)
				+ " with " + String.valueOf(init) + " tokens");

		Place p = pn.createPlace("p0");
		Place pfirst = p;
		Transition t = pn.createTransition("t0");
		p.setInitialToken(init);
		pn.createFlow(p, t);

		for (int i = 1; i < n; i++) {
			p = pn.createPlace("p" + Integer.toString(i));
			pn.createFlow(t, p);

			t = pn.createTransition("t" + Integer.toString(i));
			pn.createFlow(p, t);
		}

		pn.createFlow(t, pfirst);

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
