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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.AptRenderer;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.util.Pair;

/**
 * @author Vincent Göbel, Uli Schlachter
 */
@AptRenderer
public class AptLTSRenderer extends AbstractRenderer<TransitionSystem> implements Renderer<TransitionSystem> {
	public final static String FORMAT = "apt";

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("ats", "apt"));
	}

	// Wrapper around states to fake an "initial" extension on the initial state
	static private class InitialStateDecorator {
		private final State decoratedState;

		public InitialStateDecorator(State state) {
			decoratedState = state;
		}

		public String getId() {
			return decoratedState.getId();
		}

		public List<Pair<String, Object>> getWriteToFileExtensions() {
			List<Pair<String, Object>> result = new ArrayList<>(decoratedState.getWriteToFileExtensions());
			result.add(0, new Pair<String, Object>("initial", "true"));
			return result;
		}
	}

	static private Collection<Object> decorateStates(TransitionSystem ts) {
		Collection<Object> result = new ArrayList<>();
		result.add(new InitialStateDecorator(ts.getInitialState()));
		for (State state : ts.getNodes())
			if (!state.equals(ts.getInitialState()))
				result.add(state);
		return result;
	}

	@Override
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException {
		STGroup group = new STGroupFile("uniol/apt/io/renderer/impl/AptLTS.stg");
		ST ltsTemplate = group.getInstanceOf("lts");

		ltsTemplate.add("name", ts.getName());
		if (ts.hasExtension("description")) {
			ltsTemplate.add("description", ts.getExtension("description"));
		}
		ltsTemplate.add("extensions", ts.getWriteToFileExtensions());
		ltsTemplate.add("states", decorateStates(ts));
		ltsTemplate.add("arcs", ts.getEdges());
		ltsTemplate.add("events", ts.getAlphabetEvents());

		ltsTemplate.write(new AutoIndentWriter(writer), new ThrowingErrorListener());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
