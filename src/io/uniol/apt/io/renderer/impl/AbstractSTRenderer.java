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

package uniol.apt.io.renderer.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import uniol.apt.io.renderer.RenderException;

/**
 * Abstract Renderer which renders objects using stringtemplate
 * @param <G> Type of object that the renderer renders.
 * @author vsp
 */
public abstract class AbstractSTRenderer<G> extends AbstractRenderer<G> {
	private final STGroup group;
	private final String templateName;
	private final String format;
	private final String[] extensions;

	/**
	 * Constructor
	 *
	 * @param templateFile file name of the template file
	 * @param templateName name of the main rule in the template file
	 * @param format The name of the supported file format
	 * @param extensions The list of recommended file extensions
	 */
	protected AbstractSTRenderer(String templateFile, String templateName, String format, String... extensions) {
		this.group        = new STGroupFile(templateFile);
		this.templateName = templateName;
		this.format       = format;
		this.extensions   = extensions;
	}

	@Override
	public void render(G obj, Writer writer) throws RenderException, IOException {
		ST template = this.group.getInstanceOf(this.templateName);
		template.add(this.templateName, obj);
		template.write(new AutoIndentWriter(writer), new ThrowingErrorListener());
	}

	@Override
	public String getFormat() {
		return this.format;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList(this.extensions));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
