/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 *               2014       vsp
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

// @author Manuel Gieseking, vsp

grammar AptPNFormat;

@members {
	int descCount = 0;
	int markCount = 0;
	int nameCount = 0;
	int typeCount = 0;
}

/* type must occur once; description, name and initialMarking may occur once; netOptions, places, transitions, flows and
 * finalMarkings may occur arbitrary times */
pn:	( description | finalMarkings | flows | initialMarking | name | netOptions | places | transitions | type )* {typeCount == 1}? ;

name: {nameCount == 0}? '.name' STR {nameCount++;};

type : {typeCount == 0}? '.type' ('LPN' | 'PN') {typeCount++;};

description : {descCount == 0}? '.description' (txt=STR | txt=STR_MULTI) {descCount++;};

netOptions : '.options' (option (',' option)*)?;

places     : '.places' place*;
place      :  (id=ID | id=INT) (opts)? ;

transitions : '.transitions' transition*;
transition  : (id=ID | id=INT) (opts)? ;

/* options for places or transitions */
opts : '[' option (',' option)* ']';
option :	ID '=' STR
		| ID '=' INT
		| ID
		;

flows : '.flows' flow*;
flow  : (id=ID | id=INT) ':'  preset=set '->' postset=set (opts)?;

/* sets for flow description and markings */
set   : '{' ( | obj (',' obj)*) '}';
obj   : mult=INT '*' (id=ID | id=INT)
	| (id=ID | id=INT) ;

initialMarking : {markCount == 0}? '.initial_marking' (set)? {markCount++;};

finalMarkings : '.final_markings' set*;

/* Lexer symbols */

INT: '0'..'9'+;
ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

COMMENT: (
		'//' ~('\n'|'\r')*
		|   '/*' (. )*? '*/'
	) -> skip;

WS  :   ( ' ' | '\n' | '\r' | '\t') -> skip ;

STR: '"' ~('"' | '\n' | '\r' | '\t')*  '"';
STR_MULTI: '"' ~('"' | '\t' )*  '"';

// vim: ft=antlr3:noet:sw=8:sts=8:ts=8:tw=120
