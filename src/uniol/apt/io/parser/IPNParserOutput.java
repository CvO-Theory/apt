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
 * A interface for a petri net of type G saving the data of the parser and converting it to the petri net.
 * <p/>
 * @param <G> the PN the output should belong to.
 * <p/>
 * @author Manuel Gieseking
 */
public interface IPNParserOutput<G> extends IParserOutput<G> {

    /**
     * Adds a place with the given id and a list of attributes to the internal datastructure of the parser. If a
     * place with the same id already exists within the graph, this method automatically recovers a suitable
     * exception. Thus, nothing more have to be done and the parser creates a fitting LexerParserException if
     * needable.
     * <p/>
     * @param id         - the id of the place to add.
     * @param attributes - a map of the attributes of the added place.
     * @param input      - the stream used for parsing.
     * <p/>
     * @throws RecognitionException thrown if a place with the same id already exists within the graph.
     */
    public void addPlace(String id, Map<String, Object> attributes, IntStream input) throws RecognitionException;

    /**
     * Adds a transition with the given id and attributes to the internal datastructure of the parser. If a
     * transition with the same id already exists within the graph, this method automatically recovers a suitable
     * exception. Thus, nothing more have to be done and the parser creates a fitting LexerParserException if
     * needable.
     * <p/>
     * @param id         - the id of the transition to add.
     * @param attributes - a map of attributes of the added transition.
     * @param input      - the stream used for parsing.
     * <p/>
     * @throws RecognitionException thrown if a transition with the same id already exists within the graph.
     */
    public void addTransition(String id, Map<String, Object> attributes, IntStream input) throws RecognitionException;

    /**
     * Adds a place with the given id and a list of attributes to the internal datastructure of the parser.
     * <p/>
     * @param id         - the id of the place to add.
     * @param attributes - a map of the attributes of the added place.
     * <p/>
     * @throws NodeAlreadyExistsException thrown if a place with the same id already exists within the graph.
     */
    public void addPlace(String id, Map<String, Object> attributes) throws NodeAlreadyExistsException;

    /**
     * Adds a transition with the given id and attributes to the internal datastructure of the parser.
     * <p/>
     * @param id         - the id of the transition to add.
     * @param attributes - a map of attributes of the added transition.
     * <p/>
     * @throws NodeAlreadyExistsException thrown if a transition with the same id already exists within the graph.
     */
    public void addTransition(String id, Map<String, Object> attributes) throws NodeAlreadyExistsException;

    /**
     * Adds a edge from the node with the id fromNode to the node with the id toNode with the given weight to the
     * internal datastructure.
     * <p/>
     * @param fromNode - the id the edge starts from.
     * @param toNode   - the id the edge ends.
     * @param weight   - the weight of the edge.
     */
    public void addFlow(String fromNode, String toNode, int weight);

    /**
     * Sets the initial marking of the petri net to the internal datastructure.
     * <p/>
     * @param marking - the inital marking of the net.
     */
    public void setInitialMarking(Map<String, Integer> marking);

    /**
     * Adds a final marking of the petri net to the internal datastructure.
     * <p/>
     * @param marking - a final marking.
     */
    public void addFinalMarking(Map<String, Integer> marking);
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
