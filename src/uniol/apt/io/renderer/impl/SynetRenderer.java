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
import java.util.TreeSet;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.PNTSRenderer;
import uniol.apt.util.StringComparator;

/**
 * @author Vincent Göbel, Thomas Strathmann
 *
 */
public class SynetRenderer implements PNTSRenderer {

	@Override
	public String render(PetriNet pn) {
		StringBuilder output = new StringBuilder();

		for (Transition t : pn.getTransitions()) {
			output.append(String.format("transition %s\n", t.getId()));
		}

		List<Place> places = new ArrayList<>();
		for (Place p : pn.getPlaces()) {
			output.append(String.format("place %s", p.getId()));
			long initialMarking = pn.getInitialMarking().getToken(p).getValue();
			if (initialMarking > 0) {
				output.append(String.format(" := %d", initialMarking));
			}
			output.append("\n");
			places.add(p);
		}

		for (Place p : places) {
			Set<Flow> postset = p.getPostsetEdges();
			Set<Flow> preset = p.getPresetEdges();
			for (Flow a : postset) {
				output.append(String.format("flow %s --", p.getId()));
				if (a.getWeight() != 1) {
					output.append(a.getWeight());
				}
				output.append(String.format("-> %s\n", a.getTarget().getId()));
			}
			for (Flow a : preset) {
				output.append(String.format("flow %s <-", p.getId()));
				if (a.getWeight() != 1) {
					output.append(a.getWeight());
				}
				output.append(String.format("-- %s\n", a.getSource().getId()));
			}
		}

		return output.toString();
	}

	@Override
	public String render(TransitionSystem ts) {
		StringBuilder output = new StringBuilder();
		StringBuilder head = new StringBuilder();
		output.append("\n");
		
		// build a map from APT state _names_ to Synet state _indices_
		HashMap<String, Integer> rename = new HashMap<>();
		
		// to ensure that the initial state is always mapped to 0 insert it first!
		rename.put(ts.getInitialState().getId(), 0);

		// add the other states (in ascending order)
		TreeSet<String> stateNames = new TreeSet<String>(new StringComparator());
		for (State s : ts.getNodes()) {
			if(s != ts.getInitialState())
				stateNames.add(s.getId());
		}
		int id = 1;
		for (String s : stateNames) {
			rename.put(s, id++);
		}

		// export edges
		for (Arc e : ts.getEdges()) {
			String label = e.getLabel();
			String source = e.getSource().getId();
			String target = e.getTarget().getId();
			output.append(String.format("(%s, %s, %s)\n",
				rename.get(source), label, rename.get(target)));
		}
		
		// write file header
		int initial = rename.get(ts.getInitialState().getId());
		head.append(String.format("des(%d, %d, %d)", initial,
			ts.getEdges().size(), ts.getNodes().size()));

		return head.append(output).toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
