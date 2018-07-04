/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2018 Uli Schlachter
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

import org.apache.commons.io.IOUtils;

import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.ParserNotFoundException;
import uniol.apt.io.parser.Parsers;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.StreamWithOptionsParameterTransformation;

/**
 * Transformation allowing to choose some parser to do the transformation.
 * @author Uli Schlachter
 */
public abstract class AbstractParsersParameterTransformation<G> extends StreamWithOptionsParameterTransformation<G> {
	private final Parsers<G> parsers;
	private final String defaultParser;
	private final String objectName;

	public AbstractParsersParameterTransformation(Parsers<G> parsers, String defaultParser, String objectName) {
		this.parsers = parsers;
		this.defaultParser = defaultParser;
		this.objectName = objectName;
	}

	@Override
	public G transform(InputStream input, String parser) throws ModuleException, IOException {
		if (parser.isEmpty())
			parser = defaultParser;
		try {
			return parsers.getParser(parser).parse(input);
		} catch (ParseException ex) {
			throw new ModuleException("Can't parse " + objectName + ": " + ex.getMessage(), ex);
		} catch (ParserNotFoundException ex) {
			StringBuilder message = new StringBuilder();
			message.append(ex.getMessage());
			message.append("\nSupported file formats:");
			for (String name : parsers.getSupportedFormats()) {
				message.append(' ').append(name);
			}
			throw new ParserNotFoundException(message.toString(), ex);
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
