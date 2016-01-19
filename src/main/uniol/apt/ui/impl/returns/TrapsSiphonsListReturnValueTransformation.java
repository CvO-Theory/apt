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
import java.util.Set;

import uniol.apt.adt.pn.Place;
import uniol.apt.analysis.trapsAndSiphons.TrapsSiphonsList;
import uniol.apt.ui.AptReturnValueTransformation;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * Transform a list of traps/siphons into a string.
 * @author Uli Schlachter
 */
@AptReturnValueTransformation(TrapsSiphonsList.class)
public class TrapsSiphonsListReturnValueTransformation implements ReturnValueTransformation<TrapsSiphonsList> {
	@Override
	public void transform(Writer output, TrapsSiphonsList tsList) throws IOException {
		boolean firstList = true;

		output.append("{");
		for (Set<Place> places : tsList) {
			if (!firstList)
				output.append(", ");
			firstList = false;

			output.append("{");

			boolean firstPlace = true;
			for (Place place : places) {
				if (!firstPlace)
					output.append(", ");
				firstPlace = false;
				output.append(place.getId());
			}

			output.append("}");
		}
		output.append("}");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
