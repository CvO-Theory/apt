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
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;

/**
 * @author Vincent Göbel
 *
 */
public class AptLTSRenderer extends AbstractRenderer<TransitionSystem> implements Renderer<TransitionSystem> {
	@Override
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException {
		writer.append(".name \"").append(ts.getName()).append("\"\n");
		writer.append(".type LTS" + "\n");
		writer.append("\n");

		writer.append(".states" + "\n");
		for (State s : ts.getNodes()) {
			writer.append(s.getId());
			if (s.equals(ts.getInitialState())) {
				writer.append("[initial]");
			}

			/* If the "comment" extension is present, escape it properly and append it as a comment */
			try {
				Object comment = s.getExtension("comment");
				if (comment instanceof String) {
					String c = (String) comment;
					writer.append(" /* ");
					writer.append(c.replace("*/", "* /"));
					writer.append(" */");
				}
			} catch (StructureException ex) {
				/* ignore Exception if comment doesn't exist */
			}
			writer.append("\n");
		}
		writer.append("\n");

		writer.append(".labels" + "\n");
		Set<String> labels = new HashSet<>();
		for (Arc e : ts.getEdges()) {
			labels.add(e.getLabel());
		}
		for (String l : labels) {
			writer.append(l).append("\n");
		}
		writer.append("\n");

		writer.append(".arcs");
		for (Arc e : ts.getEdges()) {
			writer.append("\n");
			writer.append(e.getSource().getId()).append(" ");
			writer.append(e.getLabel()).append(" ");
			writer.append(e.getTarget().getId());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
