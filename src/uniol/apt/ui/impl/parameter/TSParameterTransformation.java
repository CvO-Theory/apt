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

package uniol.apt.ui.impl.parameter;

import java.io.IOException;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.apt.APTLTSParser;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * @author Renke Grunwald
 *
 */
public class TSParameterTransformation implements ParameterTransformation<TransitionSystem> {

	@Override
	public TransitionSystem transform(String filename) throws ModuleException {
		boolean fromStandardInput = false;

		try {
			if (filename.equals(NetOrTSParameterTransformation.STANDARD_INPUT_SYMBOL)) {
				fromStandardInput = true;
				return APTLTSParser.getLTS(System.in);
			}

			return APTLTSParser.getLTS(filename);
		} catch (IOException e) {
			throw new ModuleException("Cannot parse file '" + filename + "': File does not exist");
		} catch (LexerParserException e) {
			if (fromStandardInput) {
				throw new ModuleException("Cannot parse data: \n" + e.getLexerParserMessage());
			} else {
				throw new ModuleException("Cannot parse file '" + filename + "': \n" + e.getLexerParserMessage());
			}
		} catch (NodeNotExistException | TypeMismatchException ex) {
			throw new ModuleException("Create data structure: " + ex.getMessage());
		} catch (FormatException e) {
			throw new ModuleException("Format of data does not fit: " + e.getMessage(), e);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
