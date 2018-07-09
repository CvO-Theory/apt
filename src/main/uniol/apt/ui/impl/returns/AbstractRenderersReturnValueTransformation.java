/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2018 Uli Schlachter
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

package uniol.apt.ui.impl.returns;

import java.io.IOException;
import java.io.Writer;

import uniol.apt.io.renderer.RendererNotFoundException;
import uniol.apt.io.renderer.Renderers;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ReturnValueTransformationWithOptions;

/**
 * Transform an object into a String by using a renderer.
 * @author Uli Schlachter
 */
public abstract class AbstractRenderersReturnValueTransformation<T> implements ReturnValueTransformationWithOptions<T> {
	private final Renderers<T> renderers;
	private final String defaultRenderer;

	public AbstractRenderersReturnValueTransformation(Renderers<T> renderers, String defaultRenderer) {
		this.renderers = renderers;
		this.defaultRenderer = defaultRenderer;
	}

	@Override
	public void transform(Writer output, T arg) throws ModuleException, IOException {
		transform(output, arg, defaultRenderer);
	}

	@Override
	public void transform(Writer output, T arg, String renderer) throws ModuleException, IOException {
		try {
			renderers.getRenderer(renderer).render(arg, output);
		} catch (RendererNotFoundException ex) {
			StringBuilder message = new StringBuilder();
			message.append(ex.getMessage());
			message.append("\nSupported file formats:");
			for (String name : renderers.getSupportedFormats()) {
				message.append(' ').append(name);
			}
			throw new RendererNotFoundException(message.toString(), ex);
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
