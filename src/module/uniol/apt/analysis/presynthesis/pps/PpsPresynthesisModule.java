/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.presynthesis.pps;

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
 * Exposes {@link PpsPropertyChecker} as a module.
 *
 * @author Jonas Prellberg
 */
@AptModule
public class PpsPresynthesisModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Performs pps-presynthesis checks on a transition system";
	}

	@Override
	public String getLongDescription() {
		return "Checks if it is infeasible to synthesize a plain, pure "
				+ "and safe Petri net from the given transition system. "
				+ "The following properties are checked:\n\n"
				+ "(B) If M'[a>M and M''[b>M then [b>M' <=> [a>M''\n"
				+ "(D) If M[a> and M[b> then for any K: (K[ab> <=> K[ba>)\n"
				+ "(F) If M[wv> and M[vw> and M[wc> and M[vc> then M[wvc>M' and M[vwc>M' and M[c>\n\n"
				+ "  with transitions a, b, c and sequences v, w.";
	}

	@Override
	public String getName() {
		return "ppspresynthesis";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "Input LTS");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("infeasible", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("violatedProperty", PpsPropertyResult.class, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		PpsPropertyChecker ppsChecker = new PpsPropertyChecker();
		if (!ppsChecker.hasProperties(ts)) {
			output.setReturnValue("infeasible", Boolean.class, true);
			output.setReturnValue("violatedProperty", PpsPropertyResult.class, ppsChecker.getResult());
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
