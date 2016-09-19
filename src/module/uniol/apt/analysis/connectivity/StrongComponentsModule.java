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

package uniol.apt.analysis.connectivity;

import java.util.Set;

import uniol.apt.adt.IGraph;
import uniol.apt.adt.INode;
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
 * Provide the strong components test as a module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class StrongComponentsModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Find the strongly connected components of a Petri net or LTS";
	}

	@Override
	public String getName() {
		return "strong_components";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("graph", IGraph.class, "The graph that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("strong_components", Components.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		IGraph<?, ?, ?> graph = input.getParameter("graph", IGraph.class);
		Set<? extends Set<? extends INode<?, ?, ?>>> components = run(graph);
		output.setReturnValue("strong_components", Components.class, new Components(components));
	}

	@SuppressWarnings("unchecked")
	private static <G extends IGraph<G, ?, N>, N extends INode<G, ?, N>>
			Set<? extends Set<? extends INode<?, ?, ?>>> run(IGraph<?, ?, ?> graph) {
		return Connectivity.getStronglyConnectedComponents((G) graph);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
