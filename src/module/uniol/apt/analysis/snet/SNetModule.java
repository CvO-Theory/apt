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

package uniol.apt.analysis.snet;

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
 * Provide the snet test as a module.
 *
 * @author Daniel
 */
@AptModule
public class SNetModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "snet";
	}

	@Override
	public String getTitle() {
		return "S-Net";
	}

	@Override
	public String getShortDescription() {
		return "Check if a plain Petri net is an S-net";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + "."
			+ " In a S-net, the preset and postset of any transition has at most one entry"
			+ " (plus the net is plain).";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined", "plain");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("s_net", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("information", SNetResult.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);

		SNet snet = new SNet(pn);
		boolean returnValue = snet.testPlainSNet();
		SNetResult result = snet.getResult();

		output.setReturnValue("s_net", Boolean.class, returnValue);
		output.setReturnValue("information", SNetResult.class, result);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
