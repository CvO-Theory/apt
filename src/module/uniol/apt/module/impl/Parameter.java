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

import java.util.Arrays;

/**
 * A POJO for a parameter.
 *
 * @author Renke Grunwald
 *
 */
public class Parameter {
	private final String name;
	private final Class<?> klass;
	private final String description;
	private final String[] properties;

	/**
	 * Creates a new parameter specification.
	 *
	 * @param name
	 *                name of the parameter
	 * @param klass
	 *                type of the parameter
	 * @param description
	 *                explanation of the parameter
	 * @param properties
	 *                ???
	 */
	public Parameter(String name, Class<?> klass, String description, String[] properties) {
		this.name = name;
		this.klass = klass;
		this.description = description;
		this.properties = Arrays.copyOf(properties, properties.length);
	}

	public String getName() {
		return name;
	}

	public Class<?> getKlass() {
		return klass;
	}

	public String getDescription() {
		return description;
	}

	public String[] getProperties() {
		return Arrays.copyOf(properties, properties.length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(properties);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(properties, other.properties))
			return false;
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
