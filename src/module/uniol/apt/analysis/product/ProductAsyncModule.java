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

package uniol.apt.analysis.product;

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
 * A module to compute the asynchronous product of two LTS.
 *
 * @author Jonas Prellberg
 *
 */
@AptModule
public class ProductAsyncModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "product_async";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts1", TransitionSystem.class, "The first LTS of the product");
		inputSpec.addParameter("lts2", TransitionSystem.class, "The second LTS of the product");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("product", TransitionSystem.class, ModuleOutputSpec.PROPERTY_RAW,
				ModuleOutputSpec.PROPERTY_FILE);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts1 = input.getParameter("lts1", TransitionSystem.class);
		TransitionSystem ts2 = input.getParameter("lts2", TransitionSystem.class);

		Product product = new Product(ts1, ts2);
		output.setReturnValue("product", TransitionSystem.class, product.getAsyncProduct());
	}

	@Override
	public String getShortDescription() {
		return "Compute the asynchronous product of two LTS";
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
