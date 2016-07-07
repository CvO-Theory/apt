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

package uniol.apt.generator;

import uniol.apt.adt.pn.PetriNet;

/**
 * Interface for generators of Petri nets of a given size.
 *
 * @author vsp, Uli Schlachter
 */
public interface NetGenerator {
	/**
	 * Method to generate a net of a given size.
	 *
	 * @param n Parameter for the size of the net.
	 * @return The generated Petri net
	 */
	public PetriNet generateNet(int n);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
