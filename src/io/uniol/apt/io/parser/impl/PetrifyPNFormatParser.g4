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

parser grammar PetrifyPNFormatParser;

options { tokenVocab = PetrifyPNFormatLexer; }

pn		: nl? model? transitions? type flows capacity? marking end;
model		: MODEL NAME nl;
transitions	: TRANSITIONS ID* nl;
type		: GRAPH nl;
flows		: flow*;
flow		: source=event flow_target* nl;
flow_target	: event (PAREN_OPEN INT PAREN_CLOSE)?;
capacity	: CAPACITY (ID EQUAL INT)* nl;
marking		: MARKING CURLY_OPEN token* CURLY_CLOSE nl;
token		: ID (EQUAL INT)?                                                     # tokenExplicitPlace
		| ANGLE_OPEN source=event COMMA target=event ANGLE_CLOSE (EQUAL INT)? # tokenImplicitPlace
		;
end		: END nl;
nl		: NL+;
event		: ID    # eventUnsplit
		| EVENT # eventSplit
		;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
