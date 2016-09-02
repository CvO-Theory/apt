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

import java.util.Map;
import java.util.HashMap;
import static java.util.Collections.unmodifiableMap;

import uniol.apt.extension.ExtendMode;
import uniol.apt.ui.AptParameterTransformation;
import uniol.apt.ui.ParameterTransformation;

/**
 * @author Renke Grunwald
 *
 */
@AptParameterTransformation(ExtendMode.class)
public class ExtendModeParameterTransformation extends AbstractMapParameterTransformation<ExtendMode>
		implements ParameterTransformation<ExtendMode> {
	private static final Map<String, ExtendMode> VALUES;
	static {
		Map<String, ExtendMode> map = new HashMap<>();
		map.put("next", ExtendMode.Next);
		map.put("next_valid", ExtendMode.NextValid);
		map.put("next_minimal_valid", ExtendMode.NextMinimalValid);
		VALUES = unmodifiableMap(map);
	}

	public ExtendModeParameterTransformation() {
		super(VALUES);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
