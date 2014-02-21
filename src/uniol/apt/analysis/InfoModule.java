/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

package uniol.apt.analysis;

import java.util.HashSet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Report some basic stats of the given PN.
 * 
 * @author Thomas Strathmann
 */
public class InfoModule extends AbstractModule {

	@Override
	public String getShortDescription() {
		return "Report the number of places, transitions, different transition labels," + System.lineSeparator() +
				"arcs, and tokens in the initial marking of the given Petri net.";
	}

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("num_places", Integer.class);
		outputSpec.addReturnValue("num_transitions", Integer.class);
		outputSpec.addReturnValue("num_labels", Integer.class);
		outputSpec.addReturnValue("num_arcs", Integer.class);
		outputSpec.addReturnValue("num_tokens", Integer.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		output.setReturnValue("num_places", Integer.class, pn.getPlaces().size());
		output.setReturnValue("num_transitions", Integer.class, pn.getTransitions().size());
		output.setReturnValue("num_arcs", Integer.class, pn.getEdges().size());

		int tokens = 0;
		for (Place p : pn.getPlaces()) {
			tokens += p.getInitialToken().getValue();
		}
		output.setReturnValue("num_tokens", Integer.class, tokens);
		
		HashSet<String> labels = new HashSet<>();
		for(Transition t : pn.getTransitions()) {
			labels.add(t.getLabel());
		}		
		output.setReturnValue("num_labels", Integer.class, labels.size());
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.PN };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
