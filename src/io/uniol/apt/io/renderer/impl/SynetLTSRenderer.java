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
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.util.StringComparator;

/**
 * @author Vincent Göbel, Thomas Strathmann
 *
 */
@AptRenderer
public class SynetLTSRenderer extends AbstractRenderer<TransitionSystem> implements Renderer<TransitionSystem> {
	public final static String FORMAT = "synet";

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("aut"));
	}

	@Override
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException {
		// write file header
		writer.append(String.format("des(%d, %d, %d)", 0, ts.getEdges().size(), ts.getNodes().size()));
		writer.append("\n");

		// build a map from APT state _names_ to Synet state _indices_
		HashMap<String, Integer> rename = new HashMap<>();

		// to ensure that the initial state is always mapped to 0 insert it first!
		rename.put(ts.getInitialState().getId(), 0);

		// add the other states (in ascending order)
		TreeSet<String> stateNames = new TreeSet<String>(new StringComparator());
		for (State s : ts.getNodes()) {
			if (s != ts.getInitialState())
				stateNames.add(s.getId());
		}
		int id = 1;
		for (String s : stateNames) {
			rename.put(s, id++);
		}

		// export edges
		for (Arc e : ts.getEdges()) {
			String label = e.getLabel();
			String source = e.getSource().getId();
			String target = e.getTarget().getId();
			writer.append(String.format("(%s, %s, %s)%n", rename.get(source), label, rename.get(target)));
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
