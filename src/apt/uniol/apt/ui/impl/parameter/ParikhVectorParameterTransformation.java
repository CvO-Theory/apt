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

import java.util.HashMap;
import java.util.Map;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/** @author Renke Grunwald */
public class ParikhVectorParameterTransformation implements ParameterTransformation<ParikhVector> {

	@Override
	public ParikhVector transform(String arg) throws ModuleException {
		if (arg.length() < 2) {
			throw new ModuleException("Not a Parikh vector");
		}
		arg = arg.substring(1, arg.length() - 1);

		Map<String, Integer> pvMap = new HashMap<>();
		if (arg.length() == 0) {
			return new ParikhVector(pvMap);
		}

		String[] pairs = arg.split(",");

		for (String pair : pairs) {
			String[] keyValue = pair.split("=");

			String key = keyValue[0];
			String value = keyValue[1];

			String label = key.trim();
			int count = Integer.parseInt(value.trim());

			pvMap.put(label, count);
		}

		return new ParikhVector(pvMap);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
