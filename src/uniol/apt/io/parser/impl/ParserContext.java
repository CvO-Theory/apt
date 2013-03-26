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

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import uniol.apt.io.parser.IParserOutput;

/**
 * The context of the parser. Holds a reference to a lexer, a parser, the start symbol of the grammar and the output
 * with whom the parser converts the internal structure.
 * <p/>
 * @param <G> the graph this context belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public class ParserContext<G> {

	private Lexer lexer;
	private Parser parser;
	private IParserOutput<G> output;
	private String startToken;

	/**
	 * Creates a empty context.
	 */
	public ParserContext() {
	}

	/**
	 * Creates a context with a given lexer, parser, output and the start symbol of the grammar.
	 * <p/>
	 * @param lexer      - the lexer the parser works with.
	 * @param parser     - the parser himself.
	 * @param output     - the output with whom the parser converts the internal datastructure.
	 * @param startToken - the start symbol of the grammar.
	 */
	public ParserContext(Lexer lexer, Parser parser, IParserOutput<G> output, String startToken) {
		this.lexer = lexer;
		this.output = output;
		this.startToken = startToken;
		this.parser = parser;
	}

	/**
	 * Returns the reference to the lexer.
	 * <p/>
	 * @return the reference to the lexer.
	 */
	public Lexer getLexer() {
		return lexer;
	}

	/**
	 * Returns reference to the output of the parser.
	 * <p/>
	 * @return a reference to the output of the parser.
	 */
	public IParserOutput<G> getOutput() {
		return output;
	}

	/**
	 * Returns a reference to the parser.
	 * <p/>
	 * @return a reference to the parser.
	 */
	public Parser getParser() {
		return parser;
	}

	/**
	 * Returns the start symbol of the grammar.
	 * <p/>
	 * @return the start symbol of the grammar.
	 */
	public String getStartToken() {
		return startToken;
	}

	/**
	 * Sets a reference to the lexer.
	 * <p/>
	 * @param lexer - the lexer to set.
	 */
	protected void setLexer(Lexer lexer) {
		this.lexer = lexer;
	}

	/**
	 * Sets a reference to the output.
	 * <p/>
	 * @param output - the output to set.
	 */
	protected void setOutput(IParserOutput<G> output) {
		this.output = output;
	}

	/**
	 * Sets a reference to the parser.
	 * <p/>
	 * @param parser - the parser to set.
	 */
	protected void setParser(Parser parser) {
		this.parser = parser;
	}

	/**
	 * Sets the start symbol of the grammar.
	 * <p/>
	 * @param startToken - the start symbol of the grammar.
	 */
	protected void setStartToken(String startToken) {
		this.startToken = startToken;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
