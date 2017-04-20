/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis.language;

import static uniol.apt.adt.automaton.AutomatonToRegularExpression.automatonToRegularExpression;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.fromLTS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.connectivity.Connectivity;
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
 * Convert a Petri net or LTS into a language-equivalent regular expression
 * @author Uli Schlachter
 */
@AptModule
public class ToRegularExpressionModule extends AbstractInterruptibleModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Create a language-equivalent (up to prefix creation) regular expression";
	}

	@Override
	public String getName() {
		return "to_regular_expression";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn_or_ts", PetriNetOrTransitionSystem.class,
			"A Petri net or transition system whose language should be used");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("regular_expression", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNetOrTransitionSystem arg = input.getParameter("pn_or_ts", PetriNetOrTransitionSystem.class);
		TransitionSystem ts = arg.getReachabilityLTS();
		output.setReturnValue("regular_expression", String.class, toRegularExpression(ts));
	}

	// To get shorter regular expressions, we only choose some nodes as final states for the LTS. In fact we choose
	// one node from each strongly connected component which cannot reach any other component. This guarantees that
	// the prefix language of the regular expression will be the prefix language of the lts.
	private static Set<State> chooseFinalNodes(TransitionSystem ts) {
		Set<? extends Set<State>> components = Connectivity.getStronglyConnectedComponents(ts);
		Iterator<? extends Set<State>> it = components.iterator();
		while (it.hasNext()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Set<State> component = it.next();
			boolean skip = false;

			for (State node : component) {
				for (State following : node.getPostsetNodes())
					if (!component.contains(following)) {
						skip = true;
						break;
					}
				if (skip)
					break;
			}
			if (skip)
				it.remove();
		}
		Set<State> result = new HashSet<>();
		for (Set<State> component : components)
			result.add(ts.getNode(component.iterator().next().getId()));
		return result;
	}

	private static String toRegularExpression(TransitionSystem ts) {
		Set<State> finalStates = chooseFinalNodes(ts);
		return "@(" + automatonToRegularExpression(fromLTS(ts, finalStates)) + ")";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN, Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
