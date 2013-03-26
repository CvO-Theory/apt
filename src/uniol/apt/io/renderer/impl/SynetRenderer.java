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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.PNTSRenderer;

/**
 * @author Vincent Göbel
 *
 */
public class SynetRenderer implements PNTSRenderer {

	@Override
	public String render(PetriNet pn) {
		StringBuilder output = new StringBuilder();

		for (Transition t : pn.getTransitions()) {
			output.append("transition ");
			output.append(t.getId());
			output.append("\n");
		}

		List<Place> places = new ArrayList<>();
		for (Place p : pn.getPlaces()) {
			output.append("place ");
			output.append(p.getId());
			int initialMarking = pn.getInitialMarkingCopy().getToken(p).getValue();
			if (initialMarking > 0) {
				output.append(" := ").append(initialMarking);
			}
			output.append("\n");
			places.add(p);
		}

		for (Place p : places) {
			Set<Flow> postset = p.getPostsetEdges();
			Set<Flow> preset = p.getPresetEdges();
			for (Flow a : postset) {
				output.append("flow ");
				output.append(p.getId()).append(" --");
				if (a.getWeight() != 1) {
					output.append(a.getWeight());
				}
				output.append("-> ").append(a.getTarget().getId());
				output.append("\n");
			}
			for (Flow a : preset) {
				output.append("flow ");
				output.append(p.getId()).append(" <-");
				if (a.getWeight() != 1) {
					output.append(a.getWeight());
				}
				output.append("-- ").append(a.getSource().getId());
				output.append("\n");
			}
		}


		return output.toString();
	}

	@Override
	public String render(TransitionSystem ts) {
		StringBuilder output = new StringBuilder();
		output.append("des(0,100,100)"); // Don't know what this does, "probably" not perfect this way.
		output.append("\n");
		int id = 0;
		HashMap<String, Integer> rename = new HashMap<>();
		for (Arc e : ts.getEdges()) {
			String label = e.getLabel();
			String source = e.getSource().getId();
			String target = e.getTarget().getId();
			if (!rename.containsKey(source)) {
				rename.put(source, id++);
			}
			if (!rename.containsKey(target)) {
				rename.put(target, id++);
			}
			output.append("(").append(rename.get(source)).append(",").append(label).append(",").append(rename.get(target)).append(")");
			output.append("\n");
		}

		return output.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
