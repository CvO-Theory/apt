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

package uniol.apt.analysis.coverability;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the coverability graph as a module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class CoverabilityModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Compute a Petri net's coverability graph";
	}

	@Override
	public String getName() {
		return "coverability_graph";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("lts", TransitionSystem.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
		outputSpec.addReturnValue("reachability_graph", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
	}

	/**
	 * Get the graph for a given net
	 * @param pn The Petri net to look at
	 * @return The coverability graph.
	 */
	protected CoverabilityGraph getGraph(PetriNet pn) {
		return CoverabilityGraph.get(pn);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		TransitionSystem result = getGraph(pn).toCoverabilityLTS();
		boolean isReachability = true;
		for (State node : result.getNodes()) {
			CoverabilityGraphNode coverNode =
				(CoverabilityGraphNode) node.getExtension(CoverabilityGraphNode.class.getName());
			if (coverNode.getMarking().hasOmega()) {
				isReachability = false;
			}
			/* Put the node's marking as a comment into the file */
			node.putExtension("marking", coverNode.getMarking().toString(),
					ExtensionProperty.WRITE_TO_FILE);
		}
		output.setReturnValue("lts", TransitionSystem.class, result);
		output.setReturnValue("reachability_graph", Boolean.class, isReachability);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
