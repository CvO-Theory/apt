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

import java.util.Map;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;
import uniol.apt.io.parser.impl.exception.NodeAlreadyExistsException;

/**
 * A interface for a LTS of type G saving the data of the parser and converting it to the LTS.
 * <p/>
 * @param <G> the LTS the output should belong to.
 * <p/>
 * @author Manuel Gieseking
 */
public interface ILTSParserOutput<G> extends IParserOutput<G> {

	/**
	 * Adds a state with the given id and attributes to the internal datastructure. If a state with the same id
	 * already exists within the graph, this method automatically recovers a suitable exception. Thus, nothing more
	 * have to be done and the parser creates a fitting LexerParserException if needable.
	 * <p/>
	 * @param id         - the id of the state.
	 * @param attributes - a map of attributes.
	 * @param input      - the stream used for parsing.
	 * @throws RecognitionException thrown if a state with the same id already exists within the graph.
	 */
	public void addState(String id, Map<String, Object> attributes, IntStream input) throws RecognitionException;

	/**
	 * Adds a state with the given id and attributes to the internal datastructure.
	 * <p/>
	 * @param id         - the id of the state.
	 * @param attributes - a map of attributes.
	 * @throws NodeAlreadyExistsException thrown if a state with the same id already exists within the graph.
	 */
	public void addState(String id, Map<String, Object> attributes) throws NodeAlreadyExistsException;

	/**
	 * Adds a label with the given id and attributes to the internal datastructure.
	 * <p/>
	 * @param id         - the id of the Label.
	 * @param attributes - a map of attributes.
	 */
	public void addLabel(String id, Map<String, Object> attributes);

	/**
	 * Adds a arc from the state with idFrom to a state with idTo and a given label to the internal datastructure.
	 * <p/>
	 * @param idFrom - the id of state the arc starts from.
	 * @param label  - the label the arc has.
	 * @param idTo   - the id of the state the arc ends.
	 */
	public void addArc(String idFrom, String label, String idTo);

	/**
	 * Sets the state with the given id as initial state in the internal datastructure.
	 * <p/>
	 * @param id - the id of the state which should be initial.
	 */
	public void setInitialState(String id);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
