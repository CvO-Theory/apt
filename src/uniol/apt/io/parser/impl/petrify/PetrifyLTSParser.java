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

package uniol.apt.io.parser.impl.petrify;

import java.io.FileInputStream;
import java.io.IOException;

import java.io.InputStream;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.ANTLRParser;
import uniol.apt.io.parser.impl.apt.APTLTSParserOutput;
import uniol.apt.io.parser.impl.apt.APTParserContext;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Parses a file in petrify format to a transitionsystem.
 * <p/>
 * @author Manuel Gieseking
 */
public class PetrifyLTSParser {

	private PetrifyLTSParser() {
	}

	/**
	 * Returns the transitionsystem from a given file in petrify format.
	 * <p/>
	 * @param path - the path to the file in petrify format which should be parsed.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *                               all.
	 */
	public static TransitionSystem getLTS(String path) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException, FormatException {
		return getLTS(path, false);
	}

	/**
	 * Returns the transitionsystem from a given file in petrify format.
	 * <p/>
	 * @param data - the data in petrify format which should be parsed.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *                               all.
	 */
	public static TransitionSystem getLTS(InputStream data) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException, FormatException {
		return getLTS(data, false);
	}

	/**
	 * Returns the transitionsystem from a given file in petrify format.
	 * <p/>
	 * @param path             - the path to the file in petrify format which should be parsed.
	 * @param suppressWarnings - flag to tell the lexer and parser to suppress the warnings.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *                               all.
	 */
	public static TransitionSystem getLTS(String path, boolean suppressWarnings) throws IOException,
		NodeNotExistException, TypeMismatchException, LexerParserException, StructureException, FormatException {
		return getLTS(new FileInputStream(path), suppressWarnings);
	}

	/**
	 * Returns the transitionsystem from a given file in petrify format.
	 * <p/>
	 * @param data             - the data in petrify format which should be parsed.
	 * @param suppressWarnings - flag to tell the lexer and parser to suppress the warnings.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *                               all.
	 */
	public static TransitionSystem getLTS(InputStream data, boolean suppressWarnings) throws IOException,
		NodeNotExistException, TypeMismatchException, LexerParserException, StructureException, FormatException {
		APTParserContext<TransitionSystem> ctx = new APTParserContext<>(PetrifyLTSFormatLexer.class,
			PetrifyLTSFormatParser.class, APTLTSParserOutput.class, "start");
		((PetrifyLTSFormatLexer) ctx.getLexer()).suppressWarnings(suppressWarnings);
		((PetrifyLTSFormatParser) ctx.getParser()).suppressWarnings(suppressWarnings);
		return new ANTLRParser().parse(data, ctx);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
