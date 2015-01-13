/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.SpanningTree;
import uniol.apt.util.equations.EquationSystem;

/**
 * A basis of abstract regions of a LTS.
 * This class calculates a basis of abstract regions for a LTS via proposition 6.14 from from "Petri Net Synthesis" by
 * Badouel, Bernardinello and Darondeau.
 * @author Uli Schlachter
 */
public class RegionBasis extends ArrayList<Region> {
	public static final long serialVersionUID = 1L;

	/**
	 * Calculates a region basis for the given transition system.
	 * @param utility An instance of the RegionUtility class for the transition system.
	 */
	public RegionBasis(RegionUtility utility) {
		EquationSystem system = new EquationSystem(utility.getNumberOfEvents());

		// The events on each fundamental circle must form a T-Invariant of a Petri Net which generates this
		// transition system. Thus, each region must have zero effect on such a circle.
		for (Arc chord : utility.getSpanningTree().getChords()) {
			system.addEquation(utility.getParikhVectorForEdge(chord));
		}

		Set<List<Integer>> basis = system.findBasis();
		List<Region> result = new ArrayList<>();
		for (List<Integer> vector : basis)
			add(Region.createPureRegionFromVector(utility, vector));
	}

	/**
	 * Calculates a region basis for the given transition system.
	 * @param tree A spanning tree for the given transition system.
	 */
	public RegionBasis(SpanningTree<TransitionSystem, Arc, State> tree) {
		this(new RegionUtility(tree));
	}

	/**
	 * Calculates a region basis for the given transition system.
	 * @param ts The transition system to calculate a basis for.
	 */
	public RegionBasis(TransitionSystem ts) {
		this(new SpanningTree<TransitionSystem, Arc, State>(ts, ts.getInitialState()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
