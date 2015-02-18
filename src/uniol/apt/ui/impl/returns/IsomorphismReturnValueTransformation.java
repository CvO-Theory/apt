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

import java.util.Collection;

import org.apache.commons.collections4.MapIterator;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.isomorphism.Isomorphism;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * @author Uli Schlachter
 * @param <T> the type of the node that is transformed into a string
 */
public class IsomorphismReturnValueTransformation implements ReturnValueTransformation<Isomorphism> {
	@Override
	public String transform(Isomorphism isomorphism) {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;

		MapIterator<State, State> iterator = isomorphism.mapIterator();
		while (iterator.hasNext()) {
			State n = iterator.next();
			State m = iterator.getValue();
			if (!first)
				sb.append(", ");
			sb.append(n.getId());
			sb.append("=");
			sb.append(m.getId());
			first = false;
		}

		sb.append("]");
		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
