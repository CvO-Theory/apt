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
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;
import uniol.apt.io.parser.impl.synet.SynetLTSParser;
import uniol.apt.io.parser.impl.synet.SynetPNParser;
import uniol.apt.io.renderer.impl.APTRenderer;
import uniol.apt.module.exception.ModuleException;

/**
 *
 * Converts a file from synet format into the apt format.
 *
 * @author Manuel Gieseking
 */
public class Synet2Apt {

	private Synet2Apt() {
	}

	/**
	 *
	 * Converts a file in synet format given by the filename into the apt format returning it as string. Decides
	 * wheater the given file is a petrinet or a transitionsystem by examine the extention of the filename.
	 * .aut leads to lts and .net leads to pn.
	 *
	 * @param filename - the name of the file in synet format which should be parsed.
	 * @return a string with the graph in apt format.
	 * @throws IOException thrown if the file do not end with .net or .aut or is not readable.
	 * @throws ModuleException thrown if the net has a omega marking.
	 * @throws NodeNotExistException thrown if it is referenced to a node, which is not defined in the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not fit.
	 * @throws LexerParserException thrown if the file is not parseable.
	 * @throws StructureException thrown if an error by converting the file to the datastructure occure.
	 */
	public static String convert(String filename) throws IOException, ModuleException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException {
		if (filename.endsWith(".aut")) {
			return convert(filename, Type.LTS);
		} else if (filename.endsWith(".net")) {
			return convert(filename, Type.PN);
		} else {
			throw new IOException("File extention must be .aut or .net.");
		}
	}

	/**
	 * Converts a file in synet format given by the filename @filename into the apt format returning it as string.
	 * Decides which parser to use by the type @type.
	 *
	 * @param filename - the name of the file in synet format which should be parsed.
	 * @param type - the type of the graph.
	 * @return a string with the graph in apt format.
	 * @throws IOException thrown if the file do not end with .net or .aut or is not readable.
	 * @throws ModuleException thrown if the net has a omega marking.
	 * @throws NodeNotExistException thrown if it is referenced to a node, which is not defined in the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not fit.
	 * @throws LexerParserException thrown if the file is not parseable.
	 * @throws StructureException thrown if an error by converting the file to the datastructure occure.
	 */
	public static String convert(String filename, Type type) throws IOException, ModuleException,
		NodeNotExistException, TypeMismatchException, LexerParserException, StructureException {
		APTRenderer renderer = new APTRenderer();
		switch (type) {
			case LTS:
				return renderer.render(SynetLTSParser.getLTS(filename));
			case LPN:
				return renderer.render(SynetPNParser.getPetriNet(filename));
			case PN:
				return renderer.render(SynetPNParser.getPetriNet(filename));
			default:
				throw new TypeMismatchException(Type.LTS.name() + ", " + Type.PN.name()
					+ " or " + Type.LPN.name(), type.name());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
