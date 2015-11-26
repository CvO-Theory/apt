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

package uniol.apt.io.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Class which provides the correct renderer for a given format.
 *
 * @author vsp
 *
 * @param <T> The class which the renderers should render.
 */
public class AbstractRenderers<T> implements Renderers<T> {
	private Map<String, Renderer<T>> renderers;

	/**
	 * Constructor
	 *
	 * @param clazz Class object of the which the renderers should render
	 */
	@SuppressWarnings("unchecked") // I hate type erasure and other java things ...
	protected AbstractRenderers(Class<T> clazz) {
		this.renderers   = new HashMap<>();

		String className = clazz.getCanonicalName();

		ClassLoader cl   = getClass().getClassLoader();
		try {
			Enumeration<URL> rendererNames = cl.getResources("META-INF/uniol/apt/compiler/"
					+ Renderer.class.getCanonicalName() + "/" + className);

			while (rendererNames.hasMoreElements()) {
				try (InputStream is = rendererNames.nextElement().openStream()) {
					LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
					while (lIter.hasNext()) {
						String rendererName = lIter.next();
						Class<? extends Renderer<T>> rendererClass;
						try {
							rendererClass = (Class<? extends Renderer<T>>)
								cl.loadClass(rendererName);
						} catch (ClassNotFoundException ex) {
							throw new RuntimeException(String.format(
									"Could not load class %s",
									rendererName), ex);
						}
						Renderer<T> renderer;
						try {
							renderer = rendererClass.newInstance();
						} catch (ClassCastException
								| IllegalAccessException
								| InstantiationException ex) {
							throw new RuntimeException(String.format(
									"Could not instantiate %s",
									rendererName), ex);
						}
						String format = renderer.getFormat();
						if (format == null || format.equals("")
								|| !format.equals(format.toLowerCase())) {
							throw new RuntimeException(String.format(
									"Renderer %s reports an invalid format: %s",
									rendererName, format));
						}
						Renderer<T> oldRenderer = this.renderers.get(format);
						if (oldRenderer != null
								&& !oldRenderer.getClass().equals(rendererClass)) {
							throw new RuntimeException(String.format(
								"Different renderers claim, to interpret format %s:"
								+ " %s and %s", format,
								oldRenderer.getClass().getCanonicalName(),
								rendererName));
						}
						this.renderers.put(format, renderer);
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to discover renderers", ex);
		}

	}

	@Override
	public Renderer<T> getRenderer(String format) throws RendererNotFoundException {
		Renderer<T> renderer = this.renderers.get(format.toLowerCase());
		if (renderer == null) {
			throw new RendererNotFoundException(String.format("Renderer for format %s not found", format));
		}
		return renderer;
	}

	@Override
	public Set<String> getSupportedFormats() {
		return this.renderers.keySet();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
