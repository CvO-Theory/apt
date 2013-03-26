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

package uniol.apt.io.parser;

import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * A interface for a graph of type G saving the data of the parser and converting it to the graph.
 * <p/>
 * @param <G> the graph the output belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public interface IParserOutput<G> {

	/**
	 * Enum for the possible types of a graph. LTS: labeled transitionsystem TS: transitionsystem PN: petri net
	 */
	public enum Type {

		LPN, LTS, PN
	};

	/**
	 * Sets the name of the graph.
	 * <p/>
	 * @param name - the name of the graph.
	 */
	public void setName(String name);

	/**
	 * Sets a description of the graph.
	 * <p/>
	 * @param description - the description of the graph.
	 */
	public void setDescription(String description);

	/**
	 * Sets the type of the graph.
	 * <p/>
	 * @param type - the of the graph (LPN, LTS, PN)
	 */
	public void setType(Type type);

	/**
	 * Converts the data saved in this class to the graph of type G.
	 * <p/>
	 * @return the graph containing the data of this class.
	 * <p/>
	 * @throws NodeNotExistException thrown if a used node do not exists in the graph.
	 * @throws TypeMismatchException thrown if the type does not match to the graph.
	 */
	public G convertToDatastructure() throws NodeNotExistException, TypeMismatchException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
