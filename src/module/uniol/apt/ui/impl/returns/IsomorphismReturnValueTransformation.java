/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

import org.apache.commons.collections4.MapIterator;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.isomorphism.Isomorphism;
import uniol.apt.ui.AptReturnValueTransformation;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * @author Uli Schlachter
 */
@AptReturnValueTransformation(Isomorphism.class)
public class IsomorphismReturnValueTransformation implements ReturnValueTransformation<Isomorphism> {
	@Override
	public void transform(Writer output, Isomorphism isomorphism) throws IOException {
		boolean first = true;

		output.append("[");
		MapIterator<State, State> iterator = isomorphism.mapIterator();
		while (iterator.hasNext()) {
			State n = iterator.next();
			State m = iterator.getValue();
			if (!first)
				output.append(", ");
			output.append(n.getId());
			output.append("=");
			output.append(m.getId());
			first = false;
		}

		output.append("]");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
