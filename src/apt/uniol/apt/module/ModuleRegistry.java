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
import java.util.Collections;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;


/**
 * Used to register modules that are used in APT.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleRegistry {
	Trie<String, Module> modulesEntries = new PatriciaTrie<>();

	/**
	 * Finds a module by its name.
	 *
	 * @param name
	 *            the name of a module
	 * @return the module
	 */
	public Module findModule(String name) {
		return modulesEntries.get(name);
	}

	/**
	 * Finds all modules that start with the given prefix.
	 *
	 * @param prefix
	 *            the prefix
	 * @return the modules
	 */
	public Collection<Module> findModulesByPrefix(String prefix) {
		return Collections.unmodifiableCollection(this.modulesEntries.prefixMap(prefix).values());
	}

	/**
	 * Register a module such that it's used in APT.
	 *
	 * @param module
	 */
	public void registerModule(Module module) {
		modulesEntries.put(module.getName(), module);
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
	 * Gets all modules that are used in APT.
	 *
	 * @return the modules
	 */
	public Collection<Module> getModules() {
		return Collections.unmodifiableCollection(this.modulesEntries.values());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
