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

package uniol.apt.io.converter;

import java.io.IOException;

import uniol.apt.io.parser.IParserOutput.Type;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.SynetLTSParser;
import uniol.apt.io.parser.impl.SynetPNParser;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;
import uniol.apt.io.renderer.impl.AptLTSRenderer;
import uniol.apt.io.renderer.impl.AptPNRenderer;
import uniol.apt.module.exception.ModuleException;

/**
 *
 * Converts a file from synet format into the apt format.
 * <p/>
 * @author Manuel Gieseking
 */
public class Synet2Apt {

	private Synet2Apt() {
	}

	/**
	 *
	 * Converts a file in synet format given by the filename into the apt format returning it as string. Decides
	 * wheater the given file is a petrinet or a transitionsystem by examine the extention of the filename. .aut
	 * leads to lts and .net leads to pn.
	 * <p/>
	 * @param filename - the name of the file in synet format which should be parsed.
	 * <p/>
	 * @return a string with the graph in apt format.
	 * <p/>
	 * @throws IOException           thrown if the file do not end with .net or .aut or is not readable.
	 * @throws ModuleException       thrown if the net has a omega marking.
	 * @throws TypeMismatchException thrown if the type of the graph do not fit.
	 * @throws ParseException        thrown if the file can't get parsed
	 */
	public static String convert(String filename) throws IOException, ModuleException, TypeMismatchException,
			ParseException {
		if (filename.endsWith(".aut")) {
			return convert(filename, Type.LTS);
		} else if (filename.endsWith(".net")) {
			return convert(filename, Type.PN);
		} else {
			throw new IOException("File extention must be .aut or .net.");
		}
	}

	/**
	 * Converts a file in synet format given by the filename
	 * <p/>
	 * @filename into the apt format returning it as string. Decides which parser to use by the type
	 * @type.
	 * <p/>
	 * @param filename - the name of the file in synet format which should be parsed.
	 * @param type     - the type of the graph.
	 * <p/>
	 * @return a string with the graph in apt format.
	 * <p/>
	 * @throws IOException           thrown if the file do not end with .net or .aut or is not readable.
	 * @throws ModuleException       thrown if the net has a omega marking.
	 * @throws TypeMismatchException thrown if the type of the graph do not fit.
	 * @throws ParseException        thrown if the file can't get parsed
	 */
	public static String convert(String filename, Type type) throws IOException, ModuleException,
			TypeMismatchException, ParseException {
		switch (type) {
			case LTS:
				return new AptLTSRenderer().render(new SynetLTSParser().parseLTSFile(filename));
			case LPN:
			case PN:
				return new AptPNRenderer().render(new SynetPNParser().parsePNFile(filename));
			default:
				throw new TypeMismatchException(Type.LTS.name() + ", " + Type.PN.name()
					+ " or " + Type.LPN.name(), type.name());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
