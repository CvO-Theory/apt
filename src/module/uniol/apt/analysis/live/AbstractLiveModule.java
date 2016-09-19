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

package uniol.apt.analysis.live;

import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.NoSuchTransitionException;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.exception.ModuleException;

/**
 * Base class used by the various liveness testing modules
 * @author Uli Schlachter, vsp
 */
abstract public class AbstractLiveModule extends AbstractInterruptibleModule implements InterruptibleModule {
	@Override
	final public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addOptionalParameterWithoutDefault("transition", String.class,
			"A transition that should be checked for liveness");
	}

	abstract protected void findNonLiveTransition(ModuleOutput output, PetriNet pn) throws ModuleException;
	abstract protected void checkTransitionLiveness(ModuleOutput output, PetriNet pn, Transition transition)
		throws ModuleException;

	@Override
	final public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		String id = input.getParameter("transition", String.class);
		if (id == null) {
			findNonLiveTransition(output, pn);
		} else {
			Transition transition;
			try {
				transition = pn.getTransition(id);
			} catch (NoSuchNodeException e) {
				throw new NoSuchTransitionException(pn, e);
			}

			checkTransitionLiveness(output, pn, transition);
		}
	}

	@Override
	final public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
