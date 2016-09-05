/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C) 2015       vsp
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

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.AptLTSParser;
import uniol.apt.io.parser.impl.AptPNParser;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.AptParameterTransformation;
import uniol.apt.ui.ParameterTransformation;

/**
 * Use a filename to get either a Petri net or labeled transition system.
 *
 * @author vsp
 */
@AptParameterTransformation(value = PetriNetOrTransitionSystem.class, fileSource = true)
public class NetOrTSParameterTransformation implements ParameterTransformation<PetriNetOrTransitionSystem> {

	@Override
	public PetriNetOrTransitionSystem transform(String input) throws ModuleException {
		PetriNet pn = null;
		ParseException pnEx = null;
		TransitionSystem ts = null;
		ParseException tsEx = null;

		try {
			pn = new AptPNParser().parseString(input);
		} catch (ParseException ex) {
			pnEx = ex;
		}
		try {
			ts = new AptLTSParser().parseString(input);
		} catch (ParseException ex) {
			tsEx = ex;
		}

		if (pnEx == null && tsEx == null) {
			throw new ModuleException("Parsers can't decide if the input is a Petri net or a transition "
					+ "system.");
		} else if (pnEx != null && tsEx != null) {
			throw new ModuleException(
					String.format("Input is neither a Petri net nor a transition system.%n"
						+ "Petri net parser error: %s%nTransition system parser error: %s",
						pnEx.getMessage(), tsEx.getMessage()));
		}

		if (pn != null) {
			return new PetriNetOrTransitionSystem(pn);
		} else {
			assert ts != null;
			return new PetriNetOrTransitionSystem(ts);
		}
	}

}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
