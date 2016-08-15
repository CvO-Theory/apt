/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
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

// @author Uli Schlachter

lexer grammar PetrifyPNFormatLexer;

MODEL		: '.model' -> mode(MODEL_MODE);
TRANSITIONS	: '.inputs' | '.outputs' | '.internal' | '.dummy';
GRAPH		: '.graph';
MARKING		: '.marking';
END		: '.end';
CAPACITY	: '.capacity';
PAREN_OPEN	: '(';
PAREN_CLOSE	: ')';
CURLY_OPEN	: '{';
CURLY_CLOSE	: '}';
ANGLE_OPEN	: '<';
ANGLE_CLOSE	: '>';
COMMA		: ',';
EQUAL		: '=';
NL		: '\n';

EVENT		: [a-zA-Z_] [a-zA-Z0-9_:\-]* '/' [0-9]+ ;
ID		: [a-zA-Z_] [a-zA-Z0-9_:\-]*;
INT		: [1-9][0-9]*;

COMMENT		: '#' ~('\n')* -> skip;
WS		: (' ' | '\r' | '\t')+ -> skip;

mode MODEL_MODE;

NAME		: ~[ \n#]+;

// "Copy" some rules from the default mode, NL leaves the mode
M_NL		: NL -> type(NL), mode(DEFAULT_MODE);
M_COMMENT	: COMMENT -> type(COMMENT), skip;
M_WS		: WS -> type(WS), skip;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
