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

package uniol.apt.io.parser.impl.exception;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;

/**
 * An exception thrown if the parser or the lexer are not able to analyse the file.
 * <p/>
 * @author Manuel Gieseking
 */
public class LexerParserException extends FormatException {

	private static final long serialVersionUID = 1L;
	private RecognitionException exception;
	private String lexerMsg = "";
	private String parserMsg = "";

	/**
	 * Creates a exception by creating a message from the information of the parser.
	 * <p/>
	 * @param parser - the parser found the error.
	 * @param lexer  - the lexer found the error.
	 * @param e      - the RecognitionException thrown by the parser.
	 */
	public LexerParserException(Parser parser, Lexer lexer, RecognitionException e) {
		exception = e;
		String hdr = parser.getErrorHeader(e);
		String msg = parser.getErrorMessage(e, parser.getTokenNames());
		parserMsg = hdr + " " + msg;
		hdr = lexer.getErrorHeader(e);
		msg = lexer.getErrorMessage(e, lexer.getTokenNames());
		lexerMsg += hdr + " " + msg;
	}

	/**
	 * Sets the lexer error message to this exception.
	 * <p/>
	 * @param lexerMsg - the lexer error message to set.
	 */
	public void setLexerMsg(String lexerMsg) {
		this.lexerMsg = lexerMsg;
	}

	/**
	 * Returns the lexer error message belonging to this exception.
	 * <p/>
	 * @return the lexer error message.
	 */
	public String getLexerMsg() {
		return lexerMsg;
	}

	/**
	 * Returns the parser error message belonging to this exception.
	 * <p/>
	 * @return the parser error message.
	 */
	public String getParserMsg() {
		return parserMsg;
	}

	/**
	 * Returns the RecognitionException belonging to this exception.
	 * <p/>
	 * @return the RecogntitionException.
	 */
	public RecognitionException getException() {
		return exception;
	}

	/**
	 * Returns the combined error message. That means the lexer error and the parser error. Or only one of them if
	 * the other isn't set.
	 * <p/>
	 * @return the whole error message.
	 */
	public String getLexerParserMessage() {
		StringBuilder sb = new StringBuilder();
		if (!lexerMsg.isEmpty()) {
			sb.append("[ERROR] Lexer: ").append(lexerMsg).append("\n");
		}
		if (!parserMsg.isEmpty()) {
			sb.append("[ERROR] Parser: ").append(parserMsg);
		}
		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
