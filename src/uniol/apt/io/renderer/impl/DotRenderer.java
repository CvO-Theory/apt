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

package uniol.apt.io.renderer.impl;

import java.util.Formatter;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * This class converts Petri nets and transition systems to dot file strings.
 *
 *
 * @author Renke Grunwald
 *
 */
public class DotRenderer {

	private static String PN_PLACE_TEMPLATE =
		"%1$s[label=\"%2$s\"]\n%1$s_label[shape=plaintext,label=\"\"]\n%1$s -> %1$s_label[penwidth=0,"
		+ "label=\"%1$s\",arrowhead=none]\n";
	private static String PN_TRANSITION_TEMPLATE =
		"%1$s[label=\"%2$s\"]\n%1$s_label[shape=plaintext,label=\"\"]\n%1$s -> %1$s_label[penwidth=0,"
		+ "label=\"%1$s\",arrowhead=none]\n";
	private static String PN_FLOW_TEMPLATE = "%1$s -> %2$s[label=\"%3$s\"]\n";
	private static final String TS_NODE_TEMPLATE =
		"%1$s[label=\"%2$s\"];\n";
	private static final String TS_INITIAL_NODE_TEMPLATE =
		"%1$s[label=\"%2$s\", shape=circle];\n";
	private static final String TS_EDGE_TEMPLATE =
		"%1$s -> %2$s[label=\"%3$s\"];\n";

	/**
	 * Converts Petri net to dot file strings.
	 * @param pn Petri net
	 * @return String
	 */
	public String render(PetriNet pn) {
		// TODO: Use external labels (from recent Graphviz versions)
		StringBuilder sb = new StringBuilder();

		sb.append("digraph G {\n");
		sb.append("edge [fontsize=20]\n");

		//t1[label="t1"]
		//t1_label[shape=plaintext,label=""]
		//t1 -> t1_label[penwidth=0,label="t1",arrowhead=none]

		sb.append("node [fontsize=20,shape=rect,height=0.5,width=0.5,fixedsize=true];\n");
		Formatter transitionFormat = new Formatter(sb);

		for (Transition transition : pn.getTransitions()) {
			transitionFormat.format(PN_TRANSITION_TEMPLATE, transition.getId(), transition.getLabel());
		}

		transitionFormat.close();

		sb.append("node [fontsize=20,shape=circle,height=0.5,width=0.5,fixedsize=true];\n");
		Formatter placeFormat = new Formatter(sb);

		for (Place place : pn.getPlaces()) {
			Token tokens = pn.getInitialMarking().getToken(place);
			String tokensString = "";

			if (tokens.getValue() > 0) {
				tokensString = "" + tokens.getValue();
			}

			placeFormat.format(PN_PLACE_TEMPLATE, place.getId(), tokensString);
		}

		placeFormat.close();

		Formatter flowFormat = new Formatter(sb);

		for (Flow arc : pn.getEdges()) {
			if (arc.getWeight() >= 1) {
				String weightString = " ";

				if (arc.getWeight() > 1) {
					weightString = "" + arc.getWeight();
				}

				flowFormat.format(PN_FLOW_TEMPLATE, arc.getSource().getId(), arc.getTarget().getId(),
						"" + weightString);
			}
		}

		flowFormat.close();
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Converts transition net to dot file strings.
	 * @param ts transition system
	 * @return String
	 */
	public String render(TransitionSystem ts) {
		StringBuilder sb = new StringBuilder();

		sb.append("digraph G {\n");
		sb.append("node [shape = point, color=white, fontcolor=white]; start;");
		sb.append("edge [fontsize=20]\n");
		sb.append("node [fontsize=20,shape=circle,color=black, fontcolor=black, height=0.5,width=0.5,fixedsize=true];\n");

		Formatter nodeFormat = new Formatter(sb);

		for (State node : ts.getNodes()) {
			if (ts.getInitialState().equals(node)) {
				nodeFormat.format(TS_INITIAL_NODE_TEMPLATE, node.getId(), node.getId());
			} else {
				nodeFormat.format(TS_NODE_TEMPLATE, node.getId(), node.getId());
			}
		}

		nodeFormat.close();

		Formatter edgeFormat = new Formatter(sb);

		edgeFormat.format(TS_EDGE_TEMPLATE, "start", ts.getInitialState().getId(), "");

		for (Arc edge : ts.getEdges()) {
			edgeFormat.format(TS_EDGE_TEMPLATE, edge.getSource().getId(), edge.getTarget().getId(),
					edge.getLabel());
		}

		edgeFormat.close();
		sb.append("}\n");

		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
