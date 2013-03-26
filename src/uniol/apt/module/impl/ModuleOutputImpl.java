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

import java.util.HashMap;
import java.util.Map;

import uniol.apt.module.ModuleOutput;
import uniol.apt.module.exception.ModuleException;

/**
 * This class makes it possible to retrieve return values from modules.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleOutputImpl implements ModuleOutput {
	private final Map<String, Object> nameReturnValues = new HashMap<>();
	private final ModuleOutputSpecImpl spec;

	public ModuleOutputImpl(ModuleOutputSpecImpl spec) {
		this.spec = spec;
	}

	@Override
	public <T> void setReturnValue(String name, Class<T> klass, T value) throws ModuleException {
		if (!spec.hasReturnValue(name, klass))
			throw new ModuleException("Return value type mismatch");
		nameReturnValues.put(name, value);
	}

	public Object getValue(String name) {
		return nameReturnValues.get(name);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
