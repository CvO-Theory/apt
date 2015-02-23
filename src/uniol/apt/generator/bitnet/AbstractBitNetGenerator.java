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

package uniol.apt.generator.bitnet;

import uniol.apt.adt.pn.PetriNet;

/**
 * Abstract Framework for generators of bit nets
 *
 * @author vsp
 */
abstract class AbstractBitNetGenerator implements BitNetGenerator {
	/**
	 * Override point to generate places, transition and arcs for a bit
	 *
	 * @param pn net which get generated
	 * @param i number of the bit
	 * @return The new bit
	 */
	protected abstract Bit addBitPTA(PetriNet pn, int i);

	@Override
	public PetriNet generateNet(int n) {
		if (n < 1)
			throw new IllegalArgumentException("Number of bits must at least be 1.");

		PetriNet pn = new PetriNet(String.valueOf(n) + "-bit net");
		Bit[] bits = new Bit[n];

		for (int i = 0; i < n; i++) {
			bits[i] = addBitPTA(pn, i);
		}

		pn.putExtension("bits", bits);

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
