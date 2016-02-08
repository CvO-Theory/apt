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

package uniol.apt.check;

import uniol.apt.adt.pn.PetriNet;
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
 * Provide the check as a module.
 *
 * @author Daniel
 *
 */
@AptModule
public class CheckModule extends AbstractModule implements Module {

	@Override
	public String getName() {
		return "check";
	}

	@Override
	public String getTitle() {
		return "Check";
	}

	@Override
	public String getShortDescription() {
		return "Search for a Petri net which fulfills the given attributes";
	}

	@Override
	public String getLongDescription() {
		return "Supported generators:\n"
			+ "  bitnet, chance, cycle, quadPhilgen, smartchance, tnetgen2, tnetgen3, triPhilgen\n\n"
			+ "Supported attributes:\n"
			+ "  bounded, !bounded, freeChoice, !freeChoice, isolated, !isolated, k-marking, !k-marking, "
			+ "persistent, !persistent, plain, !plain, pure, !pure, reversible, !reversible, "
			+ "snet, !snet, !strongly_k-separable, stronglyLive, !stronglyLive, tnet, !tnet, "
			+ "!weakly_k-separable\n"
			+ "(Choose a natural number for k)\n\n"
			+ "Short description of each generator:\n"
			+ "  bitnet      : Generates a bit net with varying number of bits.\n"
			+ "  chance      : Generates a Petri net through random with a chance of finding a valid"
			+ " net by reacting to result of last generated Petri net.\n"
			+ "  cycle       : Generates a cycle net with varying size of the cycle.\n"
			+ "  quadPhilgen : Generates a philosoph net with varying number of philosophs "
			+ "and four states.\n"
			+ "  smartchance : Like chance but more complex reaction to analyse results of last"
			+ " generated Petri nets.\n"
			+ "  tnetgen2    : Generates t-nets with varying number of maximum places and tokens.\n"
			+ "                (maximum transitions will be set to 'maximum places * 2')\n"
			+ "  tnetgen3    : Generates t-nets with varying number of maximum places, "
			+ "transitions and tokens.\n"
			+ "  triPhilgen  : Generates a philosoph net with varying number of philosophs "
			+ "and three states.\n"
			+ "\n"
			+ "For detailed descriptions see analysis modules and generator packet.\n\n"
			+ "Example calls:\n"
			+ "  apt check 5 chance 'snet' '!tnet'\n"
			+ "  apt check 10 chance '2-marking'\n";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("maxSeconds", Integer.class, "Max. execution time in seconds");
		inputSpec.addParameter("generator", String.class, "Generator");
		inputSpec.addOptionalParameterWithoutDefault("attribute1", String.class, "Attribute 1");
		inputSpec.addOptionalParameterWithoutDefault("attribute2", String.class, "Attribute 2");
		inputSpec.addOptionalParameterWithoutDefault("attribute3", String.class, "Attribute 3");
		inputSpec.addOptionalParameterWithoutDefault("attribute4", String.class, "Attribute 4");
		inputSpec.addOptionalParameterWithoutDefault("attribute5", String.class, "Attribute 5");
		inputSpec.addOptionalParameterWithoutDefault("attribute6", String.class, "Attribute 6");
		inputSpec.addOptionalParameterWithoutDefault("attribute7", String.class, "Attribute 7");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("\n//Petri net found", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {

		Integer intMaxSeconds = input.getParameter("maxSeconds", Integer.class);
		String strGenerator = input.getParameter("generator", String.class);
		String strAttribute1 = input.getParameter("attribute1", String.class);
		String strAttribute2 = input.getParameter("attribute2", String.class);
		String strAttribute3 = input.getParameter("attribute3", String.class);
		String strAttribute4 = input.getParameter("attribute4", String.class);
		String strAttribute5 = input.getParameter("attribute5", String.class);
		String strAttribute6 = input.getParameter("attribute6", String.class);
		String strAttribute7 = input.getParameter("attribute7", String.class);

		Check c = new Check();

		c.setGenerator(strGenerator);

		// loop....
		if (strAttribute1 != null) {
			c.addAttribute(strAttribute1);
		}
		if (strAttribute2 != null) {
			c.addAttribute(strAttribute2);
		}
		if (strAttribute3 != null) {
			c.addAttribute(strAttribute3);
		}
		if (strAttribute4 != null) {
			c.addAttribute(strAttribute4);
		}
		if (strAttribute5 != null) {
			c.addAttribute(strAttribute5);
		}
		if (strAttribute6 != null) {
			c.addAttribute(strAttribute6);
		}
		if (strAttribute7 != null) {
			c.addAttribute(strAttribute7);
		}

		PetriNet pn = c.search(intMaxSeconds);

		String returnString = null;

		if (pn == null) {
			returnString = "\nUnfortunately no Petri net was found -- ";
			returnString += c.getCounter() + " Petri nets have been tested.\n";

			if (c.getBestMatch() != null) {
				returnString += "A Petri net with following attributes was found:" + c.getBestMatch();
			}
		} else {
			try {
				returnString = "\n" + new AptPNRenderer().render(pn);
			} catch (ModuleException e) {
				returnString = "\nUnable to render found Petri net.";
			}
		}

		output.setReturnValue("\n//Petri net found", String.class, returnString);
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
