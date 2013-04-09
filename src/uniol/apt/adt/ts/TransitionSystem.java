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

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.extension.Extensible;

/**
 * Represents a Transitionsystem. With states, arcs and an alphabet. It holds the pre- and postsets with the help of
 * SoftReferences so its more efficient. That means it saves the pre- and postsets, but if there is not enough space the
 * garbage collector can delete this sets and if they are needed, they will just new calculated.
 * <p/>
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class TransitionSystem extends Extensible implements IGraph<TransitionSystem, Arc, State> {

	private String name;
	private int nextStateId = 0;
	private final Map<ArcKey, Arc> arcs = new HashMap<>();
	private final SortedMap<String, State> states = new TreeMap<>();
	private final SortedSet<String> alphabet = new TreeSet<>();
	private final Map<String, Integer> alphabetCount = new HashMap<>();
	private SoftReference<Map<String, SoftReference<Set<State>>>> presetNodes = null;
	private SoftReference<Map<String, SoftReference<Set<State>>>> postsetNodes = null;
	private SoftReference<Map<String, SoftReference<Set<Arc>>>> presetEdges = null;
	private SoftReference<Map<String, SoftReference<Set<Arc>>>> postsetEdges = null;
	private State initialState = null;
	private long labelRev = 0;

	/**
	 * Creates a new TransitionSystem with no name (e.g. "").
	 */
	public TransitionSystem() {
		this("");
	}

	/**
	 * Creates a new TransitionSystem with the given name.
	 * <p/>
	 * @param name the name of the transitionsystem as String.
	 */
	public TransitionSystem(String name) {
		this.name = name;
		initialisePrePostSets();
	}

	/**
	 * Copy-Constructor. Attention all extensions will be copied by reference.
	 * <p/>
	 * @param ts the transition system that gets copied.
	 */
	public TransitionSystem(TransitionSystem ts) {
		this.name = ts.name;
		this.nextStateId = ts.nextStateId;
		this.labelRev = ts.labelRev;
		for (String key : ts.states.keySet()) {
			this.states.put(key, new State(this, ts.states.get(key)));
		}
		for (ArcKey key : ts.arcs.keySet()) {
			this.arcs.put(key, new Arc(this, ts.arcs.get(key)));
		}
		this.alphabet.addAll(ts.alphabet);
		this.alphabetCount.putAll(ts.alphabetCount);
		this.initialState = states.get(ts.getInitialState().getId());
		copyExtensions(ts);
		initialisePrePostSets();
	}

	/**
	 * Initialises the SoftReferences for the pre- and postsets.
	 */
	private void initialisePrePostSets() {
		Map<String, SoftReference<Set<State>>> pre = new HashMap<>();
		Map<String, SoftReference<Set<State>>> post = new HashMap<>();
		presetNodes = new SoftReference<>(pre);
		postsetNodes = new SoftReference<>(post);
		Map<String, SoftReference<Set<Arc>>> preE = new HashMap<>();
		Map<String, SoftReference<Set<Arc>>> postE = new HashMap<>();
		presetEdges = new SoftReference<>(preE);
		postsetEdges = new SoftReference<>(postE);
	}

	/**
	 * Sets the initial state of the TransitionSystem.
	 * <p/>
	 * @param state the state which should get inital.
	 * <p/>
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
	 * <p/>
	 * @param id the id of the state which should get initial.
	 * <p/>
	 * @throws IllegalArgumentException on passing an empty id.
	 * @throws NoSuchNodeException      if the state with the id does not exist in this TransitionSystem.
	 */
	public void setInitialState(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id == null");
		}
		this.initialState = this.states.get(id);
		if (this.initialState == null) {
			throw new NoSuchNodeException(this, id);
		}
	}

	/**
	 * Returns the initial state of this TransitionSystem.
	 * <p/>
	 * @return the initial state.
	 * <p/>
	 * @throws StructureException if the initial state is not set.
	 */
	public State getInitialState() {
		if (this.initialState == null) {
			throw new StructureException("Initial state is not set in graph '" + getName() + "'.");
		}
		return this.initialState;
	}

	/**
	 * Creates an arc with an id for the source node, an id for the target node and a label.
	 * <p/>
	 * @param sourceId the id of the source state.
	 * @param targetId the id of the target state.
	 * @param label    the label this arc will receive.
	 * <p/>
	 * @return the created arc.
	 * <p/>
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
		if (this.arcs.containsKey(key)) {
			throw new ArcExistsException(this, key);
		}
		this.arcs.put(key, a);
		this.addLabel(label);
		//update pre- and postsets
		calcPresetNodes(targetId).add(this.getNode(sourceId));
		calcPostsetNodes(sourceId).add(this.getNode(targetId));
		calcPresetEdges(targetId).add(a);
		calcPostsetEdges(sourceId).add(a);
		return a;
	}

	/**
	 * Creates an arc from one state (source) to another state (target) with the label.
	 * <p/>
	 * @param source the source state.
	 * @param target the target state.
	 * @param label  the label this arc will receive.
	 * <p/>
	 * @return the created arc.
	 * <p/>
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
	 * <p/>
	 * @param arc the arc to copy.
	 * <p/>
	 * @return the created arc.
	 * <p/>
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
	 * Creates a new state with the given id
	 * <p/>
	 * @param id the id of the state.
	 * <p/>
	 * @return the created state.
	 * <p/>
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
		s = new State(this, id);
		states.put(id, s);
		// update pre- and postsets
		Map<String, SoftReference<Set<State>>> preSets = presetNodes.get();
		if (preSets == null) {
			preSets = new HashMap<>();
			presetNodes = new SoftReference<>(preSets);
		}
		Set<State> pre = new HashSet<>();
		preSets.put(id, new SoftReference<>(pre));

		Map<String, SoftReference<Set<State>>> postSets = postsetNodes.get();
		if (postSets == null) {
			postSets = new HashMap<>();
			postsetNodes = new SoftReference<>(postSets);
		}
		Set<State> post = new HashSet<>();
		postSets.put(id, new SoftReference<>(post));

		Map<String, SoftReference<Set<Arc>>> preSetsE = presetEdges.get();
		if (preSetsE == null) {
			preSetsE = new HashMap<>();
			presetEdges = new SoftReference<>(preSetsE);
		}
		Set<Arc> preE = new HashSet<>();
		preSetsE.put(id, new SoftReference<>(preE));

		Map<String, SoftReference<Set<Arc>>> postSetsE = postsetEdges.get();
		if (postSetsE == null) {
			postSetsE = new HashMap<>();
			postsetEdges = new SoftReference<>(postSetsE);
		}
		Set<Arc> postE = new HashSet<>();
		postSetsE.put(id, new SoftReference<>(postE));
		return s;
	}

	/**
	 * Creates a new state with an auto-generated id. The id has the form sx with x the next free integer id in this
	 * TransitionSystem. For example s0, s1, etc..
	 * <p/>
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
	 * <p/>
	 * @param state the state to get the id and extensions.
	 * <p/>
	 * @return the newly created state.
	 * <p/>
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
	 * <p/>
	 * @param stateList a list of states to get the id and extensions of those states.
	 * <p/>
	 * @return a list of the newly created states.
	 * <p/>
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
	 * <p/>
	 * @param idList a list of ids
	 * <p/>
	 * @return a list of the newly created states.
	 * <p/>
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
	 * <p/>
	 * @param count how many states should be created.
	 * <p/>
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
	 * <p/>
	 * @param sourceId the source state id.
	 * @param targetId the target state id.
	 * @param label    the label of the arc which want to be removed.
	 * <p/>
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
		ArcKey key = createArcKey(sourceId, targetId, label);
		Arc a = this.arcs.get(key);
		if (a == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId);
		}
		// update pre- and postsets
		if (presetNodes.get() != null) {
			Set<State> pre = presetNodes.get().get(targetId).get();
			if (pre != null) {
				pre.remove(states.get(sourceId));
			}
		}
		if (postsetNodes.get() != null) {
			Set<State> post = postsetNodes.get().get(sourceId).get();
			if (post != null) {
				post.remove(states.get(targetId));
			}
		}
		if (presetEdges.get() != null) {
			Set<Arc> pre = presetEdges.get().get(targetId).get();
			if (pre != null) {
				pre.remove(a);
			}
		}
		if (postsetEdges.get() != null) {
			Set<Arc> post = postsetEdges.get().get(sourceId).get();
			if (post != null) {
				post.remove(a);
			}
		}

		arcs.remove(key);
		removeLabel(label);
	}

	/**
	 * Removes the given arc from this TransitionSystem by searching an arc with the same source, target and label
	 * as the given arc. It also updates the alphabet, if the removing arc was the only arc with it's label.
	 * <p/>
	 * @param a the arc which want to be removed.
	 * <p/>
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
	 * <p/>
	 * @param id the id of the state which should be removed.
	 * <p/>
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
		if (presetNodes.get() != null && presetNodes.get().get(id).get() != null) {
			presetNodes.get().remove(id);
		}
		if (postsetNodes.get() != null && postsetNodes.get().get(id).get() != null) {
			postsetNodes.get().remove(id);
		}
		if (presetEdges.get() != null && presetEdges.get().get(id).get() != null) {
			presetEdges.get().remove(id);
		}
		if (postsetEdges.get() != null && postsetEdges.get().get(id).get() != null) {
			postsetEdges.get().remove(id);
		}

		if (initialState != null && initialState.getId().equals(id)) {
			initialState = null;
		}
		this.states.remove(id);
	}

	/**
	 * Removes the given state from the TransitionSystem by searching a node with the same id as the given state.
	 * <p/>
	 * @param state the state to remove.
	 * <p/>
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
	 * <p/>
	 * @param sourceId the source state id of this arc.
	 * @param targetId the target state id of this arc.
	 * @param label    the label of the arc.
	 * <p/>
	 * @return the arc.
	 * <p/>
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
		Arc a = this.arcs.get(createArcKey(sourceId, targetId, label));
		if (a == null) {
			throw new NoSuchEdgeException(this, sourceId, targetId, label);
		}
		return a;
	}

	/**
	 * Gets the arc with the source, target and label from this TransitionSystem.
	 * <p/>
	 * @param source the source state of the arc.
	 * @param target the target state of the arc.
	 * @param label  the label of the arc.
	 * <p/>
	 * @return the arc.
	 * <p/>
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
	 * <p/>
	 * @return the alphabet of this TransitionSystem.
	 */
	public Set<String> getAlphabet() {
		return Collections.unmodifiableSortedSet(this.alphabet);
	}

	/**
	 * Creates an arc key to find or save an arc by it's primary key, that mean it's sourceId, targetId and label.
	 * <p/>
	 * @param sourceId the source node's id.
	 * @param targetId the target node's id.
	 * @param label    the label of the arc.
	 * <p/>
	 * @return the ArcKey.
	 * <p/>
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
	 * <p/>
	 * @param sourceId the source node's id of the arc.
	 * @param targetId the target node's id of the arc.
	 * @param oldLabel the old label of the arc.
	 * @param newLabel the new label of the arc.
	 * <p/>
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
			ArcKey oldKey = createArcKey(sourceId, targetId, oldLabel);
			ArcKey newKey = createArcKey(sourceId, targetId, newLabel);
			if (this.arcs.containsKey(newKey)) {
				throw new ArcExistsException(this, newKey);
			}
			Arc a = this.arcs.remove(oldKey);
			a.label = newLabel;
			removeLabel(oldLabel);
			addLabel(newLabel);
			this.arcs.put(newKey, a);
		}
	}

	/**
	 * Updates the alphabet by incrementing the occurrences of this label or if it's new adding it to the alphabet.
	 * <p/>
	 * @param label the label to add.
	 */
	private void addLabel(String label) {
		Integer count = alphabetCount.get(label);
		if (count == null) {
			alphabetCount.put(label, 1);
			alphabet.add(label);
		} else {
			alphabetCount.put(label, ++count);
		}
		++labelRev;
	}

	/**
	 * Removes a label from the alphabet. That mean decrementing the occurrences of this label or if it's the last
	 * occurrences deleting it from the alphabet.
	 * <p/>
	 * @param label the label to remove.
	 */
	private void removeLabel(String label) {
		Integer count = alphabetCount.get(label);
		if (count != null) {
			if (count == 1) {
				alphabetCount.remove(label);
				alphabet.remove(label);
			} else {
				alphabetCount.put(label, --count);
			}
			++labelRev;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the revision number of the label. For example for the parikh vectors to see, if they have to update.
	 * <p/>
	 * @return the revision number of the label.
	 */
	public long getLabelRev() {
		return labelRev;
	}

	/**
	 * Retrieves the node with the given id.
	 * <p/>
	 * @param id The id of the node as a String.
	 * <p/>
	 * @return a reference object of a state identified by the id.
	 * <p/>
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
		return Collections.unmodifiableSet(new HashSet<>(this.arcs.values()));
	}

	@Override
	public Set<State> getNodes() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(this.states.values()));
	}
	
	@Override
	public State getNodeByExtension(String key, Object value) {
		for(State s : this.states.values()) {
			Object ext = s.getExtension(key);
			if(ext != null && ext.equals(value)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Calculates the preset nodes of a node with the given id.
	 * <p/>
	 * @param id - the id of the node
	 * <p/>
	 * @return the preset nodes of the given node.
	 */
	private Set<State> calcPresetNodes(String id) {
		// save hardreference so that garbage won't work
		Map<String, SoftReference<Set<State>>> preSets = presetNodes.get();
		if (preSets == null) {
			preSets = new HashMap<>();
			presetNodes = new SoftReference<>(preSets);
		}
		SoftReference<Set<State>> preSoft = preSets.get(id);
		Set<State> pre = (preSoft != null) ? preSoft.get() : null;
		if (pre == null) {
			pre = new HashSet<>();
			for (Arc a : this.getPresetEdges(id)) {
				pre.add(a.getSource());
			}
			preSets.put(id, new SoftReference<>(pre));
		}
		return pre;
	}

	/**
	 * Calculates the postset nodes of a node with the given id.
	 * <p/>
	 * @param id - the id of the node
	 * <p/>
	 * @return the postset nodes of the given node.
	 */
	private Set<State> calcPostsetNodes(String id) {
		Map<String, SoftReference<Set<State>>> postSets = postsetNodes.get();
		if (postSets == null) {
			postSets = new HashMap<>();
			postsetNodes = new SoftReference<>(postSets);
		}
		SoftReference<Set<State>> postSoft = postSets.get(id);
		Set<State> post = (postSoft != null) ? postSoft.get() : null;
		if (post == null) {
			post = new HashSet<>();
			for (Arc a : this.getPostsetEdges(id)) {
				post.add(a.getTarget());
			}
			postSets.put(id, new SoftReference<>(post));
		}
		return post;
	}

	/**
	 * Calculates the preset edges of a node with the given id.
	 * <p/>
	 * @param id - the id of the node
	 * <p/>
	 * @return the preset edges of the given node.
	 */
	private Set<Arc> calcPresetEdges(String id) {
		Map<String, SoftReference<Set<Arc>>> preSets = presetEdges.get();
		if (preSets == null) {
			preSets = new HashMap<>();
			presetEdges = new SoftReference<>(preSets);
		}
		SoftReference<Set<Arc>> preSoft = preSets.get(id);
		Set<Arc> pre = (preSoft != null) ? preSoft.get() : null;
		if (pre == null) {
			pre = new HashSet<>();
			for (Arc a : this.getEdges()) {
				if (a.getTarget().getId().equals(id)) {
					pre.add(a);
				}
			}
			preSets.put(id, new SoftReference<>(pre));
		}
		return pre;
	}

	/**
	 * Calculates the postset edges of a node with the given id.
	 * <p/>
	 * @param id - the id of the node
	 * <p/>
	 * @return the postset edges of the given node.
	 */
	private Set<Arc> calcPostsetEdges(String id) {
		Map<String, SoftReference<Set<Arc>>> postSets = postsetEdges.get();
		if (postSets == null) {
			postSets = new HashMap<>();
			postsetEdges = new SoftReference<>(postSets);
		}
		SoftReference<Set<Arc>> postSoft = postSets.get(id);
		Set<Arc> post = (postSoft != null) ? postSoft.get() : null;
		if (post == null) {
			post = new HashSet<>();
			for (Arc a : this.getEdges()) {
				if (a.getSource().getId().equals(id)) {
					post.add(a);
				}
			}
			postSets.put(id, new SoftReference<>(post));
		}
		return post;
	}

	/**
	 * Retrieves a view of all nodes which have an outgoing edge to the node identified by the id.
	 * <p/>
	 * @param id the id of a node.
	 * <p/>
	 * @return a unmodifiable set of states.
	 * <p/>
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
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of states.
	 * <p/>
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
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of arcs.
	 * <p/>
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
		return Collections.unmodifiableSet(calcPresetEdges(id));
	}

	/**
	 * Retrieves a view of all edges beginning in the node with the given id.
	 * <p/>
	 * @param id The id of a node.
	 * <p/>
	 * @return A unmodifiable set of arcs.
	 * <p/>
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
		return Collections.unmodifiableSet(calcPostsetEdges(id));
	}

	/**
	 * Retrieves a view of all edges beginning in the given node.
	 * <p/>
	 * @param node The node.
	 * <p/>
	 * @return A unmodifiable set of arcs.
	 * <p/>
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
	 * <p/>
	 * @param node The node.
	 * <p/>
	 * @return A unmodifiable set of states.
	 * <p/>
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
	 * <p/>
	 * @param node The node.
	 * <p/>
	 * @return A unmodifiable set of arcs.
	 * <p/>
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
	 * <p/>
	 * @param node the node.
	 * <p/>
	 * @return a unmodifiable set of states.
	 * <p/>
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
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
