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
import java.io.InputStream;

import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.StreamParameterTransformation;

/**
 * Abstract transformation that uses a parser to interpret a given string.
 *
 * @author Renke Grunwald
 * @author Jonas Prellberg
 * @param <G>
 *                transformation target type
 */
public abstract class AbstractParserParameterTransformation<G> extends StreamParameterTransformation<G> {

	private final Parser<G> parser;
	private final String objectName;

	public AbstractParserParameterTransformation(Parser<G> parser, String objectName) {
		this.parser = parser;
		this.objectName = objectName;
	}

	@Override
	public G transform(InputStream input) throws ModuleException, IOException {
		try {
			return parser.parse(input);
		} catch (ParseException ex) {
			throw new ModuleException("Can't parse " + objectName + ": " + ex.getMessage(), ex);
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
