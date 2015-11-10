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
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.SynetLTSParser;
import uniol.apt.io.parser.impl.SynetPNParser;
import uniol.apt.io.renderer.impl.AptLTSRenderer;
import uniol.apt.io.renderer.impl.AptPNRenderer;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * A Module for converting a file in synet format to a file in apt format.
 *
 * @author Manuel Gieseking
 */
@AptModule
public class Synet2AptModule extends AbstractModule implements Module {

	private final static String DESCRIPTION = "Convert Synet format to APT format";
	private final static String TITLE = "Synet2Apt";
	private final static String NAME = "synet2apt";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("input_filename", String.class, "The file that should be converted.");
		inputSpec.addOptionalParameter("input_type", String.class, null, "The type of the graph."
			+ " Possible values: ts or pn.");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("output_filename", String.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		String filename = input.getParameter("input_filename", String.class);
		String type = input.getParameter("input_type", String.class);
		try {
			String out;
			if (type == null) {
				if (filename.endsWith(".aut"))
					type = "ts";
				else if (filename.endsWith(".net"))
					type = "pn";
				else
					throw new ParseException("type not set and file extension isn't .aut or .net");
			}
			switch (type) {
				case "ts":
				case "lts":
					out = convertLTS(filename);
					break;
				case "pn":
				case "lpn":
					out = convertPN(filename);
					break;
				default:
					throw new ModuleException("input_type has to be ts or pn");
			}
			output.setReturnValue("output_filename", String.class, out);
		} catch (IOException ex) {
			throw new ModuleException("Can't read file: " + ex.getMessage(), ex);
		} catch (ParseException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		}
	}

	private static String convertLTS(String filename) throws IOException, ModuleException, ParseException {
		return new AptLTSRenderer().render(new SynetLTSParser().parseFile(filename));
	}

	private static String convertPN(String filename) throws IOException, ModuleException, ParseException {
		return new AptPNRenderer().render(new SynetPNParser().parseFile(filename));
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return DESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.CONVERTER};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
