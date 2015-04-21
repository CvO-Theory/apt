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

package uniol.apt.ui.impl.parameter;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.io.parser.impl.apt.AptRegexFormatParser;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * @author Uli Schlachter
 */
public class FiniteAutomatonParameterTransformation implements ParameterTransformation<FiniteAutomaton> {

	@Override
	public FiniteAutomaton transform(String regularExpression) throws ModuleException {
		try {
			return AptRegexFormatParser.parseString(regularExpression);
		} catch (FormatException e) {
			throw new ModuleException(e.getMessage(), e);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
