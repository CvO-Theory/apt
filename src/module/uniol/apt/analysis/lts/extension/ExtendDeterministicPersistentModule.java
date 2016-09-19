/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

package uniol.apt.analysis.lts.extension;

import java.util.Collection;

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
 * Module to extend a transition system to a deterministic persistent transition system.
 *
 * @author vsp
 */
@AptModule
public class ExtendDeterministicPersistentModule extends AbstractInterruptibleModule implements InterruptibleModule {
	@Override
	public String getName() {
		return "extend_deterministic_persistent";
	}

	@Override
	public String getShortDescription() {
		return "Extend a transition system to an deterministic persistent transition system.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("ts", TransitionSystem.class,
			"The deterministic transition system that should be extended");
		inputSpec.addOptionalParameterWithDefault("rounds", Integer.class, Integer.MAX_VALUE, "unlimited",
			"Maximum allowed number of processing rounds that add new states");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("needed_arcs", Collection.class);
		outputSpec.addReturnValue("complete", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("extended_ts", TransitionSystem.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("ts", TransitionSystem.class);
		int maxRounds = input.getParameter("rounds", Integer.class);
		Collection<?> neededArcs = new ExtendDeterministicPersistent().extendTs(ts, maxRounds);
		if (!neededArcs.isEmpty())
			output.setReturnValue("needed_arcs", Collection.class, neededArcs);
		output.setReturnValue("complete", Boolean.class, neededArcs.isEmpty());
		output.setReturnValue("extended_ts", TransitionSystem.class, ts);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
