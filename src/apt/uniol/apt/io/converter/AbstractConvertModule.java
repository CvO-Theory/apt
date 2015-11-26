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

package uniol.apt.io.converter;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import uniol.apt.APT;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.AbstractParsers;
import uniol.apt.io.renderer.AbstractRenderers;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * A Module for converting between different file formats.
 * @param <T> The type of object between which we convert.
 * @author Uli Schlachter
 */
public abstract class AbstractConvertModule<T> extends AbstractModule implements Module {
	private final AbstractParsers<T> parsers;
	private final AbstractRenderers<T> renderers;

	/**
	 * Create an converter module for the given parsers and renderers.
	 * @param parsers The parsers that are supported.
	 * @param renderers The renderers that are supported.
	 */
	protected AbstractConvertModule(AbstractParsers<T> parsers, AbstractRenderers<T> renderers) {
		this.parsers = parsers;
		this.renderers = renderers;
	}

	@Override
	final public String getLongDescription() {
		Set<String> parseFormats = parsers.getSupportedFormats();
		Set<String> renderFormats = renderers.getSupportedFormats();

		Set<String> allFormats = new TreeSet<>();
		allFormats.addAll(parseFormats);
		allFormats.addAll(renderFormats);

		StringBuffer sb = new StringBuffer(getShortDescription());
		sb.append("\n\nSupported file formats:");
		for (String format : allFormats) {
			boolean canParse = parseFormats.contains(format);
			boolean canRender = renderFormats.contains(format);

			sb.append("\n - ");
			if (canParse && canRender) {
				sb.append(format);
			} else if (canParse && !canRender) {
				sb.append(format + " (only as input format)");
			} else if (!canParse && canRender) {
				sb.append(format + " (only as output format)");
			} else
				assert false;
		}
		return sb.toString();
	}

	@Override
	final public Category[] getCategories() {
		return new Category[]{Category.CONVERTER};
	}

	@Override
	final public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("input_format", String.class, "The format of input.");
		inputSpec.addParameter("output_format", String.class, "The format of output.");
		inputSpec.addParameter("input_filename", String.class, "The file that should be converted.");
	}

	@Override
	final public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("output_filename", String.class, ModuleOutputSpec.PROPERTY_FILE,
				ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	final public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String inputFormat = input.getParameter("input_format", String.class);
		String outputFormat = input.getParameter("output_format", String.class);
		String filename = input.getParameter("input_filename", String.class);

		T obj;
		try {
			Parser<T> parser = parsers.getParser(inputFormat);
			if (filename.equals(APT.STANDARD_INPUT_SYMBOL))
				obj = parser.parse(System.in);
			else
				obj = parser.parseFile(filename);
		} catch (ParseException | IOException e) {
			throw new ModuleException("Cannot parse file " + filename + ": " + e.getMessage(), e);
		}
		String result = renderers.getRenderer(outputFormat).render(obj);
		output.setReturnValue("output_filename", String.class, result);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
