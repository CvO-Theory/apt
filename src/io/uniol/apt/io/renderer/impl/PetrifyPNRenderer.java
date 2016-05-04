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

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;

/**
 * Creates a string which returns a Petri net or transition system in the Petrify-format.
 * @author Uli Schlachter
 *
 */
@AptRenderer
public class PetrifyPNRenderer extends GenetPNRenderer implements Renderer<PetriNet> {
	@Override
	public String getFormat() {
		return "petrify";
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("g"));
	}

	// Verify that the net can be expressed in petrify file format.
	@Override
	protected void verifyNet(PetriNet pn) throws RenderException {
		super.verifyNet(pn);
		for (Flow f : pn.getEdges())
			if (f.getWeight() != 1)
				throw new RenderException("Only flow weight 1 can be expressed"
						+ " in the Petrify file format");
		for (Place p : pn.getPlaces())
			if (pn.getInitialMarking().getToken(p).getValue() > 1)
				throw new RenderException("Cannot express token counts above 1 in"
						+ " the initial marking in the Petrify file format");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
