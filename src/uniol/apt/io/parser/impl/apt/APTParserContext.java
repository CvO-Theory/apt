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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import uniol.apt.io.parser.IParserOutput;
import uniol.apt.io.parser.impl.ParserContext;

/**
 * The context for a parser uses antlr. It provides the possiblity to just give classes and the rest ist generated.
 * After that it holds a reference to a lexer, a parser, the start symbol of the grammatic and the output with whom the
 * parser converts the internal datastructure.
 * <p/>
 * @param <G> the graph this context belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public class APTParserContext<G> extends ParserContext<G> {

	/**
	 * Creates a context by classes.
	 * <p/>
	 * @param lexer      - the class of the lexer.
	 * @param parser     - the class of the parser.
	 * @param output     - the class of the output.
	 * @param startToken - the start symbol of the grammar.
	 */
	public APTParserContext(Class<? extends Lexer> lexer, Class<? extends Parser> parser,
		Class<? extends IParserOutput<G>> output, String startToken) {
		try {
			Lexer l = lexer.newInstance();
			IParserOutput<G> o = output.newInstance();
			for (Constructor<?> c : parser.getConstructors()) {
				if (c.getParameterTypes().length == 2 && c.getParameterTypes()[1].isInterface()) {
					Parser p = (Parser) c.newInstance(new CommonTokenStream(l), o);
					setParser(p);
				}
			}
			setLexer(l);
			setOutput(o);
			setStartToken("start");
		} catch (IllegalArgumentException | InvocationTargetException |
			SecurityException | InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
