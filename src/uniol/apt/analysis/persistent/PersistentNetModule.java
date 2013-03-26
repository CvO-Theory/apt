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

package uniol.apt.analysis.persistent;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Vincent Göbel
 *
 */
public class PersistentNetModule extends AbstractModule {

	@Override
	public String getName() {
		return "pn_persistent";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("persistent", Boolean.class, "persistent", ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("marking", Marking.class);
		outputSpec.addReturnValue("first_label", String.class);
		outputSpec.addReturnValue("second_label", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {

		PetriNet pn = input.getParameter("pn", PetriNet.class);

		PersistentNet persistent = new PersistentNet(pn);
		persistent.check();

		output.setReturnValue("persistent", Boolean.class, persistent.isPersistent());
		output.setReturnValue("marking", Marking.class, persistent.getMarking());
		output.setReturnValue("first_label", String.class, persistent.getLabel1());
		output.setReturnValue("second_label", String.class, persistent.getLabel2());
	}

	@Override
	public String getTitle() {
		return "Persistent";
	}

	@Override
	public String getShortDescription() {
		return "Check if the given Petri net is persistent";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
