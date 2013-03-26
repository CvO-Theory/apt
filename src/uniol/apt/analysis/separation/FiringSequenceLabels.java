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

package uniol.apt.analysis.separation;

import java.util.ArrayList;

// TODO move to ??? directory
/**
 * This class represents the labels of transitions of a firing sequence.
 *
 * @author Daniel
 */
public class FiringSequenceLabels extends ArrayList<String> {
	private static final long serialVersionUID = 42;


	/**
	 * Construct a new, empty firing sequence.
	 */
	public FiringSequenceLabels() {
	}

	/* if needed...
	public FiringSequence toFiringSequence(PetriNet pn) {
	}*/

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
