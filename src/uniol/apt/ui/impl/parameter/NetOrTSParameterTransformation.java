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

package uniol.apt.ui.impl.parameter;

import java.io.IOException;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.apt.APTParser;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * Use a filename to get either a Petri net or labeled transition system.
 *
 * @author Renke Grunwald
 *
 */
public class NetOrTSParameterTransformation implements ParameterTransformation<PetriNetOrTransitionSystem> {

	/**
	 * Symbol that signals that a pn or lts should be read from the standard input.
	 */
	public static final String STANDARD_INPUT_SYMBOL = "-";

	@Override
	public PetriNetOrTransitionSystem transform(String filename) throws ModuleException {
		APTParser parser = new APTParser();
		boolean fromStandardInput = false;

		try {
			if (filename.equals(NetOrTSParameterTransformation.STANDARD_INPUT_SYMBOL)) {
				fromStandardInput = true;
				parser.parse(System.in);
			} else {
				parser.parse(filename);
			}
		} catch (IOException e) {
			throw new ModuleException("Cannot parse file '" + filename + "': File does not exist");
		} catch (ParseException ex) {
			throw new ModuleException("Can't parse Petri net or transition system: " + ex.getMessage());
		}

		PetriNet pn = parser.getPn();
		TransitionSystem ts = parser.getTs();

		if (pn != null) {
			return new PetriNetOrTransitionSystem(pn);
		} else {
			return new PetriNetOrTransitionSystem(ts);
		}
	}
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
