/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.Region;

/**
 * Interface for something that synthesizes a Petri net.
 * @author Uli Schlachter
 */
public interface Synthesizer {
	/**
	 * Get separating regions. A collection of regions is separating if each solvable separation problem is also
	 * solved by a region from this collection.
	 * @return A separating collection of regions.
	 */
	public Collection<Region> getSeparatingRegions();

	/**
	 * Get all unsolvable event/state separation problems. Such an unsolvable problem consists of an event, which is
	 * the key of a map, and a state, which is an entry in the value of the map.
	 * @return All unsolvable event/state separation problems.
	 */
	public Map<String, Set<State>> getUnsolvableEventStateSeparationProblems();

	/**
	 * Get all unsolvable sate separation problems. This should return the equivalence class of unsolvable states,
	 * i.e.e each state should be contained in at most one set of the returned collection.
	 * @return All unsolvable state separation problems.
	 */
	public Collection<Set<State>> getUnsolvableStateSeparationProblems();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
