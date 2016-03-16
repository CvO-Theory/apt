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

package uniol.apt.adt.ts;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.bag.TreeBag;

import uniol.apt.adt.AbstractGraph;
import uniol.apt.adt.CollectionToUnmodifiableSetAdapter;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.SoftMap;
import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.exception.StructureException;

import static org.apache.commons.collections4.iterators.EmptyIterator.emptyIterator;

/**
 * Represents a Transitionsystem. With states, arcs and an alphabet.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class TransitionSystem extends AbstractGraph<TransitionSystem, Arc, State>
	implements IGraph<TransitionSystem, Arc, State> {

	private String name;
	private int nextStateId = 0;
	private final SortedMap<String, State> states = new TreeMap<>();
	private final SortedBag<String> alphabet = new TreeBag<>();
	private final Map<String, Set<State>> presetNodes = new SoftMap<>();
	private final Map<String, Set<State>> postsetNodes = new SoftMap<>();
	private final Map<String, Map<ArcKey, Arc>> presetEdges = new HashMap<>();
	private final Map<String, Map<ArcKey, Arc>> postsetEdges = new HashMap<>();
	private int numArcs = 0;
	private State initialState = null;

	/**
	 * A cache for getting preset arcs by label. The mapping is <code>nodeId → (label → arcPreset)</code>.
	 */
	private final Map<String, Map<String, Set<Arc>>> presetEdgesByLabel = new HashMap<>();

	/**
	 * A cache for getting postset arcs by label. The mapping is <code>nodeId → (label → arcPostset)</code>.
	 */
	private final Map<String, Map<String, Set<Arc>>> postsetEdgesByLabel = new HashMap<>();

	/**
	 * Creates a new TransitionSystem with no name (e.g. "").
	 */
	public TransitionSystem() {
		this("");
	}

	/**
	 * Creates a new TransitionSystem with the given name.
	 * @param name the name of the transitionsystem as String.
	 */
	public TransitionSystem(String name) {
		this.name = name;
	}

	/**
	 * Copy-Constructor. Attention all extensions will be copied by reference.
	 * @param ts the transition system that gets copied.
	 */
	public TransitionSystem(TransitionSystem ts) {
		this.name = ts.name;
		this.nextStateId = ts.nextStateId;
		for (String key : ts.states.keySet()) {
			addState(key, new State(this, ts.states.get(key)));
		}
		// Iterate over all ArcKey instances
		for (Map<ArcKey, Arc> postsets : ts.postsetEdges.values()) {
			for (Map.Entry<ArcKey, Arc> entry : postsets.entrySet()) {
				addArc(entry.getKey(), new Arc(this, entry.getValue()));
			}
		}
		this.initialState = states.get(ts.getInitialState().getId());
		copyExtensions(ts);
	}

	/**
	 * Sets the initial state of the TransitionSystem.
	 * @param state the state which should get inital.
	 * @throws IllegalArgumentException on passing an empty state.
	 * @throws NoSuchNodeException      thrown if the state does not exist in this TransitionSystem.
	 */
	public void setInitialState(State state) {
		if (state == null) {
			throw new IllegalArgumentException("state == null");
		}
		setInitialState(state.getId());
	}

	/**
	 * Sets the initial state of the TransitionSystem.
	 * @param id the id of the state which should get initial.
	 * @throws IllegalArgumentException on passing an empty id.
	 * @throws NoSuchNodeException      if the state with the id does not exist in this TransitionSystem.
	 */
	public void setInitialState(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		this.initialState = this.states.get(id);
		invokeListeners();
		if (this.initialState == null) {
			throw new NoSuchNodeException(this, id);
		}
	}

	/**
	 * Returns the initial state of this TransitionSystem.
	 * @return the initial state.
	 * @throws StructureException if the initial state is not set.
	 */
	public State getInitialState() {
		if (this.initialState == null) {
			throw new StructureException("Initial state is not set in graph '" + getName() + "'.");
		}
		return this.initialState;
	}

	/**
	 * Add a given arc directly without any checks.
	 * @param key the key of the arc
	 * @param arc the arc to add
	 * @return the arc
	 */
	private Arc addArc(ArcKey key, Arc arc) {
		String targetId = key.getTargetId();
		String sourceId = key.getSourceId();
		this.presetEdges.get(targetId).put(key, arc);
		this.postsetEdges.get(sourceId).put(key, arc);
		this.numArcs++;
		this.addLabel(key.getLabel());
		//update pre- and postsets
		Set<State> preNodes = presetNodes.get(targetId);
		if (preNodes != null) {
			preNodes.add(this.getNode(sourceId));
		}
		Set<State> postNodes = postsetNodes.get(sourceId);
		if (postNodes != null) {
			postNodes.add(this.getNode(targetId));
		}
		// Update postsetByLabel cache.
		onArcAddedUpdateByLabelCache(arc);
		invokeListeners();
		return arc;
	}

	/**
	 * Creates an arc with an id for the source node, an id for the target node and a label.
	 * @param sourceId the id of the source state.
	 * @param targetId the id of the target state.
	 * @param label    the label this arc will receive.
	 * @return the created arc.
	 * @throws ArcExistsException       if a arc with this sourceId, targetId and label already exists in this
	 *                                  TransitionSystem.
	 * @throws NoSuchNodeException      if one of the nodes does not exist in this TransitionSystem.
	 * @throws IllegalArgumentException upon passing a null element.
	 */
	public Arc createArc(String sourceId, String targetId, String label) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		Arc a = new Arc(this, sourceId, targetId, label);
		ArcKey key = createArcKey(sourceId, targetId, label);
		if (postsetEdges.get(sourceId).containsKey(key)) {
			throw new ArcExistsException(this, key);
		}
		return addArc(key, a);
	}

	/**
	 * Creates an arc from one state (source) to another state (target) with the label.
	 * @param source the source state.
	 * @param target the target state.
	 * @param label  the label this arc will receive.
	 * @return the created arc.
	 * @throws ArcExistsException       if a arc with this source, target and label already exists in this
	 *                                  TransitionSystem.
	 * @throws NoSuchNodeException      if one of the nodes does not exist in this TransitionSystem.
	 * @throws IllegalArgumentException upon passing a null element.
	 */
	public Arc createArc(State source, State target, String label) {
		if (source == null) {
			throw new IllegalArgumentException("source == null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target == null");
		}
		return createArc(source.getId(), target.getId(), label);
	}

	/**
	 * Creates an arc with the same source node, target node and label as the given arc and copies the extensions
	 * from the arc marked as to copy to the new arc.
	 * @param arc the arc to copy.
	 * @return the created arc.
	 * @throws ArcExistsException       if a arc with the same source node, target node and label as the given arc
	 *                                  already exists in this TransitionSystem.
	 * @throws NoSuchNodeException      if one of the nodes (target node or source node of the given arc) does not
	 *                                  exist.
	 * @throws IllegalArgumentException upon passing a null element.
	 */
	public Arc createArc(Arc arc) {
		if (arc == null) {
			throw new IllegalArgumentException("arc == null");
		}
		Arc a = createArc(arc.getSourceId(), arc.getTargetId(), arc.getLabel());
		a.copyExtensions(arc);
		return a;
	}

	/**
	 * Add a given state directly without any checks.
	 * @param id the id of the state
	 * @param state the state to add
	 * @return the state
	 */
	private State addState(String id, State state) {
		states.put(id, state);
		// update pre- and postsets
		presetNodes.put(id, new HashSet<State>());
		postsetNodes.put(id, new HashSet<State>());
		presetEdges.put(id, new HashMap<ArcKey, Arc>());
		postsetEdges.put(id, new HashMap<ArcKey, Arc>());
		invokeListeners();
		return state;
	}

	/**
	 * Creates a new state with the given id
	 * @param id the id of the state.
	 * @return the created state.
	 * @throws NodeExistsException      if the state with this id already exists in this TransitionSystem.
	 * @throws IllegalArgumentException upon passing a null element.
	 */
	public State createState(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		State s = states.get(id);
		if (s != null) {
			throw new NodeExistsException(this, id);
		}
		return addState(id, new State(this, id));
	}

	/**
	 * Creates a new state with an auto-generated id. The id has the form sx with x the next free integer id in this
	 * TransitionSystem. For example s0, s1, etc..
	 * @return the newly created state.
	 */
	public State createState() {
		while (states.containsKey("s" + nextStateId)) {
			++nextStateId;
		}
		State s = createState("s" + nextStateId);
		++nextStateId;
		return s;
	}

	/**
	 * Creates a new state in this TransitionSystem by copying the id from another state and copying the extensions
	 * from the state marked as to copy to the new state.
	 * @param state the state to get the id and extensions.
	 * @return the newly created state.
	 * @throws IllegalArgumentException if the passed state is null.
	 * @throws NodeExistsException      if the given state in this TransitionSystem already exists.
	 */
	public State createState(State state) {
		if (state == null) {
			throw new IllegalArgumentException("state == null");
		}
		State s = createState(state.getId());
		s.copyExtensions(state);
		return s;
	}

	/**
	 * Creates a few new states in this TransitionSystem by copying the id from the given states and copying the
	 * extensions from the states marked as to copy to the new state.
	 * @param stateList a list of states to get the id and extensions of those states.
	 * @return a list of the newly created states.
	 * @throws IllegalArgumentException if the passed array of states is null.
	 * @throws NodeExistsException      if one state of the given list in this TransitionSystem already exists.
	 */
	public State[] createStates(State... stateList) {
		if (stateList == null) {
			throw new IllegalArgumentException("states == null");
		}
		State[] out = new State[stateList.length];
		for (int i = 0; i < stateList.length; i++) {
			out[i] = this.createState(stateList[i]);
		}
		return out;
	}

	/**
	 * Creates a few new states in this TransitionSystem by the given ids.
	 * @param idList a list of ids
	 * @return a list of the newly created states.
	 * @throws IllegalArgumentException if the passed array of ids is null.
	 * @throws NodeExistsException      if one id of the list in this TransitionSystem already exists.
	 */
	public State[] createStates(String... idList) {
		if (idList == null) {
			throw new IllegalArgumentException("states == null");
		}
		State[] out = new State[idList.length];
		for (int i = 0; i < idList.length; i++) {
			out[i] = this.createState(idList[i]);
		}
		return out;
	}

	/**
	 * Creates #count new states in this TransitionSystem with a auto generated ids.
	 * @param count how many states should be created.
	 * @return a list of the newly created states.
	 */
	public State[] createStates(int count) {
		State[] out = new State[count];
		for (int i = 0; i < count; i++) {
			out[i] = this.createState();
		}
		return out;
	}

	/**
	 * Removes the arc with the sourceId, targetI and label from this TransitionSystem if exists. It also updates
	 * the alphabet, if the removing arc was the only arc with this label.
	 * @param sourceId the source state id.
	 * @param targetId the target state id.
	 * @param label    the label of the arc which want to be removed.
	 * @throws IllegalArgumentException on passing null to any argument.
	 * @throws NoSuchNodeException      if the node does not exist in this TransitionSystem.
	 * @throws NoSuchEdgeException      if there is no arc with given sourceId, targetId and label in this
	 *                                  TransitionSystem.
	 */
	public void removeArc(String sourceId, String targetId, String label) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		// createArcKey() makes sure the node exists
		ArcKey key = createArcKey(sourceId, targetId, label);
		Arc a = this.postsetEdges.get(sourceId).get(key);
		if (a == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId);
		}
		// update pre- and postsets
		Set<State> preNodes = presetNodes.get(targetId);
		if (preNodes != null) {
			preNodes.remove(states.get(sourceId));
		}
		Set<State> postNodes = postsetNodes.get(sourceId);
		if (postNodes != null) {
			postNodes.remove(states.get(targetId));
		}

		// Update postsetByLabel cache.
		onArcRemovedUpdateByLabelCache(a);

		Arc old;
		old = presetEdges.get(targetId).remove(key);
		assert old == a;
		old = postsetEdges.get(sourceId).remove(key);
		assert old == a;
		this.numArcs--;
		removeLabel(label);
		invokeListeners();
	}

	/**
	 * Removes the given arc from this TransitionSystem by searching an arc with the same source, target and label
	 * as the given arc. It also updates the alphabet, if the removing arc was the only arc with it's label.
	 * @param a the arc which want to be removed.
	 * @throws StructureException       if the arc does not belong to the TransitionSystem. That mean it was not
	 *                                  created by this TransitionSystem.
	 * @throws IllegalArgumentException on passing null to any argument.
	 * @throws NoSuchNodeException      if the node does not exist in this TransitionSystem.
	 * @throws NoSuchEdgeException      if there is no arc such as the given arc in this TransitionSystem.
	 */
	public void removeArc(Arc a) {
		if (a == null) {
			throw new IllegalArgumentException("a == null");
		}
		if (this != a.getGraph()) {
			throw new StructureException("arc '" + a.toString() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		removeArc(a.getSourceId(), a.getTargetId(), a.getLabel());
	}

	/**
	 * Removes the state with the id from the TransitionSystem. It also sets the initial state of this
	 * TransitionSystem to null if the state belonging to this id has been the initial state of this
	 * TransitionSystem.
	 * @param id the id of the state which should be removed.
	 * @throws NoSuchNodeException      if there is no state with the given id in this TransitionSystem.
	 * @throws IllegalArgumentException on passing null to any argument.
	 */
	public void removeState(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!states.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}

		// update arcs
		Collection<Arc> pe = new HashSet<>(getPresetEdges(id));
		for (Arc a : pe) {
			this.removeArc(a);
		}
		pe = new HashSet<>(getPostsetEdges(id));
		for (Arc a : pe) {
			this.removeArc(a);
		}

		// update pre- and postsets
		presetNodes.remove(id);
		postsetNodes.remove(id);
		presetEdges.remove(id);
		postsetEdges.remove(id);

		// Update postsetByLabel cache.
		postsetEdgesByLabel.remove(id);

		if (initialState != null && initialState.getId().equals(id)) {
			initialState = null;
		}
		this.states.remove(id);
		invokeListeners();
	}

	/**
	 * Removes the given state from the TransitionSystem by searching a node with the same id as the given state.
	 * @param state the state to remove.
	 * @throws StructureException       if the state does not belong to the TransitionSystem. That mean was not
	 *                                  created by this TransitionSystem.
	 * @throws IllegalArgumentException on passing null to any argument.
	 * @throws NoSuchNodeException      if the state is not in this TransitionSystem.
	 */
	public void removeState(State state) {
		if (state == null) {
			throw new IllegalArgumentException("state == null");
		}
		if (this != state.getGraph()) {
			throw new StructureException("node'" + state.getId() + "' does not belong to the net '"
				+ this.getName() + "'.");
		}
		removeState(state.getId());
	}

	/**
	 * Gets the arc with the sourceId, targetId and label from this TransitionSystem.
	 * @param sourceId the source state id of this arc.
	 * @param targetId the target state id of this arc.
	 * @param label    the label of the arc.
	 * @return the arc.
	 * @throws IllegalArgumentException on passing null to any argument.
	 * @throws NoSuchNodeException      if some of the nodes given by there id's does not exist in this
	 *                                  TransitionSystem.
	 * @throws NoSuchEdgeException      if the arc given by the id's of source an target node and the label does not
	 *                                  exist in this TransitionSystem.
	 */
	public Arc getArc(String sourceId, String targetId, String label) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}
		// createArcKey() makes sure the node exists
		Arc a = postsetEdges.get(sourceId).get(createArcKey(sourceId, targetId, label));
		if (a == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId, label);
		}
		return a;
	}

	/**
	 * Gets the arc with the source, target and label from this TransitionSystem.
	 * @param source the source state of the arc.
	 * @param target the target state of the arc.
	 * @param label  the label of the arc.
	 * @return the arc.
	 * @throws IllegalArgumentException on passing null to any argument.
	 * @throws NoSuchNodeException      if the node does not exist.
	 * @throws NoSuchEdgeException      if the arc given by the id's of source an target node and the label does not
	 *                                  exist in this TransitionSystem.
	 * @throws StructureException       if the state source or target don't belong to this TransitionSystem. That
	 *                                  mean has not been created by this TransitionSystem.
	 */
	public Arc getArc(State source, State target, String label) {
		if (source == null) {
			throw new IllegalArgumentException("source == null");
		}
		if (target == null) {
			throw new IllegalArgumentException("target == null");
		}
		if (this != source.getGraph()) {
			throw new StructureException("source state '" + source.getId()
				+ "' does not belong to the net '" + this.getName() + "'.");
		}
		if (this != target.getGraph()) {
			throw new StructureException("target state '" + target.getId()
				+ "' does not belong to the net '" + this.getName() + "'.");
		}
		return getArc(source.getId(), target.getId(), label);
	}

	/**
	 * Gets a view of the alphabet of the TransitionSystem as an unmodifiableSortedSet.
	 * @return the alphabet of this TransitionSystem.
	 */
	public Set<String> getAlphabet() {
		return Collections.unmodifiableSet(this.alphabet.uniqueSet());
	}

	/**
	 * Creates an arc key to find or save an arc by it's primary key, that mean it's sourceId, targetId and label.
	 * @param sourceId the source node's id.
	 * @param targetId the target node's id.
	 * @param label    the label of the arc.
	 * @return the ArcKey.
	 * @throws NoSuchNodeException thrown if one of the states does not exist in this TransitionSystem.
	 */
	private ArcKey createArcKey(String sourceId, String targetId, String label) {
		if (!states.containsKey(sourceId)) {
			throw new NoSuchNodeException(this, sourceId);
		}
		if (!states.containsKey(targetId)) {
			throw new NoSuchNodeException(this, targetId);
		}
		return new ArcKey(sourceId, targetId, label);
	}

	/**
	 * Sets a new label to the arc identified by it's sourceId, targetId and the old label. It also updates the
	 * alphabet.
	 * @param sourceId the source node's id of the arc.
	 * @param targetId the target node's id of the arc.
	 * @param oldLabel the old label of the arc.
	 * @param newLabel the new label of the arc.
	 * @throws NoSuchNodeException      if one of the nodes does not exist in this TransitionSystem.
	 * @throws IllegalArgumentException if one argument is null.
	 * @throws ArcExistsException       thrown if there already exists an arc with sourceId, targetId and the new
	 *                                  label.
	 */
	void setArcLabel(String sourceId, String targetId, String oldLabel, String newLabel) {
		if (sourceId == null) {
			throw new IllegalArgumentException("sourceId == null");
		}
		if (targetId == null) {
			throw new IllegalArgumentException("targetId == null");
		}
		if (newLabel == null) {
			throw new IllegalArgumentException("label == null");
		}
		if (!oldLabel.equals(newLabel)) {
			// createArcKey() makes sure the node exists
			ArcKey oldKey = createArcKey(sourceId, targetId, oldLabel);
			ArcKey newKey = createArcKey(sourceId, targetId, newLabel);
			Map<ArcKey, Arc> postEdges = postsetEdges.get(sourceId);
			Map<ArcKey, Arc> preEdges = presetEdges.get(targetId);
			if (postEdges.containsKey(newKey)) {
				throw new ArcExistsException(this, newKey);
			}
			Arc a = postEdges.remove(oldKey);
			Arc a2 = preEdges.remove(oldKey);
			assert a == a2;
			onArcRemovedUpdateByLabelCache(a);
			a.label = newLabel;
			removeLabel(oldLabel);
			addLabel(newLabel);
			onArcAddedUpdateByLabelCache(a);
			preEdges.put(newKey, a);
			postEdges.put(newKey, a);
			invokeListeners();
		}
	}

	/**
	 * Updates the alphabet by incrementing the occurrences of this label or if it's new adding it to the alphabet.
	 * @param label the label to add.
	 */
	private void addLabel(String label) {
		alphabet.add(label);
	}

	/**
	 * Removes a label from the alphabet. That mean decrementing the occurrences of this label or if it's the last
	 * occurrences deleting it from the alphabet.
	 * @param label the label to remove.
	 */
	private void removeLabel(String label) {
		alphabet.remove(label, 1);
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
	 * @param id The id of the node as a String.
	 * @return a reference object of a state identified by the id.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the id exists in this transitionsystem.
	 */
	@Override
	public State getNode(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		State s = this.states.get(id);
		if (s == null) {
			throw new NoSuchNodeException(this, id);
		}
		return s;
	}

	@Override
	public Set<Arc> getEdges() {
		return new AbstractSet<Arc>() {
			@Override
			public int size() {
				return TransitionSystem.this.numArcs;
			}

			@Override
			public Iterator<Arc> iterator() {
				return new Iterator<Arc>() {
					private Iterator<Map<ArcKey, Arc>> postsetIter
						= TransitionSystem.this.postsetEdges.values().iterator();
					private Iterator<Arc> arcIter = emptyIterator();

					@Override
					public boolean hasNext() {
						while (!arcIter.hasNext() && postsetIter.hasNext())
							arcIter = postsetIter.next().values().iterator();
						return arcIter.hasNext();
					}

					@Override
					public Arc next() {
						// Update arcIter, if needed
						hasNext();
						return arcIter.next();
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
	public Set<State> getNodes() {
		// This really behaves like a Set, but the Map doesn't know that its values are unique.
		return new CollectionToUnmodifiableSetAdapter<>(this.states.values());
	}

	/**
	 * Calculates the preset nodes of a node with the given id.
	 * @param id - the id of the node
	 * @return the preset nodes of the given node.
	 */
	private Set<State> calcPresetNodes(String id) {
		Set<State> pre = presetNodes.get(id);
		if (pre == null) {
			pre = new HashSet<>();
			for (Arc a : this.getPresetEdges(id)) {
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
	private Set<State> calcPostsetNodes(String id) {
		Set<State> post = postsetNodes.get(id);
		if (post == null) {
			post = new HashSet<>();
			for (Arc a : this.getPostsetEdges(id)) {
				post.add(a.getTarget());
			}
			postsetNodes.put(id, post);
		}
		return post;
	}

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the node identified by the id.
	 * @param id the id of a node.
	 * @return a unmodifiable set of states.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<State> getPresetNodes(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!states.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		return Collections.unmodifiableSet(calcPresetNodes(id));
	}

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of states.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<State> getPostsetNodes(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!states.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		return Collections.unmodifiableSet(calcPostsetNodes(id));
	}

	/**
	 * Retrieves a view of all edges targeting the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of arcs.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Arc> getPresetEdges(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!states.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		// This really behaves like a Set, but the Map doesn't know that its values are unique
		return new CollectionToUnmodifiableSetAdapter<>(presetEdges.get(id).values());
	}

	/**
	 * Retrieves a view of all edges beginning in the node with the given id.
	 * @param id The id of a node.
	 * @return A unmodifiable set of arcs.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 */
	@Override
	public Set<Arc> getPostsetEdges(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		if (!states.containsKey(id)) {
			throw new NoSuchNodeException(this, id);
		}
		// This really behaves like a Set, but the Map doesn't know that its values are unique
		return new CollectionToUnmodifiableSetAdapter<>(postsetEdges.get(id).values());
	}

	/**
	 * Retrieves a view of all edges beginning in the given node.
	 * @param node The node.
	 * @return A unmodifiable set of arcs.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Arc> getPostsetEdges(State node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the ts '"
				+ this.getName() + "'.");
		}
		return getPostsetEdges(node.getId());
	}

	/**
	 * Retrieves a view of all nodes which have an incoming edge from the given node.
	 * @param node The node.
	 * @return A unmodifiable set of states.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<State> getPostsetNodes(State node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the ts '"
				+ this.getName() + "'.");
		}
		return getPostsetNodes(node.getId());
	}

	/**
	 * Retrieves a view of all edges targeting the given node.
	 * @param node The node.
	 * @return A unmodifiable set of arcs.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<Arc> getPresetEdges(State node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the ts '"
				+ this.getName() + "'.");
		}
		return getPresetEdges(node.getId());
	}

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the given node
	 * @param node the node.
	 * @return a unmodifiable set of states.
	 * @throws IllegalArgumentException thrown if null is passed to the method.
	 * @throws NoSuchNodeException      thrown if no node with the given id exists in this graph.
	 * @throws StructureException       thrown if the given node does not belong to this graph.
	 */
	@Override
	public Set<State> getPresetNodes(State node) {
		if (node == null) {
			throw new IllegalArgumentException("node == null");
		}
		if (this != node.getGraph()) {
			throw new StructureException("node'" + node.getId() + "' does not belong to the ts '"
				+ this.getName() + "'.");
		}
		return getPresetNodes(node.getId());
	}

	/**
	 * Returns the set of states from which the given node can be reached
	 * with the given label.
	 *
	 * @param node
	 *                the target node
	 * @param label
	 *                the label to look for
	 * @return a set of nodes that allow to reach the target node by the
	 *         given label
	 */
	public Set<State> getPresetNodesByLabel(State node, String label) {
		Set<Arc> arcs = getPresetEdgesByLabel(node, label);
		Set<State> states = new HashSet<>();
		for (Arc arc : arcs) {
			states.add(arc.getSource());
		}
		return states;
	}

	/**
	 * Returns the set of arcs that end in the given node and have the
	 * given label.
	 *
	 * @param node
	 *                the target node of all arcs in the result
	 * @param label
	 *                the label of all arcs in the result
	 * @return an unmodifiable set of arcs that end at the target node and
	 *         have the given label
	 */
	public Set<Arc> getPresetEdgesByLabel(State node, String label) {
		Set<Arc> arcs = getPresetEdgesByLabelCache(node.getId()).get(label);
		if (arcs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(arcs);
	}

	/**
	 * Returns a map of labels to arc presets for the given node.
	 *
	 * @param targetNodeId
	 *                all arc's source node id
	 * @return a map of labels to sets of arcs
	 */
	private Map<String, Set<Arc>> getPresetEdgesByLabelCache(String targetNodeId) {
		Map<String, Set<Arc>> result = presetEdgesByLabel.get(targetNodeId);
		if (result == null) {
			result = new HashMap<>();
			presetEdgesByLabel.put(targetNodeId, result);
		}
		return result;
	}

	/**
	 * Returns the set of states that is reached by arcs from the given node
	 * with the given label.
	 *
	 * @param node
	 *                the source node
	 * @param label
	 *                the label to look for
	 * @return a set of nodes that can be reached by arcs with
	 *         the given label
	 */
	public Set<State> getPostsetNodesByLabel(State node, String label) {
		Set<Arc> arcs = getPostsetEdgesByLabel(node, label);
		Set<State> states = new HashSet<>();
		for (Arc arc : arcs) {
			states.add(arc.getTarget());
		}
		return states;
	}

	/**
	 * Returns the set of arcs that start in the given node and have the
	 * given label.
	 *
	 * @param node
	 *                the source node of all arcs in the result
	 * @param label
	 *                the label of all arcs in the result
	 * @return an unmodifiable set of arcs that begin at the source node and
	 *         have the given label
	 */
	public Set<Arc> getPostsetEdgesByLabel(State node, String label) {
		Set<Arc> arcs = getPostsetEdgesByLabelCache(node.getId()).get(label);
		if (arcs == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(arcs);
	}

	/**
	 * Returns a map of labels to arc sets for the given node.
	 *
	 * @param sourceNodeId
	 *                all arc's source node id
	 * @return a map of labels to sets of arcs
	 */
	private Map<String, Set<Arc>> getPostsetEdgesByLabelCache(String sourceNodeId) {
		Map<String, Set<Arc>> result = postsetEdgesByLabel.get(sourceNodeId);
		if (result == null) {
			result = new HashMap<>();
			postsetEdgesByLabel.put(sourceNodeId, result);
		}
		return result;
	}

	/**
	 * Needs to be called whenever an arc is added to keep the cache
	 * consistent.
	 *
	 * @param arc the added arc
	 */
	private void onArcAddedUpdateByLabelCache(Arc arc) {
		// Update postset arc by label cache.
		Map<String, Set<Arc>> postsetsByLabel = getPostsetEdgesByLabelCache(arc.getSourceId());
		Set<Arc> postset = postsetsByLabel.get(arc.getLabel());
		if (postset == null) {
			postset = new HashSet<>();
		}
		postset.add(arc);
		postsetsByLabel.put(arc.getLabel(), postset);
		postsetEdgesByLabel.put(arc.getSourceId(), postsetsByLabel);
		// Update preset arc by label cache.
		Map<String, Set<Arc>> presetsByLabel = getPresetEdgesByLabelCache(arc.getTargetId());
		Set<Arc> preset = presetsByLabel.get(arc.getLabel());
		if (preset == null) {
			preset = new HashSet<>();
		}
		preset.add(arc);
		presetsByLabel.put(arc.getLabel(), preset);
		presetEdgesByLabel.put(arc.getTargetId(), presetsByLabel);
	}

	/**
	 * Needs to be called whenever an arc is removed to keep the cache
	 * consistent.
	 *
	 * @param arc the removed arc
	 */
	private void onArcRemovedUpdateByLabelCache(Arc arc) {
		// Update postset arc by label cache.
		Map<String, Set<Arc>> postsetsByLabel = getPostsetEdgesByLabelCache(arc.getSourceId());
		Set<Arc> postset = postsetsByLabel.get(arc.getLabel());
		if (postset == null) {
			postset = new HashSet<>();
		}
		postset.remove(arc);
		postsetsByLabel.put(arc.getLabel(), postset);
		postsetEdgesByLabel.put(arc.getSourceId(), postsetsByLabel);
		// Update preset arc by label cache.
		Map<String, Set<Arc>> presetsByLabel = getPresetEdgesByLabelCache(arc.getTargetId());
		Set<Arc> preset = presetsByLabel.get(arc.getLabel());
		if (preset == null) {
			preset = new HashSet<>();
		}
		preset.remove(arc);
		presetsByLabel.put(arc.getLabel(), preset);
		presetEdgesByLabel.put(arc.getTargetId(), presetsByLabel);
	}

	/**
	 * Returns true if this TS contains a state with the given id.
	 *
	 * @param sourceId
	 *                the state's id
	 * @return true, if a state with the id exists
	 */
	public boolean containsState(String sourceId) {
		return states.containsKey(sourceId);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
