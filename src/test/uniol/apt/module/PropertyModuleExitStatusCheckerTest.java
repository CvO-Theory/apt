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

package uniol.apt.module;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.impl.ExitStatus;
import uniol.apt.module.impl.ModuleInvoker;

/**
 * @author Renke Grunwald
 *
 */
public class PropertyModuleExitStatusCheckerTest {

	@Test
	public void testNoReturnValues() throws ModuleException {
		Module module = new NoReturnValuesModule();

		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> values = invoker.invoke(module);

		ModuleExitStatusChecker checker = new PropertyModuleExitStatusChecker();

		assertEquals(ExitStatus.SUCCESS, checker.check(module, values));
	}

	private static class NoReturnValuesModule extends AbstractModule {

		@Override
		public String getName() {
			return "no_properties_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
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

	@Test
	public void testNoSuccessProperties() throws ModuleException {
		Module module = new NoSuccessPropertiesModule();

		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> values = invoker.invoke(module);

		ModuleExitStatusChecker checker = new PropertyModuleExitStatusChecker();

		assertEquals(ExitStatus.SUCCESS, checker.check(module, values));
	}

	private static class NoSuccessPropertiesModule extends AbstractModule {

		@Override
		public String getName() {
			return "no_success_properties_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
			outputSpec.addReturnValue("something", Object.class);
			outputSpec.addReturnValue("anything", Object.class);
		}

		@Override
		public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
			output.setReturnValue("something", Object.class, new Object());
			output.setReturnValue("anything", Object.class, new Object());
		}

		@Override
		public Category[] getCategories() {
			return new Category[]{Category.MISC};
		}
	}

	@Test
	public void testSingleSuccessProperty() throws ModuleException {
		Module successModule = new SingleSuccessPropertyModule(true);
		Module failureModule = new SingleSuccessPropertyModule(false);

		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> successValues = invoker.invoke(successModule);
		List<Object> failureValues = invoker.invoke(failureModule);

		ModuleExitStatusChecker checker = new PropertyModuleExitStatusChecker();

		assertEquals(ExitStatus.SUCCESS, checker.check(successModule, successValues));
		assertEquals(ExitStatus.FAILURE, checker.check(failureModule, failureValues));
	}

	private static class SingleSuccessPropertyModule extends AbstractModule {

		private boolean successful;

		public SingleSuccessPropertyModule(boolean successful) {
			this.successful = successful;
		}

		@Override
		public String getName() {
			return "single_success_property_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
			outputSpec.addReturnValue("not_so_important", Object.class);
			outputSpec.addReturnValue("successful", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		}

		@Override
		public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
			output.setReturnValue("not_so_important", Object.class, new Object());
			output.setReturnValue("successful", Boolean.class, successful);
		}

		@Override
		public Category[] getCategories() {
			return new Category[]{Category.MISC};
		}
	}

	@Test
	public void testMultipleSuccessProperties() throws ModuleException {
		Module allSuccessModule = new MultipleSuccessPropertiesModule(true, true);
		Module allFailureModule = new MultipleSuccessPropertiesModule(false, false);
		Module mixedFailureModule = new MultipleSuccessPropertiesModule(true, false);

		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> allSuccessValues = invoker.invoke(allSuccessModule);
		List<Object> allFailureValues = invoker.invoke(allFailureModule);
		List<Object> mixedFailureValues = invoker.invoke(mixedFailureModule);

		ModuleExitStatusChecker checker = new PropertyModuleExitStatusChecker();

		assertEquals(ExitStatus.SUCCESS, checker.check(allSuccessModule, allSuccessValues));
		assertEquals(ExitStatus.FAILURE, checker.check(allFailureModule, allFailureValues));
		assertEquals(ExitStatus.FAILURE, checker.check(mixedFailureModule, mixedFailureValues));
	}

	private static class MultipleSuccessPropertiesModule extends AbstractModule {

		private final boolean firstSuccessful, secondSuccessful;

		public MultipleSuccessPropertiesModule(boolean firstSuccessful, boolean secondSuccessful) {
			this.firstSuccessful = firstSuccessful;
			this.secondSuccessful = secondSuccessful;
		}

		@Override
		public String getName() {
			return "multiple_success_properties_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
			outputSpec.addReturnValue("not_so_important", Object.class);
			outputSpec.addReturnValue("first_successful", Boolean.class,
					ModuleOutputSpec.PROPERTY_SUCCESS);
			outputSpec.addReturnValue("second_successful", Boolean.class,
					ModuleOutputSpec.PROPERTY_SUCCESS);
		}

		@Override
		public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
			output.setReturnValue("not_so_important", Object.class, new Object());
			output.setReturnValue("first_successful", Boolean.class, firstSuccessful);
			output.setReturnValue("second_successful", Boolean.class, secondSuccessful);
		}

		@Override
		public Category[] getCategories() {
			return new Category[]{Category.MISC};
		}
	}

	@Test
	public void testNullReturnValues() throws ModuleException {
		Module module = new NullReturnValuesModule();

		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> values = invoker.invoke(module);

		ModuleExitStatusChecker checker = new PropertyModuleExitStatusChecker();

		assertEquals(ExitStatus.SUCCESS, checker.check(module, values));
	}

	private static class NullReturnValuesModule extends AbstractModule {

		@Override
		public String getName() {
			return "null_return_values_module";
		}

		@Override
		public void require(ModuleInputSpec inputSpec) {
		}

		@Override
		public void provide(ModuleOutputSpec outputSpec) {
			outputSpec.addReturnValue("some_object", Object.class);
			outputSpec.addReturnValue("some_boolean", Boolean.class);
			outputSpec.addReturnValue("successful", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
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
