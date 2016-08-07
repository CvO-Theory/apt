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

package uniol.apt.analysis.presynthesis.pps;

import java.util.ArrayList;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;

/**
 * Holds a list of arcs that represent a path through an LTS.
 *
 * @author Jonas Prellberg
 */
public class Path {

	private final List<Arc> arcs;

	/**
	 * Initializes a new path with the given list of arcs. It is not checked
	 * if they are consecutive.
	 */
	public Path(List<Arc> arcs) {
		this.arcs = arcs;
	}

	/**
	 * Returns the arcs that make up this path.
	 */
	public List<Arc> getArcs() {
		return arcs;
	}

	/**
	 * Returns a list of labels corresponding to this path's arcs.
	 */
	public List<String> getLabels() {
		List<String> labels = new ArrayList<>();
		for (Arc arc : arcs) {
			labels.add(arc.getLabel());
		}
		return labels;
	}

	/**
	 * Returns the last state of this path.
	 */
	public State getTarget() {
		return arcs.get(arcs.size() - 1).getTarget();
	}

	@Override
	public String toString() {
		return arcs.toString();
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
