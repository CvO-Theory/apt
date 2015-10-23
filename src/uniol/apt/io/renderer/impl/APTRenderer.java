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
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.stringtemplate.v4.AutoIndentWriter;
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
		if (pn.getInitialMarking().hasOmega()) {
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
		StringWriter writer = new StringWriter();
		try {
			render(writer, pn);
		} catch (IOException e) {
			// A StringWriter shouldn't throw IOExceptions
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	/**
	 * Render the given Petri net into the APT file format.
	 * @param output the writer to send the result to
	 * @param pn the Petri net that should be represented as a string.
	 * @throws ModuleException when the Petri net cannot be expressed in the APT file format.
	 * @throws IOException when writing to the output produces an exception.
	 */
	public void render(Writer output, PetriNet pn) throws ModuleException, IOException {
		verifyNet(pn);

		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/APTPN.stg");
		ST pnTemplate = group.getInstanceOf("pn");
		pnTemplate.add("name", pn.getName());

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

		pnTemplate.write(new AutoIndentWriter(output));
	}

	/**
	 * Render the given Petri net into the APT file format.
	 * @param ts transition system
	 * @return the string representation of the net.
	 */
	public String render(TransitionSystem ts) {
		StringWriter writer = new StringWriter();
		try {
			render(writer, ts);
		} catch (IOException e) {
			// A StringWriter shouldn't throw IOExceptions
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	/**
	 * Render the given Petri net into the APT file format.
	 * @param output the writer to send the result to
	 * @param ts transition system
	 * @throws IOException when writing to the output produces an exception.
	 */
	public void render(Writer output, TransitionSystem ts) throws IOException {
		output.append(".name \"").append(ts.getName()).append("\"\n");
		output.append(".type LTS" + "\n");
		output.append("\n");

		output.append(".states" + "\n");
		for (State s : ts.getNodes()) {
			output.append(s.getId());
			if (s.equals(ts.getInitialState())) {
				output.append("[initial]");
			}

			/* If the "comment" extension is present, escape it properly and append it as a comment */
			try {
				Object comment = s.getExtension("comment");
				if (comment instanceof String) {
					String c = (String) comment;
					output.append(" /* ");
					output.append(c.replace("*/", "* /"));
					output.append(" */");
				}
			} catch (Exception e) {
			}
			output.append("\n");
		}
		output.append("\n");

		output.append(".labels" + "\n");
		Set<String> labels = new HashSet<>();
		for (Arc e : ts.getEdges()) {
			labels.add(e.getLabel());
		}
		for (String l : labels) {
			output.append(l).append("\n");
		}
		output.append("\n");

		output.append(".arcs");
		for (Arc e : ts.getEdges()) {
			output.append("\n");
			output.append(e.getSource().getId()).append(" ");
			output.append(e.getLabel()).append(" ");
			output.append(e.getTarget().getId());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
