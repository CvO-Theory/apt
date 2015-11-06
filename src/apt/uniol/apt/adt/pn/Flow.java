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

import java.util.Objects;
import uniol.apt.adt.Edge;

/**
 * A flow represents an edge of a petri net. It goes from one place to a transition or from one transition to a place. A
 * flow can have a weight.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Flow extends Edge<PetriNet, Flow, Node> {

	int weight = 1;

	/**
	 * Constructor to create a new Flow with given sourceId, targetId and weight.
	 * <p/>
	 * @param net      the net this flow belongs to.
	 * @param sourceId the source node's id.
	 * @param targetId the target node's id.
	 * @param weight   the weight for this flow.
	 */
	Flow(PetriNet net, String sourceId, String targetId, int weight) {
		super(net, sourceId, targetId);
		this.weight = weight;
	}

	/**
	 * Constructor for copying a flow to another petri net. The constructor also copies the references of the
	 * extensions.
	 * <p/>
	 * @param net the net this flow should belong to.
	 * @param f   the flow which should be copied.
	 */
	Flow(PetriNet net, Flow f) {
		super(net, f);
		this.weight = f.weight;
	}

	/**
	 * Sets the weight of this flow. To maintain consistency it's just a delegate to the petri net.
	 * <p/>
	 * @param w the weight to set.
	 */
	public void setWeight(int w) {
		this.graph.setFlowWeight(this.sourceId, this.targetId, w);
	}

	/**
	 * Gets the weight of this flow.
	 * <p/>
	 * @return the weight of this flow.
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Gets the place for this flow. To maintain consistency it's just a delegate to the petri net.
	 * <p/>
	 * @return the place of the flow.
	 */
	public Place getPlace() {
		return this.graph.getFlowPlace(this.sourceId, this.targetId);
	}

	/**
	 * Gets the transition for this flow. To maintain consistency it's just a delegate to the petri net.
	 * <p/>
	 * @return the transition of the flow.
	 */
	public Transition getTransition() {
		return this.graph.getFlowTransition(this.sourceId, this.targetId);
	}

	/**
	 * Gets the source node's id for this flow.
	 * <p/>
	 * @return the source node's id of this flow.
	 */
	String getSourceId() {
		return sourceId;
	}

	/**
	 * Gets the target node's id for this flow.
	 * <p/>
	 * @return the target node's id of this flow.
	 */
	String getTargetId() {
		return targetId;
	}

	/**
	 * Compares this flow to another by checking there weight, source- and targetid. Also checking if the belong to
	 * the same net.
	 * <p/>
	 * @param obj - the other object to compare to
	 * <p/>
	 * @return true if the label, source- and targetid are equal and the reference of petri net are the same.
	 */
	public boolean compare(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Flow other = (Flow) obj;
		if (!Objects.equals(this.weight, other.weight)) {
			return false;
		}
		if (!Objects.equals(this.sourceId, other.sourceId)) {
			return false;
		}
		if (!Objects.equals(this.targetId, other.targetId)) {
			return false;
		}
		if (this.graph != other.graph) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getSourceId() + "--" + weight + "->" + getTargetId();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
