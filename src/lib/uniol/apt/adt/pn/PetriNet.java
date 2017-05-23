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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import uniol.apt.adt.AbstractGraph;
import uniol.apt.adt.CollectionToUnmodifiableSetAdapter;
import uniol.apt.adt.EdgeKey;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.SoftMap;
import uniol.apt.adt.exception.FlowExistsException;
import uniol.apt.adt.exception.IllegalFlowException;
import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.exception.TransitionFireException;

import static org.apache.commons.collections4.iterators.EmptyIterator.emptyIterator;

/**
 * The PetriNet is the base class for representing a petri net. With flows, places, transitions, a initial marking and
 * final markings.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class PetriNet extends AbstractGraph<PetriNet, Flow, Node> implements IGraph<PetriNet, Flow, Node> {

	private String name;
	private long nextPlaceId = 0;
	private long nextTransitionId = 0;
	private final SortedMap<String, Node> nodes = new TreeMap<>();
	private final SortedMap<String, Place> places = new TreeMap<>();
	private List<Place> placesList = Collections.emptyList();
	private final SortedMap<String, Transition> transitions = new TreeMap<>();
	private final Map<String, Set<Node>> presetNodes = new SoftMap<>();
	private final Map<String, Set<Node>> postsetNodes = new SoftMap<>();
	private final Map<String, Map<EdgeKey, Flow>> presetEdges = new HashMap<>();
	private final Map<String, Map<EdgeKey, Flow>> postsetEdges = new HashMap<>();
	private int numFlows = 0;
	private Marking initialMarking = new Marking(this);
	private final Set<Marking> finalMarkings = new HashSet<>();

	/**
	 * Creates a new PetriNet with the name "".
	 */
	public PetriNet() {
		this("");
	}

	/**
	 * Creates a new PetriNet with the given name.
	 * @param name Name of the petri net as String.
	 */
	public PetriNet(String name) {
		this.name = name;
	}

	/**
	 * Copies a petri net. The constructor also copies the references of the extensions.
	 * @param pn the petri net that should be copied.
	 */
	public PetriNet(PetriNet pn) {
		this.name = pn.name;
		this.nextPlaceId = pn.nextPlaceId;
		this.nextTransitionId = pn.nextTransitionId;
		for (String key : pn.places.keySet()) {
			this.addPlace(key, new Place(this, pn.places.get(key)));
		}
		for (String key : pn.transitions.keySet()) {
			this.addTransition(key, new Transition(this, pn.transitions.get(key)));
		}
		// Iterate over all EdgeKey instances
		for (Map<EdgeKey, Flow> postsets : pn.postsetEdges.values()) {
			for (Map.Entry<EdgeKey, Flow> entry : postsets.entrySet()) {
				this.addFlow(entry.getKey(), new Flow(this, entry.getValue()));
			}
		}
		for (Marking m : pn.finalMarkings) {
			this.finalMarkings.add(new Marking(this, m));
		}
		this.initialMarking = new Marking(this, pn.initialMarking);
		copyExtensions(pn);
	}

	/**
	 * Creates a new flow in this petri net with the given sourceId, targetId and weight 1.
	 * @param sourceId the id of the source node.
	 * @param targetId the id of the target node.
	 * @return a reference object to the created flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 * @throws FlowExistsException      thrown if a flow with the same source and target already exists in this
	 *                                  petri net.
	 * @throws IllegalFlowException     thrown if argument don't correspond to a place and a transition.
	 */
	public Flow createFlow(String sourceId, String targetId) {
		return createFlow(sourceId, targetId, 1);
	}

	/**
	 * Add a given flow directly without any checks.
	 * @param key the EdgeKey of the flow
	 * @param f the flow to add
	 * @return the flow
	 */
	private Flow addFlow(EdgeKey key, Flow f) {
		String targetId = key.getTargetId();
		String sourceId = key.getSourceId();
		this.presetEdges.get(targetId).put(key, f);
		this.postsetEdges.get(sourceId).put(key, f);
		this.numFlows++;
		//update pre- and postsets
		Set<Node> preNodes = presetNodes.get(targetId);
		if (preNodes != null) {
			preNodes.add(this.getNode(sourceId));
		}
		Set<Node> postNodes = postsetNodes.get(sourceId);
		if (postNodes != null) {
			postNodes.add(this.getNode(targetId));
		}
		invokeListeners();
		return f;
	}

	/**
	 * Creates a new flow in this petri net with the given sourceId, targetId and weight.
	 * @param sourceId the id of the source node.
	 * @param targetId the id of the target node.
	 * @param weight   the weight of the new created flow.
	 * @return a reference object to the created flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 * @throws FlowExistsException      thrown if a flow with the same source and target already exists in this
	 *                                  petri net.
	 * @throws IllegalFlowException     thrown if argument don't correspond to a place and a transition.
	 */
	public Flow createFlow(String sourceId, String targetId, int weight) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		if (weight < 0) {
			throw new IllegalArgumentException("weight < 0");
		}

		final EdgeKey key = this.createEdgeKey(sourceId, targetId);
		// createEdgeKey() makes sure the node exists
		if (this.postsetEdges.get(sourceId).containsKey(key)) {
			throw new FlowExistsException(this, key);
		}
		boolean hasPlace = places.containsKey(sourceId) || places.containsKey(targetId);
		boolean hasTransition = transitions.containsKey(sourceId) || transitions.containsKey(targetId);
		if (!hasPlace || !hasTransition) {
			throw new IllegalFlowException(this, key);
		}
		Flow f = new Flow(this, getNode(sourceId), getNode(targetId), weight);
		if (weight > 0) {
			addFlow(key, f);
		}
		return f;
	}

	/**
	 * Creates a new flow in this petri net with the given source, target and weight.
	 * @param source the source node.
	 * @param target the target node.
	 * @param weight the weight of the new created flow.
	 * @return a reference object to the created flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 * @throws FlowExistsException      thrown if a flow with the same source and target already exists in this
	 *                                  petri net.
	 * @throws IllegalFlowException     thrown if argument don't correspond to a place and a transition.
	 */
	public Flow createFlow(Node source, Node target, int weight) {
		if (source == null) {
			throw new IllegalArgumentException("source == null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target == null");
		}
		return createFlow(source.getId(), target.getId(), weight);
	}

	/**
	 * Creates a new flow in this petri net with the given source, target and weight 1.
	 * @param source the source node.
	 * @param target the target node.
	 * @return a reference object to the created flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 * @throws FlowExistsException      thrown if a flow with the same source and target already exists in this
	 *                                  petri net.
	 * @throws IllegalFlowException     thrown if argument don't correspond to a place and a transition.
	 */
	public Flow createFlow(Node source, Node target) {
		return createFlow(source, target, 1);
	}

	/**
	 * Creates a new flow in this petri net by copying the values from an existing arc. Also copies the references
	 * of the extensions of the given flow.
	 * @param flow the template flow.
	 * @return a reference object to the created flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 * @throws FlowExistsException      thrown if a flow with the same source and target already exists in this
	 *                                  petri net.
	 * @throws IllegalFlowException     thrown if argument don't correspond to a place and a transition.
	 */
	public Flow createFlow(Flow flow) {
		if (flow == null) {
			throw new IllegalArgumentException("flow == null");
		}
		Flow f = createFlow(flow.getSourceId(), flow.getTargetId(), flow.getWeight());
		f.copyExtensions(flow);
		return f;
	}

	/**
	 * Add a given place directly without any checks.
	 * @param id the id of the place
	 * @param p the place to add
	 * @return the place
	 */
	private Place addPlace(String id, Place p) {
		this.places.put(id, p);
		this.nodes.put(id, p);
		this.placesList = new ArrayList<>(this.placesList);
		this.placesList.add(p);
		// update pre- and postsets
		presetNodes.put(id, new HashSet<Node>());
		postsetNodes.put(id, new HashSet<Node>());
		presetEdges.put(id, new HashMap<EdgeKey, Flow>());
		postsetEdges.put(id, new HashMap<EdgeKey, Flow>());
		invokeListeners();
		return p;
	}

	/**
	 * Creates a new place to the petri net with the given id.
	 * @param id the id of the new place.
	 * @return a reference object to the created place.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Place createPlace(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}

		if (!this.nodes.containsKey(id)) {
			return addPlace(id, new Place(this, id));
		} else {
			throw new NodeExistsException(this, id);
		}
	}

	/**
	 * Creates a new place with an autogenerated id to the petri net.
	 * @return a reference object to the created place.
	 */
	public Place createPlace() {
		while (nodes.containsKey("p" + nextPlaceId)) {
			++nextPlaceId;
		}
		Place p = createPlace("p" + nextPlaceId);
		++nextPlaceId;
		return p;
	}

	/**
	 * Creates a new place to the petri net by copying the values from a existing place. It also copies the
	 * references of the extension of the given place.
	 * @param p the template place.
	 * @return a reference object to the created place.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Place createPlace(Place p) {
		if (p == null) {
			throw new IllegalArgumentException("p == null");
		}
		Place place = createPlace(p.getId());
		place.copyExtensions(p);
		return place;
	}

	/**
	 * Creates a few new places in this petri net by copying the values from the other places. It also copies the
	 * references of the extension of the given places.
	 * @param placeList a list of places to get the id and extensions from.
	 * @return a list of the newly created places.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Place[] createPlaces(Place... placeList) {
		if (placeList == null) {
			throw new IllegalArgumentException("places == null");
		}
		Place[] out = new Place[placeList.length];
		for (int i = 0; i < placeList.length; i++) {
			out[i] = this.createPlace(placeList[i]);
		}
		return out;
	}

	/**
	 * Creates a few new places in this petri net with the given ids.
	 * @param idList a list of ids.
	 * @return a list of the newly created places.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Place[] createPlaces(String... idList) {
		if (idList == null) {
			throw new IllegalArgumentException("places == null");
		}
		Place[] out = new Place[idList.length];
		for (int i = 0; i < idList.length; i++) {
			out[i] = this.createPlace(idList[i]);
		}
		return out;
	}

	/**
	 * Creates #count new places in this petri net with auto generated ids.
	 * @param count how many places should be created.
	 * @return a list of the newly created places.
	 */
	public Place[] createPlaces(int count) {
		Place[] out = new Place[count];
		for (int i = 0; i < count; i++) {
			out[i] = this.createPlace();
		}
		return out;
	}

	/**
	 * Creates a new transition to the petri net with the given id. The label of this transition will be it's id.
	 * @param id the id of the new transition.
	 * @return a reference object to the created transition.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Transition createTransition(String id) {
		return createTransition(id, id);
	}

	/**
	 * Add a given transition directly without any checks.
	 * @param id the id of the transition
	 * @param t the transition to add
	 * @return the transition
	 */
	private Transition addTransition(String id, Transition t) {
		this.transitions.put(id, t);
		this.nodes.put(id, t);
		// update pre- and postsets
		presetNodes.put(id, new HashSet<Node>());
		postsetNodes.put(id, new HashSet<Node>());
		presetEdges.put(id, new HashMap<EdgeKey, Flow>());
		postsetEdges.put(id, new HashMap<EdgeKey, Flow>());
		invokeListeners();
		return t;
	}

	/**
	 * Creates a new transition to the petri net with the given id and label.
	 * @param id    the id of the new transition.
	 * @param label the label of the new transition.
	 * @return a reference object to the created transition.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Transition createTransition(String id, String label) {
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}

		if (!this.nodes.containsKey(id)) {
			final Transition t = new Transition(this, id);
			t.label = label;
			return addTransition(id, t);
		} else {
			throw new NodeExistsException(this, id);
		}
	}

	/**
	 * Creates a new transition with an autogenerated id and the same as label to the petri net.
	 * @return a reference object to the created transition.
	 */
	public Transition createTransition() {
		while (nodes.containsKey("t" + nextTransitionId)) {
			++nextTransitionId;
		}
		Transition t = createTransition("t" + nextTransitionId);
		++nextTransitionId;
		return t;
	}

	/**
	 * Creates a new transition to the petri net by copying the values from a existing place. It also copies the
	 * references of the extension of the given transition.
	 * @param t the template transition.
	 * @return a reference object to the created transition.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Transition createTransition(Transition t) {
		if (t == null) {
			throw new IllegalArgumentException("t == null");
		}
		Transition trans = createTransition(t.getId(), t.getLabel());
		trans.copyExtensions(t);
		return trans;
	}

	/**
	 * Creates a few new transitions in this petri net by copying the values from the other transitions. It also
	 * copies the references of the extension of the given transitions.
	 * @param transitionList a list of transitions to get the id, label and extensions from.
	 * @return a list of the newly created transitions.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Transition[] createTransitions(Transition... transitionList) {
		if (transitionList == null) {
			throw new IllegalArgumentException("transitions == null");
		}
		Transition[] out = new Transition[transitionList.length];
		for (int i = 0; i < transitionList.length; i++) {
			out[i] = this.createTransition(transitionList[i]);
		}
		return out;
	}

	/**
	 * Creates a few new transitions in this petri net with the given ids.
	 * @param idList a list of ids.
	 * @return a list of the newly created transitions.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NodeExistsException      thrown if an other node with the same id already exists in the petri net.
	 */
	public Transition[] createTransitions(String... idList) {
		if (idList == null) {
			throw new IllegalArgumentException("transitions == null");
		}
		Transition[] out = new Transition[idList.length];
		for (int i = 0; i < idList.length; i++) {
			out[i] = this.createTransition(idList[i]);
		}
		return out;
	}

	/**
	 * Creates #count new transitions in this petri net with auto generated ids and the same as label.
	 * @param count how many transitions should be created.
	 * @return a list of the newly created transitions.
	 */
	public Transition[] createTransitions(int count) {
		Transition[] out = new Transition[count];
		for (int i = 0; i < count; i++) {
			out[i] = this.createTransition();
		}
		return out;
	}

	/**
	 * Removes a flow with the given source and target id from this petri net.
	 * @param sourceId the id of the source node of the flow to remove.
	 * @param targetId the id of the target node of the flow to remove.
	 * @throws NoSuchEdgeException      thrown if there is no flow with the given target and source in this petri
	 *                                  net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the ids does not match a node in this petri net.
	 */
	public void removeFlow(String sourceId, String targetId) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		// createEdgeKey() makes sure the node exists
		final EdgeKey key = this.createEdgeKey(sourceId, targetId);
		Flow f = postsetEdges.get(sourceId).get(key);
		if (f == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId);
		}
		// update pre- and postsets
		Set<Node> preNodes = presetNodes.get(targetId);
		if (preNodes != null) {
			preNodes.remove(nodes.get(sourceId));
		}
		Set<Node> postNodes = postsetNodes.get(sourceId);
		if (postNodes != null) {
			postNodes.remove(nodes.get(targetId));
		}
		Flow old;
		old = presetEdges.get(targetId).remove(key);
		assert old == f;
		old = postsetEdges.get(sourceId).remove(key);
		assert old == f;
		this.numFlows--;
		invokeListeners();
	}

	/**
	 * Removes a flow with the given source and target from this petri net.
	 * @param source the source node of the flow to remove.
	 * @param target the target node of the flow to remove.
	 * @throws NoSuchEdgeException      thrown if there is no flow with the given target and source in this petri
	 *                                  net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given nodes do not belong to this petri net.
	 */
	public void removeFlow(Node source, Node target) {
		if (source == null) {
			throw new IllegalArgumentException("source == null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target == null");
		}
		removeFlow(source.getId(), target.getId());
	}

	/**
	 * * Removes a flow from this petri net.
	 * @param flow the flow to remove.
	 * @throws NoSuchEdgeException      thrown if this flow does not belong to this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the belonging nodes do not match in this petri net.
	 */
	public void removeFlow(Flow flow) {
		if (flow == null) {
			throw new IllegalArgumentException("flow == null");
		}
		removeFlow(flow.getSourceId(), flow.getTargetId());
	}

	/**
	 * Removes a node with the given id from this petri net. It also removes all the flows that lead to/from the
	 * node.
	 * @param id the id of the node to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given node do not belong to this petri net.
	 */
	public void removeNode(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (this.transitions.containsKey(id)) {
			removeTransition(id);
		} else if (this.places.containsKey(id)) {
			removePlace(id);
		} else {
			throw new NoSuchNodeException(this, id);
		}
	}

	/**
	 * Removes a node from this petri net. It also removes all the flows that lead to/from the node.
	 * @param node the node to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given node do not belong to this petri net.
	 */
	public void removeNode(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		removeNode(node.getId());
	}

	/**
	 * Removes a node from this petri net. It also removes all the flows that lead to/from the node.
	 * @param id the id of the node to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given node do not belong to this petri net.
	 */
	private void rmNode(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		Node n = this.nodes.get(id);
		if (n == null) {
			throw new NoSuchNodeException(this, id);
		}

		// update flows
		Collection<Flow> pe = new HashSet<>(getPresetEdges(id));
		for (Flow f : pe) {
			this.removeFlow(f);
		}
		pe = new HashSet<>(getPostsetEdges(id));
		for (Flow f : pe) {
			this.removeFlow(f);
		}

		// update pre- and postsets
		presetNodes.remove(id);
		postsetNodes.remove(id);
		presetEdges.remove(id);
		postsetEdges.remove(id);
		this.nodes.remove(id);
	}

	/**
	 * Removes a place from this petri net. It also removes all the flows that lead to/from the place.
	 * @param id the id of the place to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given place do not belong to this petri net.
	 */
	public void removePlace(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!this.places.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		this.placesList = new ArrayList<>(this.placesList);
		this.placesList.remove(places.get(id));
		rmNode(id);
		places.remove(id);
		invokeListeners();
	}

	/**
	 * Removes a place from this petri net. It also removes all the flows that lead to/from the place.
	 * @param place the place to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given place do not belong to this petri net.
	 */
	public void removePlace(Place place) {
		if (place == null) {
			throw new IllegalArgumentException("place == null");
		}
		removePlace(place.getId());
	}

	/**
	 * Removes a transition from this petri net. It also removes all the flows that lead to/from the transition.
	 * @param id the id of the transition to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given transition do not belong to this petri net.
	 */
	public void removeTransition(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!this.transitions.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		rmNode(id);
		this.transitions.remove(id);
		invokeListeners();
	}

	/**
	 * Removes a transition from this petri net. It also removes all the flows that lead to/from the transition.
	 * @param trans the transition to remove.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given transition do not belong to this petri net.
	 */
	public void removeTransition(Transition trans) {
		if (trans == null) {
			throw new IllegalArgumentException("trans == null");
		}
		removeTransition(trans.getId());
	}

	/**
	 * Returns a flow with the given source and target id.
	 * @param sourceId the id of the source node of the flow.
	 * @param targetId the id of the target node of the flow.
	 * @return a reference to the flow with the given source and target.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the given nodes do not belong to this petri net.
	 * @throws NoSuchEdgeException      thrown if there is no flow between the given nodes in this petri net.
	 */
	public Flow getFlow(String sourceId, String targetId) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		// createEdgeKey() makes sure the node exists
		EdgeKey key = this.createEdgeKey(sourceId, targetId);
		Flow f = this.postsetEdges.get(sourceId).get(key);
		if (f == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId);
		}
		return f;
	}

	/**
	 * Returns a flow with the given source and target.
	 * @param source the source node of the flow.
	 * @param target the target node of the flow.
	 * @return a reference to the flow with the given source and target.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the given nodes do not belong to this petri net.
	 * @throws NoSuchEdgeException      thrown if there is no flow between the given nodes in this petri net.
	 */
	public Flow getFlow(Node source, Node target) {
		if (source == null) {
			throw new IllegalArgumentException("source == null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target == null");
		}
		return getFlow(source.getId(), target.getId());
	}

	/**
	 * Returns the place with the given id.
	 * @param id the id of the place to get.
	 * @return a reference to the place.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given id do not belong to this petri net.
	 */
	public Place getPlace(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		Place p = places.get(id);
		if (p == null) {
			throw new NoSuchNodeException(this, id);
		}
		return p;
	}

	/**
	 * Returns the transition with the given id.
	 * @param id the id of the transition to get.
	 * @return a reference to the transition.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the given id do not belong to this petri net.
	 */
	public Transition getTransition(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		Transition t = transitions.get(id);
		if (t == null) {
			throw new NoSuchNodeException(this, id);
		}
		return t;
	}

	/**
	 * Checks if this petri net contains a node with the given id.
	 * @param id the id of the node.
	 * @return true if the node exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsNode(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		return nodes.containsKey(id);
	}

	/**
	 * Checks if this petri net contains a node.
	 * @param n the node.
	 * @return true if the node exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("id == null");
		}
		return containsNode(n.getId());
	}

	/**
	 * Checks if this petri net contains a place with the given id.
	 * @param id the id of the place.
	 * @return true if the place exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsPlace(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		return places.containsKey(id);
	}

	/**
	 * Checks if this petri net contains a place.
	 * @param p the place.
	 * @return true if the place exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsPlace(Place p) {
		if (p == null) {
			throw new IllegalArgumentException("p == null");
		}
		return containsPlace(p.getId());
	}

	/**
	 * Checks if this petri net contains a transition with the given id.
	 * @param id the id of the transition.
	 * @return true if the transition exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsTransition(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		return transitions.containsKey(id);
	}

	/**
	 * Checks if this petri net contains a transition.
	 * @param t the transition.
	 * @return true if the transition exists in this petri net.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 */
	public boolean containsTransition(Transition t) {
		if (t == null) {
			throw new IllegalArgumentException("t == null");
		}
		return containsTransition(t.getId());
	}

	/**
	 * Gets a view of the places of this petri net.
	 * @return a view of the places of this petri net.
	 */
	public Set<Place> getPlaces() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique.
		return new CollectionToUnmodifiableSetAdapter<>(this.places.values());
	}

	/**
	 * Gets a view of the transitions of this petri net.
	 * @return a view of the transitions of this petri net.
	 */
	public Set<Transition> getTransitions() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique.
		return new CollectionToUnmodifiableSetAdapter<>(this.transitions.values());
	}

	/**
	 * Sets the initial marking for this petri net.
	 * @param m the marking to set as initial.
	 * @throws IllegalArgumentException thrown if the given marking do not belong to this petri net.
	 */
	public void setInitialMarking(Marking m) {
		if (m.getNet() != this) {
			throw new IllegalArgumentException("Marking do not belong to the"
				+ " net " + getName());
		}
		this.initialMarking = m;
		invokeListeners();
	}

	/**
	 * Returns a copy of the initial marking of this petri net.
	 * @return the copy of the initial marking of this petri net.
	 */
	public Marking getInitialMarking() {
		return new Marking(initialMarking);
	}

	/**
	 * Returns a copy of the initial marking of this petri net.
	 * @return the copy of the initial marking of this petri net.
	 * @deprecated Use {@link #getInitialMarking()} instead, because the function name is better.
	 */
	@Deprecated
	public Marking getInitialMarkingCopy() {
		return getInitialMarking();
	}

	/**
	 * Adds a final marking to this petri net.
	 * @param m the marking to add as final marking.
	 * @return true on successfully added.
	 * @throws IllegalArgumentException thrown if the given marking do not belong to this petri net.
	 */
	public boolean addFinalMarking(Marking m) {
		if (m.getNet() != this) {
			throw new IllegalArgumentException("Marking do not belong to the"
				+ " net " + getName());
		}
		return this.finalMarkings.add(m);
	}

	/**
	 * Gets a view of the final markings of this petri net.
	 * @return a view of the final markings of this petri net.
	 */
	public Set<Marking> getFinalMarkings() {
		return Collections.unmodifiableSet(this.finalMarkings);
	}

	/**
	 * Get a list of all places. This list is never modified and is instead replaced every time a place is created
	 * or removed. This is used by the {@link Marking} class.
	 * @return A list with all places of the net.
	 */
	List<Place> getPlacesList() {
		return placesList;
	}

	/**
	 * Creates an edge key to find or save a flow by it's primary key, that mean it's sourceId and targetId.
	 * @param sourceId the id of the source node.
	 * @param targetId the id of the target node.
	 * @return the edge key.
	 * @throws NoSuchNodeException thrown if one of the nodes does not exist in this petri net.
	 */
	private EdgeKey createEdgeKey(String sourceId, String targetId) {
		final Node s = this.getNode(sourceId);
		if (s == null) {
			throw new NoSuchNodeException(this, sourceId);
		}
		final Node t = this.getNode(targetId);
		if (t == null) {
			throw new NoSuchNodeException(this, targetId);
		}

		return new EdgeKey(sourceId, targetId);
	}

	/**
	 * Returns the initial token count of a place given by it's id.
	 * @param id the id of the place.
	 * @return the token count of the place.
	 */
	Token getInitialToken(String id) {
		return initialMarking.getToken(id);
	}

	/**
	 * Changes the initial marking of this petri net by setting the token count of a place given by it's id.
	 * @param id the id of the place.
	 * @param t  the token count to set.
	 */
	void setInitialToken(String id, Token t) {
		initialMarking = initialMarking.setTokenCount(id, t);
		invokeListeners();
	}

	/**
	 * Sets the weight of a flow with the given source and target id. Is the weight less then 1 the flow will be
	 * removed from this petri net.
	 * @param sourceId the id of the source node.
	 * @param targetId the id of the target node.
	 * @param w        the weight of the flow.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the nodes does not exist in this petri net.
	 * @throws NoSuchEdgeException      thrown if there is no flow between the given nodes in this petri net.
	 */
	void setFlowWeight(String sourceId, String targetId, int w) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		// createEdgeKey() makes sure the node exists
		EdgeKey key = createEdgeKey(sourceId, targetId);
		Flow f = postsetEdges.get(sourceId).get(key);
		if (f == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId);
		}
		if (w < 1) {
			this.removeFlow(sourceId, targetId);
		} else {
			f.weight = w;
			invokeListeners();
		}
	}

	/**
	 * Returns the place of a flow with the given source and target id.
	 * @param sourceId the id of the source node of the flow.
	 * @param targetId the id of the target node of the flow.
	 * @return the place which id is equal to the sourceId or targetId.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the nodes does not exist in this petri net.
	 */
	Place getFlowPlace(String sourceId, String targetId) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		Place p = places.get(sourceId);
		if (p != null) {
			return p;
		} else if (places.containsKey(targetId)) {
			return places.get(targetId);
		} else {
			throw new NoSuchNodeException(this, sourceId);
		}
	}

	/**
	 * Returns the transition of a flow with the given source and target id.
	 * @param sourceId the id of the source node of the flow.
	 * @param targetId the id of the target node of the flow.
	 * @return the transition which id is equal to the sourceId or targetId.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if one of the nodes does not exist in this petri net.
	 */
	Transition getFlowTransition(String sourceId, String targetId) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		Transition t = transitions.get(sourceId);
		if (t != null) {
			return t;
		} else if (transitions.containsKey(targetId)) {
			return transitions.get(targetId);
		} else {
			throw new NoSuchNodeException(this, sourceId);
		}
	}

	/**
	 * Sets the label of a transition with the given id.
	 * @param id    the id of the transition.
	 * @param label the label.
	 * @throws IllegalArgumentException thrown if passing a null argument.
	 * @throws NoSuchNodeException      thrown if the transition does not exist in this petri net.
	 */
	void setTransitionLabel(String id, String label) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		Transition t = transitions.get(id);
		if (t == null) {
			throw new NoSuchNodeException(this, id);
		}
		t.label = label;
		invokeListeners();
	}

	/**
	 * Checks if a transition can be fired under a given marking.
	 * @param id the id of the transition.
	 * @param m  the marking.
	 * @return true if the transition can be fired under the given marking.
	 */
	boolean getTransitionIsFireable(String id, Marking m) {
		for (Flow f : this.getPresetEdges(id)) {
			if (m.getToken(f.getPlace()).compareTo(Token.valueOf(f.getWeight())) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fires a transition and returns the resulting marking.
	 * @param id the id of the transition.
	 * @param m  the marking.
	 * @return the resulting marking.
	 * @throws TransitionFireException thrown if the transition is not fireable under the given marking.
	 */
	Marking fireTransition(String id, Marking m) {
		if (getTransitionIsFireable(id, m)) {
			for (Flow f : this.getPresetEdges(id)) {
				m = m.addTokenCount(f.getPlace(), -f.getWeight());
			}
			for (Flow f : this.getPostsetEdges(id)) {
				m = m.addTokenCount(f.getPlace(), +f.getWeight());
			}
			return m;
		} else {
			throw new TransitionFireException("transition '" + id
				+ "' is not fireable in marking '" + m.toString() + "'.");
		}
	}

	/**
	 * Calculates and returns the incidencematrix of this petri net.
	 * @return the incidencematrix of this petri net.
	 */
	public int[][] getIncidenceMatrix() {
		int[][] incidenceMatrix = new int[this.places.size()][this.transitions.size()];
		for (Flow a : getEdges()) {
			Transition t = a.getTransition();
			final int row = this.indexOfPlace(a.getPlace());
			final int col = this.indexOfTransition(t);
			if ((row >= 0) && (col >= 0)) {
				incidenceMatrix[row][col] += (a.getSourceId().equals(t.getId()))
					? a.getWeight() : -a.getWeight();
			}
		}
		return incidenceMatrix;
	}

	/**
	 * Returns the index of a place in the lexically ordered list of places.
	 * @param p the place.
	 * @return the index of the place.
	 */
	private int indexOfPlace(Place p) {
		final Iterator<Place> iter = this.places.values().iterator();
		for (int i = 0; iter.hasNext(); ++i) {
			if (Objects.equals(iter.next(), p)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of a transition in the lexically ordered list of transitions.
	 * @param t the transition.
	 * @return the index of the transition.
	 */
	private int indexOfTransition(Transition t) {
		final Iterator<Transition> iter = this.transitions.values().iterator();
		for (int i = 0; iter.hasNext(); ++i) {
			if (Objects.equals(iter.next(), t)) {
				return i;
			}
		}
		return -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Retrieves the node with the given id.
	 * @param id the id of the node.
	 * @return a reference object of a node identified by the id.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the id exists in this petri net.
	 */
	@Override
	public Node getNode(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		Node node = nodes.get(id);
		if (node != null) {
			return node;
		} else {
			throw new NoSuchNodeException(this, id);
		}
	}

	@Override
	public Set<Flow> getEdges() {
		return new AbstractSet<Flow>() {
			@Override
			public int size() {
				return PetriNet.this.numFlows;
			}

			@Override
			public Iterator<Flow> iterator() {
				return new Iterator<Flow>() {
					private Iterator<Map<EdgeKey, Flow>> postsetIter
						= PetriNet.this.postsetEdges.values().iterator();
					private Iterator<Flow> flowIter = emptyIterator();

					@Override
					public boolean hasNext() {
						while (!flowIter.hasNext() && postsetIter.hasNext())
							flowIter = postsetIter.next().values().iterator();
						return flowIter.hasNext();
					}

					@Override
					public Flow next() {
						// Update flowIter, if needed
						hasNext();
						return flowIter.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	public Set<Node> getNodes() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique.
		return new CollectionToUnmodifiableSetAdapter<>(this.nodes.values());
	}

	/**
	 * Calculates the preset nodes of a node with the given id.
	 * @param id - the id of the node
	 * @return the preset nodes of the given node.
	 */
	private Set<Node> calcPresetNodes(String id) {
		Set<Node> pre = presetNodes.get(id);
		if (pre == null) {
			pre = new HashSet<>();
			for (Flow a : this.getPresetEdges(id)) {
				pre.add(a.getSource());
			}
			presetNodes.put(id, pre);
		}
		return pre;
	}

	/**
	 * Calculates the postset nodes of a node with the given id.
	 * @param id - the id of the node
	 * @return the postset nodes of the given node.
	 */
	private Set<Node> calcPostsetNodes(String id) {
		Set<Node> post = postsetNodes.get(id);
		if (post == null) {
			post = new HashSet<>();
			for (Flow a : this.getPostsetEdges(id)) {
				post.add(a.getTarget());
			}
			postsetNodes.put(id, post);
		}
		return post;
	}

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the node identified by the id.
	 * @param id the id of a node.
	 * @return a unmodifiable set of nodes.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Node> getPresetNodes(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!nodes.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		return Collections.unmodifiableSet(calcPresetNodes(id));
	}

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of nodes.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Node> getPostsetNodes(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!nodes.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		return Collections.unmodifiableSet(calcPostsetNodes(id));
	}

	/**
	 * Retrieves a view of all edges targeting the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of flows.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Flow> getPresetEdges(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!nodes.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}

		return Collections.unmodifiableSet(new LinkedHashSet<>(presetEdges.get(id).values()));
	}

	/**
	 * Retrieves a view of all edges beginning in the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of flows.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Flow> getPostsetEdges(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!nodes.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		return Collections.unmodifiableSet(new LinkedHashSet<>(postsetEdges.get(id).values()));
	}

	/**
	 * Retrieves a view of all edges beginning in the given node.
	 * @param node The node.
	 * @return A unmodifiable set of flows.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Flow> getPostsetEdges(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		return getPostsetEdges(node.getId());
	}

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the given node.
	 * @param node The node.
	 * @return A unmodifiable set of nodes.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Node> getPostsetNodes(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		return getPostsetNodes(node.getId());
	}

	/**
	 * Retrieves a view of all edges targeting the given node.
	 * @param node The node.
	 * @return A unmodifiable set of flows.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Flow> getPresetEdges(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		return getPresetEdges(node.getId());
	}

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the given node
	 * @param node the node.
	 * @return a unmodifiable set of nodes.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Node> getPresetNodes(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		return getPresetNodes(node.getId());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
