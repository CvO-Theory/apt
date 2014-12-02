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

package uniol.apt.io.parser.impl.synet;

import java.util.Map;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.AbstractLTSParserOutput;
import uniol.apt.io.parser.impl.ParserArc;
import uniol.apt.io.parser.impl.exception.NodeAlreadyExistsException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Holds the data of the SynetLTSParser and converts it into the apt datastructure.
 * <p/>
 * @author Manuel Gieseking
 */
public class SynetLTSParserOutput extends AbstractLTSParserOutput<TransitionSystem> {

	@Override
	public void addState(String id, Map<String, Object> attributes) throws NodeAlreadyExistsException {
		try {
			super.addState(id, attributes);
		} catch (NodeAlreadyExistsException e) {
		}
	}

	@Override
	public TransitionSystem convertToDatastructure() throws NodeNotExistException, TypeMismatchException,
		StructureException {
		if (type != Type.LTS) {
			throw new TypeMismatchException("LTS", type.name());
		}
		TransitionSystem ts = new TransitionSystem("");
		// Add states
		for (String stateId : states.keySet()) {
			ts.createState(stateId);
		}

		// Arcs
		for (ParserArc parserArc : arcs) {
			State fromNode, toNode;
			try {
				fromNode = ts.getNode(parserArc.getFromId());
				toNode = ts.getNode(parserArc.getToId());
			} catch (NoSuchNodeException e) {
				throw new NodeNotExistException(e.getNodeId());
			}
			ts.createArc(fromNode.getId(), toNode.getId(), parserArc.getLabel());
		}
		try {
			State init = ts.getNode("0");
			ts.setInitialState(init);
		} catch (NoSuchNodeException e) {
			throw new StructureException("No initial state is set in lts: '" + ts.getName() + "'.", e);
		}
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
