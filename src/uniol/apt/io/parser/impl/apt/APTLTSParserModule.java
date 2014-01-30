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

package uniol.apt.io.parser.impl.apt;

import java.io.IOException;
import java.io.InputStream;


import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Module for parsing a file in apt format into a LTS.
 * <p/>
 * @author Manuel Gieseking
 */
public class APTLTSParserModule extends AbstractModule {

	private final static String DESCRIPTION = "This module parses a LTS.";
	private final static String TITLE = "LTS-Parser";
	private final static String NAME = "LTS-Parser";

	@Override
	public String getName() {
		return NAME;
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
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("inputstream", InputStream.class,
			"The input stream from which the system is read");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("lts", TransitionSystem.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		InputStream data = input.getParameter("inputstream", InputStream.class);
		try {
			output.setReturnValue("lts", TransitionSystem.class, APTLTSParser.getLTS(data));
		} catch (IOException e) {
			throw new ModuleException("Cannot parse file '" + data.toString() + "': File does not exist");
		} catch (LexerParserException e) {
			throw new ModuleException("Cannot parse file '" + data.toString() + "': \n"
				+ e.getLexerParserMessage());
		} catch (NodeNotExistException | TypeMismatchException ex) {
			throw new ModuleException("Create datastructur: " + ex.getMessage());
		} catch (StructureException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		} catch (FormatException ex) {
			throw new ModuleException(ex.getMessage(), ex);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
