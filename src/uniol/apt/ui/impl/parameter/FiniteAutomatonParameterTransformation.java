/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.ui.impl.parameter;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.io.parser.impl.RegexParser;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * @author Uli Schlachter
 */
public class FiniteAutomatonParameterTransformation implements ParameterTransformation<FiniteAutomaton> {

	@Override
	public FiniteAutomaton transform(String regularExpression) throws ModuleException {
		try {
			return RegexParser.parseRegex(regularExpression);
		} catch (ParseException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		}
	}

	/**
	 * Get a description on the accepted format for words that this class can parse.
	 * @return A human readable description.
	 */
	static public String getDescription() {
		return "Let's start with an example: (abc?)*\nThis regular expression describes the language where"
			+ " every word consists of a sequence of 'a', then 'b', then an optional 'c'. This sequence can"
			+ " be repeated infinitely often or never at all.\n\n"
			+ "As this example demonstrates, single letter events are just entered as is. White space is"
			+ " not significant and gets ignored. Concatenation is expressed by writing some"
			+ " sub-expressions next to each other. If you need an event consisting of more than just a"
			+ " single letter, enclose it in angle brackets like this: <event>\n\n"
			+ "Supported operations are:\n"
			+ " - a* is the Kleene closure of 'a', including the empty word. This means any sequence"
			+ " of 'a' is allowed.\n"
			+ " - a+ is the Kleene plus of 'a' (without explicitly including the empty word).\n"
			+ " - a? matches either 'a' or nothing, meaning that 'a' is made optional.\n"
			+ " - a|b matches either 'a' or 'b'.\n"
			+ " - ~ describes the empty language\n"
			+ " - $ is the language containing only the empty word\n\n"
			+ "The precedence of the operators is:\n"
			+ " * + and ? bind the strongest, then concatenation and then the union operator. Parentheses"
			+ " can be used to influence the precedence, for example in (a(bc?)+)*.\n\n"
			+ "Finally, comments of the form\n  /* comment */\nand\n  // comment\nare supported.";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
