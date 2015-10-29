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

package uniol.apt.io.renderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import uniol.apt.adt.ts.TransitionSystem;

/**
 * Interface for labeled transition system renderers
 *
 * @author vsp
 */
public interface LTSRenderer {
	/**
	 * Render a labeled transition system into a file
	 *
	 * @param ts the labeled transition system to render
	 * @param filename the name of the desired output file
	 * @throws RenderException if the renderer can't describe the labeled transition system
	 * @throws IOException if writing failes
	 */
	public void renderFile(TransitionSystem ts, String filename) throws RenderException, IOException;

	/**
	 * Render a labeled transition system into a file
	 *
	 * @param ts the labeled transition system to render
	 * @param file the desired output file
	 * @throws RenderException if the renderer can't describe the labeled transition system
	 * @throws IOException if writing failes
	 */
	public void renderFile(TransitionSystem ts, File file) throws RenderException, IOException;

	/**
	 * Render a labeled transition system into a outputstream
	 *
	 * @param ts the labeled transition system to render
	 * @param os the outputstream
	 * @throws RenderException if the renderer can't describe the labeled transition system
	 * @throws IOException if writing failes
	 */
	public void render(TransitionSystem ts, OutputStream os) throws RenderException, IOException;

	/**
	 * Render a labeled transition system into a writer
	 *
	 * @param ts the labeled transition system to render
	 * @param writer the writer
	 * @throws RenderException if the renderer can't describe the labeled transition system
	 * @throws IOException if writing failes
	 */
	public void render(TransitionSystem ts, Writer writer) throws RenderException, IOException;

	/**
	 * Render a labeled transition system into a string
	 *
	 * @param ts the labeled transition system to render
	 * @return a string describing the labeled transition system
	 * @throws RenderException if the renderer can't describe the labeled transition system
	 */
	public String render(TransitionSystem ts) throws RenderException;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
