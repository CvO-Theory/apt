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

import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Renke Grunwald
 *
 */
public class ModuleRegistryTest {

	@Test
	public void testFindModule() {
		final Module module1 = mock(Module.class);
		when(module1.getName()).thenReturn("module1");

		ModuleRegistry registry = new AbstractModuleRegistry() {
			{
				registerModule(module1);
			}
		};

		assertEquals(registry.findModule("module1"), module1);
	}

	@Test
	public void testFindNonExistentModule() {
		assertTrue(new AbstractModuleRegistry() {
		}.findModule("module1") == null);
	}

	@Test
	public void testGetModules() {
		final Module module1 = mock(Module.class);
		when(module1.getName()).thenReturn("module1");

		final Module module2 = mock(Module.class);
		when(module2.getName()).thenReturn("module2");

		ModuleRegistry registry = new AbstractModuleRegistry() {
			{
				registerModule(module1);
				registerModule(module2);
			}
		};

		assertEquals(registry.getModules().size(), 2);
		assertTrue(registry.getModules().contains(module1));
		assertTrue(registry.getModules().contains(module2));
	}

	@Test
	public void testGetModulesByPrefix() {
		final Module module1 = mock(Module.class);
		when(module1.getName()).thenReturn("a_module");

		final Module module2 = mock(Module.class);
		when(module2.getName()).thenReturn("some_module");

		final Module module3 = mock(Module.class);
		when(module3.getName()).thenReturn("another_module");

		ModuleRegistry registry = new AbstractModuleRegistry() {
			{
				registerModule(module1);
				registerModule(module2);
				registerModule(module3);
			}
		};

		Collection<Module> modules;

		modules = registry.findModulesByPrefix("some");

		assertFalse(modules.contains(module1));
		assertTrue(modules.contains(module2));
		assertFalse(modules.contains(module3));

		modules = registry.findModulesByPrefix("a");

		assertTrue(modules.contains(module1));
		assertFalse(modules.contains(module2));
		assertTrue(modules.contains(module3));

		modules = registry.findModulesByPrefix("another");

		assertFalse(modules.contains(module1));
		assertFalse(modules.contains(module2));
		assertTrue(modules.contains(module3));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
