/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C) 2016       Uli Schlachter
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

package uniol.apt.generator.bitnet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Generate a bit net.
 *
 * Each bit has two states:
 * 0, when the bit isn't set
 * 1, when the bit is set
 *
 * Bits can be unset and set. When bit i is unset, it also sets bit i+1 (if such
 * a bit exists.
 *
 * @author vsp, Uli Schlachter
 */
public class ConnectedBitNetGenerator implements BitNetGenerator {

	static final private String BIT_NAME_PATTERN = "bit%d_state%d";

	@Override
	public PetriNet generateNet(int n) {
		if (n < 1)
			throw new IllegalArgumentException("Number of bits must at least be 1.");

		PetriNet pn = new PetriNet("Connected " + String.valueOf(n) + "-bit net");

		Place unset = pn.createPlace(String.format(BIT_NAME_PATTERN, 0, 0));
		Place set = pn.createPlace(String.format(BIT_NAME_PATTERN, 0, 1));
		unset.setInitialToken(1);

		Transition t = pn.createTransition("set0");
		pn.createFlow(unset, t);
		pn.createFlow(t, set);

		for (int i = 1; i < n; i++) {
			t = pn.createTransition("unset" + Integer.toString(i - 1) + "_set" + Integer.toString(i));
			pn.createFlow(set, t);
			pn.createFlow(t, unset);

			unset = pn.createPlace(String.format(BIT_NAME_PATTERN, i, 0));
			set = pn.createPlace(String.format(BIT_NAME_PATTERN, i, 1));
			unset.setInitialToken(1);

			pn.createFlow(unset, t);
			pn.createFlow(t, set);
		}

		t = pn.createTransition("unset" + Integer.toString(n - 1));
		pn.createFlow(set, t);
		pn.createFlow(t, unset);

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
