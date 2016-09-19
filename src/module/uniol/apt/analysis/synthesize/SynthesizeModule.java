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

package uniol.apt.analysis.synthesize;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AptModule;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the net synthesis as a module.
 * @author Uli Schlachter
 */
@AptModule
public class SynthesizeModule extends AbstractSynthesizeModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Synthesize a Petri Net from a transition system";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ getOptionsDescription("", "") + "\n\nExample calls:\n\n"
			+ " apt " + getName() + " none lts.apt\n"
			+ " apt " + getName() + " 3-bounded lts.apt\n"
			+ " apt " + getName() + " pure,safe lts.apt\n"
			+ " apt " + getName() + " upto-language-equivalence lts.apt\n";
	}

	@Override
	public String getName() {
		return "synthesize";
	}

	@Override
	protected void requireExtra(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class,
				"The LTS that should be synthesized to a Petri Net");
	}

	@Override
	protected void provideExtra(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("failedStateSeparationProblems", String.class);
		outputSpec.addReturnValue("failedEventStateSeparationProblems", String.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		SynthesizePN synthesize = runSynthesis(new ReturnTS(ts), input, output);

		if (!synthesize.wasSuccessfullySeparated()) {
			Set<Set<String>> failedSSP = new HashSet<>();
			for (Set<State> group : synthesize.getFailedStateSeparationProblems()) {
				Set<String> states = new TreeSet<>();
				for (State state : group)
					states.add(state.getId());
				failedSSP.add(states);
			}
			output.setReturnValue("failedStateSeparationProblems", String.class, failedSSP.toString());

			Map<String, Set<String>> failedESSP = new TreeMap<>();
			for (Map.Entry<String, Set<State>> entry :
					synthesize.getFailedEventStateSeparationProblems().entrySet()) {
				failedESSP.put(entry.getKey(), getStateIDs(entry.getValue()));
			}
			output.setReturnValue("failedEventStateSeparationProblems",
					String.class, failedESSP.toString());
		}
	}

	static protected class ReturnTS implements TransitionSystemForOptions {
		final private TransitionSystem ts;

		public ReturnTS(TransitionSystem ts) {
			this.ts = ts;
		}

		@Override
		public Collection<String> supportedExtraOptions() {
			return Collections.emptyList();
		}

		@Override
		public TransitionSystem getTS(Collection<String> enabledOptions) {
			return ts;
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
