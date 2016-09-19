/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.sum;

import uniol.apt.adt.pn.PetriNet;
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
 * A module to compute the asynchronous sum of two PN.
 *
 * @author Jonas Prellberg
 *
 */
@AptModule
public class SumAsyncModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "sum_async";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn1", PetriNet.class, "The first PN of the sum");
		inputSpec.addParameter("pn2", PetriNet.class, "The second PN of the sum");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("sum", PetriNet.class, ModuleOutputSpec.PROPERTY_RAW,
				ModuleOutputSpec.PROPERTY_FILE);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn1 = input.getParameter("pn1", PetriNet.class);
		PetriNet pn2 = input.getParameter("pn2", PetriNet.class);

		Sum sum = new Sum(pn1, pn2);
		output.setReturnValue("sum", PetriNet.class, sum.getAsyncSum());
	}

	@Override
	public String getShortDescription() {
		return "Compute the synchronous sum of two PN";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.PN };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
