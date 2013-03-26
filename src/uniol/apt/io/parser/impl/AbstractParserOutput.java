/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

import uniol.apt.io.parser.IParserOutput;

/**
 * A class for a graph of type G saving the data of the parser and converting it to the graph.
 * <p/>
 * @param <G> the graph this output belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public abstract class AbstractParserOutput<G> implements IParserOutput<G> {

	protected String name;
	protected Type type;
	protected String description;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
