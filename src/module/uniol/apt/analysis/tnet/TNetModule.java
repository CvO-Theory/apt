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

package uniol.apt.analysis.tnet;

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
 * Provide the tnet test as a module.
 *
 * @author Daniel
 */
@AptModule
public class TNetModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "tnet";
	}

	@Override
	public String getTitle() {
		return "T-Net";
	}

	@Override
	public String getShortDescription() {
		return "Check if a plain Petri net is a T-net";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "."
			+ " In a T-net, the preset and postset of any place has at most one entry"
			+ " (plus the net is plain).";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined", "plain");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("t_net", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("information", TNetResult.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);

		TNet tnet = new TNet(pn);
		boolean returnValue = tnet.testPlainTNet();
		TNetResult result = tnet.getResult();

		output.setReturnValue("t_net", Boolean.class, returnValue);
		output.setReturnValue("information", TNetResult.class, result);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
