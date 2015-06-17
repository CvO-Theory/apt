/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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

package uniol.apt.tasks.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.FileUtils;
import uniol.apt.io.parser.impl.apt.AptRegexFormatParser;
import uniol.apt.io.parser.impl.exception.FormatException;

/**
 * A tester for the AptRegexFormatParser.
 * @author Uli Schlachter
 */
public class APTRegexParserTester extends ParserTester {

	/** Construct a new tester */
	public APTRegexParserTester(File outputDir) throws FileNotFoundException, UnsupportedEncodingException {
		super(outputDir, "APTRegexParserTester");
	}

	@Override
	public void parse(File file) throws Exception, UnparsableException {
		try {
			AptRegexFormatParser.parseString(FileUtils.readFileToString(file));
		} catch (FormatException | IOException e) {
			throw new UnparsableException(e);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
