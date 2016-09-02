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

import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.Module;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.ModuleInvocationException;

/**
 * @author Renke Grunwald
 *
 */
public class ModuleInvokerTest {

	@Test
	public void testExampleModule() throws ModuleException {
		Module module = new ExampleModule();
		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> values = invoker.invoke(module, "Hello World!");
		String lowerCaseString = (String) values.get(0);

		assertEquals(lowerCaseString, "hello world!");
	}

	@Test
	public void testRealWorldModule() throws ModuleException {
		Module module = new RealWorldModule();
		ModuleInvoker invoker = new ModuleInvoker();

		PetriNet net = new PetriNet("some_net");
		TransitionSystem aut = new TransitionSystem("some_aut");

		List<Object> values = invoker.invoke(module, net, aut);

		assertEquals(values.get(0), net);
		assertEquals(values.get(1), aut);
	}

	@Test
	public void testAllowAtLeastOneNullReturnValues() throws ModuleException {
		Module module = new TestReturnValuesModule("Hello", null);
		ModuleInvoker invoker = new ModuleInvoker();
		List<Object> values = invoker.invoke(module);

		assertTrue(values != null);
	}

	@Test
	public void testAllowAllNullReturnValues() throws ModuleException {
		Module module = new TestReturnValuesModule(null, null);
		ModuleInvoker invoker = new ModuleInvoker();

		List<Object> values = null;

		try {
			values = invoker.invoke(module);
		} catch (ModuleInvocationException e) {
			e.printStackTrace();
		}

		assertTrue(values != null);
	}

	@Test(expectedExceptions = ModuleInvocationException.class)
	public void testDisallowAllNullParameters() throws ModuleException {
		Module module = new TestParameterModule();
		ModuleInvoker invoker = new ModuleInvoker();
		invoker.invoke(module, (Object[]) null);
	}

	@Test(expectedExceptions = ModuleInvocationException.class)
	public void testDisallowAtLeastOneNullParameters() throws ModuleException {
		Module module = new TestParameterModule();
		ModuleInvoker invoker = new ModuleInvoker();
		invoker.invoke(module, new Object(), null);
	}

	private static class EvenMoreSpecialPN extends PetriNet {
		public EvenMoreSpecialPN(String id) {
			super(id);
		}
	}

	@Test
	public void testRealWorldModuleWithSubclasses() throws ModuleException {
		Module module = new RealWorldModule();
		ModuleInvoker invoker = new ModuleInvoker();

		EvenMoreSpecialPN net = new EvenMoreSpecialPN("some_net");
		TransitionSystem aut = new TransitionSystem("some_aut");

		List<Object> values = invoker.invoke(module, net, aut);

		assertEquals(values.get(0), net);
		assertEquals(values.get(1), aut);
	}

	@Test
	public void testOptionalParameter() throws ModuleException {
		final Object defaultValue = new Object();

		OptionalParameterModule module = new OptionalParameterModule(defaultValue);
		ModuleInvoker invoker = new ModuleInvoker();

		invoker.invoke(module);

		assertEquals(module.getActualValue(), defaultValue);
	}

	@Test
	public void testProvidedOptionalParameter() throws ModuleException {
		final Object defaultValue = new Object();
		final Object otherValue = new Object();

		OptionalParameterModule module = new OptionalParameterModule(defaultValue);
		ModuleInvoker invoker = new ModuleInvoker();

		invoker.invoke(module, otherValue);

		assertEquals(module.getActualValue(), otherValue);
	}

	@Test
	public void testAdditionalOptionalParameter() throws ModuleException {
		final Object defaultValue = new Object();

		AdditionalOptionalParameterModule module = new AdditionalOptionalParameterModule(defaultValue);
		ModuleInvoker invoker = new ModuleInvoker();

		invoker.invoke(module, "mandatory_value");

		assertEquals(module.getActualValue(), defaultValue);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
