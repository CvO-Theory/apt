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

package uniol.apt.generator.module;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.isolated.IsolatedTransitionsIterable;
import uniol.apt.generator.marking.MarkingIterable;
import uniol.apt.generator.tnet.TNetGenerator;
import uniol.apt.io.renderer.impl.AptPNRenderer;

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
 * Module for generating T-nets.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class TNetGeneratorModule extends AbstractModule implements Module {

	@Override
	public String getShortDescription() {
		return "Construct all T-nets up to a given size.";
	}

	@Override
	public String getName() {
		return "tnet_generator";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("np", Integer.class, "The maximum number of place for the Petri nets");
		inputSpec.addParameter("nt", Integer.class, "The maximum number of transitions for the Petri nets");
		inputSpec.addParameter("m", Integer.class, "The maximum number of token for the Petri nets");
		inputSpec.addOptionalParameterWithDefault("directory", String.class, "output", "output",
			"Directory for writing the results to");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("result", String.class);
	}

	private String renderNet(PetriNet arg) throws ModuleException {
		return new AptPNRenderer().render(arg);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		Integer np = input.getParameter("np", Integer.class);
		Integer nt = input.getParameter("nt", Integer.class);
		Integer m = input.getParameter("m", Integer.class);
		Iterable<PetriNet> iter = new TNetGenerator(np, nt);
		iter = new IsolatedTransitionsIterable(iter, nt);
		iter = new MarkingIterable(iter, m);

		String directory = input.getParameter("directory", String.class);
		File dir = new File(directory);
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new ModuleException("Could not create directory '" + directory + "'");
		}

		int counter = 0;
		for (PetriNet pn : iter) {
			File name = new File(dir, Integer.toString(++counter) + ".apt");
			try {
				FileUtils.write(name, renderNet(pn));
			} catch (IOException e) {
				throw new ModuleException("Error writing to file: " + e.getMessage());
			}
		}

		StringBuilder result = new StringBuilder("Wrote ");
		result.append(counter).append(" Petri nets to directory '").append(directory).append("'");
		output.setReturnValue("result", String.class, result.toString());
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GENERATOR};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
