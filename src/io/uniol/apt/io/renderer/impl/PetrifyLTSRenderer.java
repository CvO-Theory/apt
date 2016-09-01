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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.io.renderer.Renderer;

/**
 * Creates a string which returns a petri net or transitionsystem in the Petrify-format.
 * @author Sören Dierkes
 *
 */
@AptRenderer
public class PetrifyLTSRenderer extends AbstractRenderer<TransitionSystem> implements Renderer<TransitionSystem> {
	public final static String FORMAT = "petrify";

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("g"));
	}

	@Override
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException {
		// Petrify does not like:
		// - empty names (attempted fix below)
		// - names containing spaces (thus this code was disabled)
		/*
		sb.append(".model ").append(ts.getName());
		if (ts.getName().isEmpty())
			sb.append("model");
		sb.append("\n");
		*/

		writer.append(".inputs ");
		for (String label : ts.getAlphabet()) {
			writer.append(label).append(" ");
		}
		writer.append("\n");

		writer.append(".state graph");
		writer.append("\n");

		for (Arc e : ts.getEdges()) {
			String label = e.getLabel();
			String source = e.getSource().getId();
			String target = e.getTarget().getId();
			writer.append(source).append(" ").append(label).append(" ").append(target);
			writer.append("\n");
		}

		writer.append(".marking {").append(ts.getInitialState().getId()).append("}");
		writer.append("\n");

		writer.append(".end");
		writer.append("\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
