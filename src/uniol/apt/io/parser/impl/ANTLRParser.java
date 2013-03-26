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

package uniol.apt.io.parser.impl;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import org.antlr.runtime.RecognitionException;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.runtime.ANTLRInputStream;
import uniol.apt.io.parser.IParser;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Implementation of a parser using antlr parser and lexer to parse a file.
 * <p/>
 * @author Manuel Gieseking
 */
public class ANTLRParser implements IParser {

	@Override
	public <G> G parse(InputStream data, ParserContext<G> ctx) throws IOException, StructureException,
		NodeNotExistException, TypeMismatchException, LexerParserException {
		try {
			ctx.getLexer().setCharStream(new ANTLRInputStream(data));
			ctx.getParser().getClass().getMethod("start").invoke(ctx.getParser());
			return ctx.getOutput().convertToDatastructure();
		} catch (NoSuchMethodException |
			SecurityException | IllegalAccessException | IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException e) {
			Throwable ex = e.getCause();
			if (ex instanceof RecognitionException) {
				RecognitionException exi = (RecognitionException) ex;
				LexerParserException exception = new LexerParserException(ctx.getParser(),
					ctx.getLexer(), exi);
				throw exception;
			} else if (ex instanceof StructureException) {
				throw (StructureException) ex;
			} else {
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public <G> G parse(String path, ParserContext<G> ctx) throws IOException, StructureException,
		NodeNotExistException, TypeMismatchException, LexerParserException {
		return parse(new FileInputStream(path), ctx);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
