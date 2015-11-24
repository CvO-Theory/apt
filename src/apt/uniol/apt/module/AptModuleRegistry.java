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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Used to register modules that are used in APT.
 *
 * @author vsp
 *
 */
public class AptModuleRegistry extends AbstractModuleRegistry {
	public static final AptModuleRegistry INSTANCE = new AptModuleRegistry();

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	private AptModuleRegistry() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			Enumeration<URL> moduleNames = cl.getResources("META-INF/uniol/apt/compiler/"
					+ Module.class.getCanonicalName());

			while (moduleNames.hasMoreElements()) {
				try (InputStream is = moduleNames.nextElement().openStream()) {
					LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
					while (lIter.hasNext()) {
						String moduleName = lIter.next();
						Class<? extends Module> moduleClass;
						try {
							moduleClass =
								(Class<? extends Module>) cl.loadClass(moduleName);
						} catch (ClassNotFoundException ex) {
							throw new RuntimeException(String.format(
									"Could not load class %s",
									moduleName), ex);
						}
						Module module;
						try {
							module = moduleClass.newInstance();
						} catch (ClassCastException
								| IllegalAccessException
								| InstantiationException ex) {
							throw new RuntimeException(String.format(
									"Could not instantiate %s",
									moduleName), ex);
						}
						String name = module.getName();
						if (name == null || name.equals("")
								|| !name.equals(name.toLowerCase())) {
							throw new RuntimeException(String.format(
									"Module %s reports an invalid name: %s",
									moduleName, name));
						}
						Module oldModule = findModule(name);
						if (oldModule != null && !oldModule.getClass().equals(moduleClass)) {
							throw new RuntimeException(String.format(
									"Different modules claim, to have name %s:"
									+ "%s and %s", name,
									oldModule.getClass().getCanonicalName(),
									moduleName));
						}
						registerModule(module);
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to discover modules", ex);
		}

	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
