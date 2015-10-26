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
import java.io.OutputStream;
import java.io.Writer;

import uniol.apt.adt.pn.PetriNet;

/**
 * Interface for Petri net renderers
 *
 * @author vsp
 */
public interface PNRenderer {
	/**
	 * Render a Petri net into a file
	 *
	 * @param pn the Petri net to render
	 * @param filename the name of the desired output file
	 * @throws RenderException if the renderer can't describe the Petri net
	 * @throws IOException if writing failes
	 */
	public void renderFile(PetriNet pn, String filename) throws RenderException, IOException;

	/**
	 * Render a Petri net into a file
	 *
	 * @param pn the Petri net to render
	 * @param file the desired output file
	 * @throws RenderException if the renderer can't describe the Petri net
	 * @throws IOException if writing failes
	 */
	public void renderFile(PetriNet pn, File file) throws RenderException, IOException;

	/**
	 * Render a Petri net into a outputstream
	 *
	 * @param pn the Petri net to render
	 * @param os the outputstream
	 * @throws RenderException if the renderer can't describe the Petri net
	 * @throws IOException if writing failes
	 */
	public void render(PetriNet pn, OutputStream os) throws RenderException, IOException;

	/**
	 * Render a Petri net into a writer
	 *
	 * @param pn the Petri net to render
	 * @param writer the writer
	 * @throws RenderException if the renderer can't describe the Petri net
	 * @throws IOException if writing failes
	 */
	public void render(PetriNet pn, Writer writer) throws RenderException, IOException;

	/**
	 * Render a Petri net into a string
	 *
	 * @param pn the Petri net to render
	 * @return a string describing the Petri net
	 * @throws RenderException if the renderer can't describe the Petri net
	 */
	public String render(PetriNet pn) throws RenderException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
