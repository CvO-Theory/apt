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
public class ExtendTSModule extends AbstractExtendTSModule implements Module {
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
	void addRequire(ModuleInputSpec inputSpec) { /* empty */ }

	@Override
	void initExtender(ExtendTransitionSystem extender, ModuleInput input) { /* empty */ }

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
