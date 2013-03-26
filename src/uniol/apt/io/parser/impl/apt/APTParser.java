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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.exception.LexerParserException;
import uniol.apt.io.parser.impl.exception.NodeNotExistException;
import uniol.apt.io.parser.impl.exception.StructureException;
import uniol.apt.io.parser.impl.exception.TypeMismatchException;

/**
 * Parses a file in apt format into a lts or pn; depending on the type parameter mention in the file.
 * <p/>
 * @author Manuel Gieseking
 */
public class APTParser {

	private TransitionSystem ts = null;
	private PetriNet pn = null;

	/**
	 * Parses a given file into a petri net or transitionsystem depending on the type specified in the file.
	 * <p/>
	 * @param path the file with the data to parse.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public void parse(String path) throws IOException, NodeNotExistException, TypeMismatchException,
		LexerParserException, StructureException {
		parse(new FileInputStream(path));
	}

	/**
	 * Parses a given file into a petri net or transitionsystem depending on the type specified in the file.
	 * <p/>
	 * @param is the inputstream with the data to parse.
	 * <p/>
	 * @throws IOException           thrown if the file could not be read.
	 * @throws NodeNotExistException thrown if a node is used, but do not belong the graph.
	 * @throws TypeMismatchException thrown if the type of the graph do not match the specified type in the file or
	 *                               type not found.
	 * @throws LexerParserException  thrown if the file could not be parsed.
	 * @throws StructureException    thrown if the parsed data could not be converted into the graph.
	 */
	public void parse(InputStream is) throws IOException, NodeNotExistException, TypeMismatchException,
		LexerParserException, StructureException {
		pn = null;
		ts = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		String data = builder.toString();
		is = new ByteArrayInputStream(data.getBytes());

		if (data.matches("(?s).*\\.type\\s+(LTS|TS)(?s).*")) {
			ts = APTLTSParser.getLTS(is);
		} else if (data.matches("(?s).*\\.type\\s+(LPN|PN)(?s).*")) {
			pn = APTPNParser.getPetriNet(is);
		} else {
			throw new TypeMismatchException("File type PN, LPN, TS, LTS needed.");
		}
	}

	/**
	 * Returns the petri net or null if a transitionsystem had been parsed.
	 * <p/>
	 * @return the petri net or null.
	 */
	public PetriNet getPn() {
		return pn;
	}

	/**
	 * Returns the transitionsystem or null if a petri net hab been parsed.
	 * <p/>
	 * @return the transitionsystem or null.
	 */
	public TransitionSystem getTs() {
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
