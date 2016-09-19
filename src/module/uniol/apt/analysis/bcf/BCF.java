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

package uniol.apt.analysis.bcf;

import static java.util.Collections.disjoint;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Tests if a Petri net is behaviourally conflict-free. A Petri Net is BCF if
 * for every reachable marking and activated transitions t1, t2, the preset of
 * the transitions are disjunct.
 * @author Uli Schlachter, vsp
 */
public class BCF {
	/** This class represents the result of check(). */
	public static class Result {
		public final Transition t1, t2;
		public final Marking m;
		public final List<Transition> sequence;

		/**
		 * Construct a new Result instance from the given values.
		 * @param m The marking that serves as a counter-example
		 * @param t1 First transition for the counter-example
		 * @param t2 Second transition for the counter-example
		 * @param sequence Sequence
		 */
		Result(Marking m, List<Transition> sequence, Transition t1, Transition t2) {
			this.m = m;
			this.sequence = sequence;
			this.t1 = t1;
			this.t2 = t2;
		}
	}

	/**
	 * Check if the net for the given LTS is BCF and return null. Otherwise, a counterexample is given.
	 * @param pn The Petri net that should be checked
	 * @return null if the Petri net is behaviourally conflict-free, else a counter-example
	 * @throws UnboundedException If the given Petri net is not bounded and thus can't be examined.
	 */
	public Result check(PetriNet pn) throws UnboundedException {
		CoverabilityGraph cover = CoverabilityGraph.get(pn);

		for (CoverabilityGraphNode node : cover.getNodes()) {
			Set<CoverabilityGraphEdge> edges = new HashSet<>(node.getPostsetEdges());
			Marking marking = node.getMarking();
			if (marking.hasOmega()) {
				throw new UnboundedException(marking.getNet());
			}

			// We have to check all pairs of edges. We do that by picking a random edge, removing it from
			// the set and then checking it against all the edges that are left. Repeat until done.
			while (!edges.isEmpty()) {
				Iterator<CoverabilityGraphEdge> it = edges.iterator();
				CoverabilityGraphEdge edge1 = it.next();
				Transition trans1 = edge1.getTransition();
				it.remove();

				for (CoverabilityGraphEdge edge2 : edges) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

					Transition trans2 = edge2.getTransition();
					assert edge1 != edge2;
					if (!check(marking, trans1, trans2)) {
						// Found a counterexample!
						return new Result(marking, node.getFiringSequence(), trans1, trans2);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if the given arguments satisfy the BCF-condition.
	 * @param mark The marking that should be examined.
	 * @param t1 The first transition.
	 * @param t2 The second transition.
	 * @return true if the arguments satisfy the BCF-condition
	 */
	protected boolean check(Marking mark, Transition t1, Transition t2) {
		// Precondition: t1 and t2 are activated/fireable under mark

		// If the intersection of the two presets is *not* empty, the Petri net in question is not BCF.
		return disjoint(t1.getPreset(), t2.getPreset());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
