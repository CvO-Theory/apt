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

package uniol.apt.analysis.factorization;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
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
 * Module that allows to decompose a LTS into asynchronous factors.
 *
 * @author Jonas Prellberg
 */
@AptModule
public class FactorizationModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "factorize";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The connected LTS that should factorized");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("is_product", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("factor1", TransitionSystem.class, ModuleOutputSpec.PROPERTY_RAW,
				ModuleOutputSpec.PROPERTY_FILE);
		outputSpec.addReturnValue("factor2", TransitionSystem.class, ModuleOutputSpec.PROPERTY_RAW,
				ModuleOutputSpec.PROPERTY_FILE);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		if (!Connectivity.isWeaklyConnected(ts)) {
			throw new ModuleException("LTS factorization can only be performed on connected LTS");
		}

		Factorization factorization = new Factorization(ts);
		boolean result = factorization.hasFactors();
		output.setReturnValue("is_product", Boolean.class, result);
		if (result) {
			output.setReturnValue("factor1", TransitionSystem.class, factorization.getFactor1());
			output.setReturnValue("factor2", TransitionSystem.class, factorization.getFactor2());
		}
	}

	@Override
	public String getShortDescription() {
		return "Decompose a LTS into its factors (if possible)";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
