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

import java.util.HashSet;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Vincent Göbel, vsp
 *
 */
public class APTRenderer {

	/**
	 * Verify that the net can be expressed in APT file format.
	 * @param pn the net to verify
	 */
	private static void verifyNet(PetriNet pn) throws ModuleException {
		if (pn.getInitialMarkingCopy().hasOmega()) {
			throw new ModuleException("Cannot express an initial marking with at least one OMEGA"
					+ "token in the APT file format");
		}
	}

	/**
	 * Render the given Petri net into the APT file format.
	 * @param pn the Petri net that should be represented as a string.
	 * @return the string representation of the net.
	 * @throws ModuleException when the Petri net cannot be expressed in the LoLA file format, for example when
	 * invalid identifiers are used or when the net has no places or no transitions.
	 */
	public String render(PetriNet pn) throws ModuleException {
		verifyNet(pn);

		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/APTPN.stg");
		ST pnTemplate = group.getInstanceOf("pn");
		pnTemplate.add("name", pn.getName());

		// Handle places
		pnTemplate.add("places", pn.getPlaces());

		// Handle the initial marking
		for (Place p : pn.getPlaces()) {
			Token val = pn.getInitialMarkingCopy().getToken(p);
			if (val.getValue() != 0) {
				pnTemplate.addAggr("marking.{place, weight}", p, val.getValue());
			}
		}

		// Handle transitions (and arcs)
		pnTemplate.add("transitions", pn.getTransitions());

		return pnTemplate.render();
	}

	/**
	 * Render the given Petri net into the APT file format.
	 * @param ts transition system
	 * @return the string representation of the net.
	 */
	public String render(TransitionSystem ts) {
		StringBuilder header = new StringBuilder();
		StringBuilder body = new StringBuilder();

		header.append("\n.name \"").append(ts.getName()).append("\"\n");
		header.append(".type LTS" + "\n");

		body.append(".states" + "\n");
		for (State s : ts.getNodes()) {
			body.append(s.getId());
			if (s.equals(ts.getInitialState())) {
				body.append("[initial]");
			}

			/* If the "comment" extension is present, escape it properly and append it as a comment */
			Object comment = s.getExtension("comment");
			if (comment instanceof String) {
				String c = (String) comment;
				body.append(" /* ");
				body.append(c.replace("*/", "* /"));
				body.append(" */");
			}
			body.append("\n");
		}
		body.append("\n");

		body.append(".labels" + "\n");
		Set<String> labels = new HashSet<>();
		for (Arc e : ts.getEdges()) {
			labels.add(e.getLabel());
		}
		for (String l : labels) {
			body.append(l).append("\n");
		}
		body.append("\n");

		body.append(".arcs" + "\n");
		for (Arc e : ts.getEdges()) {
			body.append(e.getSource().getId()).append(" ");
			body.append(e.getLabel()).append(" ");
			body.append(e.getTarget().getId());
			body.append("\n");
		}

		return (header.toString() + "\n" + body.toString());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
