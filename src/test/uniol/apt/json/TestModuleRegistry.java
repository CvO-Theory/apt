/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.json;

import java.util.Arrays;
import java.util.Collection;

import uniol.apt.analysis.coverability.CoverabilityModule;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.impl.ExampleModule;

public class TestModuleRegistry implements ModuleRegistry {
	@Override
	public Module findModule(String name) {
		if ("example".equals(name))
			return new ExampleModule();
		if ("coverability_graph".equals(name))
			return new CoverabilityModule();
		return null;
	}

	@Override
	public Collection<Module> findModulesByPrefix(String prefix) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Collection<Module> getModules() {
		return Arrays.<Module>asList(new ExampleModule(), new CoverabilityModule());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
