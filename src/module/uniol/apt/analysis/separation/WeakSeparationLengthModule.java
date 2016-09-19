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

package uniol.apt.analysis.separation;

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
 * Provide weak separation for all firable sequences up to a max length as a module.
 *
 * @author Daniel
 */
@AptModule
public class WeakSeparationLengthModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getName() {
		return "weak_separation_length";
	}

	@Override
	public String getTitle() {
		return "Weak separation with respect to k (length)";
	}

	@Override
	public String getShortDescription() {
		return "Check if all sequences up to a length are weakly k-separable";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\nExample calls:\n\n"
			+ "To check all firing sequences to up a length of 3:\n"
			+ "apt weak_separation_length petri_net.apt 3\n\n"
			+ "To check all firing sequences to up a length of 3 and set k to 2:\n"
			+ "apt weak_separation_length petri_net.apt 3 2\n\n"
			+ "To check all firing sequences to up a length of 3 and set k to 2 with more output:\n"
			+ "apt weak_separation_length petri_net.apt 3 2 verbose\n";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addParameter("length", Integer.class, "Maximum length of firing sequences");
		inputSpec.addOptionalParameterWithDefault("k", Integer.class, 0, "0", "Value of k");
		inputSpec.addOptionalParameterWithDefault("verbose", String.class, "", "", "Optional more output");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("result", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String returnValue = "";
		boolean verbose = false;

		// get k
		Integer k = input.getParameter("k", Integer.class);

		if (k < 0) {
			throw new ModuleException("k must be greater than zero.");
		}

		// interpret verbose
		String stringVerbose = input.getParameter("verbose", String.class);
		if (stringVerbose.equals("verbose")) {
			verbose = true;
		}

		// get length
		int maxLength = input.getParameter("length", Integer.class);

		if (maxLength < 0) {
			throw new ModuleException("max_length must be greater than or equal zero.");
		}

		// get petri net
		PetriNet pn = input.getParameter("pn", PetriNet.class);

		// lets try to separate the petri net
		Separation separation = new Separation(pn, false, k, maxLength, verbose);
		returnValue = separation.getOutputLog();

		output.setReturnValue("result", String.class, returnValue);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
