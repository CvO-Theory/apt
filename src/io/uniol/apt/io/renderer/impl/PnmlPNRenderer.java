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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.Renderer;

/**
 * Renderer for the ISO/IEC 15909 PNML format.
 *
 * @author Manuel Gieseking, Jonas Prellberg
 */
@AptRenderer
public class PnmlPNRenderer extends AbstractRenderer<PetriNet> implements Renderer<PetriNet> {

	private static class Renderer {
		private Map<String, String> uniqueIdMap;
		private int idCounter;

		public void render(PetriNet pn, Writer writer) throws IOException {
			uniqueIdMap = new HashMap<>();
			idCounter = 0;

			writer.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n");
			writer.append("<pnml xmlns=\"http://www.pnml.org/version-2009/grammar/pnml\">\n");
			writer.append("<net id=\"").append(toUniqueId(pn.getName()))
			      .append("\" type=\"http://www.pnml.org/version-2009/grammar/ptnet\">\n");
			writer.append("<page id=\"single-page\">");

			for (Place place : pn.getPlaces()) {
				writer.append("<place id=\"").append(place.getId()).append("\">\n");
				writer.append("<name>\n<text>").append(place.getId()).append("</text>\n</name>\n");
				writer.append("<initialMarking>\n")
				      .append("<text>")
				      .append(place.getInitialToken().toString())
				      .append("</text>\n")
				      .append("</initialMarking>\n");
				writer.append("</place>\n");
			}

			for (Transition transition : pn.getTransitions()) {
				writer.append("<transition id=\"").append(toUniqueId(transition.getId()))
					.append("\">\n");
				writer.append("<name>\n<text>").append(transition.getLabel())
					.append("</text>\n</name>\n");
				writer.append("</transition>\n");
			}

			for (Flow flow : pn.getEdges()) {
				writer.append("<arc id=\"")
				      .append(toUniqueId(flow.getSource().getId()))
				      .append("-")
				      .append(toUniqueId(flow.getTarget().getId()))
				      .append("\" source=\"")
				      .append(toUniqueId(flow.getSource().getId()))
				      .append("\" target=\"")
				      .append(toUniqueId(flow.getTarget().getId()))
				      .append("\">\n");
				writer.append("<inscription>\n<text>")
				      .append(Integer.toString(flow.getWeight()))
				      .append("</text>\n</inscription>\n");
				writer.append("</arc>\n");
			}

			writer.append("</page>\n");
			writer.append("</net>\n");
			writer.append("</pnml>");
		}

		private String toUniqueId(String id) {
			if (uniqueIdMap.containsKey(id)) {
				return uniqueIdMap.get(id);
			}

			String unique = id;
			while (uniqueIdMap.containsValue(unique)) {
				unique = id + "-" + idCounter;
				idCounter += 1;
			}
			uniqueIdMap.put(id, unique);
			return unique;
		}
	}

	public final static String FORMAT = "pnml";

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("pnml", "xml"));
	}

	@Override
	public void render(PetriNet pn, Writer writer) throws IOException {
		Renderer renderer = new Renderer();
		renderer.render(pn, writer);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
