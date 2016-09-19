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

package uniol.apt.analysis.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

/**
 * Provides methods to compute the synchronous or asynchronous product of two
 * LTS.
 *
 * @author Jonas Prellberg
 */
public class Product {

	/**
	 * Key for the extension of the product states with the first source
	 * state.
	 */
	private static final String EXTENSION_KEY_1 = "product_source_lts1_state";

	/**
	 * Key for the extension of the product states with the second source
	 * state.
	 */
	private static final String EXTENSION_KEY_2 = "product_source_lts2_state";

	/**
	 * The first factor for the product.
	 */
	private final TransitionSystem ts1;

	/**
	 * The second factor for the product.
	 */
	private final TransitionSystem ts2;

	/**
	 * The resulting product.
	 */
	private TransitionSystem result;

	/**
	 * A mapping from a pair of factor states to a product state. Used to
	 * prevent creation of duplicate product states.
	 */
	private Map<Pair<State, State>, State> resultStateCache;

	/**
	 * A queue to save new product states that have still to be examined,
	 * i.e. starting from which new arcs and states will be created.
	 */
	private Queue<State> workQueue;

	/**
	 * Creates a new Product instance that allows to compute the synchronous
	 * or asynchronous product of two given LTS.
	 *
	 * @param ts1
	 *                first operand/factor
	 * @param ts2
	 *                second operand/factor
	 */
	public Product(TransitionSystem ts1, TransitionSystem ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}

	/**
	 * Computes the synchronous product of the two transition systems
	 * supplied to the constructor.
	 *
	 * @return The synchronous product transition system.
	 */
	public TransitionSystem getSyncProduct() {
		init();

		while (!workQueue.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			State curr = workQueue.poll();

			// Retrieve states of the operand transition systems.
			State s1 = (State) curr.getExtension(EXTENSION_KEY_1);
			State s2 = (State) curr.getExtension(EXTENSION_KEY_2);

			for (Arc arc1 : s1.getPostsetEdges()) {
				State target1 = arc1.getTarget();
				for (State target2 : s2.getPostsetNodesByLabel(arc1.getLabel())) {
					// Create new product state.
					State prod = createOrGetProductState(target1, target2);

					// Connect curr state with new state.
					result.createArc(curr, prod, arc1.getLabel());
				}
			}
		}

		return result;
	}

	/**
	 * Computes the asynchronous product of the two transition systems
	 * supplied to the constructor.
	 *
	 * @return The asynchronous product transition system.
	 */
	public TransitionSystem getAsyncProduct() {
		init();

		while (!workQueue.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			State curr = workQueue.poll();

			// Retrieve states of the operand transition systems.
			State s1 = (State) curr.getExtension(EXTENSION_KEY_1);
			State s2 = (State) curr.getExtension(EXTENSION_KEY_2);

			for (Arc arc1 : s1.getPostsetEdges()) {
				// Create new product state.
				State prod = createOrGetProductState(arc1.getTarget(), s2);

				// Connect curr state with the new state.
				result.createArc(curr, prod, arc1.getLabel());
			}
			for (Arc arc2 : s2.getPostsetEdges()) {
				// Create new product state.
				State prod = createOrGetProductState(s1, arc2.getTarget());

				// Connect curr state with the new state.
				result.createArc(curr, prod, arc2.getLabel());
			}
		}

		return result;
	}

	/**
	 * Initializes data structures.
	 */
	private void init() {
		result = new TransitionSystem();
		resultStateCache = new HashMap<>();
		workQueue = new LinkedList<>();

		// Create the initial state for the product.
		State init = createOrGetProductState(ts1.getInitialState(), ts2.getInitialState());
		result.setInitialState(init);
	}

	/**
	 * Returns the product state of s1 and s2. If it does not yet exist in
	 * the result transition system, the state is created. If it exists, it
	 * is returned.
	 *
	 * Attention: This function modifies the workQueue. Newly created states
	 * are automatically inserted.
	 *
	 * @param s1
	 *                first state
	 * @param s2
	 *                second state
	 * @return the product state
	 */
	private State createOrGetProductState(State s1, State s2) {
		Pair<State, State> statePair = new Pair<>(s1, s2);
		if (resultStateCache.containsKey(statePair)) {
			return resultStateCache.get(statePair);
		} else {
			State state = result.createState();
			state.putExtension(EXTENSION_KEY_1, s1);
			state.putExtension(EXTENSION_KEY_2, s2);
			resultStateCache.put(statePair, state);
			workQueue.add(state);
			return state;
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
