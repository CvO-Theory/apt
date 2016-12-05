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
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;

/**
 * @author vsp
 *
 */
@AptRenderer
public class AptPNRenderer extends AbstractRenderer<PetriNet> implements Renderer<PetriNet> {
	public final static String FORMAT = "apt";

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("apn", "apt"));
	}

	// Verify that the net can be expressed in APT file format.
	private static void verifyNet(PetriNet pn) throws RenderException {
		if (pn.getInitialMarking().hasOmega()) {
			throw new RenderException("Cannot express an initial marking with at least one OMEGA"
					+ "token in the APT file format");
		}
	}

	@Override
	public void render(PetriNet pn, Writer writer) throws RenderException, IOException {
		verifyNet(pn);

		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/AptPN.stg");
		ST pnTemplate = group.getInstanceOf("pn");
		pnTemplate.add("name", pn.getName());
		if (pn.hasExtension("description")) {
			pnTemplate.add("description", pn.getExtension("description"));
		}

		// Handle extensions
		pnTemplate.add("extensions", pn.getWriteToFileExtensions());

		// Handle places
		pnTemplate.add("places", pn.getPlaces());

		// Handle the initial marking
		for (Place p : pn.getPlaces()) {
			Token val = pn.getInitialMarking().getToken(p);
			if (val.getValue() != 0) {
				pnTemplate.addAggr("marking.{place, weight}", p, val.getValue());
			}
		}

		// Handle transitions (and arcs)
		pnTemplate.add("transitions", pn.getTransitions());

		// Check if any flow has an extension that should be written out
		boolean haveFlowWithExtension = false;
		for (Flow flow : pn.getEdges()) {
			if (!flow.getWriteToFileExtensions().isEmpty()) {
				haveFlowWithExtension = true;
				break;
			}
		}
		pnTemplate.add("have_flow_with_extension", haveFlowWithExtension);

		pnTemplate.write(new AutoIndentWriter(writer), new ThrowingErrorListener());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
