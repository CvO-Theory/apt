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
import uniol.apt.io.parser.ILTSParserOutput;
import uniol.apt.io.parser.impl.exception.NodeAlreadyExistsException;

/**
 * A class for a transitionsystem of type G saving the data of the parser and converting it to the transitionsystem.
 * <p/>
 * @param <G> the transitionsystem the output belongs to.
 * <p/>
 * @author Manuel Gieseking
 */
public abstract class AbstractLTSParserOutput<G> extends AbstractParserOutput<G> implements ILTSParserOutput<G> {

	protected Map<String, ParserNode> states = new HashMap<>();
	protected Map<String, ParserNode> labels = new HashMap<>();
	protected Set<ParserArc> arcs = new HashSet<>();

	@Override
	public void addState(String id, Map<String, Object> attributes, IntStream input) throws RecognitionException {
		try {
			addState(id, attributes);
		} catch (NodeAlreadyExistsException ne) {
			RecognitionException re = new RecognitionException(input);
			re.initCause(ne);
			throw re;
		}
	}

	@Override
	public void addState(String id, Map<String, Object> attributes) throws NodeAlreadyExistsException {
		if (states.containsKey(id)) {
			throw new NodeAlreadyExistsException(id);
		}
		ParserNode state = new ParserNode(id);
		state.putAllOptions(attributes);
		states.put(id, state);
	}

	@Override
	public void addLabel(String id, Map<String, Object> attributes) {
		ParserNode label = new ParserNode(id);
		label.putAllOptions(attributes);
		labels.put(id, label);
	}

	@Override
	public void addArc(String fromNode, String label, String toNode) {
		arcs.add(new ParserArc(fromNode, label, toNode));
	}

	@Override
	public void setInitialState(String id) {
		ParserNode node = states.get(id);
		if (node != null) {
			node.putOption("initial", "true");
		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("initial", "true");
			try {
				addState(id, map);
			} catch (NodeAlreadyExistsException ex) {
				//not possible since node == null
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
