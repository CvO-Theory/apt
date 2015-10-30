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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.PNRenderer;
import uniol.apt.io.renderer.RenderException;

/**
 * Creates a string which returns a petri net or transitionsystem in the Petrify-format.
 * @author Sören Dierkes
 *
 */
public class PetrifyPNRenderer extends AbstractPNRenderer implements PNRenderer {
	@Override
	public void render(PetriNet pn, Writer writer) throws RenderException, IOException {
		// Petrify does not like:
		// - empty names (attempted fix below)
		// - names containing spaces (thus this code was disabled)
		/*
		sb.append(".model ").append(pn.getName());
		if (pn.getName().isEmpty())
			sb.append("model");
		sb.append("\n");
		*/

		writer.append(".inputs ");
		ArrayList<String> c = new ArrayList<>(0);
		for (Transition s : pn.getTransitions()) {
			if (!c.contains(s.getLabel())) {
				c.add(s.getLabel());
				writer.append(s.getLabel()).append(" ");
			}
		}
		writer.append("\n");

		writer.append(".graph");
		writer.append("\n");

		String marking = "";
		long mark = 0;
		Set<Place> places = pn.getPlaces();
		int placesCounter = 0;
		for (Place p : places) {
			mark = pn.getInitialMarking().getToken(p).getValue();
			if (mark > 1) {
				throw new RenderException("Too many marks, Petrify is only able to read one bounded net");
			}

			Set<Transition> preSet = p.getPreset();
			Set<Transition> postSet = p.getPostset();

			if (preSet.size() >= 1 || postSet.size() >= 1) {
				placesCounter++;
				if (mark == 1) {
					marking += " p" + placesCounter;
				}
				if (!preSet.isEmpty()) {
					for (Transition t : preSet) {
						writer.append(t.getLabel()).append(" p").append(String.valueOf(placesCounter));
						writer.append("\n");
					}
				}

				if (!preSet.isEmpty()) {
					for (Transition t : postSet) {
						writer.append("p").append(String.valueOf(placesCounter)).append(" ").append(t.getLabel());
						writer.append("\n");
					}
				}
			} else {
				if (!preSet.isEmpty() && !postSet.isEmpty()) {
					String pre = "";
					String post = "";
					Iterator<Transition> itr = preSet.iterator();
					pre = itr.next().getLabel();
					writer.append(pre).append(" ");

					itr = postSet.iterator();
					post = itr.next().getLabel();
					writer.append(post);
					writer.append("\n");

					if (mark == 1) {
						marking += " <" + pre + "," + post + ">";
					}
				} else if (preSet.isEmpty()) {
					if (!postSet.isEmpty()) {
						Iterator<Transition> itr = postSet.iterator();
						placesCounter++;
						writer.append("p").append(String.valueOf(placesCounter)).append(" ").append(itr.next().getLabel());
						writer.append("\n");
					} else {
						writer.append("p").append(String.valueOf(++placesCounter));
						writer.append("\n");
					}

					if (mark == 1) {
						marking += " p" + placesCounter;
					}
				}
			}
		}

		writer.append(".marking {").append(marking.trim()).append("}");
		writer.append("\n");

		writer.append(".end");
		writer.append("\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
