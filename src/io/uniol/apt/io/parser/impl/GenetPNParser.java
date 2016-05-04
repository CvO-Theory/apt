/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       Uli Schlachter
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

package uniol.apt.io.parser.impl;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;

/**
 * Genet PN format parser
 * @author Uli Schlachter
 */
@AptParser
public class GenetPNParser extends PetrifyPNParser implements Parser<PetriNet> {
	@Override
	public String getFormat() {
		return "genet";
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("g"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
