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

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;
import uniol.apt.io.parser.impl.petrify.PetrifyLTSParser;
import uniol.apt.io.parser.impl.petrify.PetrifyPNParser;
import uniol.apt.io.renderer.impl.APTRenderer;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * A Module for converting a file in petrify format to a file in apt format.
 * <p/>
 * @author Manuel Gieseking
 */
public class Petrify2AptModule extends AbstractModule {

	private final static String DESCRIPTION = "Convert Petrify format to APT format";
	private final static String TITLE = "Petrify2Apt";
	private final static String NAME = "petrify2apt";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("input_filename", String.class, "The file that should be converted.");
		inputSpec.addOptionalParameter("input_type", String.class, "pn", "The type of the graph."
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
			String out = null;
			switch (type) {
				case "pn":
					PetrifyPNParser parser = new PetrifyPNParser();
					parser.parse(FileUtils.readFileToString(new File(filename)));
					PetriNet pn = parser.getPN();
					out = new APTRenderer().render(pn);
					break;
				case "ts":
					TransitionSystem lts = PetrifyLTSParser.getLTS(filename);
					out = new APTRenderer().render(lts);
					break;
				default:
					throw new ModuleException("input_type has to be ts or pn");
			}
			output.setReturnValue("output_filename", String.class, out);
		} catch (NodeNotExistException | TypeMismatchException ex) {
			throw new ModuleException("Create datastructur: " + ex.getMessage());
		} catch (IOException e) {
			throw new ModuleException("Cannot parse file '" + filename + "': File does not exist", e);
		} catch (LexerParserException e) {
			throw new ModuleException("Cannot parse file '" + filename + "': \n"
				+ e.getLexerParserMessage());
		} catch (StructureException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		} catch (FormatException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		}
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
