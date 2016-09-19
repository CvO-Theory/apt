/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis;

import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedList;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.coverability.CoverabilityGraphEdge;
import uniol.apt.analysis.coverability.CoverabilityGraphNode;
import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.PowerSet.powerSet;

/**
 * Generate a step net for of a Petri Net. A step net simulates the step semantics of the given Petri nets. In the step
 * semantics, markings can fire concurrently instead of sequentially. A set of transitions is enabled if there is enough
 * token so that the combination of the presets of the transitions can be satisfied by the available token.
 *
 * For simulating the step semantics, each step is materialized as a transition which combines flows of some other
 * transition.
 * @author Uli Schlachter
 */
public class GenerateStepNet {
	static final public String TRANSITIONS_KEY = "TransitionsOfStep";

	// templates for graphviz export
	static final public String TS_NODE_TEMPLATE = "%1$s[label=\"%2$s\"]; // node for marking %3$s%n";
	static final public String TS_INIT_TEMPLATE = "%1$s[label=\"%2$s\", shape=circle]; // node for marking %3$s%n";
	static final public String TS_EDGE_TEMPLATE = "%1$s -> %2$s[label=\"%3$s\"];%n";

	final private PetriNet pn;
	final private PetriNet stepNet;
	final private Collection<Marking> maximalReachableMarkings;
	final private Collection<Collection<Transition>> disabledSteps = new LinkedList<>();

	/**
	 * Create a new instance for the given Petri net
	 * @param pn The Petri net whose step net should be created and examined.
	 */
	public GenerateStepNet(PetriNet pn) {
		this.pn = pn;
		this.maximalReachableMarkings = getMaximalReachableMarkings(pn);
		this.stepNet = generateStepNet();
	}

	/**
	 * Get the calculated step net.
	 * @return the step net.
	 */
	public PetriNet getStepNet() {
		return this.stepNet;
	}

	/**
	 * Get the label of a step
	 * @param transitions The transitions that are part of this step
	 * @return A string describing this step uniquely.
	 */
	static public String getStepLabel(Collection<Transition> transitions) {
		StringBuilder result = new StringBuilder("{");
		boolean first = true;
		for (Transition t : transitions) {
			if (!first)
				result.append(",");
			first = false;
			result.append(t.getId());
		}
		result.append("}");
		return result.toString();
	}

	/**
	 * Test if a marking is smaller or equal to some other marking.
	 * @param mark1 The first marking to compare
	 * @param mark2 The second marking to compare
	 * @return true iff for every place p: mark1.getToken(p) &lt;= mark2.getToken(p)
	 */
	static public boolean isMarkingLessOrEqual(Marking mark1, Marking mark2) {
		for (Place p : mark1.getNet().getPlaces()) {
			int cmp = mark1.getToken(p).compareTo(mark2.getToken(p));
			if (cmp > 0)
				return false;
		}
		return true;
	}

	/**
	 * Get all pairwise uncomparable reachable markings of a Petri net.
	 * @param pn The Petri net whose markings should be computed
	 * @return A set of reachable markings. There are no two entries a and b in this set for which {@link
	 * isMarkingLessOrEqual} returns true.
	 */
	static public Collection<Marking> getMaximalReachableMarkings(PetriNet pn) {
		Collection<Marking> result = new HashSet<>();
		for (CoverabilityGraphNode node : CoverabilityGraph.get(pn).getNodes()) {
			Marking mark = node.getMarking();
			Iterator<Marking> iter = result.iterator();
			boolean skip = false;
			while (iter.hasNext()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				Marking mark2 = iter.next();
				if (isMarkingLessOrEqual(mark, mark2)) {
					skip = true;
					break;
				}
				if (isMarkingLessOrEqual(mark2, mark)) {
					iter.remove();
				}
			}
			if (!skip)
				result.add(mark);
		}
		return result;
	}

	/**
	 * Check if a step is reasonable which means that it can be enabled in some reachable marking.
	 * @param transitions The transitions that should be part of the step.
	 * @return true if this step is reasonable.
	 */
	public boolean isStepReasonable(Collection<Transition> transitions) {
		if (transitions.isEmpty())
			// Skip the empty step
			return false;

		Collection<Collection<Transition>> disabledStepsToRemove = new LinkedList<>();
		for (Collection<Transition> disabledStep : disabledSteps) {
			if (transitions.containsAll(disabledStep)) {
				// "disabledSteps" can not fire, because we have too few token. "transitions" contains
				// all these transitions plus some more and thus can also not fire.

				// Since disabledSteps should contain the smallest disabled steps, there should be no
				// entry which is larger than "transitions", because then it must also be larger than
				// "disabledStep".
				assert disabledStepsToRemove.isEmpty();
				return false;
			}
			if (disabledStep.containsAll(transitions)) {
				// The disabledStep is a superset of the step we are now looking at. If this step turns
				// out to be disabled, then it will be added to disabledSteps and replaces this entry.
				disabledStepsToRemove.add(disabledStep);
			}
		}

		// How many token does this step need to fire?
		Map<Place, Token> requiredToken = new HashMap<>();
		for (Place place : pn.getPlaces()) {
			int requiredOnPlace = 0;
			for (Flow flow : place.getPostsetEdges()) {
				if (transitions.contains(flow.getTarget()))
					requiredOnPlace += flow.getWeight();
			}
			requiredToken.put(place, Token.valueOf(requiredOnPlace));
		}

		// Is there any reachable marking which has enough token for this step to fire?
		for (Marking mark : maximalReachableMarkings) {
			boolean enabled = true;
			for (Map.Entry<Place, Token> entry : requiredToken.entrySet()) {
				if (mark.getToken(entry.getKey()).compareTo(entry.getValue()) < 0) {
					enabled = false;
					break;
				}
			}
			if (enabled)
				return true;
		}

		// Step is disabled, use this information for following rounds
		disabledSteps.add(transitions);
		disabledSteps.removeAll(disabledStepsToRemove);
		return false;
	}

	// Create and return the step net of our Petri net
	private PetriNet generateStepNet() {
		PetriNet result = new PetriNet("Step net of " + pn.getName());

		Map<Place, Place> placeMap = new HashMap<>();
		for (Place place : pn.getPlaces()) {
			placeMap.put(place, result.createPlace(place));
		}

		result.setInitialMarking(new Marking(result, pn.getInitialMarking()));

		// TODO Replace this implementation by an idea from vsp:
		// Start with small steps (only single transitions) and add new transitions to generate larger steps.
		// Every new step can be checked with checkSimplyLive(). The result would be equivalent to this
		// approach, but (likely) faster.
		for (Collection<Transition> transitions : powerSet(new ArrayList<>(pn.getTransitions()))) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			if (!isStepReasonable(transitions))
				continue;

			// Create the step
			Transition t = result.createTransition();
			t.putExtension(TRANSITIONS_KEY, getStepLabel(transitions));
			for (Place place : pn.getPlaces()) {
				int backwardWeight = 0;
				int forwardWeight = 0;
				for (Flow flow : place.getPostsetEdges()) {
					if (transitions.contains(flow.getTarget()))
						forwardWeight += flow.getWeight();
				}
				for (Flow flow : place.getPresetEdges()) {
					if (transitions.contains(flow.getSource()))
						backwardWeight += flow.getWeight();
				}
				result.createFlow(placeMap.get(place), t, forwardWeight);
				result.createFlow(t, placeMap.get(place), backwardWeight);
			}
		}

		return result;
	}

	/**
	 * Render the coverability graph of the generated step net in the DOT file format.
	 * @return string containing the coverability graph in the dot file format
	 */
	public String renderCoverabilityGraphAsDot() {
		CoverabilityGraph graph = CoverabilityGraph.get(getStepNet());

		StringBuilder sb = new StringBuilder();
		sb.append("digraph G {\n");
		sb.append("node [shape = point, color=white, fontcolor=white]; start;");
		sb.append("edge [fontsize=20]\n");
		sb.append("node [fontsize=20,shape=circle,color=black,fontcolor=black,"
				+ "height=0.5,width=0.5,fixedsize=true];\n");

		Formatter format = new Formatter(sb);

		Map<CoverabilityGraphNode, String> nodeLabels = new HashMap<>();
		nodeLabels.put(graph.getInitialNode(), "s0");
		format.format(TS_INIT_TEMPLATE, "s0", "s0", graph.getInitialNode().getMarking().toString());
		int nextState = 1;
		for (CoverabilityGraphNode node : graph.getNodes()) {
			if (!graph.getInitialNode().equals(node)) {
				String name = "s" + (nextState++);
				nodeLabels.put(node, name);
				format.format(TS_NODE_TEMPLATE, name, name, node.getMarking().toString());
			}
		}

		format.format(TS_EDGE_TEMPLATE, "start", "s0", "");

		for (CoverabilityGraphEdge edge : graph.getEdges()) {
			String source = nodeLabels.get(edge.getSource());
			String target = nodeLabels.get(edge.getTarget());
			String label = edge.getTransition().getExtension(TRANSITIONS_KEY).toString();
			format.format(TS_EDGE_TEMPLATE, source, target, label);
		}

		format.close();
		sb.append("}\n");

		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
