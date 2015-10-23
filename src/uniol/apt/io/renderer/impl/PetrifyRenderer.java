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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.PNTSRenderer;
import uniol.apt.module.exception.ModuleException;

/**
 * Creates a string which returns a petri net or transitionsystem in the Petrify-format.
 * @author Sören Dierkes
 *
 */
public class PetrifyRenderer implements PNTSRenderer {

	@Override
	public String render(PetriNet pn) throws ModuleException {
		StringBuilder sb = new StringBuilder();

		// Petrify does not like:
		// - empty names (attempted fix below)
		// - names containing spaces (thus this code was disabled)
		/*
		sb.append(".model ").append(pn.getName());
		if (pn.getName().isEmpty())
			sb.append("model");
		sb.append("\n");
		*/

		sb.append(".inputs ");
		ArrayList<String> c = new ArrayList<>(0);
		for (Transition s : pn.getTransitions()) {
			if (!c.contains(s.getLabel())) {
				c.add(s.getLabel());
				sb.append(s.getLabel()).append(" ");
			}
		}
		sb.append("\n");

		sb.append(".graph");
		sb.append("\n");

		String marking = "";
		long mark = 0;
		Set<Place> places = pn.getPlaces();
		int placesCounter = 0;
		for (Place p : places) {
			mark = pn.getInitialMarking().getToken(p).getValue();
			if (mark > 1) {
				throw new ModuleException("Too many marks, Petrify is only able to read one bounded net");
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
						sb.append(t.getLabel()).append(" p").append(placesCounter);
						sb.append("\n");
					}
				}

				if (!preSet.isEmpty()) {
					for (Transition t : postSet) {
						sb.append("p").append(placesCounter).append(" ").append(t.getLabel());
						sb.append("\n");
					}
				}
			} else {
				if (!preSet.isEmpty() && !postSet.isEmpty()) {
					String pre = "";
					String post = "";
					Iterator<Transition> itr = preSet.iterator();
					pre = itr.next().getLabel();
					sb.append(pre).append(" ");

					itr = postSet.iterator();
					post = itr.next().getLabel();
					sb.append(post);
					sb.append("\n");

					if (mark == 1) {
						marking += " <" + pre + "," + post + ">";
					}
				} else if (preSet.isEmpty()) {
					if (!postSet.isEmpty()) {
						Iterator<Transition> itr = postSet.iterator();
						placesCounter++;
						sb.append("p").append(placesCounter).append(" ").append(itr.next().getLabel());
						sb.append("\n");
					} else {
						sb.append("p").append(++placesCounter);
						sb.append("\n");
					}

					if (mark == 1) {
						marking += " p" + placesCounter;
					}
				}
			}
		}

		sb.append(".marking {").append(marking.trim()).append("}");
		sb.append("\n");

		sb.append(".end");
		sb.append("\n");

		return sb.toString();
	}

	@Override
	public String render(TransitionSystem ts) {
		StringBuilder sb = new StringBuilder();

		// Petrify does not like:
		// - empty names (attempted fix below)
		// - names containing spaces (thus this code was disabled)
		/*
		sb.append(".model ").append(ts.getName());
		if (ts.getName().isEmpty())
			sb.append("model");
		sb.append("\n");
		*/

		sb.append(".inputs ");
		ArrayList<String> c = new ArrayList<>(0);
		for (Arc s : ts.getEdges()) {
			if (!c.contains(s.getLabel())) {
				c.add(s.getLabel());
				sb.append(s.getLabel()).append(" ");
			}
		}
		sb.append("\n");

		sb.append(".state graph");
		sb.append("\n");

		for (Arc e : ts.getEdges()) {
			String label = e.getLabel();
			String source = e.getSource().getId();
			String target = e.getTarget().getId();
			sb.append(source).append(" ").append(label).append(" ").append(target);
			sb.append("\n");
		}

		sb.append(".marking {").append(ts.getInitialState().getId()).append("}");
		sb.append("\n");

		sb.append(".end");
		sb.append("\n");
		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
