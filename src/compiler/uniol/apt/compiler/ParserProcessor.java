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

package uniol.apt.compiler;

import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;

/**
 * Annotation Processor which searches for classes annotated with AptParser, analyzes them and write them in lists in
 * META-INF where the Parsers class can find them.
 *
 * @author vsp
 */
public class ParserProcessor extends AbstractAptServiceProcessor {
	/**
	 * Constructor
	 */
	public ParserProcessor() {
		super(AptParser.class, Parser.class);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
