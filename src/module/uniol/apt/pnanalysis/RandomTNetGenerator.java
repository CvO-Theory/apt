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

package uniol.apt.pnanalysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.generator.marking.MarkingNetGenerator;
import uniol.apt.generator.tnet.TNetGenerator;

/**
 * Generates random t-nets and systems.
 * @author Manuel Gieseking
 */
public class RandomTNetGenerator {

	/**
	 * Hidden Constructor.
	 */
	private RandomTNetGenerator() {
	}

	/**
	 * Creates a t-net with maximal g places.
	 * @param g the maximal number of places.
	 * @return a random created t-net.
	 */
	public static PetriNet createRandomTNet(int g) {
		Random r = new Random();
		int placeCount = r.nextInt(g) + 1;
		PetriNet net = new PetriNet("Random T-Net");
		net.createPlaces(placeCount);
		int transitionCount = r.nextInt(2 * placeCount + 1) + 1;
		net.createTransitions(transitionCount);
		Transition[] transitions = new Transition[net.getTransitions().size()];
		net.getTransitions().toArray(transitions);
		for (Place place : net.getPlaces()) {
			// Postset
			int transId = r.nextInt(transitions.length);
			net.createFlow(place, transitions[transId]);
			// Preset
			transId = r.nextInt(transitions.length);
			net.createFlow(transitions[transId], place);
		}
		// Make to a net for the E-Module
		Set<Transition> emptyPre = new HashSet<>();
		Set<Transition> emptyPost = new HashSet<>();
		Transition dead = null;
		for (Transition transition : transitions) {
			Set<Node> post = transition.getPostsetNodes();
			Set<Node> pre = transition.getPresetNodes();
			// Delete all dead Transitions except of one. If there are two not all cycles would have the
			// same ParikhVector
			if (pre.isEmpty() && post.isEmpty()) {
				if (dead == null) {
					dead = transition;
				} else {
					net.removeTransition(transition);
				}
				continue;
			}
			if (pre.isEmpty()) {
				emptyPre.add(transition);
			}
			if (post.isEmpty()) {
				emptyPost.add(transition);
			}
		}
		if (emptyPost.size() <= emptyPre.size()) {
			// Merge all with an empty preset (that means not bounded) and with an empty postset (that means
			// not reversible)
			for (Transition transition : emptyPost) {
				if (emptyPre.iterator().hasNext()) {
					Transition t = emptyPre.iterator().next();
					emptyPre.remove(t);
					Set<Node> set = t.getPostsetNodes();
					for (Node node : set) {
						net.createFlow(transition, node);
					}
					net.removeNode(t);
				}
			}
			// Fix the rest
			for (Transition transition : emptyPre) {
				Iterator<Transition> it = net.getTransitions().iterator();
				Transition t = null;
				while (it.hasNext() && ((t = it.next()) == dead || emptyPre.contains(t))) {
					if (!it.hasNext()) {
						it = net.getTransitions().iterator();
					}
				}
				if (t == null) {
					// couldn't appear, but if, then there wouldn't be a problem
					// since this is only an optimization
					continue;
				}
				Set<Node> set = transition.getPostsetNodes();
				for (Node node : set) {
					net.createFlow(t, node);
				}
				net.removeNode(transition);
			}
		} else {
			// Merge all with an empty preset (that means not bounded) and with an empty postset (that means
			// not reversible)
			for (Transition transition : emptyPre) {
				if (emptyPost.iterator().hasNext()) {
					Transition t = emptyPost.iterator().next();
					emptyPost.remove(t);
					Set<Node> set = t.getPresetNodes();
					for (Node node : set) {
						net.createFlow(node, transition);
					}
					net.removeNode(t);
				}
			}
			// Fix the rest
			for (Transition transition : emptyPost) {
				Iterator<Transition> it = net.getTransitions().iterator();
				Transition t = null;
				while (it.hasNext() && ((t = it.next()) == dead || emptyPost.contains(t))) {
					if (!it.hasNext()) {
						it = net.getTransitions().iterator();
					}
				}
				if (t == null) {
					// couldn't appear, but if, then there wouldn't be a problem
					// since this is only an optimization
					continue;
				}
				Set<Node> set = transition.getPresetNodes();
				for (Node node : set) {
					net.createFlow(node, t);
				}
				net.removeNode(transition);
			}
		}
		return net;
	}

	/**
	 * Creates a t-system with maximal g places and maximal k token on every place.
	 * @param g the maximal number of places.
	 * @param k the maximal number of token on a place.
	 * @return a random created t-system.
	 */
	public static PetriNet createRandomTSystem(int g, int k) {
		PetriNet net = createRandomTNet(g);
		if (k == 0) {
			return net;
		}
		Random r = new Random();
		for (Place place : net.getPlaces()) {
			int tokenCount = r.nextInt(k);
			place.setInitialToken(tokenCount);
		}
		return net;
	}

	/**
	 * Creates a t-system with maximal g places and maximal k token on every place. with the reservoir sampling
	 * algorithm. Attention it's only suitable for small number of places and token.
	 * @param g the maximal number of places.
	 * @param k the maximal number of token
	 * @return a random created t-system.
	 */
	public static PetriNet reservoirSampling(int g, int k) {
		// Reservoir sampling for parameter 1
		TNetGenerator gen = new TNetGenerator(g, false);
		Random r = new Random();
		if (!gen.iterator().hasNext()) {
			return null;
		}
		int n = 0;
		PetriNet tnet = gen.iterator().next();
		for (PetriNet net : gen) {
			++n;
			int pos = r.nextInt(n);
			if (pos == 0) {
				tnet = net;
			}
		}
		// Markings
		MarkingNetGenerator generator = new MarkingNetGenerator(tnet, k);
		PetriNet tSystem;
		if (!generator.iterator().hasNext()) {
			tSystem = tnet;
		} else {
			tSystem = generator.iterator().next();
		}
		n = 0;
		for (PetriNet net : generator) {
			++n;
			int pos = r.nextInt(n);
			if (pos == 0) {
				tSystem = net;
			}
		}
		return tSystem;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
