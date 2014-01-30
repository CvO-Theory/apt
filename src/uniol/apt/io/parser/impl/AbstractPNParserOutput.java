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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import uniol.apt.io.parser.IPNParserOutput;
import uniol.apt.io.parser.impl.exception.NodeAlreadyExistsException;

/**
 * A class for a petri net of type G saving the data of the parser and converting it to the petri net.
 * <p/>
 * @param <G> the petri net the output belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public abstract class AbstractPNParserOutput<G> extends AbstractParserOutput<G> implements IPNParserOutput<G> {

	protected Map<String, ParserNode> places = new HashMap<>();
	protected Map<String, ParserNode> transitions = new HashMap<>();
	protected Map<ParserFlow, ParserFlow> flows = new HashMap<>();
	protected Map<String, Integer> initMarking = new HashMap<>();
	protected Set<Map<String, Integer>> finalMarkings = new HashSet<>();

	@Override
	public void addPlace(String id, Map<String, String> attributes, IntStream input) throws RecognitionException {
		try {
			addPlace(id, attributes);
		} catch (NodeAlreadyExistsException ne) {
			RecognitionException re = new RecognitionException(input);
			re.initCause(ne);
			throw re;
		}
	}

	@Override
	public void addTransition(String id, Map<String, String> attributes, IntStream input) throws RecognitionException {
		try {
			addTransition(id, attributes);
		} catch (NodeAlreadyExistsException ne) {
			RecognitionException re = new RecognitionException(input);
			re.initCause(ne);
			throw re;
		}
	}

	@Override
	public void addPlace(String id, Map<String, String> attributes) throws NodeAlreadyExistsException {
		if (places.containsKey(id)) {
			throw new NodeAlreadyExistsException(id);
		}
		ParserNode place = new ParserNode(id);
		place.putAllOptions(attributes);
		places.put(id, place);
	}

	@Override
	public void addTransition(String id, Map<String, String> attributes) throws NodeAlreadyExistsException {
		if (transitions.containsKey(id)) {
			throw new NodeAlreadyExistsException(id);
		}
		ParserNode trans = new ParserNode(id);
		trans.putAllOptions(attributes);
		transitions.put(id, trans);
	}

	@Override
	public void addFlow(String fromNode, String toNode, int weight) {
		ParserFlow f = new ParserFlow(fromNode, toNode, weight);
		ParserFlow sec = flows.get(f);
		if (sec != null) {
			f.setWeight(sec.getWeight() + weight);
		}
		flows.put(f, f);
	}

	@Override
	public void setInitialMarking(Map<String, Integer> marking) {
		initMarking = marking;
	}

	@Override
	public void addFinalMarking(Map<String, Integer> marking) {
		finalMarkings.add(marking);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
