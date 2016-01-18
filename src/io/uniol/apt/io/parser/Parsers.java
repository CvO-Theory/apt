/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
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

package uniol.apt.io.parser;

import java.util.Set;

/**
 * Interface which provides the correct parser for a given format.
 *
 * @author vsp
 *
 * @param <T> The class which the parsers generate.
 */
public interface Parsers<T> {
	/**
	 * Get a parser for a specific format
	 * @param format The name of the format, which the parser should parse
	 * @return A parser which can parse the format
	 * @throws ParserNotFoundException If no parser for the given format is known.
	 */
	public Parser<T> getParser(String format) throws ParserNotFoundException;

	/**
	 * Get all format names for which parsers are known
	 * @return Set of the names of all supported formats
	 */
	public Set<String> getSupportedFormats();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
