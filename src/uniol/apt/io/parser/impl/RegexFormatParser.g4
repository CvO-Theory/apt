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
expr        : expr_or ;
expr_or     : ex1=expr_concat
			(OR ex2=expr_or)?;
expr_concat : ex1=expr_repeat
			(ex2=expr_concat)?;
expr_repeat : expr_id STAR						# expr_repeat_star
		| expr_id OPT						# expr_repeat_opt
		| expr_id PLUS						# expr_repeat_plus
		| expr_id						# expr_repeat_nothing
		;
expr_id     : PAROPEN expr PARCLOSE	# expr_id_parentheses
			| ATOM		# expr_id_atom
			| ID		# expr_id_id
			| EMPTY		# expr_id_empty
			| EPSILON	# expr_id_epsilon
			;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
