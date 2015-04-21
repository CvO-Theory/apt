/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C) 2015       Uli Schlachter
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

grammar AptRegexFormat;

tokens {
	OR = '|';
	STAR = '*';
	OPT = '?';
	PLUS = '+';
	EPSILON = '$';
	EMPTY = '~';
}

@lexer::header {
	package uniol.apt.io.parser.impl.apt;
}

@header {
	package uniol.apt.io.parser.impl.apt;
	import uniol.apt.adt.automaton.FiniteAutomaton;
	import uniol.apt.adt.automaton.Symbol;
	import uniol.apt.io.parser.impl.exception.FormatException;
	import uniol.apt.io.parser.impl.exception.LexerParserException;
	import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;
}

@members {
	public static FiniteAutomaton parseString(String regex) throws FormatException {
		AptRegexFormatLexer lexer = new AptRegexFormatLexer(new ANTLRStringStream(regex));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		AptRegexFormatParser parser = new AptRegexFormatParser(tokens);
		try {
			return parser.start();
		} catch (RecognitionException ex) {
			throw new LexerParserException(parser, lexer, ex);
		}
	}

	public static FiniteAutomaton getAutomaton(String atom) {
		return getAtomicLanguage(new Symbol(atom));
	}
}

@rulecatch {
	// This disables ANTLR's default error handling
}

start       returns [FiniteAutomaton aut]	: expr EOF { $aut = $expr.aut; };
expr        returns [FiniteAutomaton aut]	: expr_or { $aut = $expr_or.aut; };
expr_or     returns [FiniteAutomaton aut]	: ex1=expr_concat { $aut = $ex1.aut; }
						(OR ex2=expr_concat { $aut = union($aut, $ex2.aut); })*;
expr_concat returns [FiniteAutomaton aut]	: ex1=expr_repeat { $aut = $ex1.aut; }
						(ex2=expr_repeat {$aut = concatenate($aut, $ex2.aut); }) *;
expr_repeat returns [FiniteAutomaton aut]	: expr_id { $aut = $expr_id.aut; }
						(STAR { $aut = kleeneStar($aut); } | OPT { $aut = optional($aut); } | PLUS { $aut = kleenePlus($aut); } )*;
expr_id     returns [FiniteAutomaton aut]	: '(' expr ')' { $aut = $expr.aut; }
						| label { $aut = $label.aut; }
						| EMPTY { $aut = getEmptyLanguage(); }
						| EPSILON { $aut = getAtomicLanguage(Symbol.EPSILON); };

label       returns [FiniteAutomaton aut]	: ATOM { $aut = getAutomaton($ATOM.text); }
						| ID { $aut = getAutomaton($ID.text.substring(1, $ID.text.length() - 1)); };

ATOM		: SYMBOL;
ID		: '<' SYMBOL+ '>';
COMMENT		: '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
			|   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};
WS		: ( ' ' | '\n' | '\r' | '\t')+ {$channel=HIDDEN;};

fragment SYMBOL	: DIGIT | LETTER | '_';
fragment DIGIT	: '0'..'9';
fragment LETTER	: 'a'..'z'|'A'..'Z';

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
