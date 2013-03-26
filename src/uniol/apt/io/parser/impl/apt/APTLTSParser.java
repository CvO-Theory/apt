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

package uniol.apt.io.parser.impl.apt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.ANTLRParser;
import uniol.apt.io.parser.impl.ParserContext;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Parses a file in apt format to a transitionsystem.
 * <p/>
 * @author Manuel Gieseking
 */
public class APTLTSParser {

	private APTLTSParser() {
	}

	/**
	 * Returns the transitionsystem from a given file in apt format.
	 * <p/>
	 * @param data - the data in apt format which should be parsed.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public static TransitionSystem getLTS(InputStream data) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException {
		return getLTS(data, false);
	}

	/**
	 * Returns the transitionsystem from a given file in apt format.
	 * <p/>
	 * @param path - the path to the file in apt format which should be parsed.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public static TransitionSystem getLTS(String path) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException {
		return getLTS(path, false);
	}

	/**
	 * Returns the transitionsystem from a given file in apt format.
	 * <p/>
	 * @param data             - the data in apt format which should be parsed.
	 * @param suppressWarnings - flag to tell the lexer and parser to suppress the warnings.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public static TransitionSystem getLTS(InputStream data, boolean suppressWarnings) throws IOException,
		NodeNotExistException, TypeMismatchException, LexerParserException, StructureException {

		ParserContext<TransitionSystem> ctx = new APTParserContext<>(AptLTSFormatLexer.class,
			AptLTSFormatParser.class, APTLTSParserOutput.class, "start");
		((AptLTSFormatLexer) ctx.getLexer()).suppressWarnings(suppressWarnings);
		((AptLTSFormatParser) ctx.getParser()).suppressWarnings(suppressWarnings);
		TransitionSystem ts = new ANTLRParser().parse(data, ctx);

		// Throw error if no initial state is set
		try {
			ts.getInitialState();
		} catch (uniol.apt.adt.exception.StructureException e) {
			throw new StructureException("No initial state is set in lts: '" + ts.getName() + "'.");
		}
		return ts;
	}

	/**
	 * Returns the transitionsystem from a given file in apt format.
	 * <p/>
	 * @param path             - the path to the file in apt format which should be parsed.
	 * @param suppressWarnings - flag to tell the lexer and parser to suppress the warnings.
	 * <p/>
	 * @return the transitionsystem belong to the file.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public static TransitionSystem getLTS(String path, boolean suppressWarnings) throws IOException,
		NodeNotExistException, TypeMismatchException, LexerParserException, StructureException {
		return getLTS(new FileInputStream(path), suppressWarnings);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
