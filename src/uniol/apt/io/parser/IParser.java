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
import java.io.InputStream;
import uniol.apt.io.parser.impl.ParserContext;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * A interface for a parser converting a stream or file in a graph.
 * <p/>
 * @author Manuel Gieseking
 */
public interface IParser {

	/**
	 * Parses a given file into a graph of type G by using the given parser context.
	 * <p/>
	 * @param <G>  the resulting graph.
	 * @param data the inputstream with the data to parse.
	 * @param ctx  the context of the parser. That means the parser, lexer, the output and a startsymbol.
	 * <p/>
	 * @return the generated graph.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *					all.
	 */
	public <G> G parse(InputStream data, ParserContext<G> ctx) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException, FormatException;

	/**
	 * Parses a given file into a graph of type G by using the given parser context.
	 * <p/>
	 * @param <G>  the resulting graph.
	 * @param path the file with the data to parse.
	 * @param ctx  the context of the parser. That means the parser, lexer, the output and a startsymbol.
	 * <p/>
	 * @return the generated graph.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 * @throws FormatException       thrown if any other problem with the format occurs. Is the super class of them
	 *					all.
	 */
	public <G> G parse(String path, ParserContext<G> ctx) throws IOException, NodeNotExistException,
		TypeMismatchException, LexerParserException, StructureException, FormatException;
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

