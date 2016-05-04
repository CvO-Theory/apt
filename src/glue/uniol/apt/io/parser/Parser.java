/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  vsp
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

package uniol.apt.io.parser;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * Interface for generic parsers.
 * @param <G> Type of object that the parser produces.
 * @author vsp, Uli Schlachter
 */
public interface Parser<G> {
	/**
	 * Which format does this parser understand?
	 * @return name of the supported format
	 */
	public String getFormat();

	/**
	 * Get a list of recommended file extensions.
	 * @return the list of recommended file extensions
	 */
	public List<String> getFileExtensions();

	/**
	 * Parse a string into an object.
	 *
	 * @param input The input string to read the object from.
	 * @return Object which got read from the input.
	 * @throws ParseException If the input can't get parsed.
	 */
	public G parseString(String input) throws ParseException;

	/**
	 * Parse an input stream into an object.
	 *
	 * @param input The input stream to read the object from.
	 * @return Object which got read from the input.
	 * @throws ParseException If the input can't get parsed.
	 * @throws IOException If readig the input fails.
	 */
	public G parse(InputStream input) throws ParseException, IOException;

	/**
	 * Parse an object from a file.
	 *
	 * @param filename The input filename to read the object from.
	 * @return Object which got read from the input.
	 * @throws ParseException If the input can't get parsed.
	 * @throws IOException If readig the input fails.
	 */
	public G parseFile(String filename) throws ParseException, IOException;

	/**
	 * Parse an object from a file.
	 *
	 * @param file The input file to read the object from.
	 * @return Object which got read from the input.
	 * @throws ParseException If the input can't get parsed.
	 * @throws IOException If readig the input fails.
	 */
	public G parseFile(File file) throws ParseException, IOException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
