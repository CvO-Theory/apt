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
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;

/**
 * @author Vincent Göbel, Thomas Strathmann
 *
 */
public class SynetPNRenderer extends AbstractRenderer<PetriNet> implements Renderer<PetriNet> {
	@Override
	public void render(PetriNet pn, Writer writer) throws RenderException, IOException {
		for (Transition t : pn.getTransitions()) {
			writer.append(String.format("transition %s\n", t.getId()));
		}

		List<Place> places = new ArrayList<>();
		for (Place p : pn.getPlaces()) {
			writer.append(String.format("place %s", p.getId()));
			long initialMarking = pn.getInitialMarking().getToken(p).getValue();
			if (initialMarking > 0) {
				writer.append(String.format(" := %d", initialMarking));
			}
			writer.append("\n");
			places.add(p);
		}

		for (Place p : places) {
			Set<Flow> postset = p.getPostsetEdges();
			Set<Flow> preset = p.getPresetEdges();
			for (Flow a : postset) {
				writer.append(String.format("flow %s --", p.getId()));
				if (a.getWeight() != 1) {
					writer.append(String.valueOf(a.getWeight()));
				}
				writer.append(String.format("-> %s\n", a.getTarget().getId()));
			}
			for (Flow a : preset) {
				writer.append(String.format("flow %s <-", p.getId()));
				if (a.getWeight() != 1) {
					writer.append(String.valueOf(a.getWeight()));
				}
				writer.append(String.format("-- %s\n", a.getSource().getId()));
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
