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

import java.util.LinkedList;
import java.util.Queue;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

public class Product {

	private final TransitionSystem ts1;
	private final TransitionSystem ts2;

	public Product(TransitionSystem ts1, TransitionSystem ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}

	public TransitionSystem getSyncProduct() {
		TransitionSystem prodTS = new TransitionSystem("sync_prod");
		
		// Create the initial state for the product.
		State init = createProductState(prodTS, ts1.getInitialState(), ts2.getInitialState());
		prodTS.setInitialState(init);

		// Queue that will save states which still need to be processed.
		Queue<State> workQueue = new LinkedList<>();
		workQueue.add(init);

		while (!workQueue.isEmpty()) {
			State curr = workQueue.poll();

			// Retrieve states of the operand transition systems.
			State s1 = (State) curr.getExtension("s1");
			State s2 = (State) curr.getExtension("s2");

			for (Arc arc1 : s1.getPostsetEdges()) {
				for (Arc arc2 : s2.getPostsetEdges()) {
					if (arc1.getLabel().equals(arc2.getLabel())) {
						// Create new product state.
						State sprod = createProductState(prodTS, arc1.getTarget(), arc2.getTarget());

						// Connect curr state with the new state.
						prodTS.createArc(curr, sprod, arc1.getLabel());

						// Add new product state to work queue.
						workQueue.add(sprod);
					}
				}
			}
		}

		return prodTS;
	}

	private State createProductState(TransitionSystem ts, State s1, State s2) {
		String name = s1.getId() + "_" + s2.getId();
		State state = ts.createState(name);
		state.putExtension("s1", s1);
		state.putExtension("s2", s2);
		return state;
	}

	public TransitionSystem getAsyncProduct() {
		throw new NotImplementedException();
	}

}
