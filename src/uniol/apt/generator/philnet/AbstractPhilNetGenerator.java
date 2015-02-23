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

package uniol.apt.generator.philnet;

import uniol.apt.adt.pn.PetriNet;

/**
 * Abstract Framework for generators of philosophers nets
 *
 * @author vsp
 */
abstract class AbstractPhilNetGenerator implements PhilNetGenerator {
	/**
	 * Override point to generate places and transition for one philosopher
	 *
	 * @param pn net which get generated
	 * @param i number of the philosopher
	 * @return {@link Philosopher} object holding informations about the new created philosopher
	 */
	protected abstract Philosopher addPhilPT(PetriNet pn, int i);

	/**
	 * Override point to generate arcs for one philosopher
	 *
	 * @param pn net which get generated
	 * @param i number of the philosopher
	 * @param next number of the neighboring philosopher
	 */
	protected abstract void addPhilA(PetriNet pn, int i, int next);

	@Override
	public PetriNet generateNet(int n) {
		if (n < 2)
			throw new IllegalArgumentException("Number of philosophers must at least be 2.");

		PetriNet pn = new PetriNet(String.valueOf(n) + "-philosophers net");
		Philosopher[] phils = new Philosopher[n];

		for (int i = 0; i < n; i++) {
			phils[i] = addPhilPT(pn, i);
		}

		for (int i = 0; i < n; i++) {
			addPhilA(pn, i, (i + 1) % n);
		}

		pn.putExtension("philosophers", phils);

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
