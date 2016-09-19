/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Members of the project group APT
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

package uniol.apt.analysis;

import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.TransitionFireException;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.language.FiringSequence;
import uniol.apt.analysis.language.Word;
import uniol.apt.module.AbstractInterruptibleModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Try to fire a given sequence in a Petri net.
 *
 * @author Uli Schlachter
 */
@AptModule
public class FireSequenceModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Try to fire a given firing sequence on a Petri net.";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". This module tries to fire a given sequence of transitions in a"
			+ " Petri net. It will report the longest enabled prefix of the sequence and print the marking"
			+ " that is reached at the end.";
	}

	@Override
	public String getName() {
		return "fire_sequence";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("sequence", Word.class, "Sequence that should be fired");
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("reached_marking", Marking.class);
		outputSpec.addReturnValue("fired_sequence", FiringSequence.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		Word sequence = input.getParameter("sequence", Word.class);
		Marking marking = pn.getInitialMarking();
		FiringSequence fired = new FiringSequence();
		boolean success = true;

		try {
			for (String name : sequence) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				Transition trans = pn.getTransition(name);
				marking = trans.fire(marking);
				fired.add(trans);
			}
			// Unset list in case everything worked successfully
			fired = null;
		} catch (NoSuchNodeException e) {
			String msg = "No transition named '" + e.getNodeId() + "' exists";
			throw new ModuleException(msg, e);
		} catch (TransitionFireException e) {
			success = false;
		}

		output.setReturnValue("success", Boolean.class, success);
		output.setReturnValue("reached_marking", Marking.class, marking);
		output.setReturnValue("fired_sequence", FiringSequence.class, fired);
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.PN };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
