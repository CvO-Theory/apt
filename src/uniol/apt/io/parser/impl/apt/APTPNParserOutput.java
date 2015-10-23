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
package uniol.apt.io.parser.impl.apt;

import java.util.HashMap;
import java.util.Map;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.impl.AbstractPNParserOutput;
import uniol.apt.io.parser.impl.ParserFlow;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Holds the data of the APTPNParser and converts it into the apt datastructure.
 * <p/>
 * @author Manuel Gieseking
 */
public class APTPNParserOutput extends AbstractPNParserOutput<PetriNet> {

	@Override
	public PetriNet convertToDatastructure() throws NodeNotExistException, TypeMismatchException {
		if (type == Type.PN || type == Type.LPN) {
			PetriNet net = new PetriNet((name != null) ? name : "");
			// description
			net.putExtension("description", description);
			// places
			for (String id : places.keySet()) {
				Place p = net.createPlace(id);
				Map<String, Object> options = places.get(id).getOptions();
				for (String opt : options.keySet()) {
					p.putExtension(opt, options.get(opt));
				}
			}
			// transitions
			for (String id : transitions.keySet()) {
				Transition t = net.createTransition(id);
				Map<String, Object> options = transitions.get(id).getOptions();
				for (String opt : options.keySet()) {
					Object value = options.get(opt);
					if (opt.equals("label")) {
						t.setLabel((String) value);
					} else {
						t.putExtension(opt, value);
					}
				}
			}
			// flows
			for (ParserFlow parserFlow : flows.values()) {
				Node from, to;
				try {
					from = net.getNode(parserFlow.getFromId());
					to = net.getNode(parserFlow.getToId());
				} catch (NoSuchNodeException e) {
					throw new NodeNotExistException(e.getNodeId());
				}
				net.createFlow(from.getId(), to.getId(), parserFlow.getWeight());
			}
			// init marking
			for (String id : initMarking.keySet()) {
				Place p;
				try {
					p = net.getPlace(id);
				} catch (NoSuchNodeException e) {
					throw new NodeNotExistException(e.getNodeId());
				}
				p.setInitialToken(initMarking.get(id));
			}
			// final markings
			for (Map<String, Integer> finalMarking : finalMarkings) {
				for (String placeId : finalMarking.keySet()) {
					try {
						net.getPlace(placeId);
					} catch (NoSuchNodeException e) {
						throw new NodeNotExistException(e.getNodeId());
					}
				}
				net.addFinalMarking(new Marking(net, finalMarking));
			}
			return net;
		} else {
			throw new TypeMismatchException("PN or LPN", type.name());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
