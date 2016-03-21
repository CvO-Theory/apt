/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.factorization;

import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * A utility class that allows to modify TransitionSystems by removing certain
 * elements.
 *
 * @author Jonas Prellberg
 *
 */
public class TransitionSystemFilter {

	private TransitionSystemFilter() {
	}

	/**
	 * Creates a copy of the given TS with all arcs removed that have a
	 * label contained within the given label set.
	 *
	 * @param ts
	 *                the original TS
	 * @param labels
	 *                a label set
	 * @return a new TS that is a copy of the given one with arcs labeled by
	 *         the label set removed
	 */
	public static TransitionSystem removeArcsByLabel(TransitionSystem ts, Set<String> labels) {
		Set<Arc> toRemove = new HashSet<>();
		TransitionSystem result = new TransitionSystem(ts);
		for (Arc arc : result.getEdges()) {
			if (labels.contains(arc.getLabel())) {
				toRemove.add(arc);
			}
		}
		for (Arc arc : toRemove) {
			result.removeArc(arc);
		}
		return result;
	}

	/**
	 * Creates a copy of the given TS that only contains arcs that have a
	 * label contained within the given label set.
	 *
	 * @param ts
	 *                the original TS
	 * @param labels
	 *                a label set
	 * @return a new TS that is a copy of the given one and only has arcs
	 *         labeled by the label set
	 */
	public static TransitionSystem retainArcsByLabel(TransitionSystem ts, Set<String> labels) {
		Set<String> otherLabels = new HashSet<>(ts.getAlphabet());
		otherLabels.removeAll(labels);
		return removeArcsByLabel(ts, otherLabels);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
