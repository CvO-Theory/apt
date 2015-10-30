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
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.PNParser;

/**
 * Abstract base class for Petri net parsers.
 *
 * @author vsp
 */
public abstract class AbstractPNParser implements PNParser {
	@Override
	public PetriNet parsePN(String input) throws ParseException {
		try {
			return parsePN(IOUtils.toInputStream(input));
		} catch (IOException e) {
			// This should never cause IOExceptions
			throw new RuntimeException(e);
		}
	}

	@Override
	public PetriNet parsePNFile(String filename) throws ParseException, IOException {
		return parsePNFile(new File(filename));
	}

	@Override
	public PetriNet parsePNFile(File file) throws ParseException, IOException {
		return parsePN(FileUtils.openInputStream(file));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
