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

import java.util.Set;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Renderer for the pnml format. It is optimized for the format PIPE http://pipe2.sourceforge.net/ likes to read.
 * <p/>
 * @author Manuel Gieseking
 */
public class PnmlPNRenderer {

	/**
	 * Render the given Petri net into the PNML file format.
	 * <p/>
	 * @param pn the Petri net that should be represented as a string.
	 * <p/>
	 * @return the string representation of the net.
	 */
	public String render(PetriNet pn) {
		StringBuilder b = new StringBuilder("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
			+ "<pnml>\n"
			+ "<net id=\"" + pn.getName() + "\" type=\"P/T net\">\n");

		// Just for PIPE
		b.append("<token id=\"Default\" enabled=\"true\" red=\"0\" green=\"0\" blue=\"0\"/>");

		Set<Place> places = pn.getPlaces();
		for (Place place : places) {
			b.append("<place id=\"").append(place.getId()).append("\">\n");
			b.append("<name>\n<value>").append(place.getId()).append("</value>\n</name>\n");
			b.append("<initialMarking>\n").append("<value>").
				append(place.getInitialToken()).append("</value>\n").
				append("</initialMarking>\n");
			b.append("</place>\n");
		}

		Set<Transition> transitions = pn.getTransitions();
		for (Transition transition : transitions) {
			b.append("<transition id=\"").append(transition.getId()).append("\">\n");
			b.append("<name>\n<value>").append(transition.getLabel()).append("</value>\n</name>\n");
			b.append("</transition>\n");
		}

		Set<Flow> flows = pn.getEdges();
		for (Flow flow : flows) {
			b.append("<arc id=\"").append(flow.getSource().getId()).append(" to ").
				append(flow.getTarget().getId()).append("\" source=\"").
				append(flow.getSource().getId()).append("\" target=\"").
				append(flow.getTarget().getId()).append("\">\n");
			b.append("<inscription>\n<value>").
				append(flow.getWeight()).
				append("</value>\n</inscription>\n");
			b.append("</arc>\n");
		}

		b.append("</net>\n");
		b.append("</pnml>");
		return b.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
