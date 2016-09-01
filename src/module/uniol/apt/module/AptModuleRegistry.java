/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

import java.util.ServiceLoader;

/**
 * Used to register modules that are used in APT.
 *
 * @author vsp
 *
 */
public class AptModuleRegistry extends AbstractModuleRegistry {
	public static final AptModuleRegistry INSTANCE = new AptModuleRegistry();

	private AptModuleRegistry() {
		super();
		for (Module module : ServiceLoader.load(Module.class, getClass().getClassLoader())) {
			String moduleName = module.getClass().getCanonicalName();
			String name = module.getName();
			if (name == null || name.equals("")
					|| !name.equals(name.toLowerCase())) {
				throw new RuntimeException(String.format(
						"Module %s reports an invalid name: %s",
						moduleName, name));
			}
			Module oldModule = findModule(name);
			if (oldModule != null && !oldModule.getClass().equals(module.getClass())) {
				throw new RuntimeException(String.format(
						"Different modules claim, to have name %s:"
						+ " %s and %s", name,
						oldModule.getClass().getCanonicalName(),
						moduleName));
			}
			registerModule(module);
		}

	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
