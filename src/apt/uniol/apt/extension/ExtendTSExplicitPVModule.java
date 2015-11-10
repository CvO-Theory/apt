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

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.analysis.lts.extension.ExtendTransitionSystem;
import uniol.apt.module.Category;
import uniol.apt.module.AptModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;

/**
 * @author Renke Grunwald
 *
 */
@AptModule
public class ExtendTSExplicitPVModule extends ExtendTSModule implements Module {
	@Override
	public String getName() {
		return "extend_pv_lts";
	}

	@Override
	public String getShortDescription() {
		return "Works like the extend_lts, but with a user specified parikh vector.";
	}

	@Override
	public String getLongDescription() {
		return "Generates extensions to a given LTS that are reversible, persistent. Also, all smallest cycles "
			+ "share the same parikh vector, which is provided by the user. This module can run in three "
			+ "different modes: It can generate the next possible extension to the given LTS, the next "
			+ "extension that satisfies the above properties or the next satisfying extension that is also "
			+ "minimal among satisfying extensions.";
	}

	@Override
	public void addRequire(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pv", ParikhVector.class, "The Parikh vector");
	}

	@Override
	protected void initExtender(ExtendTransitionSystem extender, ModuleInput input) {
		extender.setGivenPV(input.getParameter("pv", ParikhVector.class));
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.LTS};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
