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

package uniol.apt.analysis.totallyreachable;

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
 * Check if the given LTS is totally reachable.
 * @author Sören
 *
 */
@AptModule
public class TotallyReachableModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "totally_reachable";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("totally_reachable", Boolean.class, "totally_reachable",
				ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("state", State.class);
		outputSpec.addReturnValue("label", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);

		TotallyReachable totallyReachable = new TotallyReachable(ts);

		output.setReturnValue("totally_reachable", Boolean.class, totallyReachable.isTotallyReachable());
		output.setReturnValue("state", State.class, totallyReachable.getNode());
		if (!totallyReachable.isTotallyReachable()) {
			output.setReturnValue("label", String.class, totallyReachable.getLabel());
		}
	}

	@Override
	public String getTitle() {
		return "Check total reachability";
	}

	@Override
	public String getShortDescription() {
		return "Check if the given LTS is totally reachable";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
