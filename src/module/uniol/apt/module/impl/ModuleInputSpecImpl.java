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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uniol.apt.module.ModuleInputSpec;

/**
 * This class makes it possible to retrieve parameter specifications from
 * modules.
 *
 * @author Renke Grunwald
 *
 */
public class ModuleInputSpecImpl implements ModuleInputSpec {
	public Map<String, Parameter> nameParameters = new LinkedHashMap<>();
	public Map<String, OptionalParameter<?>> nameOptionalParameters = new LinkedHashMap<>();

	@Override
	public void addParameter(String name, Class<?> klass, String description, String... properties) {
		nameParameters.put(name, new Parameter(name, klass, description, properties));
	}

	@Override
	public <T> void addOptionalParameterWithDefault(String name, Class<T> klass, T defaultValue,
			String defaultValueString, String description, String... properties) {
		if (defaultValue == null || defaultValueString == null)
			throw new NullPointerException();
		nameOptionalParameters.put(name, new OptionalParameter<T>(name, klass, defaultValue,
					defaultValueString, description, properties));
	}

	@Override
	public <T> void addOptionalParameterWithoutDefault(String name, Class<T> klass,
			String description, String... properties) {
		nameOptionalParameters.put(name, new OptionalParameter<T>(name, klass,
					null, null, description, properties));
	}

	public List<Parameter> getParameters() {
		return new ArrayList<Parameter>(nameParameters.values());
	}

	public List<OptionalParameter<?>> getOptionalParameters() {
		return new ArrayList<OptionalParameter<?>>(nameOptionalParameters.values());
	}

	public List<Parameter> getAllParameters() {
		List<Parameter> allParameters = new ArrayList<Parameter>();
		allParameters.addAll(getParameters());
		allParameters.addAll(getOptionalParameters());
		return allParameters;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
