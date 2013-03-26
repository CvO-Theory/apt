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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.adt.INode;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.module.exception.ModuleException;

/**
 * This class renders Petri nets in the file format used by LoLA.
 * @author Uli Schlachter, vsp
 */
public class LoLARenderer {

	/**
	 * Verify that the net can be expressed in LoLA file format.
	 * @param pn the net to verify
	 */
	private static void verifyNet(PetriNet pn) throws ModuleException {
		if (pn.getTransitions().isEmpty()) {
			throw new ModuleException("Cannot express Petri nets without transitions in the LoLA "
					+ " file format");
		}
		if (pn.getPlaces().isEmpty()) {
			throw new ModuleException("Cannot express Petri nets without places in the LoLA "
					+ " file format");
		}
		if (pn.getInitialMarkingCopy().hasOmega()) {
			throw new ModuleException("Cannot express an initial marking with at least one OMEGA"
					+ "token in the LoLA file format");
		}

		verifyNames(pn.getPlaces());
		verifyNames(pn.getTransitions());
	}

	/**
	 * Verify that a list of INodes have ids that are valid LoLA identifiers.
	 * @param list The list to verify
	 * @throws ModuleException when a id is not a valid identifier.
	 */
	private static void verifyNames(Iterable<? extends INode<?, ?, ?>> list) throws ModuleException {
		for (INode<?, ?, ?> node : list) {
			String name = node.getId();
			// The LoLA lexer uses the following regular expression for "name":
			// [^,;:()\t \n\{\}][^,;:()\t \n\{\}]*
			if (name.isEmpty()) {
				throw new ModuleException("Empty identifiers not allowed");
			}

			String forbidden = ",;:()\t \n{}";
			for (int i = 0; i < name.length(); i++) {
				char c[] = {name.charAt(i)};
				if (forbidden.contains(new String(c))) {
					throw new ModuleException("Invalid character in identifier '" + name + "'. "
							+ "The following characters are forbidden: ,;:()\\t \\n{}");
				}
			}
		}
	}

	/**
	 * Render the given Petri net into the LoLA file format.
	 * @param pn the Petri net that should be represented as a string.
	 * @return the string representation of the net.
	 * @throws ModuleException when the Petri net cannot be expressed in the LoLA file format, for example when
	 * invalid identifiers are used or when the net has no places or no transitions.
	 */
	public String render(PetriNet pn) throws ModuleException {
		verifyNet(pn);

		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/LoLA.stg");
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
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
