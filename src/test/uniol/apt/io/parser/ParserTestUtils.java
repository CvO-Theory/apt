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

package uniol.apt.io.parser;

import java.io.IOException;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.AptPNParser;
import uniol.apt.io.parser.impl.AptLTSParser;

/**
 * Utility class to allow tests to use the parsers without the need to care
 * about parsing errors.
 *
 * @author vsp
 */
public class ParserTestUtils {
	/**
	 * Private constructor to prevent instantiation of this class
	 */
	private ParserTestUtils() { /* empty */ }

	/**
	 * Get a {@link PetriNet} from a file in apt format.
	 *
	 * @param fileName name of the file which contains the Petri net
	 * @throws ParserSkipException if any problem occurs
	 * @return The parsed Petri net.
	 */
	public static PetriNet getAptPN(String fileName) {
		try {
			return new AptPNParser().parseFile(fileName);
		} catch (IOException | ParseException ex) {
			throw new ParserSkipException(fileName, AptPNParser.class, ex);
		}
	}

	/**
	 * Get a {@link TransitionSystem} from a file in apt format.
	 *
	 * @param fileName name of the file which contains the labelled transition system
	 * @throws ParserSkipException if any problem occurs
	 * @return The parsed transition system.
	 */
	public static TransitionSystem getAptLTS(String fileName) {
		try {
			return new AptLTSParser().parseFile(fileName);
		} catch (IOException | ParseException ex) {
			throw new ParserSkipException(fileName, AptLTSParser.class, ex);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
