/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.processmining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.TransformerUtils;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Create an LTS from individual words. Each word is enabled in a given TransitionSystem by adding new states and arcs
 * to it, as needed. To shrink the size of the result, additionally it is assumed that words with the same Parikh vector
 * should reach the same state. If wanted, a callback function can be provided that replaces the Parikh vector of a word
 * reaching some state with another Parikh vector to further identify states that should be identical.
 * @author Uli Schlachter
 */
public class CreateLTS {
	private final TransitionSystem ts = new TransitionSystem();
	private final Map<Object, State> stateMap = new HashMap<>();
	private final Transformer<ParikhVector, ? extends Object> transformer;

	private final static Transformer<ParikhVector, ?> NOP_TRANSFORMER = TransformerUtils.nopTransformer();

	/**
	 * Create a new instance of this class without any additional transformations.
	 */
	public CreateLTS() {
		this(NOP_TRANSFORMER);
	}

	/**
	 * Create a new instance of this class.
	 * @param transformer The transformer to use to identify identical states.
	 */
	public CreateLTS(Transformer<ParikhVector, ? extends Object> transformer) {
		this.transformer = transformer;
		ts.setInitialState(findOrCreateState(new ParikhVector()));
	}

	/// Find state reached by the given event (or null)
	private State findOrCreateState(ParikhVector pv) {
		Object transformedValue = transformer.transform(pv);
		State result = stateMap.get(transformedValue);
		if (result == null) {
			result = ts.createState();
			stateMap.put(transformedValue, result);
		}
		return result;
	}

	/**
	 * Add a word to the produced transition system.
	 * @param word The word to add
	 */
	public void addWord(List<String> word) {
		ParikhVector pv = new ParikhVector();
		State lastState = ts.getInitialState();
		for (String event : word) {
			pv = pv.add(event);
			State state = findOrCreateState(pv);
			if (!lastState.getPostsetNodesByLabel(event).contains(state))
				ts.createArc(lastState, state, event);

			lastState = state;
		}
	}

	public TransitionSystem getTransitionSystem() {
		return new TransitionSystem(ts);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
