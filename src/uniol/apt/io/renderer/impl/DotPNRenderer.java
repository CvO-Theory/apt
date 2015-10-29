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

import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.PNRenderer;
import uniol.apt.io.renderer.RenderException;

/**
 * This class converts Petri nets and transition systems to dot file strings.
 *
 *
 * @author Renke Grunwald
 *
 */
public class DotPNRenderer extends AbstractPNRenderer implements PNRenderer {
	private final static String PN_PLACE_TEMPLATE =
		"%1$s[label=\"%2$s\"]\n%1$s_label[shape=plaintext,label=\"\"]\n%1$s -> %1$s_label[penwidth=0,"
		+ "label=\"%1$s\",arrowhead=none]\n";
	private final static String PN_TRANSITION_TEMPLATE =
		"%1$s[label=\"%2$s\"]\n%1$s_label[shape=plaintext,label=\"\"]\n%1$s -> %1$s_label[penwidth=0,"
		+ "label=\"%1$s\",arrowhead=none]\n";
	private final static String PN_FLOW_TEMPLATE = "%1$s -> %2$s[label=\"%3$s\"]\n";

	@Override
	public void render(PetriNet pn, Writer writer) throws RenderException, IOException {
		// TODO: Use external labels (from recent Graphviz versions)
		writer.append("digraph G {\n");
		writer.append("edge [fontsize=20]\n");

		//t1[label="t1"]
		//t1_label[shape=plaintext,label=""]
		//t1 -> t1_label[penwidth=0,label="t1",arrowhead=none]

		writer.append("node [fontsize=20,shape=rect,height=0.5,width=0.5,fixedsize=true];\n");
		Formatter transitionFormat = new Formatter(writer);

		for (Transition transition : pn.getTransitions()) {
			transitionFormat.format(PN_TRANSITION_TEMPLATE, transition.getId(), transition.getLabel());
		}

		transitionFormat.close();

		writer.append("node [fontsize=20,shape=circle,height=0.5,width=0.5,fixedsize=true];\n");
		Formatter placeFormat = new Formatter(writer);

		for (Place place : pn.getPlaces()) {
			Token tokens = pn.getInitialMarking().getToken(place);
			String tokensString = "";

			if (tokens.getValue() > 0) {
				tokensString = "" + tokens.getValue();
			}

			placeFormat.format(PN_PLACE_TEMPLATE, place.getId(), tokensString);
		}

		placeFormat.close();

		Formatter flowFormat = new Formatter(writer);

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
		writer.append("}\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
