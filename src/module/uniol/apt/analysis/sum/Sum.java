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

package uniol.apt.analysis.sum;

import java.util.HashMap;
import java.util.Map;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Provides methods to compute the synchronous or asynchronous sum of two PN.
 *
 * @author Jonas Prellberg
 */
public class Sum {

	/**
	 * First input PN.
	 */
	private final PetriNet pn1;

	/**
	 * Second input PN.
	 */
	private final PetriNet pn2;

	/**
	 * The resulting sum PN.
	 */
	private PetriNet sum;

	/**
	 * A map of transition ids to Transition objects inside the sum PN.
	 */
	private Map<String, Transition> transitionsById;

	/**
	 * A map of summand transitions to sum transitions.
	 */
	private Map<Transition, Transition> transitionCache;

	/**
	 * A map of summand places to sum places.
	 */
	private Map<Place, Place> placeCache;

	/**
	 * Creates a new Sum instance that allows to compute the synchronous or
	 * asynchronous sum of the two given PN.
	 *
	 * @param pn1
	 *                first summand
	 * @param pn2
	 *                second summand
	 */
	public Sum(PetriNet pn1, PetriNet pn2) {
		this.pn1 = pn1;
		this.pn2 = pn2;
	}

	/**
	 * Computes the asynchronous sum of the two PN supplied to the
	 * constructor.
	 *
	 * @return the asynchronous sum PN
	 */
	public PetriNet getAsyncSum() {
		try {
			return sum(false);
		} catch (LabelMismatchException e) {
			// If this exception is thrown during async sum
			// computation there is a bug.
			throw new AssertionError(e);
		}
	}

	/**
	 * Computes the synchronous sum of the two PN supplied to the
	 * constructor.
	 *
	 * @return the synchronous sum PN
	 * @throws LabelMismatchException
	 *                 thrown, when there exists a transition in both PNs
	 *                 with the same id but different labels
	 */
	public PetriNet getSyncSum() throws LabelMismatchException {
		return sum(true);
	}

	/**
	 * Returns either the sync or async sum depending on the boolean
	 * parameter.
	 *
	 * Since Java 7 does not have lambdas, a boolean flag is the easiest way
	 * to control how transitions are created.
	 *
	 * @param sync
	 * @return
	 * @throws LabelMismatchException
	 */
	private PetriNet sum(boolean sync) throws LabelMismatchException {
		sum = new PetriNet();
		transitionsById = new HashMap<>();
		transitionCache = new HashMap<>();
		placeCache = new HashMap<>();

		for (Flow flow : pn1.getEdges()) {
			processFlow(flow, sync);
		}
		for (Flow flow : pn2.getEdges()) {
			processFlow(flow, sync);
		}

		return sum;
	}

	/**
	 * Creates a corresponding flow in the sum PN. To do that, required
	 * places and transitions will be created or retrieved if they already
	 * exist.
	 *
	 * @param flow
	 *                a flow from one of the summands
	 * @param match
	 *                True for sync sum, so that transitions get merged by
	 *                matching id. False for async sum.
	 * @throws LabelMismatchException
	 */
	private void processFlow(Flow flow, boolean match) throws LabelMismatchException {
		InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

		Node source = flow.getSource();
		Node target = flow.getTarget();

		if (source instanceof Place && target instanceof Transition) {
			assert source == flow.getPlace();
			assert target == flow.getTransition();
			source = createOrGetPlace(flow.getPlace());
			target = createOrGetTransition(flow.getTransition(), match);
		} else {
			assert source == flow.getTransition();
			assert target == flow.getPlace();
			source = createOrGetTransition(flow.getTransition(), match);
			target = createOrGetPlace(flow.getPlace());
		}

		sum.createFlow(source, target, flow.getWeight());
	}

	private Node createOrGetTransition(Transition sourceTransition, boolean match) throws LabelMismatchException {
		if (match) {
			return createOrGetTransitionById(sourceTransition);
		} else {
			return createOrGetTransition(sourceTransition);
		}
	}

	/**
	 * Creates or retrieves a transition that is part of the sum PN. The sum
	 * transition keeps the original name as long as there are no conflicts.
	 *
	 * @param sourceTransition
	 *                the transition that the sum transition represents
	 * @return the sum transition
	 */
	private Node createOrGetTransition(Transition sourceTransition) {
		if (!transitionCache.containsKey(sourceTransition)) {
			Transition sumTransition;
			if (sum.containsTransition(sourceTransition.getId())) {
				sumTransition = sum.createTransition();
			} else {
				sumTransition = sum.createTransition(sourceTransition.getId());
			}
			transitionCache.put(sourceTransition, sumTransition);
			return sumTransition;
		}

		return transitionCache.get(sourceTransition);
	}

	/**
	 * Creates or retrieves a transition that is part of the sum PN matching
	 * the given source transition by id and label.
	 *
	 * @param sourceTransition
	 *                the transition that the sum transition represents
	 * @return a transition in the sum PN with the same id and label
	 * @throws LabelMismatchException
	 *                 thrown if there already exists a transition in the
	 *                 sum PN with a matching id but different label
	 */
	private Transition createOrGetTransitionById(Transition sourceTransition) throws LabelMismatchException {
		String id = sourceTransition.getId();
		String label = sourceTransition.getLabel();

		if (!transitionsById.containsKey(id)) {
			Transition sumTransition = sum.createTransition(id, label);
			transitionsById.put(id, sumTransition);
			return sumTransition;
		}

		Transition sumTransition = transitionsById.get(id);
		// If ids match but labels don't, throw an exception.
		if (!sumTransition.getLabel().equals(label)) {
			throw new LabelMismatchException(sumTransition, sourceTransition);
		}

		return sumTransition;
	}

	/**
	 * Creates or retrieves a place that is part of the sum PN.
	 *
	 * @param sourcePlace
	 *                the place that the new place represents
	 * @return the sum place
	 */
	private Place createOrGetPlace(Place sourcePlace) {
		if (!placeCache.containsKey(sourcePlace)) {
			Place sumPlace = sum.createPlace();
			sumPlace.setInitialToken(sourcePlace.getInitialToken());
			placeCache.put(sourcePlace, sumPlace);
			return sumPlace;
		}

		return placeCache.get(sourcePlace);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
