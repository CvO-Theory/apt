/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  vsp
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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.exception.ModuleException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class renders Petri nets in the file format used by BAGGINS.
 * @author vsp
 */
public class BagginsRenderer {
	static private class IdWeightPair {
		public int id;
		public int weight;

		private IdWeightPair(int id, int weight) {
			this.id     = id;
			this.weight = weight;
		}
	}

	/**
	 * Verify that the net can be expressed in BAGGINS file format.
	 * @param pn the net to verify
	 */
	private static void verifyNet(PetriNet pn) throws ModuleException {
		if (pn.getTransitions().isEmpty()) {
			// untested, maybe BAGGINS supports this unimportant feature ...
			throw new ModuleException("Cannot express Petri nets without transitions in the BAGGINS "
					+ " file format");
		}
		if (pn.getPlaces().isEmpty()) {
			// untested, maybe BAGGINS supports this unimportant feature ...
			throw new ModuleException("Cannot express Petri nets without places in the BAGGINS "
					+ " file format");
		}
		if (pn.getInitialMarking().hasOmega()) {
			throw new ModuleException("Cannot express an initial marking with at least one OMEGA "
					+ "token in the BAGGINS file format");
		}
	}

	/**
	 * Render the given Petri net into the BAGGINS file format.
	 * @param pn the Petri net that should be represented as a string.
	 * @return the string representation of the net.
	 * @throws ModuleException when the Petri net cannot be expressed in the BAGGINS file format, for example when
	 * the net has no places or no transitions.
	 */
	public String render(PetriNet pn) throws ModuleException {
		verifyNet(pn);

		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/Baggins.stg", '$', '$');
		ST pnTemplate = group.getInstanceOf("pn");
		pnTemplate.add("name", pn.getName());

		// Handle places
		pnTemplate.add("places", pn.getPlaces());

		List<Place> placeList = new ArrayList<>(pn.getPlaces());

		// Handle transitions (and arcs)
		for (Transition t : pn.getTransitions()) {
			List<IdWeightPair> preset  = new ArrayList<>();
			List<IdWeightPair> postset = new ArrayList<>();
			for (Flow e : t.getPresetEdges()) {
				preset.add(new IdWeightPair(placeList.indexOf(e.getSource()), -e.getWeight()));
			}
			for (Flow e : t.getPostsetEdges()) {
				postset.add(new IdWeightPair(placeList.indexOf(e.getTarget()), e.getWeight()));
			}

			pnTemplate.addAggr("transitions.{transition, preset, postset}", t, preset, postset);
		}

		return pnTemplate.render();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
