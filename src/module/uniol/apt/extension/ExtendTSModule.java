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

package uniol.apt.extension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.lts.extension.ExtendTransitionSystem;
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
 * @author Renke Grunwald
 *
 */
@AptModule
public class ExtendTSModule extends AbstractModule implements Module {
	@Override
	public String getName() {
		return "extend_lts";
	}

	@Override
	public String getShortDescription() {
		return "Generate extensions to a given LTS that satisfy certain properties.";
	}

	@Override
	public String getLongDescription() {
		return "Generate extensions to a given LTS that are reversible, persistent. Also, all smallest cycles "
			+ "share the same parikh vector. This module can run in three different modes: It can generate "
			+ "the next possible extension to the given LTS, the next extension that satisfies the above "
			+ "properties or the next satisfying extension that is also minimal among satisfying "
			+ "extensions.";
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that is extended");
		inputSpec.addParameter("g", Integer.class, "Maximum number of new nodes");
		inputSpec.addParameter("mode", ExtendMode.class, "The mode (next, next_valid, next_minimal_valid)");
		// TODO: Maybe use File.class and create a transformation
		inputSpec.addParameter("state_file", String.class, "The file to load/save the state from/to");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("valid", Boolean.class);
		outputSpec.addReturnValue("minimal", Boolean.class);
		outputSpec.addReturnValue("extended_lts", TransitionSystem.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem ts = input.getParameter("lts", TransitionSystem.class);
		int g = input.getParameter("g", Integer.class);
		ExtendMode mode = input.getParameter("mode", ExtendMode.class);
		String stateFileName = input.getParameter("state_file", String.class);

		File stateFile = new File(stateFileName);

		// Alphabet size, TODO: Use getAlphabet().size() when it works
		List<String> labels = new ArrayList<String>();
		for (Arc e : ts.getEdges()) {
			if (!labels.contains(e.getLabel()))
				labels.add(e.getLabel());
		}

		// Number of possible edges
		int codeLength = (ts.getNodes().size() + g);
		codeLength *= codeLength  * labels.size();
		ExtendStateFile state = new ExtendStateFile(stateFile, codeLength);

		ExtendTransitionSystem extender;
		BitSet currentCode = null;

		if (stateFile.exists()) {
			try {
				state.parse();
			} catch (IOException e) {
				throw new ModuleException("Can't parse state file");
			}

			extender = new ExtendTransitionSystem(ts, g, state.getMinimalCodes());
			currentCode = state.getCurrentCode();
		} else {
			extender = new ExtendTransitionSystem(ts, g);
		}

		if (currentCode != null) {
			switch (mode) {
				case Next:
					extender.findNext();
					break;
				case NextValid:
					extender.findNextValid(currentCode);
					break;
				case NextMinimalValid:
					extender.findNextMinimal(currentCode);
					break;
			}
		} else {
			switch (mode) {
				case Next:
					extender.findNext();
					break;
				case NextValid:
					extender.findNextValid();
					break;
				case NextMinimalValid:
					extender.findNextMinimal();
					break;
			}
		}

		BitSet newCode = extender.getLastGenerated();
		boolean valid = extender.isLastGeneratedValid();
		boolean minimal = false;

		if (valid) {
			minimal = extender.isLastGeneratedMinimal();
		}

		state.setMinimalCodes(extender.getListOfMinimals());
		state.setCurrentCode(newCode);

		TransitionSystem extendedTS = extender.buildLTS(newCode);

		try {
			state.render();
		} catch (IOException e) {
			throw new ModuleException("Can't render state file");
		}

		output.setReturnValue("extended_lts", TransitionSystem.class, extendedTS);
		output.setReturnValue("valid", Boolean.class, valid);

		if (valid) {
			output.setReturnValue("minimal", Boolean.class, minimal);
		} else {
			output.setReturnValue("minimal", Boolean.class, null);
		}
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
