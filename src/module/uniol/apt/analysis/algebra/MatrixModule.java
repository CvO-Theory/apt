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

package uniol.apt.analysis.algebra;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Björn von der Linde
 */
@AptModule
public class MatrixModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "matrices";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addOptionalParameterWithoutDefault("format", MatrixFileFormat.class,
				"The file format of the printed matrices");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("backward_matrix", String[][].class);
		outputSpec.addReturnValue("forward_matrix", String[][].class);
		outputSpec.addReturnValue("incidence_matrix", String[][].class);

		outputSpec.addReturnValue("output", String.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output)
		throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		MatrixFileFormat format = input.getParameter("format", MatrixFileFormat.class);
		Matrix matrix = new Matrix(pn);


		if (format == null) {
			output.setReturnValue("backward_matrix", String[][].class, matrix.getStringBackward());
			output.setReturnValue("forward_matrix", String[][].class, matrix.getStringForward());
			output.setReturnValue("incidence_matrix", String[][].class, matrix.getStringIncidence());
			return;
		}

		switch (format) {
			case MATLAB:
				output.setReturnValue("output", String.class, matrix.getMatLabMatrices() + "\n");
				return;
			case R:
				output.setReturnValue("output", String.class, matrix.getRMatrices() + "\n");
				return;
			default:
				throw new ModuleException("Unknown format: " + format);
		}
	}

	@Override
	public String getShortDescription() {
		return "Calculate forward, backward, and incidence matrices.";
	}

	@Override
	public String getLongDescription() {
		return "Calculate forward, backward, and incidence matrices. The matrices can also be printed in the "
			+ "R and MATLAB format.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
