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

package uniol.apt.analysis.fcnet;

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
 * This module tests if a plain Petri net ist restricted-free-choice. That is:
 * <code>
 *              \forall t_1,t_s\in T\colon{}^\bullet t_1\cap{}^\bullet t_2\neq\es\impl|{}^\bullet t_1|=
 *              |{}^\bullet t_2|=1
 * </code>
 * @author Manuel Gieseking
 */
@AptModule
public class FCNetModule extends AbstractInterruptibleModule implements InterruptibleModule {

	private static final String SHORTDESCRIPTION = "Check if a Petri net is restricted-free-choice";
	private static final String LONGDESCRIPTION = SHORTDESCRIPTION + ". That is:\n"
		+ "\\forall t_1,t_s\\in T\\colon{}^\\bullet t_1\\cap{}^\\bullet t_2\\neq\\es\n"
		+ "\\impl|{}^\\bullet t_1|=|{}^\\bullet t_2|=1";
	private static final String TITLE = "restricted-free-choice";
	private static final String NAME = "rfc";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("net", PetriNet.class, "The Petri net that should be examined", "plain");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("rfc", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("net", PetriNet.class);
		FCNet net = new FCNet(pn);
		output.setReturnValue("rfc", Boolean.class, net.check());
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return SHORTDESCRIPTION;
	}

	@Override
	public String getLongDescription() {
		return LONGDESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
