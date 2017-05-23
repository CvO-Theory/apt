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

package uniol.apt.adt.pn;

import uniol.apt.adt.Edge;

/**
 * A flow represents an edge of a petri net. It goes from one place to a transition or from one transition to a place. A
 * flow can have a weight.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Flow extends Edge<PetriNet, Flow, Node> {

	int weight = 1;

	/**
	 * Constructor to create a new Flow with given sourceId, targetId and weight.
	 * @param net    the net this flow belongs to.
	 * @param source the source node.
	 * @param target the target node.
	 * @param weight the weight for this flow.
	 */
	Flow(PetriNet net, Node source, Node target, int weight) {
		super(net, source, target);
		this.weight = weight;
	}

	/**
	 * Constructor for copying a flow to another petri net. The constructor also copies the references of the
	 * extensions.
	 * @param net the net this flow should belong to.
	 * @param f   the flow which should be copied.
	 */
	Flow(PetriNet net, Flow f) {
		super(net, f);
		this.weight = f.weight;
	}

	/**
	 * Sets the weight of this flow. To maintain consistency it's just a delegate to the petri net.
	 * @param w the weight to set.
	 */
	public void setWeight(int w) {
		this.graph.setFlowWeight(this.source.getId(), this.target.getId(), w);
	}

	/**
	 * Gets the weight of this flow.
	 * @return the weight of this flow.
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Gets the place for this flow. To maintain consistency it's just a delegate to the petri net.
	 * @return the place of the flow.
	 */
	public Place getPlace() {
		return this.graph.getFlowPlace(this.source.getId(), this.target.getId());
	}

	/**
	 * Gets the transition for this flow. To maintain consistency it's just a delegate to the petri net.
	 * @return the transition of the flow.
	 */
	public Transition getTransition() {
		return this.graph.getFlowTransition(this.source.getId(), this.target.getId());
	}

	/**
	 * Gets the source node's id for this flow.
	 * @return the source node's id of this flow.
	 */
	String getSourceId() {
		return source.getId();
	}

	/**
	 * Gets the target node's id for this flow.
	 * @return the target node's id of this flow.
	 */
	String getTargetId() {
		return target.getId();
	}

	@Override
	public String toString() {
		return getSourceId() + "--" + weight + "->" + getTargetId();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
