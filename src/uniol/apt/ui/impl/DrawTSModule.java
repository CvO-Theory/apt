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

package uniol.apt.ui.impl;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.impl.DotLTSRenderer;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Convert a given transition system to the Dot file format used by Graphviz.
 *
 * @author Renke Grunwald
 *
 */
public class DrawTSModule extends AbstractModule {

	@Override
	public String getName() {
		return "draw_lts";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("dot", String.class, ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);

		String dot = new DotLTSRenderer().render(ts);

		output.setReturnValue("dot", String.class, dot);
	}

	@Override
	public String getTitle() {
		return "Draw a transition system via Graphviz";
	}

	@Override
	public String getShortDescription() {
		return "Convert a transition system to the Dot format used by Graphviz";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
