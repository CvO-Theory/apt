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

package uniol.apt.analysis.bisimulation;

import java.util.ArrayList;
import java.util.List;

import uniol.apt.adt.ts.State;
import uniol.apt.util.Pair;

/**
 * This class represents a list of pairs of a non bisimilar path of two labelled transition systems. It is needed for
 * the module system.
 * @author Raffaela Ferrari
 *
 */
public class NonBisimilarPath extends ArrayList<Pair<State, State>> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new non bisimilar path.
	 * @param nonBisimilarPath Non bisimilar Pair List, for which a Path schould be created.
	 */
	public NonBisimilarPath(List<Pair<State, State>> nonBisimilarPath) {
		super(nonBisimilarPath);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
