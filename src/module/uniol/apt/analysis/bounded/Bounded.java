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

package uniol.apt.analysis.bounded;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.analysis.language.FiringSequence;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Tests if a net is (k-)bounded. A net is bounded if no place can get an unlimited number of tokens. A net is k-bounded
 * if no place can get more than k token.
 * @author Uli Schlachter, vsp
 */
public class Bounded {
	private Bounded() { /* hide Constructor */ }

	/**
	 * Returns true if all places in the Petri net are bounded.
	 * @param pn The Petri net to check.
	 * @return true if all places in the Petri net are bounded.
	 */
	static public boolean isBounded(PetriNet pn) {
		return checkBounded(pn).isBounded();
	}

	/**
	 * Check if the Petri net is (k-)bounded
	 * @param pn The Petri net to check.
	 * @return An instance of BoundedResult describing the result. This function never returns null.
	 */
	static public BoundedResult checkBounded(PetriNet pn) {
		Collection<Place> places = pn.getPlaces();
		CoverabilityGraph cover = CoverabilityGraph.get(pn);
		List<Transition> sequence = new FiringSequence();
		Place witness = null;
		long k = 0;

		// Now check all markings and places and remember the largest token count that is seen.
		for (CoverabilityGraphNode n : cover.getNodes()) {
			Marking mark = n.getMarking();
			for (Place p : places) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				Token val = mark.getToken(p);

				if (val.isOmega()) {
					// The net is unbounded, it can't get worse than this
					CoverabilityGraphNode covered = n.getCoveredNode();
					return new BoundedResult(pn, p, null, covered.getFiringSequence(),
							n.getFiringSequenceFromCoveredNode());
				}
				if (k < val.getValue()) {
					// We found a larger k, update our variables
					witness = p;
					sequence = n.getFiringSequence();
					k = val.getValue();
				}
			}
		}

		return new BoundedResult(pn, witness, k, sequence, Collections.<Transition>emptyList());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
