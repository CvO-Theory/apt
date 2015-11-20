/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  vsp
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

package uniol.apt.io.parser.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;

/**
 * Abstract base class for parsers.
 * @param <G> Type of object that the parser produces.
 * @author vsp
 */
public abstract class AbstractParser<G> implements Parser<G> {
	@Override
	public G parseString(String input) throws ParseException {
		try (InputStream is = IOUtils.toInputStream(input)) {
			return parse(is);
		} catch (IOException e) {
			// This should never cause IOExceptions
			throw new RuntimeException(e);
		}
	}

	@Override
	public G parseFile(String filename) throws ParseException, IOException {
		return parseFile(new File(filename));
	}

	@Override
	public G parseFile(File file) throws ParseException, IOException {
		try (InputStream is = FileUtils.openInputStream(file)) {
			return parse(is);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
