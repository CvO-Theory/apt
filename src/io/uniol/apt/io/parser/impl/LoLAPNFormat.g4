/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  vsp
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

/**
 * ANTLR grammar for the LoLA llnet format
 *
 * It doesn't supports hl-nets.
 * @author: vsp
 */

grammar LoLAPNFormat;

pn:		places marking transitions ;

places:		'PLACE' ID (',' ID)* ';' ;

marking:	'MARKING' pwList ;

transitions:	transition+ ;

transition:	'TRANSITION' ID transitionPreset transitionPostset ;

transitionPreset:	'CONSUME' pwList ;

transitionPostset:	'PRODUCE' pwList ;

// list of weighted places
pwList:	pw (',' pw)* ';' ;

// a weighted place
pw:		ID ':' INT ;

COMMENT:	'{' (~[\r\n])+ '}' -> skip ;

WHITESPACE:	[ \t\r\n]+ -> skip ;

INT:		[0-9]+ ;

// must be after INT
ID:		(~[,;:()\t \n\r{}])+ ;


// vim: ft=antlr3:noet:sw=8:sts=8:ts=8:tw=120
