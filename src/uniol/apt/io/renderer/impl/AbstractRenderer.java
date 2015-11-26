/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  vsp
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

package uniol.apt.io.renderer.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;

import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.RenderException;

/**
 * Abstract base class for renderers
 * @param <G> Type of object that the parser produces.
 * @author vsp
 */
public abstract class AbstractRenderer<G> implements Renderer<G> {
	@Override
	public void renderFile(G obj, String filename) throws RenderException, IOException {
		renderFile(obj, new File(filename));
	}

	@Override
	public void renderFile(G obj, File file) throws RenderException, IOException {
		try (OutputStream os = FileUtils.openOutputStream(file);
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw)) {
			render(obj, bw);
		}
	}

	@Override
	public String render(G obj) throws RenderException {
		try (Writer writer = new StringBuilderWriter()) {
			render(obj, writer);
			return writer.toString();
		} catch (IOException e) {
			// A StringWriter shouldn't throw IOExceptions
			throw new RuntimeException(e);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
