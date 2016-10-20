/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       Uli Schlachter
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

parser grammar RegexFormatParser;

options { tokenVocab = RegexFormatLexer; }

start       : expr EOF ;
expr        : exprOr ;
exprOr     : ex1=exprAnd
			(OR ex2=exprOr)?;
exprAnd    : ex1=exprConcat
			(AND ex2=exprAnd)?;
exprConcat : ex1=exprRepeat
			(ex2=exprConcat)?;
exprRepeat : exprNegatePrefix STAR						# exprRepeatStar
		| exprNegatePrefix OPT						# exprRepeatOpt
		| exprNegatePrefix PLUS						# exprRepeatPlus
		| exprNegatePrefix REPEATOPEN x=INT REPEATCLOSE			# exprRepeatExact
		| exprNegatePrefix REPEATOPEN x=INT COMMA REPEATCLOSE		# exprRepeatLeast
		| exprNegatePrefix REPEATOPEN x=INT COMMA y=INT REPEATCLOSE	# exprRepeatMinmax
		| exprNegatePrefix						# exprRepeatNothing
		;
exprNegatePrefix : exprId	# exprNPDirect
		| NEGATE exprId	# exprNPNegate
		| PREFIX exprId	# exprNPPrefix
		;
exprId     : PAROPEN expr PARCLOSE	# exprIdParentheses
			| ATOM		# exprIdAtom
			| ID		# exprIdId
			| EMPTY		# exprIdEmpty
			| EPSILON	# exprIdEpsilon
			;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
