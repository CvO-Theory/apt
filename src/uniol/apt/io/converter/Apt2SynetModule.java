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

package uniol.apt.io.converter;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.impl.SynetLTSRenderer;
import uniol.apt.io.renderer.impl.SynetPNRenderer;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * A Module for converting a file in apt format to a file in synet format.
 *
 * @author Sören
 */
public class Apt2SynetModule extends AbstractModule {

	private final static String DESCRIPTION = "Convert APT format to Synet format";
	private final static String TITLE = "Apt2Synet";
	private final static String NAME = "apt2synet";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn_or_ts", PetriNetOrTransitionSystem.class,
				"The Petri net or LTS that should be converted");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("output_filename", String.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem pnOrTs = input.getParameter("pn_or_ts", PetriNetOrTransitionSystem.class);

		PetriNet pn = pnOrTs.getNet();
		TransitionSystem ts = pnOrTs.getTs();

		String synetPN = null;
		if(pn != null) {
			synetPN = new SynetPNRenderer().render(pn);
		} else if (ts != null) {
			synetPN = new SynetLTSRenderer().render(ts);
		}
		if (synetPN == null) {
			throw new ModuleException("input_type has to be ts or pn");
		}
		output.setReturnValue("output_filename", String.class, synetPN);
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return DESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.CONVERTER };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
