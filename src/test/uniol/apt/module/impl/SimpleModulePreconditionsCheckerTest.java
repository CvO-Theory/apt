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

package uniol.apt.module.impl;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import uniol.apt.module.AbstractModule;
import uniol.apt.module.AbstractModuleRegistry;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Renke Grunwald
 *
 */
public class SimpleModulePreconditionsCheckerTest {

	@Test
	public void testIfPreconditionIsMet() {
		final Module checkingModule = new CheckingModule(true);
		final Module checkedModule = new CheckedModule();

		SimpleModulePreconditionsChecker checker = new SimpleModulePreconditionsChecker();
		ModuleRegistry registry = new AbstractModuleRegistry() {
			{
				registerModules(checkedModule, checkingModule);
			}
		};

		Something something = new Something();
		assertTrue(checker.check(registry, checkedModule, something).isEmpty());
	}

	@Test
	public void testIfPreconditionIsNotMet() {
		final Module checkingModule = new CheckingModule(false);
		final Module checkedModule = new CheckedModule();

		SimpleModulePreconditionsChecker checker = new SimpleModulePreconditionsChecker();
		ModuleRegistry registry = new AbstractModuleRegistry() {
			{
				registerModules(checkedModule, checkingModule);
			}
		};

		Something something = new Something();

		assertFalse(checker.check(registry, checkedModule, something).isEmpty());
		assertEquals(checker.check(registry, checkedModule, something).get(0).getName(), "something");
	}

	private class Something extends Object {
	};

	static private class CheckingModule extends AbstractModule {

		private boolean isMet;

		public CheckingModule(boolean isMet) {
			this.isMet = isMet;
		}

		@Override
		public String getName() {
			return "checking_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
			inputSpec.addParameter("something", Something.class, "");
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
			outputSpec.addReturnValue("awesome", Boolean.class, "awesome");
			outputSpec.addReturnValue("unrelated", Boolean.class);
			outputSpec.addReturnValue("superfluous", Object.class);
		}

		@Override
		public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
			output.setReturnValue("awesome", Boolean.class, isMet);
			output.setReturnValue("unrelated", Boolean.class, false);
			output.setReturnValue("superfluous", Object.class, new Object());
		}

		@Override
		public Category[] getCategories() {
			return new Category[]{Category.MISC};
		}
	}

	static private class CheckedModule extends AbstractModule {

		@Override
		public String getName() {
			return "checked_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
			inputSpec.addParameter("something", Something.class, "", "awesome");
			inputSpec.addParameter("anything", Something.class, "");
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
		}

		@Override
		public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		}

		@Override
		public Category[] getCategories() {
			return new Category[]{Category.MISC};
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
