/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  vsp
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Interface for generic renderers.
 * @param <G> Type of object that the renderer consumes.
 * @author vsp
 */
public interface Renderer<G> {
	/**
	 * Which format does this parser understand?
	 * @return name of the supported format
	 */
	public String getFormat();

	/**
	 * Get a list of recommended file extensions. The first entry is the preferred extension.
	 * @return the list of recommended file extensions
	 */
	public List<String> getFileExtensions();

	/**
	 * Render an object into a file
	 *
	 * @param obj the object to render
	 * @param filename the name of the desired output file
	 * @throws RenderException if the renderer can't describe the object
	 * @throws IOException if writing failes
	 */
	public void renderFile(G obj, String filename) throws RenderException, IOException;

	/**
	 * Render an object into a file
	 *
	 * @param obj the object to render
	 * @param file the desired output file
	 * @throws RenderException if the renderer can't describe the object
	 * @throws IOException if writing failes
	 */
	public void renderFile(G obj, File file) throws RenderException, IOException;

	/**
	 * Render an object into a writer
	 *
	 * @param obj the object to render
	 * @param writer the writer
	 * @throws RenderException if the renderer can't describe the object
	 * @throws IOException if writing failes
	 */
	public void render(G obj, Writer writer) throws RenderException, IOException;

	/**
	 * Render an object into a string
	 *
	 * @param obj the object to render
	 * @return a string describing the object
	 * @throws RenderException if the renderer can't describe the object
	 */
	public String render(G obj) throws RenderException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
