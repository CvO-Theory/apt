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

package uniol.apt.adt.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.adt.exception.StructureException;
import uniol.apt.util.Pair;

/**
 * The Extensible class describes an advanced object consisting of an id and a
 * key \rightarrow value property mapping and for every object a flag is saved
 * if the object should by copied (referenzcopy), if the owner object is copied.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class Extensible implements IExtensible {

	private final Map<String, Extension> extensions = new HashMap<>();

	private static class Extension {
		final Object value;
		final Set<ExtensionProperty> properties;

		public Extension(Object value) {
			this.value = value;
			this.properties = Collections.emptySet();
		}

		public Extension(Object value, ExtensionProperty... properties) {
			Set<ExtensionProperty> props = new HashSet<>(Arrays.asList(properties));

			this.value = value;

			// Premature optimisation: Save some memory
			if (props.isEmpty())
				this.properties = Collections.emptySet();
			else if (props.size() == 1)
				this.properties = Collections.singleton(props.iterator().next());
			else
				this.properties = Collections.unmodifiableSet(props);
		}
	}

	/**
	 * Returns if this extension contains the given key.
	 *
	 * @param key - key to search for
	 * @return true if this extensible has an extension names 'key'
	 */
	public boolean hasExtension(String key) {
		return this.extensions.containsKey(key);
	}

	@Override
	public void putExtension(String key, Object value, ExtensionProperty... properties) {
		this.extensions.put(key, new Extension(value, properties));
	}

	/**
	 * Saves the given value using the key as identifier with the flag to copy
	 * this object by coping the owner object.
	 * @param key An identifying key as string.
	 * @param value Any value.
	 */
	public void putExtension(String key, Object value) {
		this.extensions.put(key, new Extension(value));
	}

	/**
	 * Removes the value associated with the given key.
	 * @param key An identifying key as string.
	 */
	@Override
	public void removeExtension(String key) {
		this.extensions.remove(key);
	}

	/**
	 * Retrieves the saved value using the key as identifier.
	 * @param key An identifying key as string.
	 * @return The saved value.
	 * @throws StructureException thrown if the key is not found.
	 */
	@Override
	public Object getExtension(String key) {
		Extension ext = this.extensions.get(key);
		if (ext == null) {
			throw new StructureException("Extension '" + key + "' not found.");
		}
		return ext.value;
	}

	/**
	 * Calculates a list of pairs key-value of all extensions. Attention it's a reference copy!
	 * @return A list of key-value-pairs of all extensions.
	 */
	public List<Pair<String, Object>> getExtensions() {
		List<Pair<String, Object>> ret = new ArrayList<>();
		for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
			ret.add(new Pair<>(entry.getKey(), entry.getValue().value));
		}
		return ret;
	}

	/**
	 * Calculates a list of pairs key-value of all extensions with the given property. Attention it's a reference
	 * copy!
	 * @param property The property to look for
	 * @return A list of key-value-pairs of all extensions.
	 */
	public List<Pair<String, Object>> getExtensionsWithProperty(ExtensionProperty property) {
		List<Pair<String, Object>> ret = new ArrayList<>();
		for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
			Extension ext = entry.getValue();
			if (ext.properties.contains(property))
				ret.add(new Pair<>(entry.getKey(), ext.value));
		}
		return ret;
	}

	/**
	 * Calculates a list of pairs key-value of all extensions without the given property. Attention it's a reference
	 * copy!
	 * @param property The property to look for
	 * @return A list of key-value-pairs of all extensions.
	 */
	public List<Pair<String, Object>> getExtensionsWithoutProperty(ExtensionProperty property) {
		List<Pair<String, Object>> ret = new ArrayList<>();
		for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
			Extension ext = entry.getValue();
			if (!ext.properties.contains(property))
				ret.add(new Pair<>(entry.getKey(), ext.value));
		}
		return ret;
	}

	/**
	 * Calculates a list of pairs key-value which should be written to a file. Attention it's a reference copy!
	 * @return A list of pair key-value-pairs which should be copied.
	 */
	public List<Pair<String, Object>> getWriteToFileExtensions() {
		return getExtensionsWithProperty(ExtensionProperty.WRITE_TO_FILE);
	}

	/**
	 * Calculates a list of pairs key-value which should be copied. Attention it's a reference copy!
	 * @return A list of pair key-value-pairs which should be copied.
	 */
	public List<Pair<String, Object>> getCopyExtensions() {
		return getExtensionsWithoutProperty(ExtensionProperty.NOCOPY);
	}

	/**
	 * Copies the extensions from the given Extensible, marked as to copy, to
	 * this extension list. Attention it's just a reference copy!
	 * @param e The Extensible to copy from.
	 */
	public final void copyExtensions(Extensible e) {
		for (Pair<String, Object> pair : e.getCopyExtensions()) {
			putExtension(pair.getFirst(), pair.getSecond());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
