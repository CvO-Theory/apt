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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.module.impl.ModuleVisibility;

/**
 * Used to register modules that are used in APT.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleRegistry {
	Map<String, ModuleEntry> modulesEntries = new LinkedHashMap<>();

	static private class ModuleEntry {
		Module module;
		ModuleVisibility visibility;
	}

	/**
	 * Finds a module by its name.
	 *
	 * @param name
	 *            the name of a module
	 * @return the module
	 */
	public Module findModule(String name) {
		ModuleEntry entry = modulesEntries.get(name);

		if (entry != null) {
			return entry.module;
		}

		return null;
	}

	/**
	 * Finds a module by its name if it has one of the given visibilities.
	 *
	 * @param visibilities
	 * @param name
	 *            the name of a module
	 * @return the module
	 */
	public Module findModule(String name, ModuleVisibility... visibilities) {
		ModuleEntry entry = modulesEntries.get(name);

		if (entry != null && Arrays.asList(visibilities).contains(entry.visibility)) {
			return entry.module;
		}

		return null;
	}

	/**
	 * Finds all modules that start with the given prefix.
	 *
	 * @param prefix
	 *            the prefix
	 * @return the modules
	 */
	public Collection<Module> findModulesByPrefix(String prefix) {
		List<Module> prefixedModules = new ArrayList<Module>();

		for (Module module : getModules()) {
			if (module.getName().startsWith(prefix)) {
				prefixedModules.add(module);
			}
		}

		return prefixedModules;
	}

	/**
	 * Finds all modules that start with the given prefix and have the one of
	 * the given visibilities.
	 *
	 * @param visibilities
	 * @param prefix
	 *            the prefix
	 * @return the modules
	 */
	public Collection<Module> findModulesByPrefix(String prefix, ModuleVisibility... visibilities) {
		List<Module> prefixedModules = new ArrayList<Module>();

		for (Module module : getModules(visibilities)) {
			if (module.getName().startsWith(prefix)) {
				prefixedModules.add(module);
			}
		}

		return prefixedModules;
	}

	/**
	 * Register a module such that it's used in APT. The registered module has
	 * the given {@link ModuleVisibility}.
	 *
	 * @param visibility
	 * @param module
	 */
	public void registerModule(Module module, ModuleVisibility visibility) {
		ModuleEntry entry = new ModuleEntry();

		entry.module = module;
		entry.visibility = visibility;

		modulesEntries.put(module.getName(), entry);
	}

	/**
	 * Register a module such that it's used in APT. The registered module has
	 * the {@link ModuleVisibility} SHOWN by default.
	 *
	 * @param module
	 */
	public void registerModule(Module module) {
		ModuleEntry entry = new ModuleEntry();

		entry.module = module;
		entry.visibility = ModuleVisibility.SHOWN;

		modulesEntries.put(module.getName(), entry);
	}

	/**
	 * Registers multiple modules via {@link #registerModule(Module)}.
	 *
	 * @param modules array of modules
	 */
	public void registerModules(Module... modules) {
		for (Module module : modules) {
			registerModule(module);
		}
	}

	/**
	 * Registers multiple modules via {@link #registerModule(Module)}. The
	 * registered modules all have the the given {@link ModuleVisibility}.
	 *
	 * @param visibility
	 *            the visibility the modules should have
	 * @param modules
	 *            the modules to be registered
	 */
	public void registerModules(ModuleVisibility visibility, Module... modules) {
		for (Module module : modules) {
			registerModule(module, visibility);
		}
	}

	/**
	 * Gets all modules that are used in APT.
	 *
	 * @return the modules
	 */
	public Collection<Module> getModules() {
		List<Module> modules = new ArrayList<>();

		for (ModuleEntry entry : modulesEntries.values()) {
			modules.add(entry.module);
		}

		return modules;
	}

	/**
	 * Gets all modules that are used in APT and have one of the given
	 * visibilities.
	 *
	 * @param visibilities
	 *            the visibilities one of which the modules fulfills
	 * @return the modules
	 */
	public Collection<Module> getModules(ModuleVisibility... visibilities) {
		List<Module> modules = new ArrayList<>();

		for (ModuleEntry entry : modulesEntries.values()) {
			if (Arrays.asList(visibilities).contains(entry.visibility)) {
				modules.add(entry.module);
			}
		}

		return modules;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
