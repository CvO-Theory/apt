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

package uniol.apt.analysis.cycles.lts;

import java.util.HashSet;

/**
 * A set of parikh vectors and there sequences. It is used for getting a weight between to nodes in a transitionsystem.
 * @author Manuel Gieseking
 */
public class Weight extends HashSet<PVwithSequence> {

	private static final long serialVersionUID = 1L;

	/**
	 * Adds to weight together to a new one. That means their sequences will be concatenate and their parikh vectors
	 * added.
	 * @param w1 - First weight for addition. Left side of the concatenation.
	 * @param w2 - Second weight for addition. Right side of the concatenation.
	 * @return a new generated weight, with the addition of the parikh vectors and concatenation of the sequences of
	 *         the given weights.
	 */
	public static Weight add(Weight w1, Weight w2) {
		Weight w = new Weight();
		for (PVwithSequence p1 : w1) {
			for (PVwithSequence p2 : w2) {
				w.add(PVwithSequence.glue(p1, p2));
			}
		}
		return w;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
