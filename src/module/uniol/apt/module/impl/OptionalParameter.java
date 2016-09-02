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

import java.util.Objects;

/**
 * Makes it possible to add an optional parameter to module.
 *
 * @author Renke Grunwald
 *
 * @param <T> the type of this parameter
 */
public class OptionalParameter<T> extends Parameter {

	private final T defaultValue;
	private final String defaultValueString;

	/**
	 * Creates a new optional parameter specification.
	 *
	 * @param name
	 *                name of the parameter
	 * @param klass
	 *                type of the parameter
	 * @param defaultValue
	 *                default value for the parameter that is used when the
	 *                parameter is not set
	 * @param defaultValueString
	 *                ???
	 * @param description
	 *                explanation of the parameter
	 * @param properties
	 *                ???
	 */
	public OptionalParameter(String name, Class<T> klass, T defaultValue, String defaultValueString,
			String description, String... properties) {
		super(name, klass, description, properties);
		this.defaultValue = defaultValue;
		this.defaultValueString = defaultValueString;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getDefaultValueString() {
		return defaultValueString;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hashCode(defaultValue);
	}

	@Override
	public boolean equals(Object o) {
		if (!super.equals(o) || !(o instanceof OptionalParameter<?>))
			return false;
		OptionalParameter<?> other = (OptionalParameter<?>) o;
		return Objects.equals(other.defaultValue, defaultValue);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
