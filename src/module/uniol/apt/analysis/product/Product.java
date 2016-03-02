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

package uniol.apt.analysis.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

public class Product {

	private final TransitionSystem ts1;
	private final TransitionSystem ts2;

	private TransitionSystem result;
	private Map<Pair<State, State>, State> resultStateCache;

	public Product(TransitionSystem ts1, TransitionSystem ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}

	/**
	 * Computes the synchronous product of the two transition systems supplied
	 * to the constructor.
	 * 
	 * @return The synchronous product transition system.
	 */
	public TransitionSystem getSyncProduct() {
		result = new TransitionSystem("sync_prod");
		resultStateCache = new HashMap<>();

		// Create the initial state for the product.
		Pair<State, Boolean> init = createOrGetProductState(ts1.getInitialState(), ts2.getInitialState());
		result.setInitialState(init.getFirst());

		// Queue that will save states which still need to be processed.
		Queue<State> workQueue = new LinkedList<>();
		workQueue.add(init.getFirst());

		while (!workQueue.isEmpty()) {
			State curr = workQueue.poll();

			// Retrieve states of the operand transition systems.
			State s1 = (State) curr.getExtension("s1");
			State s2 = (State) curr.getExtension("s2");

			for (Arc arc1 : s1.getPostsetEdges()) {
				for (Arc arc2 : s2.getPostsetEdges()) {
					if (arc1.getLabel().equals(arc2.getLabel())) {
						// Create new product state.
						Pair<State, Boolean> prod = createOrGetProductState(arc1.getTarget(), arc2.getTarget());

						// Connect curr state with the new state.
						result.createArc(curr, prod.getFirst(), arc1.getLabel());

						if (prod.getSecond()) {
							// Add new product state to work queue.
							workQueue.add(prod.getFirst());
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns the product state of s1 and s2. If it does not yet exist in the
	 * result transition system, the state is created. If it exists, it is
	 * returned.
	 * 
	 * @param s1
	 *            first state
	 * @param s2
	 *            second state
	 * @return A pair of the product state and a boolean that signals if a new
	 *         state was created (true) or an existing one was returned (false).
	 */
	private Pair<State, Boolean> createOrGetProductState(State s1, State s2) {
		Pair<State, State> statePair = new Pair<>(s1, s2);
		if (resultStateCache.containsKey(statePair)) {
			return new Pair<>(resultStateCache.get(statePair), false);
		} else {
			String name = s1.getId() + "_" + s2.getId();
			State state = result.createState(name);
			state.putExtension("s1", s1);
			state.putExtension("s2", s2);
			resultStateCache.put(statePair, state);
			return new Pair<>(state, true);
		}
	}

	public TransitionSystem getAsyncProduct() {
		throw new NotImplementedException();
	}

}
