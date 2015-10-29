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

package uniol.apt.io.renderer.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Formatter;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.LTSRenderer;
import uniol.apt.io.renderer.RenderException;

/**
 * This class converts Petri nets and transition systems to dot file strings.
 *
 *
 * @author Renke Grunwald
 *
 */
public class DotLTSRenderer extends AbstractLTSRenderer implements LTSRenderer {
	private static final String TS_NODE_TEMPLATE =
		"%1$s[label=\"%2$s\"];\n";
	private static final String TS_INITIAL_NODE_TEMPLATE =
		"%1$s[label=\"%2$s\", shape=circle];\n";
	private static final String TS_EDGE_TEMPLATE =
		"%1$s -> %2$s[label=\"%3$s\"];\n";

	@Override
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException {
		writer.append("digraph G {\n");
		writer.append("node [shape = point, color=white, fontcolor=white]; start;");
		writer.append("edge [fontsize=20]\n");
		writer.append("node [fontsize=20,shape=circle,color=black, fontcolor=black, height=0.5,width=0.5,fixedsize=true];\n");

		Formatter nodeFormat = new Formatter(writer);

		for (State node : ts.getNodes()) {
			if (ts.getInitialState().equals(node)) {
				nodeFormat.format(TS_INITIAL_NODE_TEMPLATE, node.getId(), node.getId());
			} else {
				nodeFormat.format(TS_NODE_TEMPLATE, node.getId(), node.getId());
			}
		}

		nodeFormat.close();

		Formatter edgeFormat = new Formatter(writer);

		edgeFormat.format(TS_EDGE_TEMPLATE, "start", ts.getInitialState().getId(), "");

		for (Arc edge : ts.getEdges()) {
			edgeFormat.format(TS_EDGE_TEMPLATE, edge.getSource().getId(), edge.getTarget().getId(),
					edge.getLabel());
		}

		edgeFormat.close();
		writer.append("}\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
