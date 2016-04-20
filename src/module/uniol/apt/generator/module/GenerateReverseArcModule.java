/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.generator.module;

import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.exception.ArcExistsException;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

@AptModule
public class GenerateReverseArcModule extends AbstractModule implements Module {

	@Override
	public String getShortDescription() {
		return "Generate reverse arcs for all arcs with a given label";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". For all arcs labelled with the given event,"
		       + " an arc in the opposite direction labelled with the given reverseEvent is added.";
	}

	@Override
	public String getName() {
		return "generate_reverse_arc";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
		inputSpec.addParameter("event", String.class, "The event to look for");
		inputSpec.addParameter("reverseEvent", String.class, "The event for the reverse arcs");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("lts", TransitionSystem.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		String event = input.getParameter("event", String.class);
		String reverseEvent = input.getParameter("reverseEvent", String.class);

		Set<Arc> arcs = new HashSet<>();
		for (Arc arc : ts.getEdges()) {
			if (arc.getLabel().equals(event))
				arcs.add(arc);
		}
		for (Arc arc : arcs) {
			try {
				ts.createArc(arc.getTarget(), arc.getSource(), reverseEvent);
			} catch (ArcExistsException e) {
				// Great! That's exactly what we want
			}
		}
		output.setReturnValue("lts", TransitionSystem.class, ts);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GENERATOR};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
