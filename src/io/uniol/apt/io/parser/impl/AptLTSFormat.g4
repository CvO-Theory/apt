/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

// @author vsp

grammar AptLTSFormat;

@members {
	int descCount = 0;
	int nameCount = 0;
	int typeCount = 0;
}

ts		: ( name | type | description | ltsOptions | states | labels | arcs )* {typeCount == 1}?;

name		: {nameCount == 0}? '.name' STR {nameCount++;};

type		: {typeCount == 0}? '.type' 'LTS' {typeCount++;};

description	: {descCount == 0}? '.description' (txt=STR | txt=STR_MULTI) {descCount++;};

ltsOptions      : '.options' (option (',' option)*)?;

states		: '.states' state*;
state		: idi (opts)? ;

opts		: '[' option (',' option)* ']';
option		: ID '=' STR | ID | ID '=' INT ;

labels		: '.labels' label*;
label		: idi (opts)? ;

arcs		: '.arcs' arc*;
arc		: src=idi labell=idi dest=idi (opts)?;

idi		: ID | INT;

INT		: '0'..'9'+;
ID		: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

COMMENT		: (
			'//' ~('\n'|'\r')*
			| '/*' (. )*? '*/'
		) -> skip;

WS		:   ( ' ' | '\n' | '\r' | '\t') -> skip ;

STR		: '"' ~('"' | '\n' | '\r' | '\t')*  '"';
STR_MULTI	: '"' ~('"' | '\t' )*  '"';

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
