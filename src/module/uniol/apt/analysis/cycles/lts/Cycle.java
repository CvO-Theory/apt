/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017 Uli Schlachter, vsp
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.presynthesis.pps.Path;

/**
 * Representation of a cycle as a list of nodes and the Parikh vector of the edges.
 * @author Uli Schlachter, vsp
 */
public class Cycle extends CyclePV {
	private final List<Arc> arcs;
	private final List<State> nodes;

	/**
	 * Construct a new instance of this class from the given data.
	 * @param nodes The nodes that are on the cycle.
	 * @param arcs The arcs of the cycle.
	 */
	public Cycle(List<State> nodes, List<Arc> arcs) {
		super(new ParikhVector(new Path(arcs).getLabels()));
		this.arcs  = Collections.unmodifiableList(new ArrayList<>(arcs));
		this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
	}

	/**
	 * Get the arcs which build this cycle.
	 * @return The arcs which build this cycle.
	 */
	public List<Arc> getArcs() {
		return arcs;
	}

	/**
	 * Get the nodes that this cycle contains.
	 * @return The nodes on this cycle.
	 */
	public List<State> getNodes() {
		return nodes;
	}

	/**
	 * Get the identifier of nodes on this cycle.
	 * @return The IDs of the nodes on this cycle.
	 */
	public List<String> getNodeIDs() {
		List<String> result = new ArrayList<>(nodes.size());
		for (State state : nodes)
			result.add(state.getId());
		return Collections.unmodifiableList(result);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + 31 * nodes.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CyclePV && super.equals(o)))
			return false;
		return nodes.equals(((Cycle) o).nodes);
	}

	@Override
	public String toString() {
		return "Cycle: " + getNodeIDs().toString() + " Parikhvector: " + getParikhVector().toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
