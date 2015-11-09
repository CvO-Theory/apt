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

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.Renderer;

/**
 * Renderer for the pnml format. It is optimized for the format PIPE http://pipe2.sourceforge.net/ likes to read.
 * <p/>
 * @author Manuel Gieseking
 */
@AptRenderer
public class PnmlPNRenderer extends AbstractRenderer<PetriNet> implements Renderer<PetriNet> {
	@Override
	public void render(PetriNet pn, Writer writer) throws IOException {
		writer.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
		writer.append("<pnml>\n");
		writer.append("<net id=\"").append(pn.getName()).append("\" type=\"P/T net\">\n");

		// Just for PIPE
		writer.append("<token id=\"Default\" enabled=\"true\" red=\"0\" green=\"0\" blue=\"0\"/>");

		for (Place place : pn.getPlaces()) {
			writer.append("<place id=\"").append(place.getId()).append("\">\n");
			writer.append("<name>\n<value>").append(place.getId()).append("</value>\n</name>\n");
			writer.append("<initialMarking>\n").append("<value>").
				append(place.getInitialToken().toString()).append("</value>\n").
				append("</initialMarking>\n");
			writer.append("</place>\n");
		}

		for (Transition transition : pn.getTransitions()) {
			writer.append("<transition id=\"").append(transition.getId()).append("\">\n");
			writer.append("<name>\n<value>").append(transition.getLabel()).append("</value>\n</name>\n");
			writer.append("</transition>\n");
		}

		for (Flow flow : pn.getEdges()) {
			writer.append("<arc id=\"").append(flow.getSource().getId()).append(" to ").
				append(flow.getTarget().getId()).append("\" source=\"").
				append(flow.getSource().getId()).append("\" target=\"").
				append(flow.getTarget().getId()).append("\">\n");
			writer.append("<inscription>\n<value>").
				append(Integer.toString(flow.getWeight())).
				append("</value>\n</inscription>\n");
			writer.append("</arc>\n");
		}

		writer.append("</net>\n");
		writer.append("</pnml>");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
