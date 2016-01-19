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

package uniol.apt.ui.impl.returns;

import java.io.IOException;
import java.io.Writer;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Token;
import uniol.apt.ui.AptReturnValueTransformation;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * Converts a Marking-Object into a more readable format.
 *
 * @author Vincent Göbel
 */
@AptReturnValueTransformation(Marking.class)
public class MarkingReturnValueTransformation implements ReturnValueTransformation<Marking> {

	@Override
	public void transform(Writer output, Marking m) throws IOException {
		PetriNet pn = m.getNet();

		for (Place p : pn.getPlaces()) {
			Token v = m.getToken(p);

			if (v.getValue() > 0 || v.isOmega()) { // doppelt gemoppelt?
				output.append(p.getId()).append(":");
				if (v.isOmega()) {
					output.append("OMEGA");
				} else {
					output.append(Long.toString(v.getValue()));
				}
				output.append(" ");
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
